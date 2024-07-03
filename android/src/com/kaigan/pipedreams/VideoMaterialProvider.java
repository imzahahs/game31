package com.kaigan.pipedreams;

import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES11Ext;
import android.view.Surface;

import com.badlogic.gdx.backends.android.AndroidFileHandle;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;
import java.nio.ByteBuffer;

import sengine.File;
import sengine.Sys;
import sengine.graphics2d.TextureUtils;
import sengine.materials.VideoMaterial;

public class VideoMaterialProvider implements VideoMaterial.PlatformProvider {
    private static final String TAG = "AndroidVideo";

    public static void init() {
        VideoMaterial.platform = new VideoMaterialProvider();
    }

    private static final int DECODE_TIMEOUT_USEC = 10000;       // 10 milliseconds

    private static double uploadSkipTimeThreshold = 0.06;        // don't skip uploading if position is less than this

    private static int maxFailedConfigureTries = 20;
    private static int maxFailedUploadTries = 20;
    private static long failedTryDelayMs = 100;

    public static class VideoMaterialHandle implements VideoMaterial.PlatformHandle {

        public final String filename;
        private final AssetFileDescriptor assetFileDescriptor;

        public Texture texture;
        private Surface surface;
        private SurfaceTexture surfaceTexture;

        private MediaExtractor extractor;
        private MediaCodec decoder;

        private ByteBuffer[] inputBuffers;
        private final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        private int width;
        private int height;

        private double uploadedPosition = -1;
        private double bufferedPosition = -1;

        private boolean hasInputFinished = false;
        private boolean hasOutputFinished = false;

        private boolean isFrameAvailable = false;

        private int failedConfigureTry = 0;
        private int failedUploadTry = 0;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public double getUploadedPosition() {
            return uploadedPosition;
        }

        public double getBufferedPosition() {
            return bufferedPosition;
        }

        public VideoMaterialHandle(String filename) {
            this.filename = filename;

            FileHandle fileHandle = File.open(filename);
            if(fileHandle instanceof AndroidFileHandle) {
                try {
                    assetFileDescriptor = ((AndroidFileHandle)fileHandle).getAssetFileDescriptor();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to get file descriptor for: " + filename, e);
                }
            }
            else
                throw new RuntimeException("Unsupported file location or file not found: " + filename);

            reloadVideo();
        }

        @Override
        public Texture upload(VideoMaterial material, Texture existing, float timestamp, boolean ensureLoaded) {
            if(timestamp < 0)
                throw new RuntimeException("position must be >= 0");
            if(timestamp < uploadedPosition)
                reloadVideo();      // Seeking backwards
            while(true) {
                try {
                    // Keep looping until we have the correct frame ready to be updated
                    while (timestamp >= bufferedPosition && !hasOutputFinished) {
                        if (uploadedPosition != bufferedPosition && (uploadedPosition == -1 || (timestamp - bufferedPosition) < uploadSkipTimeThreshold)) {
                            if (uploadedPosition == -1 && material != null) {
                                // Update texture info
                                texture.bind();
                                texture.setFilter(material.minFilter, material.magFilter);
                                texture.setWrap(material.uWrap, material.vWrap);
                            }
                            // Upload buffered now
                            surfaceTexture.updateTexImage();
                            uploadedPosition = bufferedPosition;
                        }
                        // Supply input to decoder
                        if (!hasInputFinished) {
                            int index = decoder.dequeueInputBuffer(DECODE_TIMEOUT_USEC);
                            // There is a free input buffer to be filled up
                            if (index >= 0) {
                                ByteBuffer buffer = inputBuffers[index];
                                int chunkSize = extractor.readSampleData(buffer, 0);
                                if (chunkSize < 0) {
                                    // End of stream -- send empty frame with EOS flag set.
                                    decoder.queueInputBuffer(index, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                    hasInputFinished = true;
                                } else {
                                    long presentationTimeUs = extractor.getSampleTime();
                                    decoder.queueInputBuffer(index, 0, chunkSize, presentationTimeUs, 0);
                                    extractor.advance();
                                }
                            }
                        }

                        // Check if there is an output from decoder
                        if (!hasOutputFinished) {
                            int index = decoder.dequeueOutputBuffer(bufferInfo, DECODE_TIMEOUT_USEC);
                            if (index >= 0) {
                                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    // Finished output
                                    hasOutputFinished = true;
                                    decoder.releaseOutputBuffer(index, false);
                                } else {
                                    // Send output from decoder to surface
                                    decoder.releaseOutputBuffer(index, true);
                                    // Uploaded a frame, check timestamp
                                    if (bufferedPosition == -1)
                                        bufferedPosition = 0;       // Assume first frame always at zero
                                    else
                                        bufferedPosition = bufferInfo.presentationTimeUs / 1000000.0;
                                    // Wait for surface to process
                                    if ((uploadedPosition == -1 || (timestamp - bufferedPosition) < uploadSkipTimeThreshold)) {
                                        synchronized (this) {
                                            if (!isFrameAvailable) {
                                                try {
                                                    wait();
                                                } catch (InterruptedException e) {
                                                    throw new RuntimeException("Unexpected interrupt", e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (uploadedPosition != -1)
                        return texture;
                    return null;
                } catch (Throwable e) {
                    failedUploadTry++;
                    if(hasInputFinished || failedUploadTry >= maxFailedUploadTries)
                        throw new RuntimeException("Failed to upload", e);
                    try {
                        // Try restarting
                        decoder.flush();
                    } catch (Throwable e2) {
                        // Ignore
                    }
                    // Wait
                    try {
                        Thread.sleep(failedTryDelayMs);
                    } catch (InterruptedException e2) {
                        // ignore
                    }
                }
            }
        }

        @Override
        public Pixmap decode(float timestamp) {
            throw new RuntimeException("not supported");        // TODO: it's possible to support this
        }

        private void reloadVideo() {
            boolean waitForNextTry = false;
            while(true) {
                // Reset
                if (surface != null) {
                    surface.release();
                    surface = null;
                }
                if (surfaceTexture != null) {
                    surfaceTexture.release();
                    surfaceTexture = null;
                }
                if (texture != null) {
                    texture.dispose();
                    texture = null;
                }

                releaseDecoder();

                if (extractor != null) {
                    extractor.release();
                    extractor = null;
                }

                uploadedPosition = -1;
                bufferedPosition = -1;
                hasOutputFinished = false;
                hasInputFinished = false;
                isFrameAvailable = false;

                if(waitForNextTry) {
                    try {
                        Thread.sleep(failedTryDelayMs);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }

                try {
                    // Android requires texture to be created now
                    texture = new TextureUtils.ManualTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                    texture.bind();
                    surfaceTexture = new SurfaceTexture(texture.getTextureObjectHandle());
                    surfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            synchronized (VideoMaterialHandle.this) {
                                // Inform frame ready to be pushed
                                isFrameAvailable = true;
                                VideoMaterialHandle.this.notifyAll();
                            }
                        }
                    });
                    surface = new Surface(surfaceTexture);

                    extractor = new MediaExtractor();

                    // Load file
                    extractor.setDataSource(
                            assetFileDescriptor.getFileDescriptor(),
                            assetFileDescriptor.getStartOffset(),
                            assetFileDescriptor.getLength()
                    );

                    // Select track
                    int numTracks = extractor.getTrackCount();
                    int trackIndex = -1;
                    for (int i = 0; i < numTracks; i++) {
                        MediaFormat format = extractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("video/")) {
                            trackIndex = i;
                            break;
                        }
                    }
                    if (trackIndex == -1)
                        throw new RuntimeException("No video tracks found");
                    extractor.selectTrack(trackIndex);

                    MediaFormat format = extractor.getTrackFormat(trackIndex);
                    width = format.getInteger(MediaFormat.KEY_WIDTH);
                    height = format.getInteger(MediaFormat.KEY_HEIGHT);

                    String mime = format.getString(MediaFormat.KEY_MIME);

                    decoder = MediaCodec.createDecoderByType(mime);
                    decoder.configure(format, surface, null, 0);
                    decoder.start();

                    inputBuffers = decoder.getInputBuffers();

                    // Success
                    return;
                } catch (Exception e) {
                    failedConfigureTry++;
                    if (failedConfigureTry >= maxFailedConfigureTries)
                        throw new RuntimeException("Failed to load video: " + filename, e);
                    // Else absorb and try again
                    waitForNextTry = true;
                }
            }
        }

        @Override
        public void dispose() {
            surface.release();
            surfaceTexture.release();

            releaseDecoder();

            extractor.release();
            extractor = null;
        }

        private void releaseDecoder() {
            try {
                if(decoder != null)
                {
                    decoder.stop();
                    decoder.release();
                }
            } catch (Throwable e) {
                Sys.error(TAG, "Failed to dispose MediaCodec for: " + filename, e);
            }
            decoder = null;
        }
    }


    @Override
    public VideoMaterial.PlatformHandle open(String filename) {
        return new VideoMaterialHandle(filename);
    }

    @Override
    public VideoMaterial.Metadata inspect(String filename) {
        VideoMaterialHandle handle = new VideoMaterialHandle(filename);
        handle.upload(null, null, Float.MAX_VALUE, true);
        VideoMaterial.Metadata metadata = new VideoMaterial.Metadata(handle.getWidth(), handle.getHeight(), (float)handle.getBufferedPosition());
        handle.dispose();
        handle.texture.dispose();
        return metadata;
    }
}

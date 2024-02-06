/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.video;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.video.VideoDecoder.VideoDecoderBuffers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Desktop implementation of the VideoPlayer
 *
 * @author Rob Bogie <rob.bogie@codepoke.net>
 */
public class VideoPlayer {
    private static final String TAG = "VideoPlayer";

    private final VideoDecoder decoder;
    private final BufferedInputStream inputStream;
    private final ReadableByteChannel fileChannel;

    private Pixmap prevFrame;
    private Pixmap nextFrame;

    private double prevPosition = -1;
    private double nextPosition = -1;

    private Pixmap fetchedFrame = null;
    private double fetchedPosition = -1;

    private boolean hasEndedStream = false;
    private boolean hasEnded = false;

    public Pixmap getPrevFrame() {
        return prevFrame;
    }

    public double getPrevPosition() {
        return prevPosition;
    }

    public Pixmap getNextFrame() {
        return nextFrame;
    }

    public double getNextPosition() {
        return nextPosition;
    }

    public int getWidth() {
        return nextFrame.getWidth();
    }

    public int getHeight() {
        return nextFrame.getHeight();
    }

    public Pixmap getFetchedFrame() {
        return fetchedFrame;
    }

    public double getFetchedPosition() {
        return fetchedPosition;
    }

    public boolean isSeekingBack(double position) {
        return position < prevPosition;
    }

    public VideoPlayer(FileHandle file) {
        if (!FfMpeg.isLoaded()) {
            FfMpeg.loadLibraries();
        }

        inputStream = file.read(1024 * 1024);       // 1M buffer
        fileChannel = Channels.newChannel(inputStream);

        decoder = new VideoDecoder();

        VideoDecoderBuffers buffers = null;

        try {
            buffers = decoder.loadStream(this, "readFileContents");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load stream", e);
        }

        prevFrame = new Pixmap(buffers.getVideoWidth(), buffers.getVideoHeight(), Format.RGB888);
        nextFrame = new Pixmap(buffers.getVideoWidth(), buffers.getVideoHeight(), Format.RGB888);
    }

    /**
     * Called by jni to fill in the file buffer.
     *
     * @param buffer The buffer that needs to be filled
     * @return The amount that has been filled into the buffer.
     */
    @SuppressWarnings("unused") private int readFileContents (ByteBuffer buffer) {
        try {
            buffer.rewind();
            int read = fileChannel.read(buffer);;
            if(read == -1)
                hasEndedStream = true;
            return read;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void fetch(double position, boolean ensureLoaded) {
        if(position < 0 || position < prevPosition)
            throw new RuntimeException("Invalid position, must be > 0 and > last position");
        else if(position < nextPosition) {
            if(prevPosition == -1) {
                fetchedFrame = nextFrame;
                fetchedPosition = nextPosition;
            }
            else {
                fetchedFrame = prevFrame;
                fetchedPosition = prevPosition;
            }
        }
        else if(hasEnded) {
            if(nextPosition != -1) {
                fetchedFrame = nextFrame;
                fetchedPosition = nextPosition;
            }
            else if(prevPosition != -1) {
                // video has only one frame
                fetchedFrame = prevFrame;
                fetchedPosition = prevPosition;
            }
            else {
                // video has no frames
                fetchedFrame = null;
                fetchedPosition = -1;
            }
        }
        else {
            // Else need to load next frame
            while (position >= nextPosition && fetchNextFrame(ensureLoaded)) ;
            if (position < nextPosition) {
                if(prevPosition == -1) {
                    fetchedFrame = nextFrame;
                    fetchedPosition = nextPosition;
                }
                else {
                    fetchedFrame = prevFrame;
                    fetchedPosition = prevPosition;
                }
            }
            else if (hasEnded && nextPosition != -1) {
                fetchedFrame = nextFrame;
                fetchedPosition = nextPosition;
            }
            else if (prevPosition != -1) {
                // video has only one frame
                fetchedFrame = prevFrame;
                fetchedPosition = prevPosition;
            }
            else {
                // video has no frames
                fetchedFrame = null;
                fetchedPosition = -1;
            }
        }
    }

    public double offset = 0;

    private boolean fetchNextFrame(boolean ensureLoaded) {
        while(true) {
//            Sys.error(TAG, "fetchnextframe1 " + hasEnded + " " + hasEndedStream);
            if ((!hasEndedStream && decoder.isBuffered()) || hasEndedStream) {
                // While stream has not ended and decoder is fully buffered, OR if stream has ended, then just deplete decoder buffers one by one
//                Sys.error(TAG, "fetchnextframe2");
                ByteBuffer videoData = decoder.nextVideoFrame();
                decoder.updateAudioBuffer();
                double position = decoder.getCurrentFrameTimestamp();
//                Sys.error(TAG, "fetchnextframe3 " + position);
                if (videoData == null) {
                    // Has finished
                    hasEnded = true;
                    return false;
                }
                // Else there is video data
                if (position != nextPosition) {
                    prevPosition = nextPosition;
                    Pixmap swap = prevFrame;
                    prevFrame = nextFrame;
                    nextFrame = swap;
                    nextPosition = position;
                    if(prevPosition != -1) {
                        double delta = nextPosition - prevPosition;
                        if(delta > offset)
                            offset = delta;
                    }
                    ByteBuffer data = nextFrame.getPixels();
                    data.rewind();
                    data.put(videoData);
                    data.rewind();
                    return true;
                }
            }
            // Else have not decoded yet
            if(ensureLoaded)
                Thread.yield();     // have not decoded yet
            else
                return false;

        }
    }

    public void dispose () {
        // Wait till decode has stopped
        while(!decoder.isBuffered() && !hasEndedStream)
            Thread.yield();
//        if(decoder.isBuffered())

        decoder.dispose();        // TODO: this is causing a native crash in gdx-video.dll

        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hasEnded = true;
        hasEndedStream = true;
    }
}
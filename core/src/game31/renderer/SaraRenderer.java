package game31.renderer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.Array;

import game31.Globals;
import sengine.Sys;
import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.Renderer;
import sengine.materials.ColoredMaterial;

/**
 * Created by Azmi on 27/6/2016.
 */
public class SaraRenderer extends Renderer {
    static final String TAG = "SaraRenderer";

    public static final int TARGET_BG = 0;
    public static final int TARGET_BG_SHADOWS = 1;
    public static final int TARGET_FLAPEE_SKY = 2;
    public static final int TARGET_FLAPEE_BG = 3;
    public static final int TARGET_FLAPEE_GAMEPLAY = 4;
    public static final int TARGET_FLAPEE_LIGHTING_GENERATOR = 5;
    public static final int TARGET_INTERACTIVE = 6;
    public static final int TARGET_INTERACTIVE_TEXT = 7;
    public static final int TARGET_INTERACTIVE_TEXT_EFFECT = 8;
    public static final int TARGET_INTERACTIVE_SUB = 9;
    public static final int TARGET_INTERACTIVE_SUB_TEXT = 10;
    public static final int TARGET_INTERACTIVE_FLOATING = 11;
    public static final int TARGET_INTERACTIVE_FLOATING_TEXT = 12;
    public static final int TARGET_INTERACTIVE_OVERLAY = 13;
    public static final int TARGET_INTERACTIVE_OVERLAY_TEXT = 14;
    public static final int TARGET_APPBAR_BG = 15;
    public static final int TARGET_APPBAR = 16;
    public static final int TARGET_APPBAR_TEXT = 17;
    public static final int TARGET_IRIS_OVERLAY = 18;
    public static final int TARGET_IRIS_OVERLAY_TEXT = 19;
    public static final int TARGET_KEYBOARD_AUTOCOMPLETE = 20;
    public static final int TARGET_KEYBOARD_AUTOCOMPLETE_TEXT = 21;
    public static final int TARGET_KEYBOARD_UNDERLAY = 22;
    public static final int TARGET_KEYBOARD_UNDERLAY_TEXT = 23;
    public static final int TARGET_KEYBOARD = 24;
    public static final int TARGET_KEYBOARD_TEXT = 25;
    public static final int TARGET_OVERLAY_BG = 26;
    public static final int TARGET_OVERLAY = 27;
    public static final int TARGET_OVERLAY_INTERACTIVE = 28;
    public static final int TARGET_OVERLAY_TEXT = 29;
    public static final int TARGET_OVERLAY_DIALOG = 30;
    public static final int TARGET_TRANSITION = 31;
    public static final int TARGET_SCREEN = 32;
    public static final int TARGET_SCREEN_EFFECT = 33;
    public static final int TARGET_TEXT_EFFECT_GENERATOR = 34;
    public static final int TARGET_CONSOLE = 35;

    public static final int RENDER_FINAL = 0;
    public static final int RENDER_FIRST = 1;
    public static final int RENDER_SECOND = 2;
    public static final int RENDER_FLAPEE_GAMEPLAY = 3;
    public static final int RENDER_FLAPEE_LIGHTING = 4;
    public static final int RENDER_EFFECT1 = 5;
    public static final int RENDER_EFFECT2 = 6;
    public static final int RENDER_TEXT_EFFECT_BUFFER = 7;
    public static final int RENDER_TEXT_EFFECT1 = 8;
    public static final int RENDER_TEXT_EFFECT2 = 9;

    public static final int NUM_TARGETS = TARGET_CONSOLE + 1;
    public static final int NUM_RENDER_BUFFERS = RENDER_TEXT_EFFECT2 + 1;


    public static SaraRenderer renderer;

    public final FrameBuffer[] renderBuffers = new FrameBuffer[NUM_RENDER_BUFFERS];
    public int renderWidth;
    public int renderHeight;

    private final Array<MaterialConfiguration> firstBufferCalls = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);
    private final Array<MaterialConfiguration> secondBufferCalls = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);
    private final Array<MaterialConfiguration> skyBufferCalls = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);
    private final Array<MaterialConfiguration> gameplayBufferCalls = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);
    private final Array<MaterialConfiguration> textEffectBufferCalls = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);

    private float effectBufferSize = 0;

    // Default materials
    public final ColoredMaterial coloredMaterial;
    public final ScreenNoiseMaterial screenNoiseMaterial;

    public final NoiseMaterial.Type noiseMaterialType;

    private void copyEffectBufferCalls() {
        sortSequence(targets[TARGET_FLAPEE_SKY], skyBufferCalls);
        sortSequence(targets[TARGET_FLAPEE_BG], gameplayBufferCalls);
        sortSequence(targets[TARGET_FLAPEE_GAMEPLAY], gameplayBufferCalls);

        int count = textEffectBufferCalls.size;
        int start = targets[TARGET_INTERACTIVE_TEXT_EFFECT].size;
        sortMaterial(targets[TARGET_INTERACTIVE_TEXT_EFFECT], textEffectBufferCalls);
        targets[TARGET_INTERACTIVE_TEXT_EFFECT].addAll(textEffectBufferCalls.items, count, start);      // add back
    }

    private void buildRenderStack(Array<MaterialConfiguration> stack, boolean copyLightingCalls) {
        sortMaterial(targets[TARGET_BG], stack);
        sortMaterial(targets[TARGET_BG_SHADOWS], stack);

        // Copy all lighting render calls to lighting buffer, sequence sorted due to 2d nature
        if(copyLightingCalls)
            copyEffectBufferCalls();


        sortMaterial(targets[TARGET_INTERACTIVE], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_TEXT], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_TEXT_EFFECT], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_SUB], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_SUB_TEXT], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_FLOATING], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_FLOATING_TEXT], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_OVERLAY], stack);
        sortMaterial(targets[TARGET_INTERACTIVE_OVERLAY_TEXT], stack);

        sortMaterial(targets[TARGET_APPBAR_BG], stack);
        sortMaterial(targets[TARGET_APPBAR], stack);
        sortMaterial(targets[TARGET_APPBAR_TEXT], stack);

        sortMaterial(targets[TARGET_IRIS_OVERLAY], stack);
        sortMaterial(targets[TARGET_IRIS_OVERLAY_TEXT], stack);

        sortMaterial(targets[TARGET_KEYBOARD_AUTOCOMPLETE], stack);
        sortMaterial(targets[TARGET_KEYBOARD_AUTOCOMPLETE_TEXT], stack);
        sortMaterial(targets[TARGET_KEYBOARD_UNDERLAY], stack);
        sortMaterial(targets[TARGET_KEYBOARD_UNDERLAY_TEXT], stack);
        sortMaterial(targets[TARGET_KEYBOARD], stack);
        sortMaterial(targets[TARGET_KEYBOARD_TEXT], stack);

    }


    public void startSecondBuffer() {
        // Dump and sort all previous calls into firstBuffer
        buildRenderStack(firstBufferCalls, true);
    }

    public void stopSecondBuffer() {
        // Dump and sort all previous calls into secondBuffer
        buildRenderStack(secondBufferCalls, true);
    }

    public void clearBufferedRenderCalls() {
        buildRenderStack(stack, false);
        clearInstructions(stack);
    }

    public void createFullscreenRenderBuffers() {
        float screenWidth = Sys.system.getWidth();
        float actualHeight = Sys.system.getHeight();
        float insetHeight = Globals.topSafeAreaInset + Globals.bottomSafeAreaInset;
        float screenHeight = actualHeight - insetHeight;
        float viewportLength = screenHeight / screenWidth;

        float length = 1f / Globals.LENGTH;
        float scale = 1f;
        if(length > viewportLength) {
            scale = viewportLength / length;
        }

        int width = Math.round(scale * length * Sys.system.getWidth());
        int height = Math.round(scale * Sys.system.getWidth());

        recreateRenderBuffers(width, height, true);     // 20180911: +0.5f pixel renderScale on Screen class removes aliasing issue apparent on nearest interpolation
    }

    public void refreshRenderBuffers() {
        // Calculate new buffer size
        int width = Sys.system.getWidth();
        int height = Sys.system.getHeight() - Globals.topSafeAreaInset - Globals.bottomSafeAreaInset;       // Minus insets

        float length = (float)height / (float)width;
        boolean isScreenPerfect = true;
        if(length < Globals.LENGTH) {
            width = Math.round(height / Globals.LENGTH);
//            width = (width / 2) * 2;
//            height = (height / 2) * 2;
//            width += 2;
//            height += 2;
            isScreenPerfect = true;     // 20180911: +0.5f pixel renderScale on Screen class removes aliasing issue apparent on nearest interpolation
        }
        else {
            height = Math.round(Globals.LENGTH * width);
            isScreenPerfect = true;     // 20180911: +0.5f pixel renderScale on Screen class removes aliasing issue apparent on nearest interpolation
        }


//        int width = Sys.system.getWidth(); // TextureUtils.nearestPowerOfTwo(Sys.system.getWidth(), 0.5f);
//        int height = Sys.system.getHeight(); // TextureUtils.nearestPowerOfTwo(Sys.system.getHeight(), 0.5f);

        renderWidth = width;
        renderHeight = height;

        recreateRenderBuffers(width, height, isScreenPerfect);
    }

    private void recreateRenderBuffers(int width, int height, boolean isScreenPerfect) {
        // Assess if need to recreate
        FrameBuffer buffer = renderBuffers[0];
        if(buffer != null && buffer.getWidth() == width && buffer.getHeight() == height)
            return;         // buffers are of exact size

        // Dispose existing
        for(int c = 0; c < renderBuffers.length; c++) {
            buffer = renderBuffers[c];
            if(buffer != null)
                buffer.dispose();
            renderBuffers[c] = null;
        }

        Sys.info(TAG, "Creating framebuffers " + width + "x" + height);

        // Gameplay and godrays (always linear sampled)
        int godraysWidth = Math.round(width * Globals.r_godraysResolution);
        int godraysHeight = Math.round(width * Globals.r_godraysResolution);
        FrameBuffer gameplayBuffer = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
        if(isScreenPerfect)
            gameplayBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        else
            gameplayBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        FrameBuffer lightingBuffer = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                godraysWidth,
                godraysHeight,
                false
        );
        lightingBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);


        // TODO: find which is the best filter for frambuffers Linear vs Nearest

        FrameBuffer finalBuffer = new FrameBuffer(
                Pixmap.Format.RGB888,
                width,
                height,
                false
        );
        if(isScreenPerfect)
            finalBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        else
            finalBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        //finalBuffer.getColorBufferTexture().setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        FrameBuffer firstBuffer = new FrameBuffer(
                Pixmap.Format.RGB888,
                width,
                height,
                false
        );
        firstBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        firstBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        //firstBuffer.getColorBufferTexture().setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);

        FrameBuffer secondBuffer = new FrameBuffer(
                Pixmap.Format.RGB888,
                width,
                height,
                false
        );
        secondBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
//        secondBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        //secondBuffer.getColorBufferTexture().setWrap(Texture.TextureWrap.MirroredRepeat, Texture.TextureWrap.MirroredRepeat);




        // Text effect buffer
        FrameBuffer effectBufferSource = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
        effectBufferSource.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        width = Math.round(width * Globals.r_diffusionEffectResolution);
        height = Math.round(height * Globals.r_diffusionEffectResolution);
        FrameBuffer effectBuffer1 = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
        effectBuffer1.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        FrameBuffer effectBuffer2 = new FrameBuffer(
                Pixmap.Format.RGBA8888,
                width,
                height,
                false
        );
        effectBuffer2.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // Commit
        renderBuffers[RENDER_FINAL] = finalBuffer;
        renderBuffers[RENDER_FIRST] = firstBuffer;
        renderBuffers[RENDER_SECOND] = secondBuffer;
        renderBuffers[RENDER_FLAPEE_GAMEPLAY] = gameplayBuffer;
        renderBuffers[RENDER_FLAPEE_LIGHTING] = lightingBuffer;
        renderBuffers[RENDER_TEXT_EFFECT_BUFFER] = effectBufferSource;
        renderBuffers[RENDER_TEXT_EFFECT1] = effectBuffer1;
        renderBuffers[RENDER_TEXT_EFFECT2] = effectBuffer2;

        requestEffectBuffer(effectBufferSize);

    }

    public void requestEffectBuffer(float size) {
        effectBufferSize = size;

        int width = Math.round(renderBuffers[RENDER_FINAL].getWidth() * size);
        int height = Math.round(renderBuffers[RENDER_FINAL].getHeight() * size);

        if(renderBuffers[RENDER_EFFECT1] != null) {
            // Check dimensions
            if(renderBuffers[RENDER_EFFECT1].getWidth() == width && renderBuffers[RENDER_EFFECT1].getHeight() == height)
                return;     // it's the same size, no need to reset
            // Else different size, dispose current
            renderBuffers[RENDER_EFFECT1].dispose();
            renderBuffers[RENDER_EFFECT2].dispose();
            renderBuffers[RENDER_EFFECT1] = null;
            renderBuffers[RENDER_EFFECT2] = null;
        }

        if(size <= 0)
            return;     // no effect buffers required

        FrameBuffer effectBuffer1 = new FrameBuffer(
                Pixmap.Format.RGB888,
                width,
                height,
                false
        );
        effectBuffer1.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        FrameBuffer effectBuffer2 = new FrameBuffer(
                Pixmap.Format.RGB888,
                width,
                height,
                false
        );
        effectBuffer2.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        renderBuffers[RENDER_EFFECT1] = effectBuffer1;
        renderBuffers[RENDER_EFFECT2] = effectBuffer2;
    }

    public SaraRenderer() {
        super(NUM_TARGETS);

        // Pull vender info
        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
            System.setProperty("simulacra.gl.vendor", Gdx.gl.glGetString(GL20.GL_VENDOR));
            System.setProperty("simulacra.gl.version", Gdx.gl.glGetString(GL20.GL_VERSION));
            System.setProperty("simulacra.gl.renderer", Gdx.gl.glGetString(GL20.GL_RENDERER));
        }

        renderer = this;

        // Default materials
        coloredMaterial = new ColoredMaterial();
        screenNoiseMaterial = new ScreenNoiseMaterial(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        noiseMaterialType = new NoiseMaterial.Type();

        clearColor.set(0x00000000);
    }

    public void render() {

        initializeStates();

        // Prepare effect buffes
        copyEffectBufferCalls();


        // Render flapee buffer if needed
        if(gameplayBufferCalls.size > 0) {
            renderBuffers[RENDER_FLAPEE_GAMEPLAY].begin();
            initializeViewport(renderBuffers[RENDER_FLAPEE_GAMEPLAY].getWidth(), renderBuffers[RENDER_FLAPEE_GAMEPLAY].getHeight());

            // Sky
            for(int c = 0; c < skyBufferCalls.size; c++) {
                MaterialConfiguration render = skyBufferCalls.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();
            clearInstructions(skyBufferCalls);

            // Clear only alpha channel
            gl.glColorMask(false, false, false, true);
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            gl.glColorMask(true, true, true, true);

            for(int c = 0; c < gameplayBufferCalls.size; c++) {
                MaterialConfiguration render = gameplayBufferCalls.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();
            clearInstructions(gameplayBufferCalls);

            renderBuffers[RENDER_FLAPEE_GAMEPLAY].end();
        }


        // Text effect generation
        if(textEffectBufferCalls.size > 0) {
            // Text effect buffer source
            renderBuffers[RENDER_TEXT_EFFECT_BUFFER].begin();
            initializeViewport(renderBuffers[RENDER_TEXT_EFFECT_BUFFER].getWidth(), renderBuffers[RENDER_TEXT_EFFECT_BUFFER].getHeight());
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT);               // For some reason, this is needed to remove the glitch around transition quads

            for(int c = 0; c < textEffectBufferCalls.size; c++) {
                MaterialConfiguration render = textEffectBufferCalls.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // No need to clear instructions, it will be cleared later below
            textEffectBufferCalls.clear();

            renderBuffers[RENDER_TEXT_EFFECT_BUFFER].end();
        }

        if(targets[TARGET_TEXT_EFFECT_GENERATOR].size > 0) {
            // Text effect generation (should be just a single plane with custom shader)
            sortSequence(targets[TARGET_TEXT_EFFECT_GENERATOR], stack);

            // Swap buffers
            FrameBuffer swap = renderBuffers[RENDER_TEXT_EFFECT1];
            renderBuffers[RENDER_TEXT_EFFECT1] = renderBuffers[RENDER_TEXT_EFFECT2];
            renderBuffers[RENDER_TEXT_EFFECT2] = swap;

            // Generate
            renderBuffers[RENDER_TEXT_EFFECT1].begin();
            initializeViewport(renderBuffers[RENDER_TEXT_EFFECT1].getWidth(), renderBuffers[RENDER_TEXT_EFFECT1].getHeight());
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT);               // For some reason, this is needed to remove the glitch around transition quads

            for(int c = 0; c < stack.size; c++) {
                MaterialConfiguration render = stack.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // Clear instructions
            clearInstructions(stack);

            renderBuffers[RENDER_TEXT_EFFECT1].end();
        }


        if(targets[TARGET_FLAPEE_LIGHTING_GENERATOR].size > 0) {
            // Lighting generation (should be just a single plane with custom shader)
            sortSequence(targets[TARGET_FLAPEE_LIGHTING_GENERATOR], stack);

            renderBuffers[RENDER_FLAPEE_LIGHTING].begin();
            initializeViewport(renderBuffers[RENDER_FLAPEE_LIGHTING].getWidth(), renderBuffers[RENDER_FLAPEE_LIGHTING].getHeight());

            for(int c = 0; c < stack.size; c++) {
                MaterialConfiguration render = stack.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // Clear instructions
            clearInstructions(stack);

            renderBuffers[RENDER_FLAPEE_LIGHTING].end();
        }

        // Render first buffer if available
        if(firstBufferCalls.size > 0) {
            renderBuffers[RENDER_FIRST].begin();
            initializeViewport(renderBuffers[RENDER_FIRST].getWidth(), renderBuffers[RENDER_FIRST].getHeight());

            for(int c = 0; c < firstBufferCalls.size; c++) {
                MaterialConfiguration render = firstBufferCalls.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // Clear instructions
            clearInstructions(firstBufferCalls);

            renderBuffers[RENDER_FIRST].end();
        }

        // Render second buffer if available
        if(secondBufferCalls.size > 0) {
            renderBuffers[RENDER_SECOND].begin();
            initializeViewport(renderBuffers[RENDER_SECOND].getWidth(), renderBuffers[RENDER_SECOND].getHeight());

            for(int c = 0; c < secondBufferCalls.size; c++) {
                MaterialConfiguration render = secondBufferCalls.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // Clear instructions
            clearInstructions(secondBufferCalls);

            renderBuffers[RENDER_SECOND].end();
        }

//        Gdx.gl20.glFlush();     // TODO: lenovo pad seems to use previous render target, perhaps this would solve it. test performance impact

        // Render to final buffer
        renderBuffers[RENDER_FINAL].begin();
        initializeViewport(renderBuffers[RENDER_FINAL].getWidth(), renderBuffers[RENDER_FINAL].getHeight());


        // Render all meshes
        buildRenderStack(stack, false);
        sortSequence(targets[TARGET_TRANSITION], stack);
        sortMaterial(targets[TARGET_OVERLAY_BG], stack);
        sortMaterial(targets[TARGET_OVERLAY], stack);
        sortMaterial(targets[TARGET_OVERLAY_INTERACTIVE], stack);
        sortMaterial(targets[TARGET_OVERLAY_TEXT], stack);
        sortMaterial(targets[TARGET_OVERLAY_DIALOG], stack);

        for(int c = 0; c < stack.size; c++) {
            MaterialConfiguration render = stack.items[c];
            initializeMaterial(render.material);
            renderInstruction(render);
        }

        // Flush material
        clearMaterial();

        // Clear instructions
        clearInstructions(stack);

        // Render back to framebuffer
        renderBuffers[RENDER_FINAL].end();

        // Diffusion generation
        if(targets[TARGET_SCREEN_EFFECT].size > 0) {
            // Swap buffers
            FrameBuffer swap = renderBuffers[RENDER_EFFECT1];
            renderBuffers[RENDER_EFFECT1] = renderBuffers[RENDER_EFFECT2];
            renderBuffers[RENDER_EFFECT2] = swap;

            // Generate
            renderBuffers[RENDER_EFFECT1].begin();
            initializeViewport(renderBuffers[RENDER_EFFECT1].getWidth(), renderBuffers[RENDER_EFFECT1].getHeight());
            gl.glClear(GL20.GL_COLOR_BUFFER_BIT);               // For some reason, this is needed to remove the glitch around transition quads

            sortSequence(targets[TARGET_SCREEN_EFFECT], stack);

            for(int c = 0; c < stack.size; c++) {
                MaterialConfiguration render = stack.items[c];
                initializeMaterial(render.material);
                renderInstruction(render);
            }

            // Flush material
            clearMaterial();

            // Clear instructions
            clearInstructions(stack);

            renderBuffers[RENDER_EFFECT1].end();
        }

//        Gdx.gl20.glFlush();     // TODO: lenovo pad seems to use previous render target, perhaps this would solve it. test performance impact

        initializeViewport();

        // Clear screen
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render screen meshes
        sortSequence(targets[TARGET_SCREEN], stack);
        sortSequence(targets[TARGET_CONSOLE], stack);

        for(int c = 0; c < stack.size; c++) {
            MaterialConfiguration render = stack.items[c];
            initializeMaterial(render.material);
            renderInstruction(render);
        }

        // Flush material
        clearMaterial();

        // Clear instructions
        clearInstructions(stack);

        // Clear renderer
        clearInstructions();
    }

}

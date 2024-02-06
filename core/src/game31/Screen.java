package game31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;

import game31.gb.GBScreen;
import game31.renderer.SaraRenderer;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.ui.Menu;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 28/6/2016.
 */
public class Screen extends Menu<Grid> {


    public interface BuilderSource {
        Animation createEnterFullscreenAnim(float scale);
        Animation createExitFullscreenAnim(float scale);
    }

    public static class Internal {
        public UIElement.Group window;

        public Mesh leftGlow;
        public Mesh rightGlow;
        public Mesh topGlow;
        public Mesh bottomGlow;
        public Mesh verticalGlowShadow;
        public float verticalGlowScaleY;
        public float verticalGlowMinLength;
        public Mesh border;
        public Mesh bgLeft;
        public Mesh bgRight;

        public float bgRenderMinSize;

        public Animation bgAnimIdle;

        public Mesh defaultScreen;
    }

    private final Builder<BuilderSource> builder;
    private Internal s;

    // Screen mesh
    public Mesh defaultScreen;
    public Mesh screen;
    public Mesh overlay;
    public Mesh effectBufferGenerator;

    // Background animation
    private Animation bgLoopAnim;
    private Animation.Handler bgAnim;
    private Animation.Loop bgAnimIdle;

    private Animation overlayLoopAnim;
    private Animation.Handler overlayAnim = null;

    private Animation glowLoopAnim;
    private Animation.Handler glowAnim;

    private Animation.Instance fullscreenAnim;
    private boolean isEnteringFullscreen = false;


    public void animateGlow(Animation startAnim, Animation loopAnim) {
        glowLoopAnim = loopAnim;
        if(startAnim != null)
            glowAnim = startAnim.startAndReset();
        else if(glowLoopAnim != null)
            glowAnim = glowLoopAnim.loopAndReset();
        else
            glowAnim = null;
    }

    public void animateOverlay(Animation startAnim, Animation loopAnim) {
        overlayLoopAnim = loopAnim;
        if(startAnim != null)
            overlayAnim = startAnim.startAndReset();
        else if(overlayLoopAnim != null)
            overlayAnim = overlayLoopAnim.loopAndReset();
        else
            overlayAnim = null;
    }

    public Animation.Handler overlayAnim() {
        return overlayAnim;
    }

    public void enterFullscreen(boolean isInstant) {
        if(Sys.system.getLength() > 1f)
            return;     // screen is already portrait mode, don't enter fullscreen
        if(fullscreenAnim == null || !isEnteringFullscreen) {
            fullscreenAnim = builder.build().createEnterFullscreenAnim(calculateFullscreenScale()).startAndReset();
            if(isInstant)
                fullscreenAnim.setProgress(1f);
            SaraRenderer.renderer.createFullscreenRenderBuffers();
        }
        isEnteringFullscreen = true;
    }

    public void exitFullscreen() {
        if(fullscreenAnim != null && isEnteringFullscreen) {
            fullscreenAnim = builder.build().createExitFullscreenAnim(calculateFullscreenScale()).startAndReset();
            isEnteringFullscreen = false;
            SaraRenderer.renderer.refreshRenderBuffers();
        }
    }

    public boolean isInFullscreen() {
        return fullscreenAnim != null;
    }

    private float calculateFullscreenScale() {
        float screenWidth = Sys.system.getWidth();
        float actualHeight = Sys.system.getHeight();
        float insetHeight = Globals.topSafeAreaInset + Globals.bottomSafeAreaInset;
        float screenHeight = actualHeight - insetHeight;
        float renderWidth = SaraRenderer.renderer.renderWidth;
        float viewportLength = screenHeight / screenWidth;

        float viewportWidth = renderWidth / screenWidth;

        float heightScale = 1f / viewportLength;
        float newWidth = viewportWidth * heightScale;
        if(newWidth > viewportLength)
            heightScale *= viewportLength / newWidth;
        return heightScale;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        defaultScreen = s.defaultScreen;

        // Randomize background anim
        bgAnimIdle = s.bgAnimIdle.loopAndReset();
        bgAnimIdle.setProgress((float)Math.random());
    }

    public Animation.Handler backgroundAnim() {
        return bgAnim;
    }

    public void animateBackground(Animation startAnim, Animation loopAnim) {
        animateBackground(startAnim, loopAnim, true);
    }

    public void animateBackground(Animation startAnim, Animation loopAnim, boolean restart) {
        if(!restart && bgAnim != null && (bgAnim.anim == startAnim || bgAnim.anim == loopAnim))
            return;     // already running the anim
        bgLoopAnim = loopAnim;
        if(startAnim != null)
            bgAnim = startAnim.startAndReset();
        else if(bgLoopAnim != null)
            bgAnim = bgLoopAnim.loopAndReset();
        else
            bgAnim = null;
    }

    public Screen() {
        builder = new Builder<BuilderSource>(GBScreen.class, this);
        builder.build();

        // Default screen
        screen = defaultScreen;


        inputEnabled = true;        // Allow ALT+ENTER only when windowed mode from the start
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        super.render(v, r, renderTime);

        Matrix4 m = Matrices.model;
        Matrices.push();

        // Effect buffer generation
        if(effectBufferGenerator != null) {
            Matrices.push();
            Matrices.camera = v.compositor.camera;
            Matrices.target = SaraRenderer.TARGET_SCREEN_EFFECT;
            m.translate(0.5f, +Globals.LENGTH / 2f, 0);
            m.scale(1, -1, 1);
            effectBufferGenerator.render();
            Matrices.pop();
        }

        // Render screen
        Matrices.target = SaraRenderer.TARGET_SCREEN;

        float screenWidth = Sys.system.getWidth();
        float actualHeight = Sys.system.getHeight();
        float insetHeight = Globals.topSafeAreaInset + Globals.bottomSafeAreaInset;
        float screenHeight = actualHeight - insetHeight;
        float renderWidth = SaraRenderer.renderer.renderWidth;
        float renderHeight = SaraRenderer.renderer.renderHeight;
//        float renderHeight = SaraRenderer.renderer.renderBuffers[0].getHeight();

        float viewportLength = screenHeight / screenWidth;
        float viewportScale;
        if(viewportLength < Globals.LENGTH)
            viewportScale = (renderWidth + 0.5f) / screenWidth;         // 20180911: nearest interpolation with +0.5f pixel here solves some aliasing issues.
        else
            viewportScale = 1f;

        m.translate(
                (((screenWidth - renderWidth) / 2f) + (renderWidth / 2f)) / screenWidth,
                +v.length - ((float) Globals.topSafeAreaInset / screenWidth) - (viewportLength / 2f),
                0
        );

        if(bgAnim != null) {
            boolean isActive = true;
            if(!bgAnim.update(getRenderDeltaTime())) {
                if(bgLoopAnim != null)
                    bgAnim = bgLoopAnim.loopAndReset();
                else
                    isActive = false;
            }

            if(viewportScale < s.bgRenderMinSize) {
                bgAnim.apply(s.bgLeft);
                bgAnim.apply(s.bgRight);
            }

            if(!isActive)
                bgAnim = null;      // finished
        }
        else {
            bgAnimIdle.update(getRenderDeltaTime());
            if(viewportScale < s.bgRenderMinSize) {
                bgAnimIdle.apply(s.bgLeft);
                bgAnimIdle.apply(s.bgRight);
            }
        }

        // Fullscreen anim
        if(fullscreenAnim != null && !fullscreenAnim.updateAndApply(Matrices.getModelMatrixAnimator(viewportLength, null), getRenderDeltaTime()) && !isEnteringFullscreen)
            fullscreenAnim = null;

        // Glow anim
        if(glowAnim != null) {
            if(!glowAnim.update(getRenderDeltaTime())) {
                if(glowLoopAnim != null)
                    glowAnim = glowLoopAnim.loopAndReset();
                else
                    glowAnim = null;
            }
            if(glowAnim != null) {
                glowAnim.apply(s.leftGlow);
                glowAnim.apply(s.rightGlow);
                glowAnim.apply(s.topGlow);
                glowAnim.apply(s.bottomGlow);
            }
        }

        float renderLength = renderHeight / renderWidth;
        m.scale(viewportScale, -viewportScale * (renderLength / screen.getLength()), 1);        // Previously viewportScale, -viewportScale * ((renderHeight / renderWidth) / screen.getLength())

        if(viewportScale < s.bgRenderMinSize) {
            // Bg first
            float widthScale = (1f / Globals.LENGTH) * (1f / Globals.LENGTH);
            float scale = (((1f - widthScale) / 2f) / widthScale);
            Matrices.push();
            m.translate(-0.5f - (scale / 2f), 0, 0);
            m.scale(scale, -scale, 1);
            s.bgLeft.render();
            Matrices.pop();
            Matrices.push();
            m.translate(+0.5f + (scale / 2f), 0, 0);
            m.scale(scale, -scale, 1);
            s.bgRight.render();
            Matrices.pop();

            // Glows
            scale = Globals.LENGTH / s.leftGlow.getLength();
            widthScale = 10f;
            float glowOffsetX = -(scale * widthScale / 2f);

            {
                Matrices.push();
                m.translate(-0.5f - (scale / 2f * widthScale) - glowOffsetX, 0, 0);
                m.scale(scale * widthScale, scale, 1f);

                s.leftGlow.render();

                Matrices.pop();
            }
            {
                Matrices.push();
                m.translate(+0.5f + (scale / 2f * widthScale) + glowOffsetX, 0, 0);
                m.scale(scale * widthScale, scale, 1f);

                s.rightGlow.render();

                Matrices.pop();
            }

            // Border
            scale = Globals.LENGTH / s.border.getLength();
            {
                Matrices.push();
                m.translate(-0.5f - (scale / 2f), 0, 0);
                m.scale(scale, scale, 1f);

                s.border.render();

                Matrices.pop();
            }
            {
                Matrices.push();
                m.translate(+0.5f + (scale / 2f), 0, 0);
                m.scale(scale, scale, 1f);

                s.border.render();

                Matrices.pop();
            }
        }
        else if((v.length - Globals.LENGTH) > s.verticalGlowMinLength) {
            // Render top and bottom glows
            Matrices.push();
            m.translate(0, (+Globals.LENGTH / 2f) + (s.bottomGlow.getLength() * s.verticalGlowScaleY / 2f), 0);
            m.scale(1, s.verticalGlowScaleY, 1);

            s.bottomGlow.render();
            s.verticalGlowShadow.render();

            Matrices.pop();

            Matrices.push();
            m.translate(0, (-Globals.LENGTH / 2f) - (s.topGlow.getLength() * s.verticalGlowScaleY / 2f), 0);
            m.scale(1, s.verticalGlowScaleY, 1);

            s.topGlow.render();
            m.scale(1, -1, 1);
            s.verticalGlowShadow.render();

            Matrices.pop();

        }

        screen.render();

        // Render
        if(overlay != null) {
            if(overlayAnim != null) {
                if(!overlayAnim.updateAndApply(overlay, getRenderDeltaTime())) {
                    if(overlayLoopAnim != null)
                        overlayAnim = overlayLoopAnim.loopAndReset();
                    else
                        overlayAnim = null;
                }
            }
            overlay.render();
        }

        Matrices.pop();
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(inputType == INPUT_KEY_UP && key == Input.Keys.ENTER &&
                (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) &&
                Gdx.app.getType() == Application.ApplicationType.Desktop
        ) {
            // Toggle windowed / fullscreen
            Graphics.DisplayMode mode = Gdx.graphics.getDisplayMode();
            if(Gdx.graphics.isFullscreen()) {
                Game.game.platform.setWindowed();
            }
            else {
                Gdx.graphics.setFullscreenMode(mode);
                Gdx.graphics.setVSync(true);
            }
            return true;
        }
        return false;
    }
}

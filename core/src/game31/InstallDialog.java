package game31;

import com.badlogic.gdx.Gdx;

import javax.sound.midi.Patch;

import game31.gb.GBInstallDialog;
import sengine.File;
import sengine.animation.Animation;
import sengine.ui.Clickable;
import sengine.ui.OnClick;
import sengine.ui.OnPressed;
import sengine.ui.OnReleased;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

public class InstallDialog extends DialogBox implements OnPressed<Grid>, OnReleased<Grid>, OnClick<Grid> {


    public static class Internal {
        public UIElement<?> window;

        public TextBox titleView;
        public Clickable touchButton;
        public StaticSprite touchingView;
        public TextBox instructionView;
        public TextBox noticeView;

        public Clickable cancelButton;

        public StaticSprite doneView;
        public TextBox doneTextView;

        public Animation pressedAnim;
        public Animation releasedAnim;

        public float tDoneDelay;
    }


    // Sources
    private final Builder<Object> builder;
    private Internal s;

    private Runnable onAuthorized = null;

    private float tDoneEndScheduled = Float.MAX_VALUE;
    private boolean isAuthorized = false;

    public void setOnAuthorized(Runnable onAuthorized) {
        this.onAuthorized = onAuthorized;
    }

    public void setInternal(Internal internal) {
        s = internal;

        clear();

        prepare(s.window);
        show();
    }

    public InstallDialog() {
        builder = new Builder<Object>(GBInstallDialog.class, this);
        builder.build();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(s.touchingView.windowAnim != null && s.touchingView.windowAnim.getProgress() == 1f) {
            if(s.touchingView.windowAnim.anim == s.pressedAnim) {
                // Finished
                s.titleView.detachWithAnim();
                s.touchButton.detachWithAnim();
                s.instructionView.detachWithAnim();
                s.noticeView.detachWithAnim();
                s.cancelButton.detachWithAnim();

                // Queue done
                s.doneView.attach();
                s.doneTextView.attach();

                isAuthorized = true;
                tDoneEndScheduled = renderTime + s.tDoneDelay;
            }
            else
                s.touchingView.detach();

            s.touchingView.windowAnimation(null, false, false);
        }

        if(renderTime > tDoneEndScheduled) {
            tDoneEndScheduled = Float.MAX_VALUE;
            detachWithAnim();
        }
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Reset
        s.doneView.detach();
        s.doneTextView.detach();

        s.titleView.attach();
        s.touchButton.attach();
        s.touchingView.windowAnimation(null, false, false).detach();
        s.instructionView.attach();
        s.noticeView.attach();
        s.cancelButton.attach();

        isAuthorized = false;
        tDoneEndScheduled = Float.MAX_VALUE;
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        if(isAuthorized && onAuthorized != null)
            onAuthorized.run();
    }


    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.touchButton && (s.touchingView.windowAnim == null || s.touchingView.windowAnim.anim != s.pressedAnim)) {
            s.touchingView.attach();
            s.touchingView.windowAnimation(s.pressedAnim.startAndReset(), true, true);
        }
    }

    @Override
    public void onReleased(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.touchButton && (s.touchingView.windowAnim == null || s.touchingView.windowAnim.anim != s.releasedAnim)) {
            float progress = s.touchingView.windowAnim != null ? (1f - s.touchingView.windowAnim.getProgress()) : 0;
            s.touchingView.windowAnimation(s.releasedAnim.startAndReset(), true, true);
            s.touchingView.windowAnim.setProgress(progress);
        }
    }


    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.cancelButton) {
            detachWithAnim();
            return;
        }
    }
}

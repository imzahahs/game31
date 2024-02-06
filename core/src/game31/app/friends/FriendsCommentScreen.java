package game31.app.friends;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Keyboard;
import game31.Media;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.friends.GBFriendsCommentScreen;
import sengine.Entity;
import sengine.Sys;
import sengine.graphics2d.Mesh;
import sengine.ui.Clickable;
import sengine.ui.InputField;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 6/8/2017.
 */

public class FriendsCommentScreen extends Menu<Grid> implements OnClick<Grid>, Keyboard.KeyboardInput {

    public interface InterfaceSource {

        Mesh createCommentBg(float length, boolean isNew);

        String textLongElapsed(long time);
    }

    public static class Internal {

        public UIElement<?> window;
        public ScreenBar bars;
        public ScrollableSurface surface;

        // Comment bar
        public Clickable commentButton;
        public TextBox commentTypeMessageView;
        public InputField inputField;

        public float keyboardPaddingY;

        public float tSmoothMoveTime;

        // Comment
        public UIElement.Group commentGroup;
        public StaticSprite commentUserProfileView;
        public TextBox commentUserNameView;
        public TextBox commentUserHandleView;
        public TextBox commentMessageView;
        public Clickable commentImageView;
        public float commentImageOffsetY;
        public StaticSprite commentBgView;
        public float commentBgPaddingY;
        public float commentIntervalY;

        public float tTimeRefreshInterval;

        public HashSet<String> profanityWords;
    }

    private final FriendsApp app;

    // Interface source
    private final Builder<InterfaceSource> builder;
    private Internal s;


    // Working
    private float surfaceY;
    private FriendsPost post;
    private Clickable postView;
    private Clickable postMediaButton;
    private Clickable postCommentButton;
    private Clickable postLikeButton;
    private float tTimeRefreshScheduled = -1;
    private boolean ignoreMovingUp = false;

    private final Array<Clickable> mediaButtons = new Array<Clickable>(Clickable.class);
    private final Array<FriendsComment> mediaComments = new Array<FriendsComment>(FriendsComment.class);

    // Transition
    private Entity<?> transitionFrom;

    public void open(Entity<?> transitionFrom) {
        this.transitionFrom = transitionFrom;

        ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, Globals.grid.screensGroup);
        transition.attach(Globals.grid.screensGroup);
    }

    public void close() {
        ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, Globals.grid.screensGroup);
        transition.attach(Globals.grid.screensGroup);
    }

    private void clear() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        s.surface.detachChilds();

        postView = null;
        postMediaButton = null;
        postCommentButton = null;
        postLikeButton = null;

        mediaButtons.clear();
        mediaComments.clear();

        ignoreMovingUp = false;
    }

    private void addComment(FriendsComment comment) {
        UIElement.Group group = s.commentGroup.instantiate();
        group.find(s.commentUserProfileView).visual(comment.user.profile);

        // Name
        group.find(s.commentUserNameView).autoWidthText(comment.user.fullName);
        group.find(s.commentUserHandleView).autoWidthText(comment.user.name);

        group.find(s.commentMessageView).autoLengthText(comment.message);

        // Media
        Clickable imageView = group.find(s.commentImageView);
        if(comment.mediaName != null) {
            comment.prepareMedia();
            imageView.length(comment.media.full.length).metrics.offset(0, s.commentImageOffsetY);
            mediaButtons.add(imageView);
            mediaComments.add(comment);
        }
        else
            imageView.metrics.scaleY = 0;        // no image, make it invisible

        // Attach bg
        float length = group.autoLength().getLength() / group.getScaleY();
        length += s.commentBgPaddingY;
        StaticSprite bgView = s.commentBgView.instantiate().visual(builder.build().createCommentBg(length, false));
        group.viewport(bgView).attach();

        // Add to surface
        bgView.metrics.anchorY = surfaceY;
        surfaceY -= (bgView.getLength() + s.commentIntervalY);

        bgView.viewport(s.surface).attach();
    }

    public void show(FriendsPost post) {
        clear();

        this.post = post;

        // Add post
        postView = app.wallScreen.createPostView(post);

        // Appbar
        String time = builder.build().textLongElapsed(post.time);
        s.bars.showAppbar(post.user.fullName, time);

        // Add to surface and position
        postView.metrics.anchorY = surfaceY;

        surfaceY -= (postView.getLength() + app.wallScreen.s.postIntervalY);

        postView.viewport(s.surface).attach();

        // Recognize intent to comment and like
        postLikeButton = postView.find(app.wallScreen.s.postLikeButton);
        postCommentButton = postView.find(app.wallScreen.s.postCommentButton);

        // Intent to open media
        if(post.media != null)
            postMediaButton = postView.find(app.wallScreen.s.postImageView);
        else
            postMediaButton = null;

        // Add comments
        for(int c = 0; c < post.comments.size; c++) {
            FriendsComment comment = post.comments.items[c];
            addComment(comment);
        }

        // Done
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        // Refresh
        if(post != null) {
            Globals.grid.postMessage(new Runnable() {
                @Override
                public void run() {
                    show(post);
                }
            });
        }
    }


    public FriendsCommentScreen(FriendsApp app) {
        this.app = app;

        builder = new Builder<InterfaceSource>(GBFriendsCommentScreen.class, this);
        builder.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Scroll surface to top
        if(!ignoreMovingUp)
            s.surface.move(0, +1000);
        else
            ignoreMovingUp = false;         // else returned back, move up next time

        // Refresh elapsed time
        tTimeRefreshScheduled = -1;

        // Allow idle scares
        grid.idleScare.reschedule();
    }


    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Update media button renders
        if(postMediaButton != null)
            post.media.loadBest(postMediaButton, -1);
        for(int c = 0; c < mediaButtons.size; c++) {
            Clickable button = mediaButtons.items[c];
            Media media = mediaComments.items[c].media;
            media.loadBest(button, -1);
        }

        // Update time strings
        if(renderTime > tTimeRefreshScheduled) {
            tTimeRefreshScheduled = renderTime + s.tTimeRefreshInterval;

            app.wallScreen.updatePostViewTime(post, postView);
            String time = builder.build().textLongElapsed(post.time);
            s.bars.showAppbar(post.user.fullName, time);
        }
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        // Post triggers
        float tSysTime = Sys.getTime();
        if(tSysTime > post.tNextTriggerScheduled && postView.isEffectivelyRendering()) {
            // Trigger
            v.eval(post.user.name, post.trigger);
            // Schedule next
            post.tNextTriggerScheduled = tSysTime + Globals.tFriendsTriggerInterval;
        }
    }

    @Override
    public void onClick(Grid v, final UIElement<?> view, int b) {
        if (view == s.bars.backButton()) {
            // Stop idle scares
            v.idleScare.stop();

            v.keyboard.hideNow();

            // Transition back
            close();

            return;
        }

        if(view == s.bars.homeButton()) {
            // Stop idle scares
            v.idleScare.stop();

            v.homescreen.transitionBack(this, v);

            v.keyboard.hideNow();

            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        // Media
        if(view == postMediaButton) {
            // Stop idle scares
            v.idleScare.stop();

            // Open
            ignoreMovingUp = true;
            if(post.media.isVideo() || post.media.isAudio()) {
                if(v.trigger(Globals.TRIGGER_OPEN_VIDEO_FROM_MESSAGES)) {
                    // Clicked on a video, open
                    v.photoRollApp.videoScreen.show(post.album, post.mediaIndex, null, true);
                    v.photoRollApp.videoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                }
            }
            else {
                // Clicked on a photo, open
                v.photoRollApp.photoScreen.show(post.album, post.mediaIndex, null, false, post.user.fullName, post.message);
                v.photoRollApp.photoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
            }
        }
        if(view instanceof Clickable) {
            // Clicked on a media button on a comment
            // Stop idle scares
            v.idleScare.stop();

            Clickable button = (Clickable) view;
            int index = mediaButtons.indexOf(button, true);
            if(index != -1) {
                // Clicked on a media button
                FriendsComment comment = mediaComments.items[index];
                // Open
                ignoreMovingUp = true;      // maintain position
                if(comment.media.isVideo() || comment.media.isAudio()) {
                    if(v.trigger(Globals.TRIGGER_OPEN_VIDEO_FROM_MESSAGES)) {
                        // Clicked on a video, open
                        v.photoRollApp.videoScreen.show(comment.album, comment.mediaIndex, null, true);
                        v.photoRollApp.videoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                    }
                }
                else {
                    // Clicked on a photo, open
                    v.photoRollApp.photoScreen.show(comment.album, comment.mediaIndex, null, false, comment.user.fullName, comment.message);
                    v.photoRollApp.photoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                }
                // Done
                return;
            }
        }

        // Like or unlike
        if(view == postLikeButton) {
            app.wallScreen.clickLike(post, postView);
            app.wallScreen.refreshSocialStats(post, post.feedView);
            if(post.profileView != null)
                app.wallScreen.refreshSocialStats(post, post.profileView);
            return;
        }

        // Comment
        if(view == postCommentButton) {
            // Open to comment
            startComment();
            return;
        }

        if(view == s.commentButton) {
            String typed = s.inputField.text();
            if(typed != null && !typed.isEmpty()) {
                // Close keyboard
                v.keyboard.hide();
                keyboardClosed();
                // Check profanity
                if(checkProfanity(typed))
                    v.state.set(Globals.JABBR_IRIS_PROFANITY_MAIL, true);           // send warning
                // Sending a new comment
                FriendsComment comment = new FriendsComment(app.login.name, typed, null);
                comment.resolveUser(app.users);
                post.comments.add(comment);
                post.hasUserCommented = true;
                // Add new comment row
                addComment(comment);
                v.postMessage(new Runnable() {
                    @Override
                    public void run() {
                        s.surface.refresh();
                        s.surface.smoothMove(0, s.surface.spaceBottom(), s.tSmoothMoveTime);
                    }
                });
                // Refresh posts
                app.wallScreen.refreshSocialStats(post, postView);
                app.wallScreen.refreshSocialStats(post, post.feedView);
                if(post.profileView != null)
                    app.wallScreen.refreshSocialStats(post, post.profileView);
            }
            else {
                // Else starting to type comment
                startComment();
            }
            return;
        }

    }

    private boolean checkProfanity(String typed) {
        // Split to words
        String[] words = typed.split("\\W+");
        for(int c = 0; c < words.length; c++) {
            String word = words[c].toLowerCase();
            if(s.profanityWords.contains(word))
                return true;
        }
        return false;
    }

    private void startComment() {
        // Move to bottom
        s.surface.move(0, +1000);
        // Open keyboard
        s.commentTypeMessageView.detachWithAnim();
        s.inputField.text("");
        Globals.grid.keyboard.resetAutoComplete();
        Globals.grid.keyboard.showKeyboard(this, s.window, s.keyboardPaddingY, true, true, false, true, this, s.inputField.text(), "send");
    }

    @Override
    public void keyboardTyped(String text, int matchOffset) {
        // Update input field
        s.inputField.text(text);
    }

    @Override
    public void keyboardClosed() {
        // Clear input field
        s.inputField.text(null);
        s.commentTypeMessageView.attach();
    }

    @Override
    public void keyboardPressedConfirm() {
        onClick(Globals.grid, s.commentButton, Input.Buttons.LEFT);
    }
}

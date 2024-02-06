package game31.app.friends;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.friends.GBFriendsWallScreen;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.graphics2d.Font;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 3/9/2017.
 */

public class FriendsWallScreen extends Menu<Grid> implements OnClick<Grid> {
    static final String TAG = "FriendsWallScreen";

    public interface InterfaceSource {
        String textLikes(int likes);
        String textComments(int comments);

        String textShortElapsed(long time);
    }
    
    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public ScreenBar bars;

        // Tabs
        public Clickable tabProfile;
        public Clickable tabFeed;

        // Post
        public Clickable postView;
        public float postClearNewTime;              // If player spends this amount of time looking at posts and seen all new posts, consider as seen all posts
        public float postBgPaddingY;
        public UIElement.Group postGroup;
        public StaticSprite postUserProfileView;
        public TextBox postUserNameView;
        public TextBox postUserHandleView;
        public TextBox postTimeView;
        public Font postTimeNewFont;
        public Animation postTimeNewAnim;
        public UIElement.Group postLocationGroup;
        public float postLocationOffsetY;
        public TextBox postLocationView;
        public String postLocationFormat;
        public TextBox postMessageView;
        public Clickable postImageView;
        public float postImageOffsetY;
        public Clickable postLikeButton;
        public StaticSprite postLikeIcon;
        public Clickable postCommentButton;
        public StaticSprite postCommentIcon;
        public Sprite postLikeEmptySprite;
        public Sprite postLikeFilledSprite;
        public Sprite postCommentFilledSprite;
        public Animation postFontFilledAnim;
        public float postIntervalY;

        public Audio.Sound notificationSound;
        public Sprite notificationIcon;

        public float tTimeRefreshInterval;
    }
    
    private final FriendsApp app;

    // Sources
    private final Builder<InterfaceSource> interfaceSource;
    Internal s;

    // Current
    private float surfaceY;
    private final Array<FriendsPost> posts = new Array<FriendsPost>(FriendsPost.class);
    private final ObjectMap<UIElement, FriendsPost> commentButtons = new ObjectMap<UIElement, FriendsPost>();
    private final ObjectMap<UIElement, FriendsPost> postButtons = new ObjectMap<UIElement, FriendsPost>();
    private final ObjectMap<UIElement, FriendsPost> likeButtons = new ObjectMap<UIElement, FriendsPost>();
    private final Array<Clickable> mediaButtons = new Array<Clickable>(Clickable.class);
    private final Array<FriendsPost> mediaPosts = new Array<FriendsPost>(FriendsPost.class);
    boolean ignoreMovingUp = false;
    private float tLastButtonClick = -1;
    private float tTimeRefreshScheduled = -1;
    private boolean reorderRequired = true;
    private boolean hasSeenAllNewPosts = false;

    private Animation.Loop postTimeNewAnim;

    void refreshSocialStats(FriendsPost post, UIElement<?> view) {
        // Update social button stats
        Clickable likeButton = view.find(s.postLikeButton);
        Clickable commentButton = view.find(s.postCommentButton);
        likeButton.text().text(interfaceSource.build().textLikes(post.likes));
        commentButton.text().text(interfaceSource.build().textComments(post.comments.size + post.hiddenCommentsCount));

        // Icons
        if(post.hasUserLiked) {
            likeButton.windowAnimation(s.postFontFilledAnim.loopAndReset(), true, true);
            view.find(s.postLikeIcon).visual(s.postLikeFilledSprite);
        }
        else {
            likeButton.windowAnimation(null, false, false);
            view.find(s.postLikeIcon).visual(s.postLikeEmptySprite);
        }

        if(post.hasUserCommented) {
            commentButton.windowAnimation(s.postFontFilledAnim.loopAndReset(), true, true);
            view.find(s.postCommentIcon).visual(s.postCommentFilledSprite);
        }
    }

    void clickLike(FriendsPost post, Clickable view) {
        // Player intends to like or unlike
        if(post.hasUserLiked) {
            // Unliking
            view.find(s.postLikeIcon).visual(s.postLikeIcon.visual());       // reset to not liked
            post.likes--;
            if(post.likes < 0)
                post.likes = 0;     // unexpected
        }
        else {
            // Liking
            view.find(s.postLikeIcon).visual(s.postLikeFilledSprite);       // reset to not liked
            post.likes++;
        }
        post.hasUserLiked = !post.hasUserLiked;

        // Refresh
        refreshSocialStats(post, view);
    }

    void updatePostViewTime(FriendsPost post, UIElement<?> view) {
        view.find(s.postTimeView).text().text(interfaceSource.build().textShortElapsed(post.time));
    }

    Clickable createPostView(FriendsPost post) {
        UIElement.Group group = s.postGroup.instantiate();

        // Profile
        group.find(s.postUserProfileView).visual(post.user.profile);

        // Name
        group.find(s.postUserNameView).autoWidthText(post.user.fullName);
        group.find(s.postUserHandleView).autoWidthText(post.user.name);

        // Time
        updatePostViewTime(post, group);

        group.find(s.postMessageView).autoLengthText(post.message);

        // Location
        if(post.location != null && !post.location.isEmpty()) {
            group.find(s.postLocationGroup).metrics.offset(0, s.postLocationOffsetY);
            group.find(s.postLocationView).text(String.format(Locale.US, s.postLocationFormat, post.location));
        }
        else
            group.find(s.postLocationGroup).metrics.scaleY = 0;         // no location, make it invisible

        // Media
        if(post.mediaName != null) {
            // This post has a media, need to prepare it
            post.prepareMedia();
            // Find image view button and set length first, visuals are set on render pass dynamically
            group.find(s.postImageView).length(post.media.full.length).metrics.offset(0, s.postImageOffsetY);
        }
        else
            group.find(s.postImageView).metrics.scaleY = 0;        // no image, make it invisible

        // Social stats
        refreshSocialStats(post, group);

        // Attach a background
        float length = group.autoLength().getLength() / group.getScaleY();
        length += s.postBgPaddingY;
        Clickable postView = s.postView.instantiate().length(length);
        group.viewport(postView).attach();

        return postView;
    }

    public void add(FriendsPost post) {
        if(posts.contains(post, true))
            return;     // unexpected, already added

        // Create post view
        Clickable view = createPostView(post).viewport(s.surface).attach();

        // Recognize intent to comment
        commentButtons.put(view.find(s.postCommentButton), post);
        postButtons.put(view, post);

        // Recognize intent to like
        likeButtons.put(view.find(s.postLikeButton), post);

        // Recognize intent to open media
        if(post.mediaName != null) {
            Clickable mediaButton = view.find(s.postImageView);
            mediaButtons.add(mediaButton);
            mediaPosts.add(post);
        }

        // Keep track
        post.feedView = view;
        posts.add(post);

        if(isAttached()) {
            // Mark as new
            TextBox timeView = view.find(s.postTimeView);
            timeView.text().font(s.postTimeNewFont);
            timeView.windowAnimation(postTimeNewAnim, false, true);
            // Scroll up
            s.surface.move(0, -1000);
        }

        reorderRequired = true;
    }

    public void clear() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        s.surface.detachChilds();

        posts.clear();
        commentButtons.clear();
        postButtons.clear();
        likeButtons.clear();
        mediaButtons.clear();
        mediaPosts.clear();
        ignoreMovingUp = false;

        reorderRequired = false;
    }

    private void reorderPositions() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();

        // Add from last
        for(int c = posts.size - 1; c >= 0; c--) {
            FriendsPost post = posts.items[c];

            // Add to surface
            post.feedView.metrics.anchorY = surfaceY;
            surfaceY -= (post.feedView.getLength() + s.postIntervalY);
        }
        s.surface.refresh();

        reorderRequired = false;
    }

    private void refreshNewBg() {
        for(int c = 0; c < posts.size; c++) {
            FriendsPost post = posts.items[c];
            TextBox timeView = post.feedView.find(s.postTimeView);

            if(!post.hasSeen) {
                // Post has not been seen yet
                if(timeView.text().font != s.postTimeNewFont) {
                    timeView.text().font(s.postTimeNewFont);
                    timeView.windowAnimation(postTimeNewAnim, false, true);
                }
            }
            else if(timeView.text().font == s.postTimeNewFont) {
                timeView.text().font(s.postTimeView.text().font);
                timeView.windowAnimation(null, false, false);
            }
        }
    }

    public void refresh() {
        clear();

        for(FriendsPost post : app.activePosts)
            add(post);
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        postTimeNewAnim = s.postTimeNewAnim.loopAndReset();

        if(app.activePosts.size > 0) {
            Globals.grid.postMessage(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    public FriendsWallScreen(FriendsApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBFriendsWallScreen.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Scroll surface to top
        if(!ignoreMovingUp)
            s.surface.move(0, -1000);
        else
            ignoreMovingUp = false;         // else returned back, move up next time

        // Refresh new posts bg
        refreshNewBg();
        hasSeenAllNewPosts = false;

        // Request time refresh
        tTimeRefreshScheduled = -1;

        // Allow idle scares
        grid.idleScare.reschedule();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Animate new post bg
        postTimeNewAnim.update(getRenderDeltaTime());

        // Reorder positions if required
        if(reorderRequired)
            reorderPositions();

        // Update media button renders
        for(int c = 0; c < mediaButtons.size; c++) {
            Clickable button = mediaButtons.items[c];
            Media media = mediaPosts.items[c].media;
            media.loadBest(button, -1);
        }

        // Update time strings
        if(renderTime > tTimeRefreshScheduled) {
            tTimeRefreshScheduled = renderTime + s.tTimeRefreshInterval;

            for(int c = 0; c < posts.size; c++) {
                FriendsPost post = posts.items[c];
                updatePostViewTime(post, post.feedView);
            }
        }

    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        // Check if all new posts were seen
        boolean hasSeenNewPost = false;
        boolean hasYetToSeeNewPost = false;
        for(int c = posts.size - 1; c >= 0; c--) {
            FriendsPost post = posts.items[c];
            Clickable view = post.feedView;

            if(!post.hasSeen) {
                if(view.isRenderingEnabled())
                    hasSeenNewPost = true;
                else if(hasSeenNewPost)
                    hasYetToSeeNewPost = true;
            }
        }
        if(!hasYetToSeeNewPost)
            hasSeenAllNewPosts = true;

        // Post triggers
        float tSysTime = Sys.getTime();
        for(int c = 0; c < posts.size; c++) {
            FriendsPost post = posts.items[c];
            if(tSysTime > post.tNextTriggerScheduled && post.feedView.isEffectivelyRendering()) {
                // Trigger
                v.eval(post.user.name, post.trigger);
                // Schedule next
                post.tNextTriggerScheduled = tSysTime + Globals.tFriendsTriggerInterval;
            }
        }
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();

        // If has seen all new posts, mark them
        if(getRenderTime() > s.postClearNewTime && hasSeenAllNewPosts) {
            for(int c = 0; c < posts.size; c++) {
                FriendsPost post = posts.items[c];
                post.hasSeen = true;
            }
        }
    }


    @Override
    public void onClick(Grid v, final UIElement<?> view, int b) {
        if(view == s.bars.backButton() || view == s.bars.homeButton()) {
            // Stop idle scares
            v.idleScare.stop();

            v.homescreen.transitionBack(this, v);
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(view == s.tabProfile) {
            // Stop idle scares
            v.idleScare.stop();

            ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, app.profileScreen, v.screensGroup);
            transition.attach(v.screensGroup);

            return;
        }

        // Intent to open media
        if(view instanceof Clickable) {
            // Stop idle scares
            v.idleScare.stop();

            Clickable button = (Clickable) view;
            int index = mediaButtons.indexOf(button, true);
            if(index != -1) {
                // Clicked on a media button
                FriendsPost post = mediaPosts.items[index];
                // Open
                ignoreMovingUp = true;      // maintain position
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
                // Done
                tLastButtonClick = getRenderTime();     // Remember not to open comments view simultaneously
                return;
            }
        }


        // Intent to start comment
        FriendsPost post = commentButtons.get(view);
        if(post == null)
            post = postButtons.get(view);
        if(post != null && tLastButtonClick != getRenderTime()) {           // Dont open if already opening media
            // Stop idle scares
            v.idleScare.stop();

            // Prepare comments screen
            app.commentsScreen.show(post);
            ignoreMovingUp = true;      // maintain position

            // Transition
            app.commentsScreen.open(this);

            tLastButtonClick = getRenderTime();     // Remember not to open comments view simultaneously
            return;
        }

        // Intent to like or unlike
        post = likeButtons.get(view);
        if(post != null) {
            clickLike(post, post.feedView);
            if(post.profileView != null)
                app.wallScreen.refreshSocialStats(post, post.profileView);
            tLastButtonClick = getRenderTime();     // Remember not to open comments view simultaneously
            return;
        }
    }


}

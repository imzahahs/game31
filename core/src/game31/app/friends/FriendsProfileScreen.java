package game31.app.friends;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.gb.friends.GBFriendsProfileScreen;
import sengine.Sys;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 6/10/2017.
 */

public class FriendsProfileScreen extends Menu<Grid> implements OnClick<Grid> {

    public static class Internal {
        public UIElement<?> window;
        public ScreenBar bars;
        public ScrollableSurface surface;

        // Header
        public UIElement.Group headerGroup;
        public StaticSprite headerBannerView;
        public StaticSprite headerProfileView;
        public TextBox headerNameView;
        public TextBox headerHandleView;
        public TextBox headerDescriptionView;

        // Tabs
        public Clickable tabProfile;
        public Clickable tabFeed;
    }

    private final FriendsApp app;

    // Interface source
    private final Builder<Object> builder;
    private Internal s;

    // Working
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

    private Media bannerMedia;

    void setLogin(FriendsUser login) {
        // Banner
        bannerMedia = Globals.grid.photoRollApp.find(login.banner);
        if (bannerMedia == null)
            throw new RuntimeException("Unable to find media " + login.banner);

        s.headerProfileView.visual(login.profile);

        s.headerNameView.text().text(login.fullName);
        s.headerHandleView.text().text(login.name);

        s.headerDescriptionView.autoLengthText(login.description);

        s.headerGroup.autoLength();
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        // Refresh
        if(app.activePosts.size > 0) {
            Globals.grid.postMessage(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        }
    }

    public void refresh() {
        clear();

        for(FriendsPost post : app.activePosts) {
            if(post.user == app.login)
                add(post);
        }
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

        reorderRequired = true;
    }

    public void add(FriendsPost post) {
        if(posts.contains(post, true))
            return;     // unexpected, already added

        // Create post view
        Clickable view = app.wallScreen.createPostView(post).viewport(s.surface).attach();

        // Recognize intent to comment
        commentButtons.put(view.find(app.wallScreen.s.postCommentButton), post);
        postButtons.put(view, post);

        // Recognize intent to like
        likeButtons.put(view.find(app.wallScreen.s.postLikeButton), post);

        // Recognize intent to open media
        if(post.mediaName != null) {
            Clickable mediaButton = view.find(app.wallScreen.s.postImageView);
            mediaButtons.add(mediaButton);
            mediaPosts.add(post);
        }

        // Keep track
        post.profileView = view;
        posts.add(post);

        reorderRequired = true;
    }

    private void reorderPositions() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();

        // Header
        s.headerGroup.viewport(s.surface).attach();
        s.headerGroup.metrics.anchorY = surfaceY;
        surfaceY -= s.headerGroup.getLength();

        // Add from last
        for(int c = posts.size - 1; c >= 0; c--) {
            FriendsPost post = posts.items[c];

            // Add to surface
            post.profileView.metrics.anchorY = surfaceY;
            surfaceY -= (post.profileView.getLength() + app.wallScreen.s.postIntervalY);
        }
        s.surface.refresh();

        reorderRequired = false;
    }


    public FriendsProfileScreen(FriendsApp app) {
        this.app = app;

        // Initialize
        builder = new Builder<Object>(GBFriendsProfileScreen.class, this);
        builder.build();
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Scroll surface to top
        if(!ignoreMovingUp)
            s.surface.move(0, -1000);
        else
            ignoreMovingUp = false;         // else returned back, move up next time

        // Request time refresh
        tTimeRefreshScheduled = -1;

        // Allow idle scares
        grid.idleScare.reschedule();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Reorder positions if required
        if(reorderRequired)
            reorderPositions();

        // Header
        bannerMedia.loadBest(s.headerBannerView, s.headerBannerView.getLength());

        // Update media button renders
        for(int c = 0; c < mediaButtons.size; c++) {
            Clickable button = mediaButtons.items[c];
            Media media = mediaPosts.items[c].media;
            media.loadBest(button, -1);
        }

        // Update time strings
        if(renderTime > tTimeRefreshScheduled) {
            tTimeRefreshScheduled = renderTime + app.wallScreen.s.tTimeRefreshInterval;

            for(int c = 0; c < posts.size; c++) {
                FriendsPost post = posts.items[c];
                app.wallScreen.updatePostViewTime(post, post.profileView);
            }
        }
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        // Post triggers
        float tSysTime = Sys.getTime();
        for(int c = 0; c < posts.size; c++) {
            FriendsPost post = posts.items[c];
            if(tSysTime > post.tNextTriggerScheduled && post.profileView.isEffectivelyRendering()) {
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

        builder.stop();
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

        if(view == s.tabFeed) {
            // Stop idle scares
            v.idleScare.stop();

            ScreenTransition transition = ScreenTransitionFactory.createFadeTransition(this, app.wallScreen, v.screensGroup);
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
            app.wallScreen.clickLike(post, post.profileView);
            app.wallScreen.refreshSocialStats(post, post.feedView);
            tLastButtonClick = getRenderTime();     // Remember not to open comments view simultaneously
            return;
        }
    }

}

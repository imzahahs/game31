package game31.app.friends;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.JsonSource;
import game31.ScriptState;
import game31.app.homescreen.Homescreen;
import game31.model.FriendsAppModel;
import sengine.Entity;
import sengine.Sys;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 3/9/2017.
 */

public class FriendsApp extends Entity<Grid> implements Homescreen.App, ScriptState.OnChangeListener<Object> {
    static final String TAG = "FriendsApp";

    private SimpleDateFormat timeParser;

    public final FriendsWallScreen wallScreen;
    public final FriendsProfileScreen profileScreen;
    public final FriendsCommentScreen commentsScreen;

    // Sources
    private JsonSource<FriendsAppModel> source;

    // All posts
    private final Array<FriendsPost> posts = new Array<>(FriendsPost.class);

    // Current
    final Array<FriendsPost> activePosts = new Array<>(FriendsPost.class);
    final HashMap<String, FriendsUser> users = new HashMap<>();
    // Delayed posts
    final Array<FriendsPost> scheduledPosts = new Array<>(FriendsPost.class);
    final FloatArray scheduledPostsTime = new FloatArray();

    private boolean isNotificationsDisabled = false;

    FriendsUser login;

    public void setNotificationsDisabled(boolean notificationsDisabled) {
        isNotificationsDisabled = notificationsDisabled;
    }

    public void clear() {
        wallScreen.clear();
        profileScreen.clear();
        posts.clear();
        activePosts.clear();
        users.clear();
        scheduledPosts.clear();
        scheduledPostsTime.clear();
    }

    public void refresh(boolean refreshTags) {
        Grid v = Globals.grid;

        int totalUpdated = 0;
        String notificationTitle = null;
        String notificationSubtitle = null;
        Sprite notificationProfile = null;


        if(refreshTags) {
            // For each post, check tags
            for (int c = 0; c < posts.size; c++) {
                FriendsPost post = posts.items[c];

                // Check if already active
                if (activePosts.contains(post, true) || scheduledPosts.contains(post, true))
                    continue;      // already active

                // Else validate tags
                boolean allowed = true;
                for (int i = 0; i < post.tags.length; i++) {
                    String tag = post.tags[i];
                    // Build fully qualified name
                    if (!tag.contains("."))
                        tag = Globals.FRIENDS_STATE_PREFIX + tag;
                    if (!v.state.get(tag, false)) {
                        // This tag not allowed yet
                        allowed = false;
                        break;
                    }
                }
                if (!allowed)
                    continue;       // this post not allowed yet

                // Else allow
                scheduledPosts.add(post);
                scheduledPostsTime.add(getRenderTime() + post.tDelay);
            }
        }


        // Check scheduled posts
        for(int c = 0; c < scheduledPosts.size; c++) {
            FriendsPost post = scheduledPosts.items[c];
            float tScheduled = scheduledPostsTime.items[c];

            if(getRenderTime() >= tScheduled) {
                // Check date
                if(post.time == -1)
                    post.time = Globals.grid.getSystemTime();

                wallScreen.add(post);

                if(post.user == login)
                    profileScreen.add(post);
                activePosts.add(post);

                // Notification
                if(!isNotificationsDisabled) {
                    totalUpdated++;
                    if (totalUpdated == 1) {
                        notificationTitle = post.user.fullName;
                        notificationSubtitle = post.message;
                        notificationProfile = post.user.profile;
                    }
                }
                else
                    post.hasSeen = true;

                // Remove
                scheduledPosts.removeIndex(c);
                scheduledPostsTime.removeIndex(c);
                c--;
            }
        }

        // Notification
        if(!isNotificationsDisabled && totalUpdated > 0) {
            if(totalUpdated > 1) {
                notificationTitle = "Friends";
                notificationSubtitle = String.format(Locale.US, "There are %d new posts", totalUpdated);
                notificationProfile = null;
            }


            Sprite icon = wallScreen.s.notificationIcon;
            if(notificationProfile == null) {
                notificationProfile = icon;
                icon = null;
            }

            wallScreen.s.notificationSound.play();
            Globals.grid.notification.show(notificationProfile, icon, -1, notificationTitle, notificationSubtitle, Globals.CONTEXT_APP_FRIENDS);
            Globals.grid.homescreen.addNotification(Globals.CONTEXT_APP_FRIENDS, totalUpdated);
        }
    }

    public void pack(ScriptState state) {
        // Create a new array and sort according to order
        Array<FriendsPost> copy = new Array<>(FriendsPost.class);
        for(FriendsPost post : posts) {
            if(!activePosts.contains(post, true) && !scheduledPosts.contains(post, true))
                copy.add(post);
        }

        // To retain backwards compatibility, TODO: find a better solution (try to not duplicate configs into save file)
        state.set("FriendsApp.users", users);
        state.set("FriendsApp.posts", copy.toArray());
        state.set("FriendsApp.activePosts", activePosts.toArray());
        state.set("FriendsApp.scheduledPosts", scheduledPosts.toArray());
        float[] scheduledPostsTime = this.scheduledPostsTime.toArray();
        for(int c = 0; c < scheduledPostsTime.length; c++) {
            scheduledPostsTime[c] -= getRenderTime();
        }
        state.set("FriendsApp.scheduledPostsTime", scheduledPostsTime);

    }

    public void load(final String filename, final ScriptState state) {
        // Load config
        if(source != null)
            source.stop();
        source = new JsonSource<>(filename, FriendsAppModel.class);
        source.listener(new JsonSource.OnChangeListener<FriendsAppModel>() {
            @Override
            public void onChangeDetected(JsonSource<FriendsAppModel> source) {
                // On change, just reload this file
                load(filename, state);
            }
        });

        // Load and parse
        FriendsAppModel config = source.load();
        load(config, state);

        // Register on change
        state.removeOnChangeListener(this);
        state.addOnChangeListener(Globals.FRIENDS_STATE_PREFIX, Object.class, this);

        // Start without notifications
        setNotificationsDisabled(true);
        refresh(true);
        setNotificationsDisabled(false);
    }

    private void load(FriendsAppModel config, ScriptState state) {
        clear();

        try {
            // Users
            login = new FriendsUser(config.user);
            profileScreen.setLogin(login);

            HashMap<String, FriendsUser> savedUsers = state.get("FriendsApp.users", null);
            if (savedUsers != null)
                users.putAll(savedUsers);

            // Update
            for (FriendsAppModel.ProfileModel model : config.profiles)
                users.put(model.name, new FriendsUser(model));
            users.put(login.name, login);

            FriendsPost[] saved = state.get("FriendsApp.posts", null);

            if (saved == null || Globals.d_ignoreJabbrSaves) {
                // There is no save data, add all from config
                // Profiles
                ObjectMap<String, Sprite> profiles = new ObjectMap<>();
                for (FriendsAppModel.ProfileModel model : config.profiles) {
                    profiles.put(model.name, Sprite.load(model.profile));
                }

                // Add posts
//                for (int c = config.posts.length - 1; c >= 0; c--) {
                for (int c = 0; c < config.posts.length; c++) {
                    FriendsPost post = new FriendsPost(config.posts[c], timeParser);
                    if (!post.resolveUsers(users, login)) {
                        Sys.error(TAG, "Unable to resolve users for stock post \"" + post.message + "\" from \"" + post.username + "\"");
                        continue;
                    }
                    // Else can add
                    posts.add(post);
                }

                // Unlock main tag since there was no save
                state.set(Globals.FRIENDS_STATE_PREFIX + "main", true);
            } else {
                // Replace users
                for (int c = 0; c < saved.length; c++) {
                    FriendsPost post = saved[c];
                    if (!post.resolveUsers(users, login)) {
                        Sys.error(TAG, "Unable to resolve users for saved config post \"" + post.message + "\" from \"" + post.username + "\"");
                        continue;
                    }
                    // Else can add
                    posts.add(post);
                }

                // Unlocked posts
                FriendsPost[] active = state.get("FriendsApp.activePosts", null);
                if(active != null && active.length > 0) {
                    for (int c = 0; c < active.length; c++) {
                        FriendsPost post = active[c];
                        if (!post.resolveUsers(users, login)) {
                            Sys.error(TAG, "Unable to resolve users for saved active post \"" + post.message + "\" from \"" + post.username + "\"");
                            continue;
                        }
                        scheduledPosts.add(post);
                        scheduledPostsTime.add(-Float.MAX_VALUE);
                    }
                }

                // Scheduled posts
                FriendsPost[] scheduled = state.get("FriendsApp.scheduledPosts", null);
                float[] scheduledTime = state.get("FriendsApp.scheduledPostsTime", null);
                if (scheduled != null && scheduled.length > 0) {
                    for (int c = 0; c < scheduled.length; c++) {
                        FriendsPost post = scheduled[c];
                        if (!post.resolveUsers(users, login)) {
                            Sys.error(TAG, "Unable to resolve users for saved scheduled post \"" + post.message + "\" from \"" + post.username + "\"");
                            continue;
                        }
                        // Else can add
                        scheduledPosts.add(post);
                        scheduledPostsTime.add(getRenderTime() + scheduledTime[c]);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException("Failed to load config:\n" + Globals.gson.toJson(config), e);
        }
    }

    public FriendsApp() {
        this.wallScreen = new FriendsWallScreen(this);
        this.profileScreen = new FriendsProfileScreen(this);
        this.commentsScreen = new FriendsCommentScreen(this);

        // Time formats
        timeParser = new SimpleDateFormat("dd MMMM yyyy, h:mm a", Locale.US);
        timeParser.setTimeZone(Globals.grid.timeZone);

        load(Globals.jabbrConfigFilename, Globals.grid.state);
    }

    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        if(source != null)
            source.start();
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        if(source != null)
            source.stop();
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        // Refresh scheduled posts
        refresh(false);
    }

    @Override
    public Entity<?> open() {
        // Reset
        wallScreen.ignoreMovingUp = false;
        profileScreen.ignoreMovingUp = false;

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_TYPE_JABBR);

        return wallScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // nothing ?
    }

    @Override
    public void onChanged(String name, Object var, Object prev) {
        // Change on "friends." namespace, refresh
        refresh(true);
    }
}

package game31.app.friends;

import com.badlogic.gdx.utils.Array;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import game31.Globals;
import game31.Media;
import game31.MediaAlbum;
import game31.app.gallery.PhotoRollApp;
import game31.model.FriendsAppModel;
import sengine.mass.MassSerializable;
import sengine.ui.Clickable;

/**
 * Created by Azmi on 3/18/2017.
 */
public class FriendsPost implements MassSerializable {
    private static String[] splitCSV(String csv) {
        String[] tags = csv.trim().split("\\s*,\\s*");
        if(tags.length == 1 && tags[0].isEmpty())
            return new String[0];
        return tags;
    }

    public final String[] tags;

    public final String username;
    public long time;
    public final float tDelay;
    public final String location;

    public final String message;
    public final String mediaName;

    public int likes;
    public boolean hasUserLiked;
    public final Array<FriendsComment> comments = new Array<FriendsComment>(true, 0, FriendsComment.class);
    public boolean hasUserCommented;
    public final int hiddenCommentsCount;

    public boolean hasSeen = false;

    public final String trigger;

    // Current, transient
    public FriendsUser user;
    public Clickable feedView;
    public Clickable profileView;
    public float tNextTriggerScheduled = -1;         // Global time


    // Resolve media
    public Media media = null;
    public MediaAlbum album = null;
    public int mediaIndex = -1;

    public boolean resolveUsers(HashMap<String, FriendsUser> users, FriendsUser login) {
        user = users.get(username);
        if(user == null)
            return false;
        // Resolve comments
        for(int c = 0; c < comments.size; c++) {
            FriendsComment comment = comments.items[c];
            if(!comment.resolveUser(users))
                return false;
            if(comment.user == login)
                hasUserCommented = true;
        }
        return true;
    }

    public void prepareMedia() {
        if (mediaName == null) {
            // Reset
            media = null;
            album = null;
            mediaIndex = -1;
            return;     // nothing to prepare
        }
        // Else resolve
        PhotoRollApp photoRollApp = Globals.grid.photoRollApp;
        media = photoRollApp.unlock(mediaName, false);
        if (media == null)
            throw new RuntimeException("Unable to find media " + mediaName);
        album = photoRollApp.findAlbum(media.album);
        mediaIndex = album.indexOf(media);
        // Done
    }

    public void like() {
        if (hasUserLiked)
            return;
        hasUserLiked = true;
        likes++;
    }


    public FriendsPost(FriendsAppModel.PostModel model, SimpleDateFormat timeFormat) {

        tags = splitCSV(model.tags);

        username = model.from.name;

        try {
            if(model.from.time.equals(Globals.TIME_AUTO))
                time = -1;          // to be determined on unlock
            else
                time = timeFormat.parse(model.from.time).getTime();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to parse time: " + model.from.time, e);
        }

        tDelay = model.from.delay;
        location = model.location;

        message = model.message;
        mediaName = model.media;

        likes = model.likes.likes;
        hasUserLiked = model.likes.has_user_liked;

        for (int i = 0; i < model.comments.length; i++) {
            FriendsComment comment = new FriendsComment(model.comments[i]);
            comments.add(comment);
        }
        hiddenCommentsCount = model.hiddenCommentsCount;

        trigger = model.trigger;
        tNextTriggerScheduled = trigger != null ? -1 : Float.MAX_VALUE;
    }

    @MassConstructor
    public FriendsPost(String[] tags, String username, long time, float tDelay, String location, String message, String mediaName, int likes, boolean hasUserLiked, FriendsComment[] comments, int hiddenCommentsCount, boolean hasSeen, String trigger) {
        this.tags = tags;

        this.username = username;
        this.time = time;
        this.tDelay = tDelay;
        this.location = location;

        this.message = message;
        this.mediaName = mediaName;

        this.likes = likes;
        this.hasUserLiked = hasUserLiked;

        this.comments.addAll(comments);
        this.hiddenCommentsCount = hiddenCommentsCount;

        this.hasSeen = hasSeen;

        this.trigger = trigger;
        tNextTriggerScheduled = trigger != null ? -1 : Float.MAX_VALUE;
    }

    @Override
    public Object[] mass() {
        return new Object[] { tags, username, time, tDelay, location, message, mediaName, likes, hasUserLiked, comments.toArray(), hiddenCommentsCount, hasSeen, trigger };
    }
}

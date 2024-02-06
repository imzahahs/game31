package game31.app.friends;

import java.util.HashMap;

import game31.Globals;
import game31.Media;
import game31.MediaAlbum;
import game31.app.gallery.PhotoRollApp;
import game31.model.FriendsAppModel;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 3/18/2017.
 */
public class FriendsComment implements MassSerializable {
    public String username;
    public final String message;
    public final String mediaName;          // TODO

    // Resolve media, transient
    public FriendsUser user;
    public Media media = null;
    public MediaAlbum album = null;
    public int mediaIndex = -1;

    public boolean resolveUser(HashMap<String, FriendsUser> users) {
        user = users.get(username);
        return user != null;
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

    public FriendsComment(FriendsAppModel.CommentModel model) {
        username = model.name;
        message = model.message;
        mediaName = model.media;

    }

    @MassConstructor
    public FriendsComment(String username, String message, String mediaName) {
        this.username = username;
        this.message = message;
        this.mediaName = mediaName;
    }

    @Override
    public Object[] mass() {
        return new Object[] { username, message, mediaName };
    }
}

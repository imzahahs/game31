package game31.app;

import game31.Globals;
import game31.Media;
import game31.MediaAlbum;
import game31.app.gallery.PhotoRollApp;
import game31.app.gallery.PhotoRollGridScreen;
import game31.app.homescreen.Homescreen;
import sengine.Entity;

public class DownloadsApp implements Homescreen.App {


    private MediaAlbum getDownloadsAlbum() {
        PhotoRollApp app = Globals.grid.photoRollApp;
        MediaAlbum album = app.findAlbum(Globals.GALLERY_DOWNLOADS_ALBUM);
        if(album == null) {
            app.prepareAlbums(Globals.GALLERY_DOWNLOADS_ALBUM);
            album = app.findAlbum(Globals.GALLERY_DOWNLOADS_ALBUM);
        }
        return album;
    }

    @Override
    public Entity<?> open() {
        PhotoRollGridScreen gridScreen = Globals.grid.photoRollApp.gridScreen;
        gridScreen.show(getDownloadsAlbum(), false, null);
        return gridScreen;
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        MediaAlbum album = getDownloadsAlbum();
        int totalUnopened = 0;
        for(Media media : album.medias) {
            if(!media.wasOpened())
                totalUnopened++;
        }
        if(totalUnopened != homescreen.getTotalNotifications(Globals.CONTEXT_APP_DOWNLOADS)) {
            homescreen.clearNotifications(Globals.CONTEXT_APP_DOWNLOADS);
            if(totalUnopened > 0)
                homescreen.addNotification(Globals.CONTEXT_APP_DOWNLOADS, totalUnopened);
        }
    }
}

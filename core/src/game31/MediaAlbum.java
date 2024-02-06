package game31;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Azmi on 25/7/2016.
 */
public class MediaAlbum {
    public final Array<Media> medias = new Array<Media>(Media.class);
    public final String name;

    // For location albums
    public final float x;
    public final float y;

    public int findPrevPhoto(int index) {
        index--;
        while(index >= 0 && (medias.items[index].isVideo() || medias.items[index].isAudio()))
            index--;
        return index;
    }

    public int findNextPhoto(int index) {
        index++;
        while(index < medias.size && (medias.items[index].isVideo() || medias.items[index].isAudio()))
            index++;
        if(index == medias.size)
            return -1;  // no more
        return index;
    }

    public int countPhotoIndex(int index) {
        int count = 0;
        for(int c = 0; c < index; c++) {
            if(!medias.items[c].isVideo() && !medias.items[c].isAudio())
                count++;
        }
        return count;
    }

    public int countPhotos() {
        return countPhotoIndex(medias.size);
    }

    public int indexOf(Media media) {
        return medias.indexOf(media, true);
    }

    public MediaAlbum(String name) {
        this(name, Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public MediaAlbum(String name, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }
}

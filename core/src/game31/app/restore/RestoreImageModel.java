package game31.app.restore;

import com.badlogic.gdx.utils.IntArray;

import game31.Globals;
import sengine.graphics2d.Sprite;
import sengine.mass.MassSerializable;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 6/19/2017.
 */

@SheetsParser.Row(fields = { "name", "subtitle", "trigger" })
public class RestoreImageModel implements MassSerializable {

    public static final int P_EMPTY = 0;
    public static final int P_IMAGE = 1;
    public static final int P_CORRUPTED = -1;

    private static final String TOKEN_EMPTY = "E";
    private static final String TOKEN_IMAGE = "I";
    private static final String TOKEN_CORRUPTED = "X";



    // Working
    public String name;
    public String subtitle;
    public Sprite image;
    public final IntArray profiles = new IntArray();
    /**
     * Triggered on complete
     */
    public String trigger;

    //=== SheetParser
    public void image(String filename) {
        image = Sprite.load(filename);
        // Crop to 1
        if(image.getLength() != Globals.restoreImageLength) {
            image = new Sprite(image.length, image.getMaterial());
            image = image.crop(Globals.restoreImageLength);
        }
    }

    public void profile(String[] tokens) {
        // Check if maxed out profiles
        int totalProfiles = profiles.size / Globals.restoreImageFragments;
        if(totalProfiles >= Globals.restoreImageMaxProfiles)
            throw new RuntimeException("Maxed out total profiles of " + Globals.restoreImageMaxProfiles);
        // Parse profile tokens
        if (tokens.length != Globals.restoreImageFragments)
            throw new RuntimeException("Invalid number of fragments, expected: " + Globals.restoreImageFragments + ", found: " + tokens.length);
        for(String token : tokens) {
            if(token.equalsIgnoreCase(TOKEN_EMPTY))
                profiles.add(P_EMPTY);
            else if(token.equalsIgnoreCase(TOKEN_IMAGE))
                profiles.add(P_IMAGE);
            else if(token.equalsIgnoreCase(TOKEN_CORRUPTED))
                profiles.add(P_CORRUPTED);
            else
                throw new RuntimeException("Invalid token: " + token);
        }
    }


    //=== Mass
    public RestoreImageModel() {
    }

    @MassConstructor
    public RestoreImageModel(String name, String subtitle, Sprite image, int[] profiles, String trigger) {
        this.name = name;
        this.subtitle = subtitle;
        this.image = image;
        this.profiles.addAll(profiles);
        this.trigger = trigger;
    }

    @Override
    public Object[] mass() {
        return new Object[] { name, subtitle, image, profiles.toArray(), trigger };
    }
}

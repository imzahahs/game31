package game31.app.restore;

import sengine.mass.MassSerializable;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 6/21/2017.
 */

@SheetsParser.Row(fields = { "name", "subtitle", "trigger", "words" })
public class RestorePhraseModel implements MassSerializable {

    public String name;
    public String subtitle;
    public String trigger;
    public String[] words;


    public RestorePhraseModel() {
        // for reflection
    }

    @MassConstructor
    public RestorePhraseModel(String name, String subtitle, String trigger, String[] words) {
        this.name = name;
        this.subtitle = subtitle;
        this.trigger = trigger;
        this.words = words;
    }

    @Override
    public Object[] mass() {
        return new Object[] { name, subtitle, trigger, words };
    }
}

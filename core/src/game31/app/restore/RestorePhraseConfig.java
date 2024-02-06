package game31.app.restore;

import sengine.File;
import sengine.mass.MassSerializable;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 6/20/2017.
 */

@SheetsParser.Row(fields = { })
public class RestorePhraseConfig implements MassSerializable {

    public void phrase(RestorePhraseModel model) {
        // Save to hints
        File.saveHints(model.name, model);
    }


    @MassConstructor
    public RestorePhraseConfig() {

    }

    @Override
    public Object[] mass() {
        return new Object[] { };
    }
}

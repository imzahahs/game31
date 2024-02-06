package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 22/7/2016.
 */

@SheetsParser.Row(fields = {"thumb_config", "square_config", "photos"})
public class PhotoAppModel {

    public String thumb_config;
    public String square_config;

    public MediaModel[] photos;

}

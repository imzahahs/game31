package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 22/7/2016.
 */
@SheetsParser.Row(fields = { "filename", "name", "album", "date_text", "caption", "is_hidden", "is_hidden_until_opened", "trigger", "corruption" })
public class MediaModel {

    public String filename;
    public String name;
    public String album;
    public String date_text;
    public String caption;

    public boolean is_hidden;
    public boolean is_hidden_until_opened;

    public String trigger;

    public String corruption;
}

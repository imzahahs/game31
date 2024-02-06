package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 26/8/2016.
 */
@SheetsParser.Row(fields = { "name", "profile_pic_path", "big_profile_pic_path", "number", "device", "dialogue_tree_path", "google_sheet_url", "is_hidden", "trigger", "attributes" })
public class ContactModel {

    private static final String defaultProfilePicPath = "system/profile.png";
    private static final String defaultBigProfilePicPath = "system/profile-big.png";

    @SheetsParser.Row(fields = { "attribute", "value", "action" })
    public static class ContactAttributeModel {
        public String attribute;
        public String value;
        public String action;
    }

    public String name;
    public String profile_pic_path = defaultProfilePicPath;
    public String big_profile_pic_path = defaultBigProfilePicPath;

    // Default number attribute
    public String number;
    public String device;

    public String dialogue_tree_path;
    public String google_sheet_url;

    public boolean is_hidden;
    public String trigger;

    public ContactAttributeModel[] attributes = new ContactAttributeModel[0];

}

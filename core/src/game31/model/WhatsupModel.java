package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 20/7/2016.
 */

@SheetsParser.Row(fields = {"contacts"})
public class WhatsupModel {

    private static final String defaultProfilePicPath = "system/profile.png";


    @SheetsParser.Row(fields = {"name", "profile_pic_path", "dialogue_tree_path", "googleSheetUrl"})
    public static class ContactModel {
        public String name;
        public String profile_pic_path = defaultProfilePicPath;
        public String dialogue_tree_path;

        public String googleSheetUrl;
    }

    public ContactModel[] contacts;

}

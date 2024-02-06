package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 20/7/2016.
 */

@SheetsParser.Row(fields = { "name", "email", "conversations" })
public class MailModel {

    @SheetsParser.Row(fields = { "name", "email", "subject", "dialogue_tree_path", "google_sheet_url"})
    public static class ConversationModel {
        public String name;
        public String email;
        public String subject;
        public String dialogue_tree_path;
        public String google_sheet_url;
    }

    public String name;
    public String email;

    public ConversationModel[] conversations;

}

package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 26/8/2016.
 */

@SheetsParser.Row(fields = { "contacts", "favourites", "recents", "emergency_numbers", "default_ringtone" })
public class PhoneAppModel {

    private static final PhoneRecentModel[] defaultEmptyRecentsArray = new PhoneRecentModel[0];
    private static final String[] defaultEmptyStringsArray = new String[0];

    public static final String RECENT_INCOMING = "incoming";
    public static final String RECENT_OUTGOING = "outgoing";
    public static final String RECENT_MISSED = "missed";


    @SheetsParser.Row(fields = { "name", "time", "type" })
    public static class PhoneRecentModel {
        public String name;
        public String time;
        public String type;     // incoming, outgoing, missed

        public PhoneRecentModel() {
            // for serialization
        }

        public PhoneRecentModel(String name, String time, String type) {
            this.name = name;
            this.time = time;
            this.type = type;
        }
    }



    public ContactModel[] contacts;

    public String[] favourites;

    public PhoneRecentModel[] recents = defaultEmptyRecentsArray;

    public String[] emergency_numbers = defaultEmptyStringsArray;

    public String default_ringtone;

}

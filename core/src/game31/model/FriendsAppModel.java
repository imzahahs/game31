package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 3/18/2017.
 */

@SheetsParser.Row(fields = { "user", "profiles", "posts" })
public class FriendsAppModel {

    private static final String defaultProfilePicPath = "apps/jabbr/profile-default.png";

    private static final String undefinedString = "<undefined>";
    private static final String emptyString = "";
    private static final LikesModel emptyLikesModel = new LikesModel();
    private static final CommentModel[] emptyCommentsModelArray = new CommentModel[0];
    private static final ProfileModel[] emptyProfileModelArray = new ProfileModel[0];
    private static final PostModel[] emptyPostModelArray = new PostModel[0];



    @SheetsParser.Row(fields = { "name", "message", "media" })
    public static class CommentModel {
        public String name = undefinedString;
        public String message = null;
        public String media = null;
    }

    @SheetsParser.Row(fields = { "name", "time", "delay" })
    public static class FromModel {
        public String name = undefinedString;
        public String time = undefinedString;
        public float delay = 0;
    }

    @SheetsParser.Row(fields = { "likes", "has_user_liked" })
    public static class LikesModel {
        public int likes = 0;
        public boolean has_user_liked = false;
    }

    @SheetsParser.Row(fields = { "tags", "from", "message", "location", "media", "likes", "comments", "hiddenCommentsCount", "trigger"})
    public static class PostModel {
        public String tags = emptyString;

        public FromModel from = null;               // This is required

        public String message = null;
        public String location = null;
        public String media = null;

        public LikesModel likes = emptyLikesModel;

        public CommentModel[] comments = emptyCommentsModelArray;
        public int hiddenCommentsCount = 0;

        public String trigger = null;
    }

    @SheetsParser.Row(fields = { "name", "fullName", "profile", "banner", "description" })
    public static class ProfileModel {
        public String name = undefinedString;
        public String fullName = undefinedString;
        public String profile = defaultProfilePicPath;           // This is required
        // Profile view data
        public String banner;            // required if needed
        public String description;
    }


    // User's profile
    public ProfileModel user = null;            // This is required

    public ProfileModel[] profiles = emptyProfileModelArray;

    public PostModel[] posts = emptyPostModelArray;

}

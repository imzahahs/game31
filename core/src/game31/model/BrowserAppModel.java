package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 7/12/2017.
 */

@SheetsParser.Row(fields = { "pages", "mostVisited", "bookmarks", "history" })
public class BrowserAppModel {

    private static final PageModel[] defaultEmptyPageArray = new PageModel[0];
    private static final String[] defaultEmptyStringArray = new String[0];

    private static final String defaultFavicon = "apps/browser/favicon-default.png";

    @SheetsParser.Row(fields = { "name", "title", "url", "isPublic", "favicon", "connectingSpeed", "speed", "previewFilename", "layout" })
    public static class PageModel {
        public String name;
        public String title;
        public String url;
        public boolean isPublic;
        public String favicon = defaultFavicon;
        public float connectingSpeed = 1f;
        public float speed = 1f;
        public String previewFilename;          // only used for most visited section
        public LayoutModel layout;
    }

    public PageModel[] pages = defaultEmptyPageArray;

    public String[] mostVisited = defaultEmptyStringArray;

    public String[] bookmarks = defaultEmptyStringArray;

    public String[] history = defaultEmptyStringArray;
}

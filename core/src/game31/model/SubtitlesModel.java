package game31.model;

import sengine.utils.SheetsParser;

/**
 * Created by someguy233 on 21/5/2018.
 */

@SheetsParser.Row(fields = { "files" })
public class SubtitlesModel {

    @SheetsParser.Row(fields = { "position", "text", "duration" })
    public static class TextModel {
        public float position;

        public String text;

        public float duration = -1;
    }

    @SheetsParser.Row(fields = { "filename", "texts" })
    public static class FileModel {
        public String filename;

        public TextModel[] texts;
    }

    public FileModel[] files;
}

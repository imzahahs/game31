package game31.model;

import game31.Globals;
import sengine.animation.Animation;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 7/12/2017.
 */
@SheetsParser.Row(fields = {"layoutName", "contentName", "filename", "numRegionFiles", "bgColor", "sectionSize", "contentSectionSize", "texts", "links", "inputs", "checkboxes", "trigger"})
public class LayoutModel {

    @SheetsParser.Row(fields = {"region", "font", "fontSize", "fontColor", "alignment", "text", "wrapChars"})
    public static class LayoutTextModel {
        private static final String TAG = "LayoutTextModel";

        public String region;               // rgb / rgba hex
        public String font;
        public int fontSize;
        public String fontColor;            // rgb / rgba hex
        public String alignment;            // top left, top center, top right, left, center, right, bottom left, bottom center, bottom right
        public String text;

        public float wrapChars = 0;

        public Animation startAnim;
        public Animation idleAnim;
        public Animation endAnim;

        public void startAnim(String code) {
            startAnim = (Animation) Globals.grid.eval(TAG, code);
        }
        public void idleAnim(String code) {
            idleAnim = (Animation) Globals.grid.eval(TAG, code);
        }
        public void endAnim(String code) {
            endAnim = (Animation) Globals.grid.eval(TAG, code);
        }
    }

    @SheetsParser.Row(fields = {"region", "action"})
    public static class LayoutLink {
        public String region;
        public String action;
    }

    @SheetsParser.Row(fields = {"region", "name", "font", "fontSize", "fontColor", "wrapChars", "confirmText", "confirmAction", "inputType", "inputPaddingLeft", "inputPaddingTop", "inputPaddingRight", "inputPaddingBottom"})
    public static class LayoutInput {
        public String region;
        public String name = defaultUndefinedString;
        public String font;
        public int fontSize;
        public String fontColor;            // rgb / rgba hex
        public int wrapChars;
        public String confirmText;
        public String confirmAction;
        public String inputType = defaultInputType;
        public float inputPaddingLeft;
        public float inputPaddingTop;
        public float inputPaddingRight;
        public float inputPaddingBottom;
    }

    @SheetsParser.Row(fields = {"region", "name", "groupName", "color", "action", "inputPaddingLeft", "inputPaddingTop", "inputPaddingRight", "inputPaddingBottom"})
    public static class LayoutCheckbox {
        public String region;
        public String name = defaultUndefinedString;
        public String groupName;
        public String color = defaultCheckboxColor;            // rgb / rgba hex
        public String action;
        public float inputPaddingLeft;
        public float inputPaddingTop;
        public float inputPaddingRight;
        public float inputPaddingBottom;
    }

    private static final LayoutTextModel[] defaultEmptyTextArray = new LayoutTextModel[0];
    private static final LayoutLink[] defaultEmptyLinkArray = new LayoutLink[0];
    private static final LayoutInput[] defaultEmptyInputArray = new LayoutInput[0];
    private static final LayoutCheckbox[] defaultEmptyCheckboxesArray = new LayoutCheckbox[0];
    private static final String defaultBgColor = "ffffff";
    private static final String defaultCheckboxColor = "000000";
    private static final String defaultUndefinedString = "<undefined>";
    private static final String defaultInputType = "text";


    public String layoutName;
    public String contentName;
    public String filename;
    public int numRegionFiles;

    public String bgColor = defaultBgColor;                  // rgb / rgba hex

    public int sectionSize = Globals.layoutSectionSize;
    public int contentSectionSize = Globals.layoutContentSectionSize;

    public LayoutTextModel[] texts = defaultEmptyTextArray;
    public LayoutLink[] links = defaultEmptyLinkArray;
    public LayoutInput[] inputs = defaultEmptyInputArray;
    public LayoutCheckbox[] checkboxes = defaultEmptyCheckboxesArray;

    public String trigger;
}

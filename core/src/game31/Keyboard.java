package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

import java.util.Collections;
import java.util.List;

import game31.app.gallery.PhotoRollAlbumsScreen;
import game31.gb.GBKeyboard;
import sengine.Entity;
import sengine.Sys;
import sengine.Universe;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 12/7/2016.
 */
public class Keyboard extends Menu<Grid> implements OnClick<Grid>, PhotoRollAlbumsScreen.SelectCallback {
    static final String TAG = "Keyboard";

    public static class Internal {

        public UIElement.Group viewGroup;

        public UIElement<?> keyboardView;
        public Clickable[] keyboardKeys;
        public Clickable keyboardSpace;
        public Clickable keyboardEraseButton;
        public Clickable keyboardShift;
        public Clickable keyboardNumeric;
        public Clickable keyboardConfirm;
        public String[] uppercaseKeys;
        public String[] lowercaseKeys;
        public String[] numericKeys;
        public Animation hintAnim;

        public UIElement<?> selectorView;
        public ScrollableSurface selectorSurface;
        public UIElement<?> selectorTitle;
        // Selector row group
        public PatchedTextBox selectorRow;
        public float selectorRowIntervalY;
        public PatchedTextBox selectorKeyboardRow;
        public PatchedTextBox selectorAttachmentRow;

        public Audio.Sound keySound;
        public Audio.Sound backSound;
        public Audio.Sound enterSound;
        public Audio.Sound spaceSound;
        public float keySoundVolume;

        public Audio.Sound openSound;
        public Audio.Sound acceptSound;
        public Audio.Sound cancelSound;


        public UIElement<?> wordGroup;
        public ScrollableSurface wordSurface;
        public PatchedTextBox wordTemplate;
        public Toast hintView;
        public TextBox hintTextView;
        public Animation wordHintAnim;

    }

    private enum KeyboardState {
        Uppercase,
        Lowercase,
        Numeric
    }

    public interface KeyboardInput {
        void keyboardTyped(String text, int matchOffset);
        void keyboardClosed();
        void keyboardPressedConfirm();
    }

    private class CancelClickable extends Clickable {

        boolean queueHide = false;

        @Override
        public void activated(Universe v, int button) {
            queueHide = true;
        }

        @Override
        protected void render(Universe v, float r, float renderTime) {
            super.render(v, r, renderTime);  // no need to render

            if(queueHide) {
                queueHide = false;
                s.cancelSound.play();
                hide(false);
            }
        }
    }


    // Source
    private final Builder<Object> interfaceSource;
    private Internal s;

    
    // Working
    private String charsList;

    private final CancelClickable cancelBg;
    private boolean isKeyboardShowing = false;
    private boolean isSelectorsShowing = false;
    private boolean isManualEntryDisabled = false;

    private Entity<?> screen = null;
    private UIElement<?> movedWindow = null;
    private boolean isWindowMoved = false;
    private float movedWindowPaddingY = 0;
    private KeyboardInput input = null;

    private KeyboardState state = null;
    private boolean shouldStartUppercase = false;
    
    private final Array<String> autoComplete = new Array<String>(String.class);
    private final Array<String> hiddenAutoComplete = new Array<String>(String.class);

    private final Array<String> allowedAutoComplete = new Array<String>(String.class);
    private final Array<String> words = new Array<String>(String.class);
    private final Array<String> hiddenWords = new Array<String>(String.class);
    private final IntArray wordRatings = new IntArray();

    private final Array<PatchedTextBox> autoCompleteViews = new Array<PatchedTextBox>(PatchedTextBox.class);

    private final Array<PatchedTextBox> selectorViews = new Array<PatchedTextBox>(PatchedTextBox.class);

    private Entity<?>[] excludedViews = null;

    private String typed = "";
    private int typedMatch = 0;

    private boolean isFromAttachment = false;
    private String queuedAttachment = null;

    private boolean isWildcard = false;

    private boolean isFirstLetterUppercase = false;


    private int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (Character.toLowerCase(lhs.charAt(i - 1)) == Character.toLowerCase(rhs.charAt(j - 1))) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        int distance = cost[len0 - 1];
        // Calculate prefix distance
        for(int c = 1; c < len0 && c < len1; c++) {
            if(Character.toLowerCase(lhs.charAt(c - 1)) == Character.toLowerCase(rhs.charAt(c - 1)))
                distance -= 100;
        }
        return distance;
    }


    private void backspace() {
        int offset = typed.length();
        boolean isErasing = false;
        if(autoComplete.size > 0) {
            // There is autocomplete, erase words
            while (offset > 0) {
                char ch = typed.charAt(offset - 1);
                if (Character.isWhitespace(ch)) {
                    // Erase whitespace
                    if (isErasing)
                        break;      // erased the word
                    offset--;
                    continue;
                }
                if (Character.isLowSurrogate(ch)) {
                    if (isErasing)
                        break;      // erased the word
                    offset -= 2;
                    break;
                }
                // Else is a word
                isErasing = true;
                offset--;
            }
        }
        else if(offset > 0) {
            // No autocomplete, erase letters
            offset--;
            char ch = typed.charAt(offset);
            if(Character.isLowSurrogate(ch))
                offset--;
        }
        if(offset <= 0)
            typed = "";         // erased to start
        else
            typed = typed.substring(0, offset);
        if(typedMatch > typed.length())
            typedMatch = typed.length();
    }

    public void requestAutoCompleteLastWord() {
        if(!typed.endsWith(" "))
            onClick(Globals.grid, s.keyboardSpace, Input.Buttons.LEFT);
    }

    private void autoCompleteLastWord(String bestWord) {
        if(isWildcard)
            return;
        // For current typed, get the last word
        if(typed.length() == 0) {
            if(bestWord != null)
                typed = bestWord;
            return;     // cannot autocomplete
        }
        if(typed.charAt(typed.length() - 1) == ' ') {
            if(bestWord != null)
                typed += bestWord;
            return;     // ends with a whitespace or emoticon, can't identify word
        }
        // Extract word
        String sb = typed.trim();
        int start = sb.lastIndexOf(' ') + 1;
        while(start > 0 && (start - 2) < sb.length() && Character.isWhitespace(sb.charAt(start - 2)))
            start--;
        String word = sb.substring(start);
        // For this word, find nearest autocomplete word if not provided
        if(bestWord == null) {
            if(words.size == 0)
                return;         // no words to choose from
            int bestDistance = Integer.MAX_VALUE;
            for (int c = 0; c < words.size; c++) {
                String s = words.items[c];
                int distance = levenshteinDistance(word, s);
                if (distance < bestDistance) {
                    bestWord = s;
                    bestDistance = distance;
                }
            }
        }
        // Found the word, substitute with this word
        typed = typed.substring(0, typed.length() - word.length()) + bestWord;
    }

    public String typed() {
        return typed;
    }

    public void typed(String typed) {
        this.typed = typed != null ? typed : "";
        refreshAutoComplete();
    }

    @Override
    public void onReturnFromAttachment(Entity<?> attachmentScreen) {
        if(screen == null) {
            Sys.error(TAG, "screen not set");
            return;     // UB
        }
        ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(attachmentScreen, screen, attachmentScreen.getEntityParent());
        transition.attach(attachmentScreen.getEntityParent());
    }

    @Override
    public void onSelectedMedia(String media) {
        queuedAttachment = media;
    }


    private void submitTyped(String text) {
        if(input == null) {
            Sys.error(TAG, "input not set");
            return;     // UB
        }
        typed = text;
        typedMatch = typed.length();
        input.keyboardTyped(typed, typedMatch);
        input.keyboardPressedConfirm();
    }

    public void clearTyped() {
        typed = "";
        typedMatch = 0;
        s.hintView.detach();
    }

    public void requestTypedCallback() {
        input.keyboardTyped(typed, typedMatch);
    }

    public void refreshAutoComplete() {
        if(isWildcard)
            return;     // using wildcards, no autocomplete
        allowedAutoComplete.clear();
        typedMatch = 0;
        words.clear();
        hiddenWords.clear();
        wordRatings.clear();
        clearAutoCorrect();
        if(autoComplete.size == 0) {
            s.wordGroup.detachWithAnim();
            return;     // cannot autocomplete
        }
        // First, create a cleaned up version of typed
        int whitespaceOffset = 0;
        while(whitespaceOffset < typed.length() && Character.isWhitespace(typed.charAt(whitespaceOffset)))
            whitespaceOffset++;
        String sb = whitespaceOffset > 0 ? typed.substring(whitespaceOffset) : typed;
        // Find the offset of last word
        int wordOffset = sb.lastIndexOf(' ') + 1;
        while(wordOffset > 1 && (wordOffset - 2) < sb.length() && Character.isWhitespace(sb.charAt(wordOffset - 2)))
            wordOffset--;
        // Find autocompletes that match up to this word
        for(int c = 0; c < autoComplete.size; c++) {
            String text = autoComplete.items[c];
            if(text.startsWith(Globals.PHOTOROLL_PREFIX) || text.startsWith(Globals.KEYBOARD_WILDCARD))
                continue;       // can never autocomplete this
            boolean matched = true;
            int match = 0;
            for(int length = text.length(); match < wordOffset && match < length; match++) {
                if(Character.toLowerCase(sb.charAt(match)) != Character.toLowerCase(text.charAt(match))) {
                    matched = false;
                    break;
                }
            }
            if(match > typedMatch)
                typedMatch = match;
            if(matched && wordOffset < text.length())
                allowedAutoComplete.add(text);
        }
        if(typed.length() > 0 && Character.isWhitespace(typed.charAt(typed.length() - 1)) && typedMatch == (wordOffset - 1))
            typedMatch = typed.length();
        else
            typedMatch += whitespaceOffset;
        // Extract current typed word
        String typedWord = "";
        if(wordOffset < sb.length())
            typedWord = sb.substring(wordOffset);
        // Now for each possible autocompletes, extract next word
        for(int c = 0; c < allowedAutoComplete.size; c++) {
            String s = allowedAutoComplete.items[c];
            int end = s.indexOf(' ', wordOffset);
            if(end == -1)
                end = s.length();
            String word = s.substring(wordOffset, end);
            if(words.contains(word, false))
                continue;       // already added
            words.add(word);
            wordRatings.add(levenshteinDistance(word, typedWord));
            if(hiddenAutoComplete.contains(s, true))
                hiddenWords.add(word);
        }
        // If there are words, can't tell if there is a match yet
        if(words.size > 0)
            typedMatch = -1;
        // Now add autocomplete according to best match
        for(int c = 0; c < words.size; c++) {
            int best = -1;
            int bestRating = Integer.MAX_VALUE;
            for(int i = 0; i < words.size; i++) {
                String word = words.items[i];
                if(hiddenWords.contains(word, true))
                    continue;           // is hidden, do not show
                int rating = wordRatings.items[i];
                if(rating < bestRating) {
                    best = i;
                    bestRating = rating;
                }
            }
            // Add this word
            if(best == -1)
                break;
            addAutoCorrect(words.items[best]);
            wordRatings.items[best] = Integer.MAX_VALUE;       // mark as used
        }
        justifyAutoCorrect();
    }



    public void resetAutoComplete() {
        resetAutoComplete(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    public void resetAutoComplete(List<String> autoCompleteList, List<String> hiddenAutoCompleteList) {
        // Cheeck if autocomplete is compatibles
        if(autoCompleteViews.size > 0 && autoComplete.size == (autoCompleteList.size() + hiddenAutoCompleteList.size()) && hiddenAutoComplete.size == hiddenAutoCompleteList.size()) {
            boolean isCompatible = true;
            for(String s : autoComplete) {
                if(!autoCompleteList.contains(s) && !hiddenAutoCompleteList.contains(s)) {
                    isCompatible = false;
                    break;
                }
            }
            if(isCompatible) {
                for(String s : hiddenAutoComplete) {
                    if(!hiddenAutoCompleteList.contains(s)) {
                        isCompatible = false;
                        break;
                    }
                }
                if(isCompatible)
                    return;     // same autocomplete list
            }
        }
        // Reset
        autoComplete.clear();
        for(String s : autoCompleteList)
            autoComplete.add(s);
        hiddenAutoComplete.clear();
        for(String s : hiddenAutoCompleteList) {
            int idx = autoComplete.indexOf(s, false);
            if(idx != -1)
                autoComplete.set(idx, s);
            else
                autoComplete.add(s);
            hiddenAutoComplete.add(s);
        }
        // Refresh
        clearAutoCorrect();
        refreshAutoComplete();
        typedMatch = 0;
    }


    public void showUppercase() {
        if(state == KeyboardState.Uppercase)
            return;
        state = KeyboardState.Uppercase;
        for(int c = 0; c < s.keyboardKeys.length; c++) {
            s.keyboardKeys[c].text().text(s.uppercaseKeys[c]);
        }
    }

    public void showLowercase() {
        if(state == KeyboardState.Lowercase)
            return;
        state = KeyboardState.Lowercase;
        for(int c = 0; c < s.keyboardKeys.length; c++) {
            s.keyboardKeys[c].text().text(s.lowercaseKeys[c]);
        }
    }

    public void showNumeric() {
        if(state == KeyboardState.Numeric)
            return;
        state = KeyboardState.Numeric;
        for(int c = 0; c < s.keyboardKeys.length; c++) {
            s.keyboardKeys[c].text().text(s.numericKeys[c]);
        }
    }

    public void setTyped(String typed) {
        this.typed = typed != null ? typed : "";
        refreshAutoComplete();
    }

    public void showKeyboard(Entity<?> screen, UIElement<?> window, float paddingY, boolean isWindowMoved, boolean showSelector, boolean disableManualEntry, boolean isFirstLetterUppercase, KeyboardInput input, String typed, String confirmButton) {
        if(window != null) {
            cancelBg.viewport(viewport);
            cancelBg.attach(0);
        }
        else
            cancelBg.detach();

        queuedAttachment = null;
        isFromAttachment = false;

        this.isFirstLetterUppercase = isFirstLetterUppercase;

        this.isManualEntryDisabled = disableManualEntry;

        this.screen = screen;
        if(screen != null)
            attach(screen);
        else
            attach(Globals.grid);

        // Confirm button
        s.keyboardConfirm.text().text(confirmButton);
        movedWindow = window;
        movedWindowPaddingY = paddingY;
        this.isWindowMoved = isWindowMoved;
        this.input = input;
        this.typed = typed != null ? typed : "";
        if(isFirstLetterUppercase && (typed == null || typed.length() == 0))
            showUppercase();
        else
            showLowercase();
        shouldStartUppercase = false;

        if(!s.viewGroup.isAttached()) {
            s.openSound.play();
        }

        // Update selectors
        selectorViews.clear();
        s.selectorSurface.detachChilds();
        float selectorSurfaceY = (+s.selectorSurface.getLength() / 2f) - s.selectorSurface.paddingTop();
        boolean hasAttachment = false;          // Find if there is any attachments here
        isWildcard = false;            // Find if there are any wildcard options
        boolean isNumericWildcard = false;            // Find if there are any wildcard options
        for(String text : autoComplete) {
            if(hiddenAutoComplete.contains(text, true))
                continue;
            // Check if its attachment
            if(text.startsWith(Globals.KEYBOARD_WILDCARD)) {
                isWildcard = true;
                isNumericWildcard = text.equals(Globals.KEYBOARD_NUMERIC_WILDCARD);
                // No need to continue, reset
                s.selectorSurface.detachChilds();
                selectorViews.clear();
                hasAttachment = false;
                showSelector = false;
                isManualEntryDisabled = false;          // manual entry required
                s.wordGroup.detach();
                break;
            }
            if(text.startsWith(Globals.PHOTOROLL_PREFIX)) {
                // Is an attachment, extract title
                String[] splits = text.split(Globals.ATTACHMENT_TITLE_TOKEN, 2);
                if(splits.length == 2) {
                    // Add attachment row here
                    if(!hasAttachment) {
                        hasAttachment = true;
                        String attachmentTitle = splits[1].trim();
                        s.selectorAttachmentRow.metrics.anchorWindowY = selectorSurfaceY / s.selectorSurface.getLength();
                        s.selectorAttachmentRow.viewport(s.selectorSurface).text(attachmentTitle).refresh().attach();
                        selectorSurfaceY += (-s.selectorAttachmentRow.getLength() * s.selectorAttachmentRow.metrics.scaleY) - s.selectorRowIntervalY;
                    }
                    // Else ignore, only use the title of the first message
                }
                else
                    Sys.error(TAG, "Malformed media autocomplete: " + text);

                continue;
            }
            // Else normal message, add new selector row
            PatchedTextBox row = s.selectorRow.instantiate()
                    .viewport(s.selectorSurface)
                    .text(text)
                    .refresh()
                    .attach();
            row.text(text).refresh();
            row.metrics.anchor(0, selectorSurfaceY / s.selectorSurface.getLength());
            selectorSurfaceY += (-row.getLength() * row.metrics.scaleY) - s.selectorRowIntervalY;
            selectorViews.add(row);
        }

        if(!hasAttachment)
            s.selectorAttachmentRow.detach();           // no attachments, remove

        if(!isManualEntryDisabled && hiddenAutoComplete.size > 0) {
            s.selectorKeyboardRow.metrics.anchorWindowY = selectorSurfaceY / s.selectorSurface.getLength();
            s.selectorKeyboardRow.viewport(s.selectorSurface).attach();
        }
        else
            s.selectorKeyboardRow.detach();


        attach(getEntityParent());
        refreshAutoComplete();
        cancelBg.queueHide = false;
        s.viewGroup.attach();

        if(autoComplete.size > hiddenAutoComplete.size && showSelector) {
            // Auto show selector
            s.selectorView.attach();
            s.viewGroup.length(s.selectorView.getLength());
            s.selectorSurface.move(0, -1000);
            s.selectorTitle.attach();
            s.wordSurface.detach();
            s.keyboardView.detach();
            s.wordGroup.detach();
            isKeyboardShowing = false;
            isSelectorsShowing = true;
        }
        else {
            // Else all hidden, need manual input
            s.selectorView.detach();
            s.selectorTitle.detach();
            s.wordSurface.attach();
            s.keyboardView.attach();
            s.viewGroup.length(s.keyboardView.getLength());
            showAutoComplete();
            isKeyboardShowing = true;
            isSelectorsShowing = false;
            if(isNumericWildcard)
                showNumeric();
        }
    }

    public boolean isShowing() {
        return s.viewGroup.isAttached();
    }

    public void hide() {
        hide(true);
    }

    public void hide(boolean playAcceptSound) {
        if(playAcceptSound && s.viewGroup.isAttached())
            s.acceptSound.play();
        s.wordGroup.detachWithAnim();
        s.viewGroup.detachWithAnim();
        s.hintView.detachWithAnim();
        cancelBg.detach();
        isKeyboardShowing = false;
        isSelectorsShowing = false;
    }

    public void hideNow() {
        s.wordGroup.detach();
        s.viewGroup.detach();
        s.hintView.detach();
        cancelBg.detach();
        isKeyboardShowing = false;
        isSelectorsShowing = false;
        if(isWindowMoved && movedWindow != null && movedWindow.metrics != null)
            movedWindow.metrics.y = 0;
    }


    public void setInternal(Internal internal) {
        if(s != null) {
            s.viewGroup.detach();
        }

        s = internal;

        s.viewGroup.viewport(viewport);

        cancelBg.detach();

        // Build cache char lists
        StringBuilder sb = new StringBuilder();
        // Keyboard
        for(String ch : s.uppercaseKeys)
            sb.append(ch);
        for(String ch : s.lowercaseKeys)
            sb.append(ch);
        for(String ch : s.numericKeys)
            sb.append(ch);
        // Use this for chars list
        charsList = sb.toString();
        sb.setLength(0);

        // Refresh
        if(isKeyboardShowing) {
            if(movedWindow != null) {
                cancelBg.viewport(movedWindow);
                cancelBg.attach();
            }
            s.viewGroup.attach();
            s.keyboardView.attach();
            s.viewGroup.length(s.keyboardView.getLength());
            s.selectorView.detach();
            s.selectorTitle.detach();
            s.wordSurface.attach();

            showUppercase();
        }

        excludedViews = new Entity<?>[] {
                s.selectorView
        };
    }

    public void hintAutoCorrectViews() {
        if(words.size == 0)
            return;

        for(int c = 0; c < autoCompleteViews.size; c++)
            autoCompleteViews.items[c].windowAnimation(s.wordHintAnim.startAndReset(), true, false);
        if(typed.isEmpty())
            hint("Start typing");
        else
            hint("Continue typing");

    }

    public void hint(String hint) {
        s.hintTextView.text().text(hint);
        s.hintView.attach();
    }

    private void clearAutoCorrect() {
        for(PatchedTextBox textBox : autoCompleteViews)
            textBox.detach();
        autoCompleteViews.clear();
    }

    private void addAutoCorrect(String word) {
        float wordX = -0.5f;
        PatchedTextBox last = autoCompleteViews.size > 0 ? autoCompleteViews.peek() : null;
        if(last != null)
            wordX = last.metrics.anchorWindowX + last.metrics.scaleX;
        PatchedTextBox textBox = s.wordTemplate.instantiate().text(word);
        textBox.metrics(new UIElement.Metrics().anchor(wordX, 0).pan(+0.5f, 0));
        textBox.minSize(0f, s.wordSurface.getLength());
        textBox.refresh();
        textBox.attach();
        autoCompleteViews.add(textBox);
    }

    private void showAutoComplete() {
        if(autoCompleteViews.size == 0 || isWildcard)
            return;     // nothing to show or using wildcards
        s.wordGroup.attach();
    }

    private void hideAutoComplete() {
        s.wordGroup.detachWithAnim();
    }

    private void justifyAutoCorrect() {
        if (autoCompleteViews.size > 0)
            showAutoComplete();
        else
            hideAutoComplete();
        s.wordSurface.move(+1000, 0);
        float totalSize = 0f;
        for(int c = 0; c < autoCompleteViews.size; c++) {
            PatchedTextBox textBox = autoCompleteViews.items[c];
            totalSize += textBox.metrics.scaleX;
        }
        if(totalSize >= 1f)
            return;         // already filling up the width
        // Need to scale them up
        float scale = 1f / totalSize;
        PatchedTextBox lastTextBox = null;
        for(int c = 0; c < autoCompleteViews.size; c++) {
            PatchedTextBox textBox = autoCompleteViews.items[c];
            textBox.minSize((textBox.metrics.scaleX * scale) - textBox.horizontalPaddingSize(), s.wordSurface.getLength());
            if(lastTextBox == null)
                textBox.metrics.clear().anchorLeft();
            else
                textBox.metrics.clear().anchor(lastTextBox.metrics.anchorWindowX + lastTextBox.metrics.scaleX, 0).pan(+0.5f, 0);
            textBox.refresh();
            lastTextBox = textBox;
        }
    }

    public Keyboard() {

        // Cancel bg
        cancelBg = new CancelClickable();
        cancelBg.length(Globals.LENGTH);
        cancelBg.passThroughInput(true);

        // Create keyboard
        interfaceSource = new Builder<Object>(GBKeyboard.class, this);
        interfaceSource.build();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();

        // Check if from attachment window
        if(isFromAttachment) {
            if(queuedAttachment != null) {
                // Attachment was queued, submit
                submitTyped(queuedAttachment);
                queuedAttachment = null;
            }
            else {
                // Show back keyboard as nothing was selected
                showKeyboard(screen, movedWindow, movedWindowPaddingY, isWindowMoved, true, isManualEntryDisabled, isFirstLetterUppercase, input, null, s.keyboardConfirm.text().text);
            }
            isFromAttachment = false;
        }

    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        // Cache fonts
        if(charsList != null)
            s.keyboardKeys[0].text().font.prepare(charsList);

        super.render(grid, r, renderTime);
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        if(movedWindow != null) {
            if(s.viewGroup.isAttached()) {
                if(isWindowMoved) {
                    if (movedWindow.metrics == null)
                        movedWindow.metrics = new UIElement.Metrics();
                    movedWindow.metrics.y = 0;
                    Rectangle bounds = s.viewGroup.bounds(false, true, true, true, excludedViews);
                    float keyboardTop = bounds.y + bounds.height;
                    bounds = movedWindow.bounds(true, true, true, false, excludedViews);
                    float windowBottom = bounds.y + movedWindowPaddingY;
                    float overlapY = keyboardTop - windowBottom;
                    if (overlapY > 0f)
                        movedWindow.metrics.y += overlapY / movedWindow.getScaleY();
                }
            }
            else {
                if(isWindowMoved) {
                    movedWindow.metrics.y = 0;
                }
                screen = null;
                movedWindow = null;
                isWindowMoved = false;
                input.keyboardClosed();
                input = null;
                detach();
            }
        }
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();

        // Make sure to detach if inactive
        hideNow();
        queuedAttachment = null;
    }

    @Override
    public void onClick(Grid v, UIElement<?> button, int b) {

        if(isSelectorsShowing) {
            // using selector
            // Check if using keyboard
            if(button == s.selectorKeyboardRow) {
                s.backSound.play(s.keySoundVolume);
                showKeyboard(screen, movedWindow, movedWindowPaddingY, isWindowMoved, false, isManualEntryDisabled, isFirstLetterUppercase, input, typed, s.keyboardConfirm.text().text);
                return;
            }
            // Attachment
            if(button == s.selectorAttachmentRow) {
                // Open gallery screen for selection
                isFromAttachment = true;
                v.photoRollApp.albumsScreen.show(this);
                v.photoRollApp.albumsScreen.open(screen, v.screensGroup);
                return;

            }
            for(int c = 0; c < selectorViews.size; c++) {
                PatchedTextBox row = selectorViews.items[c];
                if(row == button) {
                    // Clicked on this row
                    submitTyped(row.text());
                    return;
                }
            }
            return;
        }

        // States
        if(button == s.keyboardShift) {
            s.keySound.play(s.keySoundVolume);
            if(state == KeyboardState.Lowercase)
                showUppercase();
            else
                showLowercase();
            return;
        }
        if(button == s.keyboardNumeric) {
            s.keySound.play(s.keySoundVolume);
            if(state == KeyboardState.Numeric) {
                if(isFirstLetterUppercase && shouldStartUppercase)
                    showUppercase();
                else
                    showLowercase();
            }
            else
                showNumeric();
            return;
        }


        // Erase
        if(button == s.keyboardEraseButton) {
            backspace();
            s.backSound.play(s.keySoundVolume);

            if(typed.length() == 0) {
                if(state == KeyboardState.Numeric)
                    shouldStartUppercase = true;
                else if(isFirstLetterUppercase)
                    showUppercase();
                else
                    showLowercase();
            }

            refreshAutoComplete();
            input.keyboardTyped(typed, typedMatch);

            return;
        }

        // Confirm
        if(button == s.keyboardConfirm) {
            s.enterSound.play(s.keySoundVolume);
            input.keyboardPressedConfirm();
            return;
        }

        // Autocomplete views
        for(int c = 0; c < autoCompleteViews.size; c++) {
            PatchedTextBox view = autoCompleteViews.items[c];
            if(view == button) {
                s.keySound.play(s.keySoundVolume);
                autoCompleteLastWord(view.text() + " ");
                refreshAutoComplete();
                input.keyboardTyped(typed, typedMatch);
                if(state != KeyboardState.Numeric)
                    showLowercase();
                return;
            }
        }

        if(button == s.keyboardSpace) {
            s.spaceSound.play(s.keySoundVolume);
            if(state == KeyboardState.Numeric) {
                if(isFirstLetterUppercase && shouldStartUppercase)
                    showUppercase();
                else
                    showLowercase();
            }
            autoCompleteLastWord(null);
            typed += " ";
            refreshAutoComplete();
            input.keyboardTyped(typed, typedMatch);
            // Vibrate
//            Gdx.input.vibrate(20);
            return;
        }


        // Keys
        for(int c = 0; c < s.keyboardKeys.length; c++) {
            // Key
            Clickable key = s.keyboardKeys[c];
            if(button == key) {
                s.keySound.play(s.keySoundVolume);
                // Only append allowed text
                if(!isWildcard && allowedAutoComplete.size == 0 && autoComplete.size > 0) {
                    // Indicate that can send
                    s.keyboardConfirm.windowAnimation(s.hintAnim.startAndReset(), true, false);
                    return;
                }
                // Lookup this key
                String ch = key.text().text;
                typed += ch;
                refreshAutoComplete();
                input.keyboardTyped(typed, typedMatch);
                if(state != KeyboardState.Numeric)
                    showLowercase();
                else if(ch.contentEquals("."))
                    shouldStartUppercase = true;
                // Vibrate
//                Gdx.input.vibrate(20);
                return;
            }
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(!isShowing())
            return false;
        if(inputType == INPUT_KEY_UP && (key == Input.Keys.BACK || key == Input.Keys.ESCAPE)) {
            s.cancelSound.play();
            hide(false);
            return true;
        }
        if((inputType & INPUT_KEY_TYPED) == 0)
            return false;
        if(character == ' ') {
            s.keyboardSpace.simulateClick();
            return true;
        }
        if(character == 8) {
            s.keyboardEraseButton.simulateClick();
            return true;
        }
        if(character == 13 || character == 10) {
            s.keyboardConfirm.simulateClick();
            return true;
        }
        // Find in alphabets
        int index = -1;
        String ch = Character.toString(character);
        for(int c = 0; c < s.lowercaseKeys.length; c++) {
            String k = s.lowercaseKeys[c];
            if(k.equalsIgnoreCase(ch)) {
                index = c;
                break;
            }
        }
        if(index != -1) {
            if((isFirstLetterUppercase && (typed == null || typed.length() == 0)) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))
                showUppercase();
            else
                showLowercase();
        }
        else {
            // Find in numeric
            for(int c = 0; c < s.numericKeys.length; c++) {
                String k = s.numericKeys[c];
                if(k.equalsIgnoreCase(ch)) {
                    index = c;
                    break;
                }
            }
            if(index != -1)
                showNumeric();
        }

        if(index != -1) {
            Clickable keyButton = s.keyboardKeys[index];
            keyButton.simulateClick();
            return true;
        }

        return false;
    }
}

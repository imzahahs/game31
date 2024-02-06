package game31.app.chats;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Keyboard;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.VoiceProfile;
import game31.app.browser.BrowserScreen;
import game31.app.gallery.PhotoRollApp;
import game31.gb.chats.GBWhatsupMessageThread;
import game31.renderer.SaraRenderer;
import sengine.File;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.ColorAnim;
import sengine.audio.Audio;
import sengine.calc.SetRandomizedSelector;
import sengine.graphics2d.Font;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.InputField;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnPressed;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 29/6/2016.
 */
public class WhatsupThreadScreen extends Menu<Grid> implements OnClick<Grid>, OnPressed<Grid>, Keyboard.KeyboardInput {
    public static final String TAG = "WhatsupThreadScreen";

    public static float MIN_TRIGGER_SUBSEQUENT_TIME = 5f;
    public static float TRIGGER_INTERVAL = 0.25f;


    private static class Trigger {
        final UIElement view;
        final String trigger;
        final float tTriggerTime;

        float tTriggerScheduled = -1;
        boolean hasTriggered = false;


        private Trigger(UIElement view, String trigger, float tTriggerTime) {
            this.view = view;
            this.trigger = trigger;
            this.tTriggerTime = tTriggerTime;
        }
    }

    public static class Internal {
        public UIElement<?> window;
        public ScrollableSurface surface;
        public ScreenBar bars;

        public float smoothScrollSpeed;

        // Appbar profile and online indicator
        public StaticSprite profileView;
        public StaticSprite onlineIndicatorView;
        public Mesh onlineIndicatorMesh;
        public Mesh offlineIndicatorMesh;
        public float tOnlineNextThreshold;

        public String statusTypingTitleFormat;
        public String statusSelfTypingTitle;
        public String statusOnline;
        public String statusOffline;
        public Animation offlineIndicatorAnim;


        // Chatbar
//        public Animation chatActiveIndicator;
        public Clickable chatButton;
        public InputField chatField;
//        public Clickable sendButton;
        public UIElement.Group sendActiveView;
        public Clickable sendActiveButton;
        public Clickable sendInactiveButton;
//        public Animation sendButtonActiveAnim;
        public float keyboardPaddingY;

        // New message feedback
        public PatchedTextBox senderTypingView;

        // New message indicator row
        public UIElement newMessagesRow;
        public float newMessageYInterval;
        public float newMessageCenterYOffset;

        // Sender message
        public PatchedTextBox senderMessageBox;
        public TextBox senderMessageTimeBox;
        public float senderMessageYInterval;
        public Font[] senderCustomFonts;
        public String[] senderCustomFontNames;
        public String senderFirstVisual;

        // Corrupted message
        public String corruptedSenderVisual;
        public String corruptedUserVisual;
        public Animation corruptedAnim;
        public Clickable corruptedFixButton;
        public UIElement.Metrics corruptedFixButtonSenderMetrics;
        public UIElement.Metrics corruptedFixButtonUserMetrics;
        public float corruptedFixInputPaddingX;
        public float corruptedFixInputPaddingY;
        public float tCorruptedTextInterval;

        // Group message
        public PatchedTextBox groupMessageBox;
        public TextBox groupMessageNameView;
        public TextBox groupMessageTimeBox;
        public float groupMessageYInterval;
        public int[] groupMemberFontColors;

        // Sender photo view
        public PatchedTextBox senderPhotoBox;
        public TextBox senderPhotoTimeBox;
        public Clickable senderPhotoView;
        public PatchedTextBox senderPhotoActionView;
        public float senderPhotoYInterval;

        // Photo corruption
        public StaticSprite corruptedImageView;

        // Sender video view
        public PatchedTextBox senderVideoBox;
        public TextBox senderVideoTimeBox;
        public TextBox senderVideoDurationView;
        public Clickable senderVideoView;
        public PatchedTextBox senderVideoActionView;
        public float senderVideoYInterval;

        // Sender audio view
        public PatchedTextBox senderAudioBox;
        public TextBox senderAudioTimeBox;
        public Clickable senderAudioPlayButton;
        public float senderAudioYInterval;

        public Sprite audioPlaySprite;
        public Sprite audioStopSprite;
        public Animation audioLevelAnim;


        // Sender link view
        public PatchedTextBox senderLinkBox;
        public TextBox senderLinkTimeBox;
        public StaticSprite senderLinkThumbnailView;
        public TextBox senderLinkUrlView;
        public PatchedTextBox senderLinkActionView;
        public float senderLinkYInterval;

        public UIElement<?> senderInviteGroup;
        public StaticSprite senderInviteImageView;
        public TextBox senderInviteTextView;
        public Clickable senderInviteAcceptButton;
        public float senderInviteYInterval;


        // User message
        public PatchedTextBox userMessageBox;
        public TextBox userMessageTimeBox;
        public float userMessageYInterval;
        public Font[] userCustomFonts;
        public String[] userCustomFontNames;
        public String userNormalVisual;

        // User photo view
        public PatchedTextBox userPhotoBox;
        public TextBox userPhotoTimeBox;
        public Clickable userPhotoView;
        public PatchedTextBox userPhotoActionView;
        public float userPhotoYInterval;

        // User video view
        public PatchedTextBox userVideoBox;
        public TextBox userVideoTimeBox;
        public TextBox userVideoDurationView;
        public Clickable userVideoView;
        public PatchedTextBox userVideoActionView;
        public float userVideoYInterval;

        // User audio view
        public PatchedTextBox userAudioBox;
        public TextBox userAudioTimeBox;
        public Clickable userAudioPlayButton;
        public float userAudioYInterval;

        // Dates
        public PatchedTextBox dateTextBox;
        public float dateYInterval;

        // Sound
        public Audio.Sound messageReceivedSound;

        public Sprite textEffectGenerator;
        public Sprite textEffectCompositor;
        public float tTextEffectTimeout;
    }


    private class CorruptedMessage {
        final PatchedTextBox messageView;
        final String corruption;
        final String[] texts;
        final String actualText;
        final Clickable fixButton;

        float tSwitchScheduled = -1;
        int textIndex = 0;

        CorruptedMessage(String corruption, PatchedTextBox box, boolean isSender) {
            this.messageView = box;
            this.corruption = corruption;
            this.actualText = box.text();
            this.texts = new String[Globals.corruptedMessageDuplicates];

            // Visual
            box.windowAnimation(s.corruptedAnim.loopAndReset(), true, true);

            String[] words = actualText.split("\\s+");
            SetRandomizedSelector<String> selector = new SetRandomizedSelector<>(words);

            StringBuilder sb = new StringBuilder();

            int widestIndex = -1;
            float widestBox = -1;

            for(int c = 0; c < texts.length; c++) {
                sb.setLength(0);
                selector.reset();

                // Jumble up texts
                for(int i = 0; i < words.length; i++) {
                    if(i > 0)
                        sb.append(' ');
                    sb.append(selector.select());
                }

                texts[c] = sb.toString();

                // Check if this text makes it larger
                box.text(texts[c]);
                box.refresh();
                if(box.metrics.scaleX > widestBox) {
                    widestBox = box.metrics.scaleX;
                    widestIndex = c;
                }
            }

            // Set widest box initially
            box.text(texts[widestIndex]);
            box.refresh();

            // Fix button
            fixButton = s.corruptedFixButton.instantiate().viewport(box).attach();
            if(isSender)
                fixButton.metrics(s.corruptedFixButtonSenderMetrics.instantiate());
            else
                fixButton.metrics(s.corruptedFixButtonUserMetrics.instantiate());
            float boxHeight = box.getLength() * box.metrics.scaleY;
            float buttonHeight = fixButton.getLength() * fixButton.metrics.scaleY;
            float padding = ((boxHeight - buttonHeight) / 2f) / buttonHeight;
            if(padding < s.corruptedFixInputPaddingY)
                padding = s.corruptedFixInputPaddingY;
            fixButton.inputPadding(s.corruptedFixInputPaddingX, padding, s.corruptedFixInputPaddingX, padding);
        }
    }

    private final WhatsupApp app;

    // Source
    private final Builder<Object> builder;
    private Internal s;

    // Working
    WhatsupContact contact;
    WhatsupContact lastContact = null;
    private boolean isClosing = false;
    private float messageY;
    private int currentReadMessages = 0;
    int currentMessages = 0;
    private boolean isSmoothScrolling = false;
    private final Array<String> memberNames = new Array<>(String.class);
    private final Array<String> memberActualNames = new Array<>(String.class);
    private String memberNameList = null;
    private int secondsToNextMessage = -1;
    private PatchedTextBox lastSenderBox;
    private PatchedTextBox lastUserBox;

    // Typed
    private String typed = "";

    private Array<String> messageDates = new Array<>(String.class);

    // Media buttons, need to maintain separate lists so that we can have that zoom transition animation
    private final ObjectMap<UIElement, UIElement> actionButtons = new ObjectMap<>();
    private final ObjectMap<PatchedTextBox, BrowserScreen.PageDescriptor> linkButtons = new ObjectMap<>();
    private final ObjectMap<Clickable, String> inviteButtons = new ObjectMap<>();
    private final Array<Clickable> mediaButtons = new Array<>(Clickable.class);
    private final Array<Media> medias = new Array<>(Media.class);

    // Audio
    private StaticSprite audioPlayIconView;
    private StaticSprite audioLevelView;
    private HorizontalProgressBar audioProgressBar;
    private Music audioTrack = null;
    private float tPlayingTime = 0;
    private float tAudioPlayingTime = 0;
    private VoiceProfile audioInfo = null;

    // Trigger tracking
    private final Array<Trigger> triggers = new Array<>(Trigger.class);
    private float tTriggerScheduled = -1;

    // Corrupted messags
    private final Array<CorruptedMessage> corruptedMessages = new Array<>(CorruptedMessage.class);

    private float tTextEffectEndScheduled = -1;

    // Specific effects
    public StaticSprite profileView() {
        return s.profileView;
    }


    public WhatsupContact contact() {
        if(contact == null)
            return lastContact;
        return contact;
    }

    private void stopAudio() {
        if(audioTrack == null)
            return;
        Globals.grid.notification.stopSubtitle(audioInfo.filename);
        audioTrack.dispose();
        audioTrack = null;
        audioInfo = null;
        audioPlayIconView.visual(s.audioPlaySprite);
        audioPlayIconView = null;
        audioLevelView.windowAnim.setProgress(0);           // reset
        audioLevelView = null;
        audioProgressBar.progress(0);
        audioProgressBar = null;

        tPlayingTime = 0;
        tAudioPlayingTime = 0;
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        // Activate ui
        s.window.viewport(viewport).attach();

        clear();
    }

    public void clear() {
        stopAudio();

        messageDates.clear();
        s.surface.detachChilds();
        messageY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        mediaButtons.clear();
        medias.clear();
        actionButtons.clear();
        linkButtons.clear();
        inviteButtons.clear();
        triggers.clear();
        memberNames.clear();
        memberActualNames.clear();
        memberNameList = null;


        s.chatField.text(null);

        corruptedMessages.clear();

        isSmoothScrolling = false;

        secondsToNextMessage = -1;
    }

    public void clearAll() {
        clear();
        contact = null;
        lastContact = null;
    }

    public void open(WhatsupContact contact) {
        // Clear
        clear();
        this.contact = contact;
        lastContact = null;
        isClosing = false;

        // Analytics
        Game.analyticsView(contact.tree.namespace, Globals.ANALYTICS_CONTENT_TYPE_CHATS);

        // Profile
        s.profileView.visual(Sprite.load(contact.profilePicFilename));

        // Status
        resetStatus();

        // Reset keyboard
        Globals.grid.keyboard.clearTyped();
        Globals.grid.keyboard.resetAutoComplete();
        // Refresh contact
        currentReadMessages = contact.readMessages;
        currentMessages = 0;

        app.refreshContact(contact);

        // Seek to unread messages if attached
        s.surface.stop();
        s.surface.refresh();
        isSmoothScrolling = false;
        if(s.newMessagesRow.isAttached()) {
            s.surface.moveTo(s.newMessagesRow);
            s.surface.move(0, s.newMessageCenterYOffset);
            isSmoothScrolling = false;
        }
        else if(currentReadMessages == 0)
            s.surface.move(0, -1000);             // never read any message from this thread, default from top
        else {
            // Move to the bottom
            s.surface.move(0, +1000);
        }

        // Always read as is current thread
        currentReadMessages = Integer.MAX_VALUE;

        // Reset text effect
        tTextEffectEndScheduled = -1;
    }

    public void close() {
        Globals.grid.keyboard.hideNow();
        isClosing = true;
    }


    public void resetStatus() {
        secondsToNextMessage = -1;

        boolean isUserMessagesAvailable = contact.tree.isUserMessagesAvailable();
        boolean isGoingToSendMessage = false;

        float tTimeToNextMessage = contact.getNextMessageScheduled();
        if(tTimeToNextMessage != -1) {
            tTimeToNextMessage -= app.getRenderTime();
            isGoingToSendMessage = tTimeToNextMessage < s.tOnlineNextThreshold;
        }

        // Title bar
        if(isUserMessagesAvailable || isGoingToSendMessage) {
            String subtitle = s.statusOnline;
            if(memberNameList != null)
                subtitle += " - " + memberNameList;
            s.bars.showAppbar(contact.name, subtitle);

            s.onlineIndicatorView.visual(s.onlineIndicatorMesh);
        }
        else {
            String subtitle = s.statusOffline;
            if(memberNameList != null)
                subtitle += " - " + memberNameList;
            s.bars.showAppbar(contact.name, subtitle);

            s.onlineIndicatorView.visual(s.offlineIndicatorMesh);
        }

        // User input
        if(isUserMessagesAvailable) {
//            s.chatField.text("");         // TODO: no need to show input cursor
            s.sendActiveView.attach();
        }
        else {
            s.sendActiveView.detachWithAnim();
            s.chatField.text(null);
        }
    }

    public void informTyping(String origin) {
        secondsToNextMessage = -1;
        if(origin.equals(WhatsupContact.ORIGIN_USER))
            s.bars.showAppbar(contact.name, s.statusSelfTypingTitle);
        else {
            String person;
            if(memberNames.size > 0) {
                if(origin.equals(WhatsupContact.ORIGIN_SENDER))
                    person = memberActualNames.items[0];        // No name was mentioned, by default use the first member name
                else {
                    // Else name was mentioned, try to resolve
                    int index = memberNames.indexOf(origin, false);
                    if(index != -1)
                        person = memberActualNames.items[index];
                    else
                        person = origin;        // Can't find the actual name
                }
            }
            else if(origin.equals(WhatsupContact.ORIGIN_SENDER))
                person = contact.name;           // contact name
            else
                person = origin;                // unrecognized, just use this name

            // Split and use
            person = person.split(" ", 2)[0];       // get the first word
            s.bars.showAppbar(contact.name, String.format(Locale.US, s.statusTypingTitleFormat, person));

            s.senderTypingView.metrics.anchorWindowY = messageY / s.surface.getLength();
            s.senderTypingView.attach();

            if(isAttached() && s.surface.spaceBottom() <= Globals.surfaceSnapDistance) {
                s.surface.refresh();
                isSmoothScrolling = true;
            }
            else
                s.surface.move(0, +1000);
        }
        s.chatButton.windowAnim = null;
        s.chatField.text(null);
    }

    public void refreshAvailableUserMessages() {
        if(contact == null)
            return;
        Keyboard keyboard = Globals.grid.keyboard;

        // Reset typed
        typed = "";
        s.chatField.text(typed);
        keyboard.clearTyped();

        // Refresh status
        resetStatus();

        contact.tree.refreshAvailableUserMessages(Globals.grid.keyboard);

        if(keyboard.isShowing()) {
            if(!contact.tree.isUserMessagesAvailable())
                keyboard.hide(!contact.isTimedReply(app));
            else                // Else got more user messages
                keyboard.showKeyboard(this, s.window, s.keyboardPaddingY, true, true, false, true, this, s.chatField.text(), "send");
        }
    }

    public void addDateView(String date) {
        PatchedTextBox box = s.dateTextBox.instantiate()
                .viewport(s.surface)
                .text(date)
                .refresh()
                .attach();
        box.metrics.anchor(0, messageY / s.surface.getLength());

        messageY += (-box.getLength() * box.metrics.scaleY) + s.dateYInterval;
    }

    private void updateUnreadMessagesRow() {
        if(currentReadMessages != 0 && currentMessages >= currentReadMessages) {
            // Add unread row
            s.newMessagesRow.viewport(s.surface).attach();
            s.newMessagesRow.metrics.anchorWindowY = messageY / s.surface.getLength();
            messageY += (-s.newMessagesRow.getLength() * s.newMessagesRow.metrics.scaleY) + s.newMessageYInterval;

            currentReadMessages = Integer.MAX_VALUE;        // already attached
        }

        // Added a message
        currentMessages++;
    }


    public void addSenderMessage(String message, String time, String date, String sender, String trigger, float tTriggerTime) {
        // Update unread messages row if applicable
        updateUnreadMessagesRow();

        // Remove typing
        s.senderTypingView.detach();

        // Sound
        if(!app.isContactsRefreshing()) {
            if(contact.customMessageSound != null) {
                contact.customMessageSound.play();
                Globals.grid.idleScare.reschedule();        // Reschedule if using custom sound
            }
            else
                s.messageReceivedSound.play();
        }

        if (message.startsWith(Globals.SECTION_PREFIX)) {
            String section = message.substring(Globals.SECTION_PREFIX.length());
            if (section.startsWith(Globals.GROUP_ADD_PREFIX)) {
                // Add to group
                int index = memberNames.indexOf(sender, false);
                if (index == -1) {
                    // Adding new member
                    String[] data = sender.split(Globals.CHATS_SPLIT_TOKEN, 2);
                    String name;
                    if(data.length == 2) {
                        name = data[1];
                        memberNames.add(data[0]);
                        memberActualNames.add(name);
                    }
                    else {
                        name = sender;
                        memberNames.add(sender);
                        memberActualNames.add(sender);
                    }
                    if (memberNameList == null)
                        memberNameList = name;
                    else
                        memberNameList = name + ", " + memberNameList;        // Show the latest member first
                }
                section = section.substring(Globals.GROUP_ADD_PREFIX.length());
            } else if (section.startsWith(Globals.GROUP_REMOVE_PREFIX)) {
                int index = memberNames.indexOf(sender, false);
                if(index != -1) {
                    memberNames.items[index] = null;
                    memberActualNames.items[index] = null;
                    // Rebuild name list
                    memberNameList = null;
                    for (int c = 0; c < memberNames.size; c++) {
                        String name = memberActualNames.items[c];
                        if(name != null) {
                            if (memberNameList == null)
                                memberNameList = name;
                            else
                                memberNameList = name + ", " + memberNameList;        // Show the latest member first
                        }
                    }
                }
                section = section.substring(Globals.GROUP_REMOVE_PREFIX.length());
            }
            // Else arbitrary section

            addDateView(section);

            lastSenderBox = null;       // Break visual continuity
        }
        else {
            // Add date
            if(messageDates.size == 0 || !messageDates.peek().contentEquals(date))
                addDateView(date);      // first date or last date does not equal
            messageDates.add(date);

            boolean isCorrupted = false;

            // Reset user visual continuity
            lastUserBox = null;

            if (message.startsWith(Globals.CORRUPTED_PREFIX)) {
                // Message is corrupted, extract actual message
                String[] data = message.substring(Globals.CORRUPTED_PREFIX.length()).split("=", 3);
                String path = data[0].trim();
                String text = data[1].trim();
                String actual;
                if (data.length == 3) {
                    actual = data[2].trim();
                    if (actual.isEmpty())
                        actual = text;
                } else
                    actual = text;

                boolean isSolved = Globals.grid.state.get(path, false);

                if (!isSolved) {
                    PatchedTextBox box = s.senderMessageBox.instantiate()
                            .viewport(s.surface)
                            .visual(s.corruptedSenderVisual)
                            .text(text);

                    CorruptedMessage corrupted = new CorruptedMessage(path, box, true);
                    corruptedMessages.add(corrupted);

                    box.attach();

                    box.metrics.anchor(0, messageY / s.surface.getLength());

                    TextBox timeBox = s.senderMessageTimeBox.instantiate()
                            .viewport(box)
                            .autoLengthText(time)
                            .attach();
                    timeBox.metrics.scaleX /= box.metrics.scaleX;
                    timeBox.metrics.scaleY /= box.metrics.scaleX;

                    // Trigger
                    if (trigger != null && !trigger.isEmpty())
                        triggers.add(new Trigger(box, trigger, tTriggerTime));

                    messageY += (-box.getLength() * box.metrics.scaleY) + s.senderMessageYInterval;

                    isCorrupted = true;

                    lastSenderBox = null;       // Break visual continuity
                } else
                    message = actual;

            }

            if (!isCorrupted) {
                if (message.startsWith(Globals.INVITE_PREFIX)) {
                    // Invite
                    String[] data = message.substring(Globals.INVITE_PREFIX.length()).split(Globals.CHATS_SPLIT_TOKEN, 4);
                    if (data.length != 4)
                        throw new RuntimeException("Malformed invite message: " + message);

                    // Populate
                    UIElement<?> box = s.senderInviteGroup.instantiate()
                            .viewport(s.surface)
                            .attach();
                    box.metrics.anchor(0, messageY / s.surface.getLength());

                    // Image
                    Sprite sprite = Sprite.load(data[0]);
                    if (sprite.length != s.senderInviteImageView.getLength()) {
                        sprite = new Sprite(sprite.length, sprite.getMaterial());
                        sprite.crop(s.senderInviteImageView.getLength());
                    }
                    box.find(s.senderInviteImageView).visual(sprite);

                    // Button text
                    Clickable inviteButton = box.find(s.senderInviteAcceptButton).text(data[1]);

                    // Text
                    box.find(s.senderInviteTextView).text(data[2]);

                    // Remember buttons to respond for click events
                    inviteButtons.put(inviteButton, data[3]);

                    // Trigger
                    if (trigger != null && !trigger.isEmpty())
                        triggers.add(new Trigger(box, trigger, tTriggerTime));

                    messageY += (-box.getLength() * box.metrics.scaleY) + s.senderInviteYInterval;

                    lastSenderBox = null;       // Break visual continuity
                } else if (message.startsWith(Globals.BROWSER_PREFIX)) {
                    // Its a link
                    String path = message.substring(Globals.BROWSER_PREFIX.length());

                    // Get page
                    BrowserScreen.PageDescriptor page = Globals.grid.browserApp.getPage(path);
                    if (page == null)
                        throw new RuntimeException("Unable to find page \"" + path + "\"");

                    PatchedTextBox box = s.senderLinkBox.instantiate()
                            .viewport(s.surface)
                            .text(page.title + " - " + page.name)
                            .refresh()
                            .attach();
                    box.metrics.anchor(0, messageY / s.surface.getLength());


                    // Remember buttons to respond for click events
                    PatchedTextBox actionView = box.find(s.senderLinkActionView);
                    actionButtons.put(actionView, box);
                    linkButtons.put(box, page);

                    // Populate fields
                    if (page.preview != null) {
                        // Crop preview if needed
                        StaticSprite thumbnailView = box.find(s.senderLinkThumbnailView);
                        float length = thumbnailView.getLength();
                        Sprite preview = page.preview;
                        if (preview.length != length) {
                            preview = new Sprite(preview.length, preview.getMaterial());
                            preview.crop(length);
                        }
                        thumbnailView.visual(preview);
                    }
                    box.find(s.senderLinkUrlView).text(page.url);

                    TextBox timeBox = s.senderLinkTimeBox.instantiate()
                            .viewport(box)
                            .autoLengthText(time)
                            .attach();
                    timeBox.metrics.scaleX /= box.metrics.scaleX;
                    timeBox.metrics.scaleY /= box.metrics.scaleX;

                    // Trigger
                    if (trigger != null && !trigger.isEmpty())
                        triggers.add(new Trigger(box, trigger, tTriggerTime));

                    messageY += (-box.getLength() * box.metrics.scaleY) + s.senderLinkYInterval;

                    if (lastSenderBox == null)
                        box.visual(s.senderFirstVisual);
                    lastSenderBox = box;
                } else if (message.startsWith(Globals.PHOTOROLL_PREFIX)) {
                    String path = message.substring(Globals.PHOTOROLL_PREFIX.length());

                    // Unlock and get this media
                    PhotoRollApp photoroll = Globals.grid.photoRollApp;
                    Media media = photoroll.unlock(path, false);

                    if (media.isVideo()) {
                        PatchedTextBox box = s.senderVideoBox.instantiate()
                                .viewport(s.surface)
                                .refresh()
                                .attach();
                        box.metrics.anchor(0, messageY / s.surface.getLength());

                        Clickable photoView = box.find(s.senderVideoView);
                        PatchedTextBox actionView = box.find(s.senderVideoActionView);

                        // Remember buttons to respond for click events
                        actionButtons.put(actionView, photoView);
                        mediaButtons.add(photoView);
                        medias.add(media);

                        // Duration
                        int seconds = (int) media.video.duration;
                        int minutes = seconds / 60;
                        seconds %= 60;
                        box.find(s.senderVideoDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));

                        TextBox timeBox = s.senderVideoTimeBox.instantiate()
                                .viewport(box)
                                .autoLengthText(time)
                                .attach();
                        timeBox.metrics.scaleX /= box.metrics.scaleX;
                        timeBox.metrics.scaleY /= box.metrics.scaleX;

                        // Trigger
                        if (trigger != null && !trigger.isEmpty())
                            triggers.add(new Trigger(box, trigger, tTriggerTime));

                        messageY += (-box.getLength() * box.metrics.scaleY) + s.senderVideoYInterval;

                        if (lastSenderBox == null)
                            box.visual(s.senderFirstVisual);
                        lastSenderBox = box;
                    } else if (media.isAudio()) {
                        PatchedTextBox box = s.senderAudioBox.instantiate()
                                .viewport(s.surface)
                                .refresh()
                                .attach();
                        box.metrics.anchor(0, messageY / s.surface.getLength());

                        Clickable button = box.find(s.senderAudioPlayButton);

                        // Reset audio anim
                        StaticSprite audioLevelView = box.find("levels");
                        audioLevelView.windowAnimation(s.audioLevelAnim.startAndReset(), false, true);
                        audioLevelView.windowAnim.setProgress(0);

                        // Remember buttons to respond for click events
                        mediaButtons.add(button);
                        medias.add(media);

                        TextBox timeBox = s.senderAudioTimeBox.instantiate()
                                .viewport(box)
                                .autoLengthText(time)
                                .attach();
                        timeBox.metrics.scaleX /= box.metrics.scaleX;
                        timeBox.metrics.scaleY /= box.metrics.scaleX;

                        // Trigger
                        if (trigger != null && !trigger.isEmpty())
                            triggers.add(new Trigger(box, trigger, tTriggerTime));

                        messageY += (-box.getLength() * box.metrics.scaleY) + s.senderAudioYInterval;

                        if (lastSenderBox == null)
                            box.visual(s.senderFirstVisual);
                        lastSenderBox = box;
                    } else {
                        PatchedTextBox box = s.senderPhotoBox.instantiate()
                                .viewport(s.surface)
                                .refresh()
                                .attach();
                        box.metrics.anchor(0, messageY / s.surface.getLength());

                        Clickable photoView = box.find(s.senderPhotoView);
                        PatchedTextBox actionView = box.find(s.senderPhotoActionView);

                        // Check if corrupted
                        if (media.corruption != null) {
                            boolean isSolved = Globals.grid.state.get(media.corruption, false);
                            if (!isSolved)
                                s.corruptedImageView.instantiate().viewport(photoView).attach();
                        }

                        // Remember buttons to respond for click events
                        actionButtons.put(actionView, photoView);
                        mediaButtons.add(photoView);
                        medias.add(media);

                        TextBox timeBox = s.senderPhotoTimeBox.instantiate()
                                .viewport(box)
                                .autoLengthText(time)
                                .attach();
                        timeBox.metrics.scaleX /= box.metrics.scaleX;
                        timeBox.metrics.scaleY /= box.metrics.scaleX;

                        // Trigger
                        if (trigger != null && !trigger.isEmpty())
                            triggers.add(new Trigger(box, trigger, tTriggerTime));

                        messageY += (-box.getLength() * box.metrics.scaleY) + s.senderPhotoYInterval;

                        if (lastSenderBox == null)
                            box.visual(s.senderFirstVisual);
                        lastSenderBox = box;
                    }
                } else {
                    if (!sender.equals(WhatsupContact.ORIGIN_SENDER)) {
                        // Its a group chat, color and append name to message
                        int index = memberNames.indexOf(sender, false);
                        String name;
                        if (index == -1) {
                            index = 0;      // UB, shouldnt happen
                            name = sender;  // unknown actual name
                            Sys.error(TAG, "Member not registered \"" + sender + "\"");
                        } else
                            name = memberActualNames.items[index];
                        // Determine color
                        index %= s.groupMemberFontColors.length;

                        // Use sender message
                        PatchedTextBox box = s.groupMessageBox.instantiate()
                                .viewport(s.surface);

                        // Custom fonts
                        String actualMessage;
                        if (message.startsWith(Globals.CHATS_FONT_PREFIX)) {
                            String[] format = message.split(Globals.CHATS_SPLIT_TOKEN, 2);
                            actualMessage = format[1];

                            // Find this custom font
                            String fontName = format[0].substring(Globals.CHATS_FONT_PREFIX.length());
                            for (int c = 0; c < s.senderCustomFontNames.length; c++) {
                                if (s.senderCustomFontNames[c].equals(fontName)) {
                                    box.font(s.senderCustomFonts[c]);
                                    box.fontTarget(SaraRenderer.TARGET_INTERACTIVE_TEXT_EFFECT);
                                    break;
                                }
                            }
                        } else
                            actualMessage = message;

                        box.text(actualMessage)
                                .refresh()
                                .attach();
                        box.metrics.anchor(0, messageY / s.surface.getLength());

                        TextBox nameView = box.find(s.groupMessageNameView);
                        nameView.text().text(name);
                        nameView.windowAnimation(new ColorAnim(s.groupMemberFontColors[index], false).startAndReset(), true, true);     // color the name box

                        TextBox timeBox = s.groupMessageTimeBox.instantiate()
                                .viewport(box)
                                .autoLengthText(time)
                                .attach();
                        timeBox.metrics.scaleX /= box.metrics.scaleX;
                        timeBox.metrics.scaleY /= box.metrics.scaleX;

                        // Trigger
                        if (trigger != null && !trigger.isEmpty())
                            triggers.add(new Trigger(box, trigger, tTriggerTime));

                        messageY += (-box.getLength() * box.metrics.scaleY) + s.groupMessageYInterval;

                        if (lastSenderBox == null)
                            box.visual(s.senderFirstVisual);
                        lastSenderBox = box;
                    } else {

                        PatchedTextBox box = s.senderMessageBox.instantiate()
                                .viewport(s.surface);

                        // Custom fonts
                        String actualMessage;
                        if (message.startsWith(Globals.CHATS_FONT_PREFIX)) {
                            String[] format = message.split(Globals.CHATS_SPLIT_TOKEN, 2);
                            actualMessage = format[1];

                            // Find this custom font
                            String fontName = format[0].substring(Globals.CHATS_FONT_PREFIX.length());
                            for (int c = 0; c < s.senderCustomFontNames.length; c++) {
                                if (s.senderCustomFontNames[c].equals(fontName)) {
                                    box.font(s.senderCustomFonts[c]);
                                    box.fontTarget(SaraRenderer.TARGET_INTERACTIVE_TEXT_EFFECT);
                                    break;
                                }
                            }
                        } else
                            actualMessage = message;

                        box.text(actualMessage)
                                .refresh()
                                .attach();

                        box.metrics.anchor(0, messageY / s.surface.getLength());

                        TextBox timeBox = s.senderMessageTimeBox.instantiate()
                                .viewport(box)
                                .autoLengthText(time)
                                .attach();
                        timeBox.metrics.scaleX /= box.metrics.scaleX;
                        timeBox.metrics.scaleY /= box.metrics.scaleX;

                        // Trigger
                        if (trigger != null && !trigger.isEmpty())
                            triggers.add(new Trigger(box, trigger, tTriggerTime));

                        messageY += (-box.getLength() * box.metrics.scaleY) + s.senderMessageYInterval;

                        if (lastSenderBox == null)
                            box.visual(s.senderFirstVisual);
                        lastSenderBox = box;
                    }
                }
            }
        }

        if(isAttached() && s.surface.spaceBottom() <= Globals.surfaceSnapDistance) {
            s.surface.refresh();
            isSmoothScrolling = true;
        }
        else
            s.surface.move(0, +1000);

        // Reset status
        resetStatus();
    }


    public void addUserMessage(String message, String time, String date, String trigger, float tTriggerTime) {
        // Update unread messages row if applicable
        updateUnreadMessagesRow();

        // Remove typing
        s.senderTypingView.detach();

        // Add date
        if(messageDates.size == 0 || !messageDates.peek().contentEquals(date))
            addDateView(date);      // first date or last date does not equal
        messageDates.add(date);

        // Reset sender visual continuity
        lastSenderBox = null;

        if(message.startsWith(Globals.PHOTOROLL_PREFIX)) {
            String path = message.substring(Globals.PHOTOROLL_PREFIX.length());

            // Unlock and get this media
            Media media = Globals.grid.photoRollApp.find(path);

            if(media.isVideo()) {
                PatchedTextBox box = s.userVideoBox.instantiate()
                        .viewport(s.surface)
                        .refresh()
                        .attach();
                box.metrics.anchor(0, messageY / s.surface.getLength());

                Clickable photoView = box.find(s.userVideoView);
                PatchedTextBox actionView = box.find(s.userVideoActionView);

                // Remember buttons to respond for click events
                actionButtons.put(actionView, photoView);
                mediaButtons.add(photoView);
                medias.add(media);

                // Duration
                int seconds = (int)media.video.duration;
                int minutes = seconds / 60;
                seconds %= 60;
                box.find(s.userVideoDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));

                TextBox timeBox = s.userVideoTimeBox.instantiate()
                        .viewport(box)
                        .autoLengthText(time)
                        .attach();
                timeBox.metrics.scaleX /= box.metrics.scaleX;
                timeBox.metrics.scaleY /= box.metrics.scaleX;

                // Trigger
                if(trigger != null && !trigger.isEmpty())
                    triggers.add(new Trigger(box, trigger, tTriggerTime));

                messageY += (-box.getLength() * box.metrics.scaleY) + s.userVideoYInterval;
                
                if(lastUserBox != null)
                    lastUserBox.visual(s.userNormalVisual);
                lastUserBox = box;
            }
            else if(media.isAudio()) {
                PatchedTextBox box = s.userAudioBox.instantiate()
                        .viewport(s.surface)
                        .refresh()
                        .attach();
                box.metrics.anchor(0, messageY / s.surface.getLength());

                Clickable button = box.find(s.userAudioPlayButton);

                // Reset audio anim
                StaticSprite audioLevelView = box.find("levels");
                audioLevelView.windowAnimation(s.audioLevelAnim.startAndReset(), false, true);
                audioLevelView.windowAnim.setProgress(0);

                // Remember buttons to respond for click events
                mediaButtons.add(button);
                medias.add(media);


                TextBox timeBox = s.userAudioTimeBox.instantiate()
                        .viewport(box)
                        .autoLengthText(time)
                        .attach();
                timeBox.metrics.scaleX /= box.metrics.scaleX;
                timeBox.metrics.scaleY /= box.metrics.scaleX;

                // Trigger
                if(trigger != null && !trigger.isEmpty())
                    triggers.add(new Trigger(box, trigger, tTriggerTime));

                messageY += (-box.getLength() * box.metrics.scaleY) + s.userAudioYInterval;

                if(lastUserBox != null)
                    lastUserBox.visual(s.userNormalVisual);
                lastUserBox = box;
            }
            else {
                PatchedTextBox box = s.userPhotoBox.instantiate()
                        .viewport(s.surface)
                        .refresh()
                        .attach();
                box.metrics.anchor(0, messageY / s.surface.getLength());

                Clickable photoView = box.find(s.userPhotoView);
                PatchedTextBox actionView = box.find(s.userPhotoActionView);

                // Check if corrupted
                if(media.corruption != null) {
                    boolean isSolved = Globals.grid.state.get(media.corruption, false);
                    if(!isSolved)
                        s.corruptedImageView.instantiate().viewport(photoView).attach();
                }

                // Remember buttons to respond for click events
                actionButtons.put(actionView, photoView);
                mediaButtons.add(photoView);
                medias.add(media);

                TextBox timeBox = s.userPhotoTimeBox.instantiate()
                        .viewport(box)
                        .autoLengthText(time)
                        .attach();
                timeBox.metrics.scaleX /= box.metrics.scaleX;
                timeBox.metrics.scaleY /= box.metrics.scaleX;

                // Trigger
                if(trigger != null && !trigger.isEmpty())
                    triggers.add(new Trigger(box, trigger, tTriggerTime));

                messageY += (-box.getLength() * box.metrics.scaleY) + s.userPhotoYInterval;

                if(lastUserBox != null)
                    lastUserBox.visual(s.userNormalVisual);
                lastUserBox = box;
            }
        }
        else {

            PatchedTextBox box;

            if(message.startsWith(Globals.CORRUPTED_PREFIX)) {
                // Message is corrupted, extract actual message
                String[] data = message.substring(Globals.CORRUPTED_PREFIX.length()).split("=", 2);
                String path = data[0].trim();
                String actual = data[1].trim();

                boolean isSolved = Globals.grid.state.get(path, false);

                if(!isSolved) {
                    box = s.userMessageBox.instantiate()
                            .viewport(s.surface)
                            .visual(s.corruptedUserVisual)
                            .font(s.senderMessageBox.font())
                            .text(actual)
                    ;

                    CorruptedMessage corrupted = new CorruptedMessage(path, box, false);
                    corruptedMessages.add(corrupted);

                    box.attach();
                }
                else {
                    // TODO: support custom fonts here or remove corrupted messages
                    box = s.userMessageBox.instantiate()
                            .viewport(s.surface)
                            .text(actual)
                            .refresh()
                            .attach();
                }
            }
            else {
                box = s.userMessageBox.instantiate()
                        .viewport(s.surface)
                        ;

                // Custom fonts
                String actualMessage;
                if(message.startsWith(Globals.CHATS_FONT_PREFIX)) {
                    String[] format = message.split(Globals.CHATS_SPLIT_TOKEN, 2);
                    actualMessage = format[1];

                    // Find this custom font
                    String fontName = format[0].substring(Globals.CHATS_FONT_PREFIX.length());
                    for(int c = 0; c < s.userCustomFontNames.length; c++) {
                        if(s.userCustomFontNames[c].equals(fontName)) {
                            box.font(s.userCustomFonts[c]);
                            box.fontTarget(SaraRenderer.TARGET_INTERACTIVE_TEXT_EFFECT);
                            break;
                        }
                    }
                }
                else
                    actualMessage = message;

                box.text(actualMessage)
                        .refresh()
                        .attach();
            }

            box.metrics.anchor(0, messageY / s.surface.getLength());

            TextBox timeBox = s.userMessageTimeBox.instantiate()
                    .viewport(box)
                    .autoLengthText(time)
                    .attach();
            timeBox.metrics.scaleX /= box.metrics.scaleX;
            timeBox.metrics.scaleY /= box.metrics.scaleX;

            // Trigger
            if(trigger != null && !trigger.isEmpty())
                triggers.add(new Trigger(box, trigger, tTriggerTime));

            messageY += (-box.getLength() * box.metrics.scaleY) + s.userMessageYInterval;

            if(lastUserBox != null)
                lastUserBox.visual(s.userNormalVisual);
            lastUserBox = box;
        }

        if(isAttached() && s.surface.spaceBottom() <= Globals.surfaceSnapDistance) {
            s.surface.refresh();
            isSmoothScrolling = true;
        }
        else
            s.surface.move(0, +1000);
    }



    public WhatsupThreadScreen(WhatsupApp app) {
        this.app = app;

        builder = new Builder<Object>(GBWhatsupMessageThread.class, this);
        builder.build();
    }

    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        builder.start();

        if(lastContact != null) {
            float surfaceY = s.surface.movedY();
            open(lastContact);
            if(!s.newMessagesRow.isAttached()) {
                s.surface.stop();
                s.surface.move(0, -s.surface.movedY() + surfaceY);
            }
        }
        else
            v.keyboard.detach();

        typed = "";
        s.chatField.text(typed);

        tTriggerScheduled = -1;

        tTextEffectEndScheduled = -1;

        // Allow idle scare
        v.idleScare.reschedule();
    }



    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Update media button renders
        for(int c = 0; c < mediaButtons.size; c++) {
            Clickable button = mediaButtons.items[c];
            Media media = medias.items[c];
            if(media.isAudio())
                continue;       // nothing to load if audio
            // If detected to be rendering, load best square (either full square or thumb square), or reset to thumb square if not rendering
            Sprite bestSprite = button.isEffectivelyRendering() ? media.loadBestSquare() : media.thumbnailSquare;
            if (button.buttonDown() != bestSprite)
                button.visuals(bestSprite);
        }

        // Update audio progress
        if(audioTrack != null) {
            Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
            if(!audioTrack.isPlaying()) {
                stopAudio();
                // Allow idle scares again
                grid.idleScare.reschedule();
            }
            else {
                float currentAudioPosition = audioTrack.getPosition();
                if (currentAudioPosition != tAudioPlayingTime) {
                    if(tPlayingTime < currentAudioPosition) {
                        tPlayingTime = currentAudioPosition;
                        tAudioPlayingTime = currentAudioPosition;
                    }
                } else if(tAudioPlayingTime != 0) {
                    // Manually calculate audio position, since there is no movement (on some devices, audio position has a very low resolution)
                    tPlayingTime += Gdx.graphics.getRawDeltaTime();
                }
                // Levels
                audioLevelView.windowAnim.setProgress(audioInfo.sample(tPlayingTime));
                // Subtitles
                Globals.grid.notification.updateSubtitles(tPlayingTime, false);

                // Update progress
                float progress = tPlayingTime / audioInfo.duration;
                if (progress > 1f)
                    progress = 1f;
                // Progress
                audioProgressBar.progress(progress);
                // Stop idle scares
                grid.idleScare.stop();
            }
        }

        // Update corrupted renders
        for(int c = 0; c < corruptedMessages.size; c++) {
            CorruptedMessage corrupted = corruptedMessages.items[c];

            if(renderTime > corrupted.tSwitchScheduled) {
                corrupted.textIndex++;
                corrupted.textIndex %= corrupted.texts.length;
                corrupted.messageView.text(corrupted.texts[corrupted.textIndex], false);
                corrupted.tSwitchScheduled = renderTime + s.tCorruptedTextInterval;
            }
        }

//        // Debug
//        float tTypingScheduled = contact.getTypingScheduled();
//        if(tTypingScheduled == Float.MAX_VALUE)
//            tTypingScheduled = 0;
//        int seconds = (int)(tTypingScheduled - app.getRenderTime());
//        if(seconds < 1)
//            seconds = 0;
//        if(seconds != secondsToNextMessage) {
//            secondsToNextMessage = seconds;
//            String title;
//            if(seconds > 0)
//                title = contact.name + " (" + seconds + ")";
//            else
//                title = contact.name;
//            appbar.mainTitleView().text().text(title);
//        }
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);

        if(isSmoothScrolling) {
            if(s.surface.spaceBottom() <= 0)
                isSmoothScrolling = false;
            else
                s.surface.move(0, +s.smoothScrollSpeed * getRenderDeltaTime());
        }

        // Update all triggers
        if(renderTime > tTriggerScheduled && renderTime > 0) {
            // Schedule next
            tTriggerScheduled = renderTime + TRIGGER_INTERVAL;

            // Update
            for (int c = 0; c < triggers.size; c++) {
                Trigger trigger = triggers.items[c];
                // Check if the views are showing
                if (trigger.tTriggerScheduled == -1) {
                    // Not scheduled, check if this view is visible
                    if (trigger.view.isRenderingEnabled()) {
                        // Schedule
                        float delay = trigger.tTriggerTime;
                        if (trigger.hasTriggered && delay < MIN_TRIGGER_SUBSEQUENT_TIME)
                            delay = MIN_TRIGGER_SUBSEQUENT_TIME;
                        trigger.tTriggerScheduled = renderTime + delay;
                    }
                }
                if (trigger.tTriggerScheduled != -1 && renderTime >= trigger.tTriggerScheduled) {
                    // Trigger
                    v.eval(contact.name, trigger.trigger);
                    // Mark as done
                    trigger.hasTriggered = true;
                    trigger.tTriggerScheduled = -1;
                }
            }
        }

        // Render text effect if needed
        if(SaraRenderer.renderer.targets[SaraRenderer.TARGET_INTERACTIVE_TEXT_EFFECT].size > 0)
            tTextEffectEndScheduled = renderTime + s.tTextEffectTimeout;
        if(renderTime < tTextEffectEndScheduled) {
            Matrix4 m = Matrices.model;

            Matrices.push();
            Matrices.camera = v.compositor.camera;
            Matrices.target = SaraRenderer.TARGET_TEXT_EFFECT_GENERATOR;

            m.translate(0.5f, +Globals.LENGTH / 2f, 0);
            m.scale(1, -1, 1);

            s.textEffectGenerator.render();

            Matrices.target = SaraRenderer.TARGET_INTERACTIVE_TEXT;
            s.textEffectCompositor.render();

            Matrices.pop();
        }
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        // Update read messages
        contact.readMessages = currentMessages;

        // Stop audio anyway
        stopAudio();

        // Clear
        if(isClosing) {
            clear();
            contact = null;
        }
        else {
            lastContact = contact;
            contact = null;
        }
    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.surface) {
            isSmoothScrolling = false;
        }
    }


    @Override
    public void onClick(Grid v, UIElement<?> view, int b) {
        if(contact == null)
            return;         // TODO: find out why this becomes null

        if(view == s.bars.backButton()) {
            if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                return;
            // Stop idle scares
            v.idleScare.stop();
            // Going back to contacts
            close();
            ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, app.contactsScreen, v.screensGroup);
            transition.attach(v.screensGroup);
            return;
        }

        if(view == s.bars.homeButton()) {
            if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                return;

            // Stop idle scares
            v.idleScare.stop();

            Globals.grid.keyboard.hideNow();
            isClosing = true;

            v.homescreen.transitionBack(this, v);
            return;
        }

        if(view == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(view == s.chatButton) {
            if(contact.tree.isUserMessagesAvailable()) {
                s.surface.move(0, +1000);
                v.keyboard.showKeyboard(this, s.window, s.keyboardPaddingY, true, true, false, true, this, s.chatField.text(), "send");
            } else {
                s.bars.subtitleView().windowAnimation(s.offlineIndicatorAnim.startAndReset(), true, false);
            }
            return;
        }

        if(view == s.sendActiveButton || view == s.sendInactiveButton) {
            if(!contact.tree.isUserMessagesAvailable()) {
                // User is offline
                s.bars.subtitleView().windowAnimation(s.offlineIndicatorAnim.startAndReset(), true, false);
                return;
            }
            // Try to send a reply to this contact
            if(typed.isEmpty())
                v.keyboard.showKeyboard(this, s.window, s.keyboardPaddingY, true, true, false, true, this, s.chatField.text(), "send");
            else if(contact.reply(app, typed)) {
                // Reschedule idle scare
                v.idleScare.reschedule();
            }
            else {
                // Dialogue didnt accept reply, refresh available messages
                refreshAvailableUserMessages();
            }
            return;
        }

        if(v.keyboard.isShowing())
            return;     // no need to respond if keyboard showing

        // Corrupted buttons
        for(int c = 0; c < corruptedMessages.size; c++) {
            CorruptedMessage corrupted = corruptedMessages.items[c];
            if(view == corrupted.fixButton) {
                if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                    return;
                // Stop idle scares
                v.idleScare.stop();
                // Open corrupted view
                v.restorePhraseApp.show(corrupted.corruption);
                v.restorePhraseApp.open(this);
                return;
            }
        }

        // Action buttons
        UIElement actualView = actionButtons.get(view);
        if(actualView != null)
            view = actualView;

        // Media
        if(view instanceof Clickable) {
            Clickable button = (Clickable)view;
            int index = mediaButtons.indexOf(button, true);

            if(index != -1) {
                // Media button
                Media media = medias.items[index];
                v.photoRollApp.unlock(media.album + "/" + media.name, true);        // Unlock as opened
                MediaAlbum album = v.photoRollApp.findAlbum(media.album);
                if (media.isAudio()) {
                    // Clicked on a audio, play audio
                    if(media.audioInfo == audioInfo)
                        stopAudio();            // Currently playing this file, just stop
                    else {
                        stopAudio();
                        audioInfo = media.audioInfo;
                        audioPlayIconView = button.find("icon");
                        audioLevelView = button.viewport().find("levels");
                        audioPlayIconView.visual(s.audioStopSprite);
                        audioProgressBar = button.find("progressbar");
                        audioTrack = Gdx.audio.newMusic(File.open(media.filename));
                        Globals.grid.notification.startSubtitle(media.filename);
                        audioTrack.play();
                    }
                }
                else if (media.isVideo()) {
                    if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                        return;
                    if (v.trigger(Globals.TRIGGER_OPEN_VIDEO_FROM_MESSAGES)) {
                        // Stop idle scares
                        v.idleScare.stop();
                        // Clicked on a video, open
                        v.photoRollApp.videoScreen.show(album, album.indexOf(media), null, true);
                        v.photoRollApp.videoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                    }
                } else {
                    if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                        return;
                    // Clicked on a photo
                    // Stop idle scares
                    v.idleScare.stop();
                    // Check for corruption
                    if(media.corruption != null) {
                        boolean isSolved = Globals.grid.state.get(media.corruption, false);
                        if(!isSolved) {
                            // Not yet solved, corruption view
                            v.restoreImageApp.show(media.corruption);
                            v.restoreImageApp.open(this);
                            return;
                        }
                    }
                    // Else open
                    v.photoRollApp.photoScreen.show(album, album.indexOf(media), null);
                    v.photoRollApp.photoScreen.open(this, v.screensGroup, view.getX(), view.getY(), view.getWidth());
                }
                return;
            }

            String inviteAction = inviteButtons.get(button);
            if(inviteAction != null) {
                // Invite button
                v.eval(contact.name, inviteAction);
                return;
            }

            return;
        }

        // Links
        if(view instanceof PatchedTextBox) {
            PatchedTextBox button = (PatchedTextBox) view;
            BrowserScreen.PageDescriptor page = linkButtons.get(button);
            if(page != null) {
                if(!v.trigger(Globals.TRIGGER_LEAVE_CHAT_THREAD_SCREEN))
                    return;
                // Stop idle scares
                v.idleScare.stop();
                // Find thumbnail for transition
                StaticSprite thumbnailView = button.find(s.senderLinkThumbnailView);
                // Open page
                v.browserApp.clearPageTab();
                v.browserApp.showPage(page);
                v.browserApp.open(this, v.screensGroup, thumbnailView.getX(), thumbnailView.getY(), thumbnailView.getWidth());
                return;
            }
        }

    }

    @Override
    public void keyboardTyped(String text, int matchOffset) {
        typed = text;
        s.chatField.text(typed);
    }


    @Override
    public void keyboardClosed() {
        // nothing
    }

    @Override
    public void keyboardPressedConfirm() {
        if(s.sendActiveView.isAttached())
            onClick(Globals.grid, s.sendActiveButton, Input.Buttons.LEFT);
        else
            onClick(Globals.grid, s.sendInactiveButton, Input.Buttons.LEFT);        // I think its not necessary ?
    }
}

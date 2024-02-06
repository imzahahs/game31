package game31.app.mail;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Locale;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.Media;
import game31.MediaAlbum;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.app.gallery.PhotoRollApp;
import game31.gb.mail.GBMailThreadScreen;
import sengine.Entity;
import sengine.calc.SetRandomizedSelector;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.PatchedTextBox;
import sengine.ui.ScrollableSurface;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 17/8/2016.
 */
public class MailThreadScreen extends Menu<Grid> implements OnClick<Grid> {
    static final String TAG = "MailThreadScreen";

    public static float MIN_TRIGGER_SUBSEQUENT_TIME = 5f;
    public static float TRIGGER_INTERVAL = 0.25f;

    public interface InterfaceSource {
        String buildThreadTimeString(String date, String time);
        String buildReplyInfoString(String name, String email, String date, String time);
    }



    private class CorruptedMessage {
        final PatchedTextBox messageView;
        final String corruption;
        final String[] texts;
        final String actualText;
        final Clickable fixButton;

        float tSwitchScheduled = -1;
        int textIndex = 0;

        CorruptedMessage(String corruption, PatchedTextBox box) {
            this.messageView = box;
            this.corruption = corruption;
            this.actualText = box.text();
            this.texts = new String[Globals.corruptedMessageDuplicates];

            // Visual
//            box.windowAnimation(s.corruptedAnim.loopAndReset(), true, true); TODO

            String[] words = actualText.split("\\s+");
            SetRandomizedSelector<String> selector = new SetRandomizedSelector<String>(words);

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
            float boxHeight = box.getLength() * box.metrics.scaleY;
            float buttonHeight = fixButton.getLength() * fixButton.metrics.scaleY;
            float padding = ((boxHeight - buttonHeight) / 2f) / buttonHeight;
            if(padding < s.corruptedFixInputPaddingY)
                padding = s.corruptedFixInputPaddingY;
            fixButton.inputPadding(s.corruptedFixInputPaddingX, padding, s.corruptedFixInputPaddingX, padding);
        }
    }

    private static class MessageStack {
        final String origin;
        final String message;
        final String time;
        final String date;
        final String trigger;
        final float tTriggerTime;

        private MessageStack(String origin, String message, String time, String date, String trigger, float tTriggerTime) {
            this.origin = origin;
            this.message = message;
            this.time = time;
            this.date = date;
            this.trigger = trigger;
            this.tTriggerTime = tTriggerTime;
        }
    }

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
        // Reply group
        public TextBox latestMessageView;
        public TextBox replyInfoView;
        public TextBox replyMessageView;
        public StaticSprite replyIndicatorView;

        public PatchedTextBox latestCorruptedView;
        public PatchedTextBox replyCorruptedView;
        public Clickable corruptedFixButton;
        public float corruptedFixInputPaddingX;
        public float corruptedFixInputPaddingY;
        public float tCorruptedTextInterval;

        public float messageIntervalY;
        public float replyInfoViewIntervalY;
        public float blockIntervalY;

        public PatchedTextBox latestActionButton;
        public PatchedTextBox replyActionButton;

        public Clickable replyImageView;
        public PatchedTextBox replyImageActionView;

        public Clickable replyVideoView;
        public PatchedTextBox replyVideoActionView;
        public TextBox replyVideoDurationView;

        public StaticSprite replyGraphicView;

        public Clickable latestImageView;
        public PatchedTextBox latestImageActionView;

        public Clickable latestVideoView;
        public PatchedTextBox latestVideoActionView;
        public TextBox latestVideoDurationView;

        public StaticSprite latestGraphicView;

        // Header group
        public UIElement.Group headerGroup;
        public TextBox headerFromView;
        public String headerFromLabel;
        public TextBox headerToView;
        public String headerToLabel;
        public TextBox headerSubjectView;
        public String headerSubjectLabel;
        public TextBox headerTimeView;

        // Window
        public UIElement<?> window;
        public ScrollableSurface surface;
        public ScreenBar bars;
        public Clickable replyButton;

    }

    // App
    private final MailApp app;

    // Interface source
    private final Builder<InterfaceSource> interfaceSource;
    private Internal s;

    // Current
    MailConversation conversation;
    MailConversation lastConversation;
    private boolean isClosing = false;
    private float surfaceY;
    private final Array<MessageStack> stack = new Array<MessageStack>(MessageStack.class);

    // Media buttons, need to maintain separate lists so that we can have that zoom transition animation
    private final IdentityMap<UIElement, UIElement> mediaActionButtons = new IdentityMap<UIElement, UIElement>();
    private final IdentityMap<UIElement, String> mediaButtons = new IdentityMap<UIElement, String>();
    private final IdentityMap<UIElement, String> actionButtons = new IdentityMap<UIElement, String>();
    private final Array<Clickable> imageButtons = new Array<Clickable>(Clickable.class);
    private final Array<Media> imageMedia = new Array<Media>(Media.class);

    // Corrupted messags
    private final Array<CorruptedMessage> corruptedMessages = new Array<CorruptedMessage>(CorruptedMessage.class);

    // Trigger tracking
    private final Array<Trigger> triggers = new Array<Trigger>(Trigger.class);
    private float tTriggerScheduled = -1;

    private String contextName;

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void open(Entity<?> transitionFrom, Entity<?> target) {
        ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
        transition.attach(target);
    }

    public void closeToHome() {
        // Stop idle scares
        Globals.grid.idleScare.stop();

        Globals.grid.homescreen.transitionBack(this, Globals.grid);
        conversation = null;        // no need to track anymore
        contextName = null;
        triggers.clear();
    }

    public void refreshNavigationTitle() {
        if(conversation == null)
            return;         // no tracking anything
        int index = app.inboxScreen.rowIndexOf(conversation);
        if(index == -1)
            return;     // unexpected
        int total = app.inboxScreen.totalVisibleRows();

        s.bars.showAppbar(conversation.email, null);
    }


    public void show(MailConversation conversation) {
        clear();

        this.conversation = conversation;
        lastConversation = null;
        isClosing = false;

        // Prepare
        s.headerSubjectView.autoLengthText(s.headerSubjectLabel + conversation.subject);

        refreshNavigationTitle();

        // Populate all
        conversation.refresh(app);

        // Fill up in reverse
        if(stack.size == 0)
            return;     // unexpected, no message
        boolean isLatestMessage = true;
        while(stack.size > 0) {
            MessageStack stackMessage = stack.pop();
            String origin = stackMessage.origin;
            String message = stackMessage.message;
            String time = stackMessage.time;
            String date = stackMessage.date;
            String trigger = stackMessage.trigger;
            float tTriggerTime = stackMessage.tTriggerTime;

            if(isLatestMessage) {
                isLatestMessage = false;
                // Fill up header
                if(origin.equals(Globals.ORIGIN_USER)) {           // TODO: support 3rd person
                    // Latest message is from user
                    s.headerFromView.text().text(s.headerFromLabel + app.loginEmail);
                    s.headerToView.text().text(s.headerToLabel + conversation.email);
                }
                else {
                    // Latest message is from sender
                    s.headerFromView.text().text(s.headerFromLabel + conversation.email);
                    s.headerToView.text().text(s.headerToLabel + app.loginEmail);
                }
                // Time
                s.headerTimeView.autoLengthText(interfaceSource.build().buildThreadTimeString(date, time));

                // Add header to surface
                s.headerGroup.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                s.headerGroup.viewport(s.surface).attach();
                s.headerGroup.autoLength();
                surfaceY -= s.headerGroup.getHeight();
                surfaceY -= s.messageIntervalY;

                // Breakdown messages into text and media blocks
                String[] blocks = message.split(Globals.MAIL_BLOCK_TOKEN);
                UIElement firstView = null;

                // Add latest message
                for(int c = 0; c < blocks.length; c++) {
                    String block = blocks[c];

                    if(block.startsWith(Globals.MAIL_GRAPHIC_PREFIX)) {
                        String path = block.substring(Globals.MAIL_GRAPHIC_PREFIX.length());
                        String[] data = path.split("=", 3);

                        StaticSprite graphicView = s.latestGraphicView.instantiate();
                        UIElement view;

                        if(data.length == 3) {
                            String align = data[0];
                            float size = Float.parseFloat(data[1]);
                            path = data[2];

                            Sprite image = Sprite.load(path);
                            graphicView.visual(image);

                            UIElement.Group container = new UIElement.Group()
                                    .metrics(graphicView.metrics)
                                    .length(image.length * size);

                            graphicView.metrics(new UIElement.Metrics().scale(size));
                            if(align.equalsIgnoreCase("left"))
                                graphicView.metrics.anchorLeft();
                            else if(align.equalsIgnoreCase("right"))
                                graphicView.metrics.anchorRight();

                            graphicView.viewport(container).attach();

                            view = container;
                        }
                        else {
                            Sprite image = Sprite.load(path);
                            graphicView.visual(image);
                            view = graphicView;
                        }

                        view.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                        view.viewport(s.surface).attach();
                        float height = view.getHeight() + s.blockIntervalY;
                        surfaceY -= height;
                    }
                    else if(block.startsWith(Globals.MAIL_ACTION_PREFIX)) {
                        String[] data = block.substring(Globals.MAIL_ACTION_PREFIX.length()).split("/", 2);

                        String title = data[0];
                        String action = data[1];

                        PatchedTextBox box = s.latestActionButton.instantiate();
                        box.text(title).refresh().calculateWindow();

                        box.viewport(s.surface).attach();
                        box.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                        float height = box.getHeight() + s.blockIntervalY;
                        surfaceY -= height;

                        // Remember
                        actionButtons.put(box, action);
                    }
                    else if(block.startsWith(Globals.PHOTOROLL_PREFIX)) {
                        String path = block.substring(MailConversation.PHOTOROLL_PREFIX.length());

                        // Unlock and get this media
                        PhotoRollApp photoroll = Globals.grid.photoRollApp;
                        Media media = photoroll.unlock(path, false);

                        if (media.isVideo()) {
                            Clickable videoView = s.latestVideoView.instantiate();
                            videoView.visuals(media.thumbnailSquare);
                            videoView.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                            // Duration
                            int seconds = (int) media.video.duration;
                            int minutes = seconds / 60;
                            seconds %= 60;
                            videoView.find(s.latestVideoDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));

                            videoView.viewport(s.surface).attach();
                            float height = videoView.getHeight() + s.blockIntervalY;
                            surfaceY -= height;

                            // Remember view for trigger
                            if(firstView == null)
                                firstView = videoView;

                            // Input
                            mediaActionButtons.put(videoView.find(s.latestVideoActionView), videoView);
                            mediaButtons.put(videoView, path);
                        } else {
                            Clickable imageView = s.latestImageView.instantiate();
                            imageView.length(media.croppedFull.length);         // visuals will be set in render pass
                            imageView.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                            // Add to image views
                            imageButtons.add(imageView);
                            imageMedia.add(media);

                            imageView.viewport(s.surface).attach();
                            float height = imageView.getHeight() + s.blockIntervalY;
                            surfaceY -= height;

                            // Remember view for trigger
                            if(firstView == null)
                                firstView = imageView;

                            // Input
                            mediaActionButtons.put(imageView.find(s.latestImageActionView), imageView);
                            mediaButtons.put(imageView, path);
                        }
                    }
                    else {
                        boolean isCorrupted = false;
                        if(block.startsWith(Globals.CORRUPTED_PREFIX)) {
                            // Message is corrupted, extract actual message
                            String[] data = block.substring(Globals.CORRUPTED_PREFIX.length()).split("=", 2);
                            String path = data[0].trim();
                            block = data[1].trim();

                            boolean isSolved = Globals.grid.state.get(path, false);

                            if(!isSolved) {
                                // Show as corrupted
                                PatchedTextBox replyView = s.latestCorruptedView.instantiate()
                                        .viewport(s.surface)
                                        .text(block)
                                        .attach();

                                CorruptedMessage corrupted = new CorruptedMessage(path, replyView);
                                corruptedMessages.add(corrupted);

                                replyView.calculateWindow();
                                replyView.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                                float height = replyView.getHeight() + s.blockIntervalY;
                                surfaceY -= height;

                                // Remember view for trigger
                                if (firstView == null)
                                    firstView = replyView;

                                isCorrupted = true;
                            }
                        }

                        if(!isCorrupted) {
                            String text;
                            int alignment;
                            if(block.startsWith(Globals.MAIL_ALIGN_CENTER_PREFIX)) {
                                text = block.substring(Globals.MAIL_ALIGN_CENTER_PREFIX.length());
                                alignment = Align.center;
                            }
                            else if(block.startsWith(Globals.MAIL_ALIGN_RIGHT_PREFIX)) {
                                text = block.substring(Globals.MAIL_ALIGN_RIGHT_PREFIX.length());
                                alignment = Align.right;
                            }
                            else {
                                // Else normal text
                                text = block;
                                alignment = Align.left;
                            }

                            TextBox replyView = s.latestMessageView.instantiate().viewport(s.surface).attach();
                            replyView.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                            replyView.autoLengthText(text);
                            replyView.text().align(alignment);
                            float height = replyView.getHeight() + s.blockIntervalY;
                            surfaceY -= height;

                            // Remember view for trigger
                            if (firstView == null)
                                firstView = replyView;
                        }

                    }
                }

                // Trigger
                if(trigger != null && !trigger.isEmpty() && firstView != null)
                    triggers.add(new Trigger(firstView, trigger, tTriggerTime));

                surfaceY -= s.messageIntervalY - s.blockIntervalY;           // offset last block interval
            }
            else {
                // Find the person who sent this message
                String name;
                String email;
                if(origin.equals(Globals.ORIGIN_USER)) {
                    name = app.loginName;
                    email = app.loginEmail;
                }
                else if(origin.equals(Globals.ORIGIN_SENDER)){
                    name = conversation.name;
                    email = conversation.email;
                }
                else {
                    String[] data = origin.split(Globals.CHATS_SPLIT_TOKEN, 2);
                    if(data.length == 2) {
                        name = data[0];
                        email = data[1];
                    }
                    else {
                        name = "";
                        email = origin;
                    }
                }

                // Add info view
                TextBox infoView = s.replyInfoView.instantiate().viewport(s.surface).attach();
                infoView.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                infoView.autoLengthText(interfaceSource.build().buildReplyInfoString(name, email, date, time));
                surfaceY -= infoView.getHeight();
                surfaceY -= s.replyInfoViewIntervalY;

                // Keep track of metrics
                float messageHeight = 0;
                float startSurfaceY = surfaceY;

                // Breakdown messages into text and media blocks
                String[] blocks = message.split(Globals.MAIL_BLOCK_TOKEN);
                UIElement firstView = null;
                for(int c = 0; c < blocks.length; c++) {
                    String block = blocks[c];

                    if(block.startsWith(Globals.MAIL_GRAPHIC_PREFIX)) {
                        String path = block.substring(Globals.MAIL_GRAPHIC_PREFIX.length());
                        String[] data = path.split("=", 3);

                        StaticSprite graphicView = s.replyGraphicView.instantiate();
                        UIElement view;

                        if(data.length == 3) {
                            String align = data[0];
                            float size = Float.parseFloat(data[1]);
                            path = data[2];

                            Sprite image = Sprite.load(path);
                            graphicView.visual(image);

                            UIElement.Group container = new UIElement.Group()
                                    .metrics(graphicView.metrics)
                                    .length(image.length * size);

                            graphicView.metrics(new UIElement.Metrics().scale(size));
                            if(align.equalsIgnoreCase("left"))
                                graphicView.metrics.anchorLeft();
                            else if(align.equalsIgnoreCase("right"))
                                graphicView.metrics.anchorRight();

                            graphicView.viewport(container).attach();

                            view = container;
                        }
                        else {
                            Sprite image = Sprite.load(path);
                            graphicView.visual(image);
                            view = graphicView;
                        }

                        view.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                        view.viewport(s.surface).attach();
                        float height = view.getHeight() + s.blockIntervalY;
                        messageHeight += height;
                        surfaceY -= height;
                    }
                    else if(block.startsWith(Globals.MAIL_ACTION_PREFIX)) {
                        String[] data = block.substring(Globals.MAIL_ACTION_PREFIX.length()).split("/", 2);

                        String title = data[0];
                        String action = data[1];

                        PatchedTextBox box = s.replyActionButton.instantiate();
                        box.text(title).refresh().calculateWindow();

                        box.viewport(s.surface).attach();
                        box.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                        float height = box.getHeight() + s.blockIntervalY;
                        messageHeight += height;
                        surfaceY -= height;

                        // Remember
                        actionButtons.put(box, action);
                    }
                    else if(block.startsWith(Globals.PHOTOROLL_PREFIX)) {
                        String path = block.substring(MailConversation.PHOTOROLL_PREFIX.length());

                        // Unlock and get this media
                        PhotoRollApp photoroll = Globals.grid.photoRollApp;
                        Media media = photoroll.unlock(path, false);

                        if (media.isVideo()) {
                            Clickable videoView = s.replyVideoView.instantiate();
                            videoView.visuals(media.thumbnailSquare);
                            videoView.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                            // Duration
                            int seconds = (int) media.video.duration;
                            int minutes = seconds / 60;
                            seconds %= 60;
                            videoView.find(s.replyVideoDurationView).text().text(String.format(Locale.US, "%d:%02d", minutes, seconds));

                            videoView.viewport(s.surface).attach();
                            float height = videoView.getHeight() + s.blockIntervalY;
                            messageHeight += height;
                            surfaceY -= height;

                            // Remember view for trigger
                            if(firstView == null)
                                firstView = videoView;

                            // Input
                            mediaActionButtons.put(videoView.find(s.replyVideoActionView), videoView);
                            mediaButtons.put(videoView, path);
                        } else {
                            Clickable imageView = s.replyImageView.instantiate();
                            imageView.length(media.croppedFull.length);         // visuals will be set in render pass
                            imageView.metrics.anchorWindowY = surfaceY / s.surface.getLength();

                            // Add to image views
                            imageButtons.add(imageView);
                            imageMedia.add(media);

                            imageView.viewport(s.surface).attach();
                            float height = imageView.getHeight() + s.blockIntervalY;
                            messageHeight += height;
                            surfaceY -= height;

                            // Remember view for trigger
                            if(firstView == null)
                                firstView = imageView;

                            // Input
                            mediaActionButtons.put(imageView.find(s.replyImageActionView), imageView);
                            mediaButtons.put(imageView, path);
                        }
                    }
                    else {
                        boolean isCorrupted = false;
                        if(block.startsWith(Globals.CORRUPTED_PREFIX)) {
                            // Message is corrupted, extract actual message
                            String[] data = block.substring(Globals.CORRUPTED_PREFIX.length()).split("=", 2);
                            String path = data[0].trim();
                            block = data[1].trim();

                            boolean isSolved = Globals.grid.state.get(path, false);

                            if(!isSolved) {
                                // Show as corrupted
                                PatchedTextBox replyView = s.replyCorruptedView.instantiate()
                                        .viewport(s.surface)
                                        .text(block)
                                        .attach();

                                CorruptedMessage corrupted = new CorruptedMessage(path, replyView);
                                corruptedMessages.add(corrupted);

                                replyView.calculateWindow();
                                replyView.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                                float height = replyView.getHeight() + s.blockIntervalY;
                                messageHeight += height;
                                surfaceY -= height;

                                // Remember view for trigger
                                if (firstView == null)
                                    firstView = replyView;

                                isCorrupted = true;
                            }
                        }

                        if(!isCorrupted) {
                            String text;
                            int alignment;
                            if(block.startsWith(Globals.MAIL_ALIGN_CENTER_PREFIX)) {
                                text = block.substring(Globals.MAIL_ALIGN_CENTER_PREFIX.length());
                                alignment = Align.center;
                            }
                            else if(block.startsWith(Globals.MAIL_ALIGN_RIGHT_PREFIX)) {
                                text = block.substring(Globals.MAIL_ALIGN_RIGHT_PREFIX.length());
                                alignment = Align.right;
                            }
                            else {
                                // Else normal text
                                text = block;
                                alignment = Align.left;
                            }

                            TextBox replyView = s.replyMessageView.instantiate().viewport(s.surface).attach();
                            replyView.metrics.anchorWindowY = surfaceY / s.surface.getLength();
                            replyView.autoLengthText(text);
                            replyView.text().align(alignment);
                            float height = replyView.getHeight() + s.blockIntervalY;
                            messageHeight += height;
                            surfaceY -= height;

                            // Remember view for trigger
                            if (firstView == null)
                                firstView = replyView;
                        }
                    }
                }

                // Trigger
                if(trigger != null && !trigger.isEmpty() && firstView != null)
                    triggers.add(new Trigger(firstView, trigger, tTriggerTime));

                // Add reply side indicator and adjust height
                StaticSprite indicatorView = s.replyIndicatorView.instantiate().viewport(s.surface).attach();
                indicatorView.metrics.anchorWindowY = startSurfaceY / s.surface.getLength();
                indicatorView.metrics.scaleY = messageHeight - s.blockIntervalY;           // offset last block interval
                surfaceY -= s.messageIntervalY - s.blockIntervalY;           // offset last block interval
            }
        }
        // Done
        stack.clear();

        // Scroll to top
        s.surface.move(0, -1000);
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        // Queue on next message
        Globals.grid.postMessage(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        });

    }

    public void refresh() {
        if(conversation != null)
            show(conversation);
        else
            clear();
    }

    public void clear() {
        surfaceY = (+s.surface.getLength() / 2f) - s.surface.paddingTop();
        s.surface.detachChilds();
        stack.clear();
        mediaActionButtons.clear();
        mediaButtons.clear();
        imageButtons.clear();
        actionButtons.clear();
        imageMedia.clear();
        triggers.clear();

        corruptedMessages.clear();

        contextName = null;
    }

    public void refreshAvailableUserMessages() {        // TODO
        if(conversation == null)
            return;
        conversation.tree.refreshAvailableUserMessages(Globals.grid.keyboard);
    }

    public void addMessage(String origin, String message, String time, String date, String trigger, float tTriggerDelay) {
        stack.add(new MessageStack(
                origin,
                message,
                time,
                date,
                trigger,
                tTriggerDelay
        ));
    }

    public MailThreadScreen(MailApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBMailThreadScreen.class, this);
        interfaceSource.build();
    }



    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        interfaceSource.start();
        tTriggerScheduled = -1;

        if(lastConversation != null) {
            float surfaceY = s.surface.movedY();
            show(lastConversation);
            s.surface.stop();
            s.surface.move(0, -s.surface.movedY() + surfaceY);
        }

        // Allow idle scare
        grid.idleScare.reschedule();

        // Analytics
        Game.analyticsView(conversation.tree.namespace, Globals.ANALYTICS_CONTENT_TYPE_MAIL);
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Update image button renders
        for(int c = 0; c < imageButtons.size; c++) {
            Clickable button = imageButtons.items[c];
            Media media = imageMedia.items[c];
            // If detected to be rendering, load best crop (either full or thumb), or reset to thumb if not rendering
            Sprite bestSprite = button.isEffectivelyRendering() ? media.loadBestCrop() : media.croppedThumb;
            if(button.buttonDown() != bestSprite)
                button.visuals(bestSprite);
        }

        // If there are any new messages, refresh
        if(stack.size > 0)
            show(conversation);


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
    }

    @Override
    protected void renderFinish(Grid v, float r, float renderTime) {
        super.renderFinish(v, r, renderTime);


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
                    v.eval(conversation.subject, trigger.trigger);
                    // Mark as done
                    trigger.hasTriggered = true;
                    trigger.tTriggerScheduled = -1;
                }
            }
        }
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        interfaceSource.stop();

        // Clear
        if(isClosing) {
            clear();
            conversation = null;
        }
        else {
            lastConversation = conversation;
            conversation = null;
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> button, int b) {
        if(button == s.bars.backButton()) {
            if(!v.trigger(Globals.TRIGGER_BACK_TO_INBOX))
                return;
            // Stop idle scares
            v.idleScare.stop();

            ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, app.inboxScreen, v.screensGroup);
            transition.attach(v.screensGroup);
            conversation = null;        // no need to track anymore
            contextName = null;
            triggers.clear();
            isClosing = true;
            return;
        }

        if(button == s.bars.homeButton()) {
            closeToHome();
            isClosing = true;
            return;
        }

        if(button == s.bars.irisButton()) {
            v.notification.openTracker();
            return;
        }

        if(button == s.replyButton) {
            // TODO app.composeScreen.show(conversation);
            // TODO app.composeScreen.open(this, v.screensGroup);
            return;
        }

//        if(button == s.prevEmailButton) {
//            int row = app.inboxScreen.rowIndexOf(conversation);
//            if(row > 0)
//                show(app.conversations.items[app.inboxScreen.conversationIndexOf(row - 1)]);
//            return;
//        }
//
//        if(button == s.nextEmailButton) {
//            int row = app.inboxScreen.rowIndexOf(conversation);
//            if(row < (app.inboxScreen.totalVisibleRows() - 1))
//                show(app.conversations.items[app.inboxScreen.conversationIndexOf(row + 1)]);
//            return;
//        }


        // Corrupted buttons
        for(int c = 0; c < corruptedMessages.size; c++) {
            CorruptedMessage corrupted = corruptedMessages.items[c];
            if(button == corrupted.fixButton) {
                // Stop idle scares
                v.idleScare.stop();

                // Open corrupted view
                v.restorePhraseApp.show(corrupted.corruption);
                v.restorePhraseApp.open(this);
                return;
            }
        }

        // Action buttons
        UIElement actualView = mediaActionButtons.get(button);
        if(actualView != null)
            button = actualView;

        // Media
        String path = mediaButtons.get(button);
        if(path != null) {
            // Stop idle scares
            v.idleScare.stop();

            // Get media location from photoroll
            PhotoRollApp photoroll = Globals.grid.photoRollApp;
            Media media = photoroll.unlock(path, true);
            if(media == null)
                throw new RuntimeException("Unable to find media " + path);
            MediaAlbum album = v.photoRollApp.findAlbum(media.album);
            int index = album.indexOf(media);

            if(media.isVideo()) {
                // Clicked on a video, open
                v.photoRollApp.videoScreen.show(album, index, null, true);
                v.photoRollApp.videoScreen.open(this, v.screensGroup, button.getX(), button.getY(), button.getWidth());
            }
            else {
                // Clicked on a photo, open
                v.photoRollApp.photoScreen.show(album, index, null, false, conversation.subject, null);
                v.photoRollApp.photoScreen.open(this, v.screensGroup, button.getX(), button.getY(), button.getWidth());
            }
            return;
        }


        // Action button
        String trigger = actionButtons.get(button);
        if(trigger != null) {
            // Stop idle scares
            v.idleScare.stop();

            v.eval(conversation.subject, trigger);
        }

    }
}

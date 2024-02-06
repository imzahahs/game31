package game31.app.chats;


import java.util.Locale;

import game31.DialogueTree;
import game31.Globals;
import game31.ScriptState;
import game31.model.WhatsupModel;
import game31.triggers.ACT1;
import sengine.audio.Audio;

/**
 * Created by Azmi on 20/7/2016.
 */
public class WhatsupContact implements ScriptState.OnChangeListener<Object> {
    static final String TAG = "WhatsupContact";

    public static final String ORIGIN_USER = "user";
    public static final String ORIGIN_SENDER = "sender";

    public static final String TIME_AUTO = "auto";


    public static float tRefreshInterval = 5.0f;            // TODO: stress test this

    public static boolean showDebugMessages = false;

    public final String name;
    public final String profilePicFilename;
    public final DialogueTree tree;

    // Current
    private int currentMessage = -1;
    private float tTypingScheduled = Float.MAX_VALUE;
    private float tNextMessageScheduled = -1;
    private float tNextRefreshScheduled = -1;
    private float tNextTimedReplyScheduled = Float.MAX_VALUE;
    public int readMessages = 0;

    // Local timing multiplier
    public float chatTimingMultiplier = 1f;

    public Audio.Sound customMessageSound = null;
    public Audio.Sound customNotificationSound = null;

    public float getTypingScheduled() {
        return tTypingScheduled;
    }

    public float getNextMessageScheduled() {
        return tNextMessageScheduled;
    }

    public boolean isTimedReply(WhatsupApp app) {
        return app.getRenderTime() > tNextTimedReplyScheduled;
    }

    public WhatsupContact(WhatsupModel.ContactModel model, ScriptState state) {
        this.name = model.name;
        this.profilePicFilename = model.profile_pic_path;
        this.tree = new DialogueTree(model.dialogue_tree_path);
        state.addOnChangeListener(tree.namespace, Object.class, this);

        // Restore state
        tree.unpack(state);

        // State
        readMessages = state.get(tree.namespace + ".readMessages", 0);
    }

    public boolean pack(ScriptState state) {
        if(!tree.pack(state) || currentMessage > 0)
            return false;

        // State
        state.set(tree.namespace + ".readMessages", readMessages);

        return true;
    }

    public boolean reply(WhatsupApp app, String userMessage) {
        int index = tree.makeCurrent(userMessage);
        if(index == -1)
            return false;       // contact is not expecting this result
        tNextTimedReplyScheduled = Float.MAX_VALUE;         // Clear timed reply
        // Else recognize user's message, only if it's not ignored by the conversation and it's not a timed reply
        DialogueTree.UserMessage message = tree.current.userMessages[index];
        if(!tree.current.isUserIgnored && !userMessage.startsWith(Globals.DIALOG_TIMER)) {
            String timeText = app.getCurrentTimeText();
            String dateText = app.getCurrentDateText();
            // Recognize as past message
            if(userMessage.startsWith(Globals.PHOTOROLL_PREFIX) || message.message.startsWith(Globals.KEYBOARD_WILDCARD)) {
                // Media message
                app.addMessage(this, ORIGIN_USER, userMessage, timeText, dateText, null, 0);
                // Need to record user message as custom as we might be dealing with wildcards
                tree.addPastUserMessage(tree.current, message, userMessage, timeText, dateText);
            }
            else {
                // Normal message
                app.addMessage(this, ORIGIN_USER, message.message, timeText, dateText, null, 0);
                tree.addPastUserMessage(tree.current, message, null, timeText, dateText);
            }
        }

        update(app);

        if(tree.current != null)
            app.refreshAvailableUserMessages(this);

        return true;
    }

    public void refresh(WhatsupApp app) {
//        tTypingScheduled = Float.MAX_VALUE;
        tNextRefreshScheduled = -1;
        tNextTimedReplyScheduled = Float.MAX_VALUE;
        // Refresh past message
        app.clearContact(this);
        // Add past messages again
        if(showDebugMessages) {
            // Count number of lines
            int lines = 0;
            for(DialogueTree.Conversation conversation : tree.conversations) {
                for(DialogueTree.UserMessage message : conversation.userMessages)
                    lines++;
                for(DialogueTree.SenderMessage message : conversation.senderMessages)
                    lines++;
            }
            app.addMessage(this, ORIGIN_SENDER, String.format(Locale.US,
                    "chats://section/Loaded %d[] lines", lines
            ), "system", "system", null, 0);
        }
        for(DialogueTree.PastMessage message : tree.pastMessages) {
            if(message.isUserMessage) {
                String text = message.customMessage;        // use custom message by default
                if(text == null) {
                    // else use normal message
                    DialogueTree.UserMessage userMessage = message.getUserMessage(tree);
                    if(userMessage != null)
                        text = userMessage.message;
                }
                if(text != null)
                    app.addMessage(this, ORIGIN_USER, text, message.timeText, message.dateText, null, 0);         // user message
            }
            else {
                DialogueTree.SenderMessage senderMessage = message.getSenderMessage(tree);
                if(senderMessage != null)
                    app.addMessage(this, senderMessage.origin, senderMessage.message, message.timeText, message.dateText, senderMessage.trigger, senderMessage.tTriggerTime);
            }
        }
        // Update
        update(app);
    }

    public void resetScheduledRefresh() {
        tNextRefreshScheduled = -1;
    }

    public void update(WhatsupApp app) {
        while(true) {
            if (tree.current != null) {
                // There is a conversation going on, check if its time
                while (true) {
                    if (app.getRenderTime() >= tTypingScheduled) {
                        // Inform that is typing
                        DialogueTree.SenderMessage message = tree.current.senderMessages[currentMessage];
                        if(message.message == null || message.message.isEmpty() || !message.message.startsWith(Globals.SECTION_PREFIX))
                            app.informTyping(this, message.origin);         // dont inform typing only if message is a section
                        tTypingScheduled = Float.MAX_VALUE;
                    }
                    if (app.getRenderTime() < tNextMessageScheduled)
                        return;         // not yet time
                    // Else time to show message or prepare first message
                    if (currentMessage != -1) {
                        // Time to show this message
                        DialogueTree.SenderMessage message = tree.current.senderMessages[currentMessage];

                        if(message.message != null && !message.message.isEmpty()) {
                            // Increase time
                            if(message.tTypingTime > 0) {
                                long millis = Globals.grid.getSystemTime();
                                long delta = (long) ((message.tTypingTime * Globals.tChatTypingTimeSkipMultiplier) * 1000f);
                                millis += delta;
                                Globals.grid.setSystemTime(millis);
                            }

                            // Prepare time and date text
                            String timeText = message.timeText.contentEquals(TIME_AUTO) ? app.getCurrentTimeText() : message.timeText;
                            String dateText = message.dateText.contentEquals(TIME_AUTO) ? app.getCurrentDateText() : message.dateText;

                            // Update app
                            tree.addPastSenderMessage(tree.current, message, timeText, dateText);
                            app.addMessage(this, message.origin, message.message, timeText, dateText, message.trigger, message.tTriggerTime);
                        }
                    }
                    // Else time for first message or already showed last message
                    currentMessage++;
                    // Try next message
                    if (currentMessage >= tree.current.senderMessages.length) {
                        // Finished messages, inform tree that conversation ended
                        tree.finishCurrent();
                        // Reset
                        currentMessage = -1;
                        tNextMessageScheduled = -1;
                        tNextRefreshScheduled = -1;
                        tTypingScheduled = Float.MAX_VALUE;
                        break;        // finished next message
                    } else {
                        // Continue next message
                        DialogueTree.SenderMessage message = tree.current.senderMessages[currentMessage];
                        if(message.tTypingTime > 0f)
                            tTypingScheduled = app.getRenderTime() + (message.tIdleTime * Globals.tChatTimingMultiplier * chatTimingMultiplier);
                        tNextMessageScheduled = app.getRenderTime() + ((message.tIdleTime + message.tTypingTime) * Globals.tChatTimingMultiplier * chatTimingMultiplier);
                    }
                }
                // Can come here only if finished a conversation
            }
            else if(app.getRenderTime() > tNextTimedReplyScheduled) {
                // It's time to reply
                tNextTimedReplyScheduled = Float.MAX_VALUE;         // Clear timed reply
                reply(app, tree.availableUserMessages.get(tree.timedUserMessageIndex).message);
                // Remove idle wait
                if(tree.current != null)
                    tTypingScheduled = app.getRenderTime();

                // Achievement
                ACT1.unlockAchievement(Globals.Achievement.ABSTAIN_FROM_CHOICE);

                return;
            }
            else if(app.getRenderTime() < tNextRefreshScheduled)
                return;         // not yet time to refresh

            // Else need to find a conversation
            tree.refreshCurrent();
            // User message could be invalidated here, so have to reset scheduled time message. The downside is whenever the tree refreshes, scheduled time message would have to start again
            tNextTimedReplyScheduled = Float.MAX_VALUE;         // Clear timed reply

            // If there are user messages, inform app to update
            if(!tree.availableUserMessages.isEmpty()) {
                // Conversation can be made, but requires user to type a message, but queue refresh as other conversation can be unlocked due to external triggers
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                app.refreshAvailableUserMessages(this);
                if(tree.timedUserMessageIndex != -1)
                    tNextTimedReplyScheduled = app.getRenderTime() + tree.timedUserMessageDelay;
                break;
            }
            else if(tree.current == null) {
                // No conversation can be made now, queue refresh
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                app.refreshAvailableUserMessages(this);
                break;
            }
            // Else a conversation has been made current, display messages
        }
    }

    @Override
    public void onChanged(String name, Object var, Object prev) {
        // Request to refresh now
        resetScheduledRefresh();
    }
}

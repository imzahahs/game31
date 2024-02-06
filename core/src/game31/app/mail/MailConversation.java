package game31.app.mail;


import game31.DialogueTree;
import game31.Globals;
import game31.ScriptState;
import game31.model.MailModel;

/**
 * Created by Azmi on 17/8/2016.
 */
public class MailConversation implements ScriptState.OnChangeListener<Object> {
    static final String TAG = "MailConversation";

    public static final String TIME_AUTO = "auto";

    public static final String PHOTOROLL_PREFIX = "photoroll://";

    public static float tRefreshInterval = 5.0f;            // TODO: stress test this

    public final String name;
    public final String email;
    public final String subject;
    public final DialogueTree tree;

    // Current
    private int currentMessage = -1;
    private float tNextMessageScheduled = -1;
    private float tNextRefreshScheduled = -1;


    public MailConversation(MailModel.ConversationModel model, ScriptState state) {
        this.name = Globals.grid.format(model.name);
        this.email = Globals.grid.format(model.email);
        this.subject = model.subject;
        this.tree = new DialogueTree(model.dialogue_tree_path);
        state.addOnChangeListener(tree.namespace, Object.class, this);

        // Restore state
        tree.unpack(state);
    }

    public boolean pack(ScriptState state) {
        if(!tree.pack(state) || currentMessage > 0)
            return false;
        return true;
    }

    public boolean reply(MailApp app, String userMessage) {
        int index = tree.makeCurrent(userMessage);
        if(index == -1)
            return false;       // contact is not expecting this result
        // Else recognize user's message
        DialogueTree.UserMessage message = tree.current.userMessages[index];
        String timeText = app.getCurrentTimeText();
        String dateText = app.getCurrentDateText();
        app.addMessage(this, Globals.ORIGIN_USER, message.message, timeText, dateText, null, 0);
        // Recognize as past message
        tree.addPastUserMessage(tree.current, message, null, timeText, dateText);
        return true;
    }

    public void refresh(MailApp app) {
        // Refresh past message
        app.clearContact(this);
        // Add past messages again
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
                    app.addMessage(this, Globals.ORIGIN_USER, text, message.timeText, message.dateText, null, 0);         // user message
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

    public void update(MailApp app) {
        while(true) {
            if (tree.current != null) {
                // There is a conversation going on, check if its time
                while (true) {
                    if (app.getRenderTime() < tNextMessageScheduled)
                        return;         // not yet time
                    // Else time to show message or prepare first message
                    if (currentMessage != -1) {
                        // Time to show this message
                        DialogueTree.SenderMessage message = tree.current.senderMessages[currentMessage];

                        // Prepare time and date text
                        String timeText = message.timeText.contentEquals(TIME_AUTO) ? app.getCurrentTimeText() : message.timeText;
                        String dateText = message.dateText.contentEquals(TIME_AUTO) ? app.getCurrentDateText() : message.dateText;

                        // Update app
                        tree.addPastSenderMessage(tree.current, message, timeText, dateText);
                        app.addMessage(this, message.origin, message.message, timeText, dateText, message.trigger, message.tTriggerTime);
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
                        break;        // finished next message
                    } else {
                        // Continue next message
                        DialogueTree.SenderMessage message = tree.current.senderMessages[currentMessage];
                        tNextMessageScheduled = app.getRenderTime() + message.tIdleTime + message.tTypingTime;
                    }
                }
                // Can come here only if finished a conversation
            }
            else if(app.getRenderTime() < tNextRefreshScheduled)
                return;         // not yet time to refresh

            // Else need to find a conversation
            tree.refreshCurrent();

            // If there are user messages, inform app to update
            if(!tree.availableUserMessages.isEmpty()) {
                // Conversation can be made, but requires user to type a message, but queue refresh as other conversation can be unlocked due to external triggers
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                app.refreshAvailableUserMessages(this);
                break;
            }
            else if(tree.current == null) {
                // No conversation can be made now, queue refresh
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                break;
            }
            // Else a conversation has been made current, display messages
        }
    }

    @Override
    public void onChanged(String name, Object var, Object prev) {
        // Request refresh now
        resetScheduledRefresh();
    }
}

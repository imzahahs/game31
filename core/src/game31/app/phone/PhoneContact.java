package game31.app.phone;

import game31.DialogueTree;
import game31.Globals;
import game31.ScriptState;
import game31.VoiceProfile;
import game31.model.ContactModel;
import sengine.graphics2d.Sprite;

/**
 * Created by Azmi on 26/8/2016.
 */
public class PhoneContact implements ScriptState.OnChangeListener<Object> {

    public static final String ATTRIB_PHONE = "phone";


    public static float tRefreshInterval = 5.0f;            // TODO: stress test this


    public static class Attribute {
        public final String attribute;
        public final String value;
        public final String action;

        public Attribute(String attribute, String value, String action) {
            this.attribute = attribute;
            this.value = value;
            this.action = action;
        }

        public Attribute(ContactModel.ContactAttributeModel model) {
            attribute = model.attribute;
            value = model.value;
            action = model.action;
        }
    }

    public final String name;
    public final Sprite profilePic;
    public final Sprite bigProfilePic;
    public final String trigger;
    public final boolean isHidden;

    public final String device;
    public final String number;

    public final Attribute[] attributes;

    public final DialogueTree tree;

    // Working
    private int currentMessage = -1;
    private float tNextRefreshScheduled = -1;
    private String message = null;
    private String nextMessage = null;


    public void resetScheduledRefresh() {
        tNextRefreshScheduled = -1;
    }

    public boolean answer(PhoneApp app, String answer) {
        if(tree == null || tree.availableUserMessages.isEmpty())
            return false;     // UB
        // Try to match with any user message
        int answerLength = answer.length();
        for(int c = 0; c < tree.availableUserMessages.size(); c++) {
            String userMessage = tree.availableUserMessages.get(c).message;
            int length = userMessage.length();
            if(length > answerLength)
                continue;       // cant possibly match
            // Else try
            boolean isMatch = true;
            for(int i = 0; i < length; i++) {
                char given = answer.charAt(answerLength - length + i);      // match from last
                char actual = userMessage.charAt(i);
                if(actual != '?' && given != actual) {
                    isMatch = false;
                    break;
                }
            }
            // Check if its a match
            if(isMatch) {
                // Recognize
                tree.makeCurrent(userMessage);
                currentMessage = -1;
                message = null;
                nextMessage = null;
                return true;
            }

            // Else try next
        }

        return false;
    }

    public boolean pack(ScriptState state) {
        if(tree != null) {
            if(!tree.pack(state) || currentMessage > 0)
                return false;
        }
        return true;
    }

    public String message() {
        return message;
    }

    public String nextMessage(PhoneApp app) {
        // Swap messages
        message = nextMessage;
        nextMessage = null;
        checkNextMessage();
        update(app);
        return message;
    }

    private void checkNextMessage() {
        if(tree == null || tree.current == null)
            return;     // UB
        while(true) {
            if(message != null && nextMessage != null)
                return;     // retrieved enough
            currentMessage++;
            if (currentMessage < tree.current.senderMessages.length) {
                // get next message
                String line = tree.current.senderMessages[currentMessage].message;
                if (line != null && !line.isEmpty()) {
                    if(message == null)
                        message = line;
                    else if(nextMessage == null)
                        nextMessage = line;
                }
                // Else try next
            }
            else
                break;
        }
        // Finished messages, inform tree that conversation ended
        tree.finishCurrent();
        // Reset
        currentMessage = -1;
        tNextRefreshScheduled = -1;
    }

    public void endCall(PhoneApp app) {
        if(tree == null)
            return;
        // Finish current conversation
        while(tree.current != null) {
            tree.finishCurrent();
            tree.refreshCurrent();
        }
        // Inform call ended
        Globals.grid.state.set(tree.namespace + ".call_ended", true, false);
        resetScheduledRefresh();
        update(app);
        // Reset
        Globals.grid.state.set(tree.namespace + ".call_ended", false, false);
        resetScheduledRefresh();
        message = null;
        nextMessage = null;
        currentMessage = -1;
    }

    public void call(PhoneApp app) {
        if(tree == null)
            return;
        if(tree.current == null) {
            // Inform called
            Globals.grid.state.set(tree.namespace + ".called", true, false);
            resetScheduledRefresh();
            update(app);
            // Reset
            Globals.grid.state.set(tree.namespace + ".called", false, false);
            resetScheduledRefresh();
        }
    }

    public void update(PhoneApp app) {
        if(tree == null)
            return;
        while(tree.current == null) {
            if(app.getRenderTime() < tNextRefreshScheduled)
                return;         // not yet time to refresh

            // Else need to find a conversation
            tree.refreshCurrent();

            // If there are user messages, inform app to update
            if(!tree.availableUserMessages.isEmpty()) {
                // Conversation can be made, but requires user to type a message, but queue refresh as other conversation can be unlocked due to external triggers
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                app.informUserInputAvailable(this);
                break;
            }
            else if(tree.current == null) {
                // No conversation can be made now, queue refresh
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                app.informUserInputAvailable(this);
                break;
            }
            else {
                // Else a conversation where npc starts first has been made current, get first message
                currentMessage = -1;
                checkNextMessage();
                if(message == null || nextMessage == null)
                    continue;           // try to get more messages
                // Else retrieved first message
                app.informHasMessage(this);
                tNextRefreshScheduled = Float.MAX_VALUE; // app.getRenderTime() + tRefreshInterval;
                break;
            }
        }
    }


    public PhoneContact(ContactModel model) {
        name = model.name;

        profilePic = Sprite.load(model.profile_pic_path);
        bigProfilePic = Sprite.load(model.big_profile_pic_path);

        // Dialogue
        if(model.dialogue_tree_path != null && !model.dialogue_tree_path.isEmpty()) {
            // Load dialog
            if (model.google_sheet_url != null && Globals.allowCallsOnlineSources)
                Globals.addGoogleSource("assets", model.dialogue_tree_path, model.google_sheet_url);
            tree = new DialogueTree(model.dialogue_tree_path);
            // Compile call voice profiles if needed
            if(Globals.compileCallVoiceProfiles) {
                for(int c = 0; c < tree.conversations.length; c++) {
                    DialogueTree.Conversation conversation = tree.conversations[c];
                    for(int i = 0 ; i < conversation.senderMessages.length; i++) {
                        DialogueTree.SenderMessage message = conversation.senderMessages[i];
                        String path = message.message;
                        VoiceProfile.load(path);
                    }
                }
            }


            Globals.grid.state.addOnChangeListener(tree.namespace, Object.class, this);
            tree.unpack(Globals.grid.state);
        }
        else
            tree = null;

        device = model.device;
        number = model.number;
        trigger = model.trigger;
        isHidden = model.is_hidden;

        if (model.attributes == null)
            attributes = new Attribute[0];
        else {
            attributes = new Attribute[model.attributes.length];
            for (int c = 0; c < model.attributes.length; c++)
                attributes[c] = new Attribute(model.attributes[c]);
        }
    }


    @Override
    public void onChanged(String name, Object var, Object prev) {
        // Request refresh now
        resetScheduledRefresh();
    }
}

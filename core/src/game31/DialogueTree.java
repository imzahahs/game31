package game31;

import java.util.ArrayList;
import java.util.Arrays;

import game31.model.DialogueTreeModel;
import sengine.File;
import sengine.Sys;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 18/7/2016.
 */
public class DialogueTree {
    static final String TAG = "DialogueTree";

    public static final String TAG_IDLE = "IDLE";       // true when there are no available conversations


    private static boolean compareIgnoreCaseAndWhitespace(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();

        int i1 = 0;
        int i2 = 0;

        boolean started = false;
        boolean whitespace1 = false;
        boolean whitespace2 = false;
        while(true) {
            // Find first characters
            char ch1 = 0;
            while(i1 < len1) {
                ch1 = s1.charAt(i1);
                if(!Character.isWhitespace(ch1))
                    break;      // found character
                whitespace1 = true;
                i1++;
            }
            char ch2 = 0;
            while(i2 < len2) {
                ch2 = s2.charAt(i2);
                if(!Character.isWhitespace(ch2))
                    break;      // found character
                whitespace2 = true;
                i2++;
            }
            boolean finished1 = i1 == len1;
            boolean finished2 = i2 == len2;
            if(finished1 || finished2)
                return finished1 && finished2;          // If either string has finished, true only if both strings are finished, else there is an incomplete match

            // Check if comparing whitespace
            if(whitespace1 || whitespace2) {
                // There was a whitespace, check if need to compare
                if(whitespace1 != whitespace2) {
                    // There was a whitespace mismatch, wrong if already started comparing letters
                    if(started)
                        return false;           // whitespace mismatch
                    // Else not yet started, so ignore whitespace mismatch
                }
                // Else whitespace matches, continue comparing letters
            }

            // Remember comparison has started
            started = true;

            // Matching letters, reset whitespace status
            whitespace1 = false;
            whitespace2 = false;

            // Else have both characters from both strings, compare
            if(Character.toLowerCase(ch1) != Character.toLowerCase(ch2))
                return false;       // doesnt compare

            // Else continue next character
            i1++;
            i2++;
        }
    }

    public static class UserMessage implements MassSerializable {
        public final String message;
        public final boolean isHidden;

        public UserMessage(DialogueTreeModel.UserMessageModel model) {
            this.message = model.message;
            this.isHidden = model.is_hidden;
        }

        @MassConstructor
        public UserMessage(String message, boolean isHidden) {
            this.message = message;
            this.isHidden = isHidden;
        }

        @Override
        public Object[] mass() {
            return new Object[] { message, isHidden };
        }
    }

    public static class SenderMessage implements MassSerializable {
        public final String message;
        public final String origin;

        public final String dateText;
        public final String timeText;

        public final float tIdleTime;
        public final float tTypingTime;

        public final String trigger;
        public final float tTriggerTime;

        public SenderMessage(DialogueTreeModel.SenderMessageModel model) {
            this.message = model.message;
            this.origin = model.origin;
            this.dateText = model.date_text;
            this.timeText = model.time_text;
            this.tIdleTime = model.idle_time;
            this.tTypingTime = model.typing_time;
            this.trigger = model.trigger;
            this.tTriggerTime = model.trigger_time;
        }

        @MassConstructor
        public SenderMessage(String message, String origin, String dateText, String timeText, float tIdleTime, float tTypingTime, String trigger, float tTriggerTime) {
            this.message = message;
            this.origin = origin;
            this.dateText = dateText;
            this.timeText = timeText;
            this.tIdleTime = tIdleTime;
            this.tTypingTime = tTypingTime;
            this.trigger = trigger;
            this.tTriggerTime = tTriggerTime;
        }

        @Override
        public Object[] mass() {
            return new Object[] { message, origin, dateText, timeText, tIdleTime, tTypingTime, trigger, tTriggerTime };
        }
    }

    public static class Conversation implements MassSerializable {
        public final String[] tags;
        public final String condition;

        public final UserMessage[] userMessages;
        public final boolean isUserIgnored;

        public final SenderMessage[] senderMessages;

        public final String[] tagsToUnlock;
        public final String[] tagsToLock;

        public final String trigger;

        public Conversation(DialogueTreeModel.ConversationModel model) {
            this.tags = splitCSV(model.tags);
            this.condition = model.condition;

            userMessages = new UserMessage[model.user_messages.length];
            for(int c = 0; c < userMessages.length; c++)
                userMessages[c] = new UserMessage(model.user_messages[c]);

            this.isUserIgnored = model.is_user_ignored;

            senderMessages = new SenderMessage[model.sender_messages.length];
            for(int c = 0; c < senderMessages.length; c++)
                senderMessages[c] = new SenderMessage(model.sender_messages[c]);

            this.tagsToUnlock = splitCSV(model.tags_to_unlock);
            this.tagsToLock = splitCSV(model.tags_to_lock);

            this.trigger = model.trigger;
        }

        @MassConstructor
        public Conversation(String[] tags, String condition, UserMessage[] userMessages, boolean isUserIgnored, SenderMessage[] senderMessages, String[] tagsToUnlock, String[] tagsToLock, String trigger) {
            this.tags = tags;
            this.condition = condition;
            this.userMessages = userMessages;
            this.isUserIgnored = isUserIgnored;
            this.senderMessages = senderMessages;
            this.tagsToUnlock = tagsToUnlock;
            this.tagsToLock = tagsToLock;
            this.trigger = trigger;
        }

        @Override
        public Object[] mass() {
            return new Object[] { tags, condition, userMessages, isUserIgnored, senderMessages, tagsToUnlock, tagsToLock, trigger };
        }
    }

    public static class DialogueTreeDescriptor implements MassSerializable {
        public static final String HINTS_EXTENSION = ".DialogueTreeDescriptor";

        public static DialogueTreeDescriptor load(String filename) {
            if(Globals.loadDialogueTreeSourcesFirst) {
                JsonSource<DialogueTreeModel> source = new JsonSource<>(filename, DialogueTreeModel.class);
                DialogueTreeModel model = source.load(false);
                if(model != null) {
                    // Loaded from source, convert to descriptor
                    try {
                        DialogueTreeDescriptor descriptor = new DialogueTreeDescriptor(model);
                        // Done parsing the descriptor, save
                        File.saveHints(filename + HINTS_EXTENSION, descriptor);
                        return descriptor;      // done
                    } catch (Throwable e) {
                        Sys.error(TAG, "Unable to parse dialogue tree \"" + filename + "\"", e);
                    } finally {
                        // Remove model from hints
                        File.removeHints(source.filename());
                    }
                }
            }
            // Else load from hints
            return File.getHints(filename + HINTS_EXTENSION);
        }


        public final String namespace;
        public final Conversation[] conversations;

        public DialogueTreeDescriptor(DialogueTreeModel model) {
            namespace = model.namespace;

            // Compile all conversations
            conversations = new Conversation[model.conversations.length];
            for(int c = 0; c < model.conversations.length; c++) {
                DialogueTreeModel.ConversationModel conversationModel = model.conversations[c];
                try {
                    conversations[c] = new Conversation(conversationModel);
                } catch (Throwable e) {
                    throw new RuntimeException("Failed to parse conversation-" + c +
                            "\n\nConversation:\n" + Globals.gson.toJson(conversationModel) +
                            "\n\nDialogue Tree:\n" + Globals.gson.toJson(model),
                            e
                    );
                }
            }
        }

        @MassConstructor
        public DialogueTreeDescriptor(String namespace, Conversation[] conversations) {
            this.namespace = namespace;
            this.conversations = conversations;
        }

        @Override
        public Object[] mass() {
            return new Object[] { namespace, conversations };
        }
    }

    public static class PastMessage implements MassSerializable {
        public final int conversation;
        public final int message;
        public final boolean isUserMessage;
        public final String customMessage;
        public final String timeText;
        public final String dateText;

        @MassConstructor
        public PastMessage(int conversation, int message, boolean isUserMessage, String customMessage, String timeText, String dateText) {
            this.conversation = conversation;
            this.message = message;
            this.isUserMessage = isUserMessage;
            this.customMessage = customMessage;
            this.timeText = timeText;
            this.dateText = dateText;
        }

        @Override
        public Object[] mass() {
            return new Object[] { conversation, message, isUserMessage, customMessage, timeText, dateText };
        }

        public UserMessage getUserMessage(DialogueTree tree) {
            if(!isUserMessage)
                throw new IllegalStateException("Cannot get user message from past sender message");
            if(tree.conversations.length <= conversation || tree.conversations[conversation].userMessages.length <= message) {
                Sys.error(TAG, "Unable to find past user message " + conversation + " " + message);
                return null;
            }
            return tree.conversations[conversation].userMessages[message];
        }

        public SenderMessage getSenderMessage(DialogueTree tree) {
            if(isUserMessage)
                throw new IllegalStateException("Cannot get sender message from past user message");
            if(tree.conversations.length <= conversation || tree.conversations[conversation].senderMessages.length <= message) {
                Sys.error(TAG, "Unable to find past sender message " + conversation + " " + message);
                return null;
            }
            return tree.conversations[conversation].senderMessages[message];
        }
    }


    private static String[] splitCSV(String csv) {
        String[] tags = csv.trim().split("\\s*,\\s*");
        if(tags.length == 1 && tags[0].isEmpty())
            return new String[0];
        return tags;
    }

    // Descriptor
    public final String namespace;
    public final Conversation[] conversations;

    // Past messages
    public ArrayList<PastMessage> pastMessages;

    // Current conversations
    public Conversation current = null;
    public final ArrayList<Conversation> available = new ArrayList<>();
    private final ArrayList<Conversation> tempIdle = new ArrayList<>();
    public final ArrayList<UserMessage> availableUserMessages = new ArrayList<>();
    public int timedUserMessageIndex = -1;
    public float timedUserMessageDelay = 0f;

    public String getLastDate() {
        int size = pastMessages.size();
        if(size == 0)
            return "";
        return pastMessages.get(size - 1).dateText;
    }

    public int activeConversations() {
        if(current != null)
            return 1;
        else
            return available.size();
    }

    public void addPastUserMessage(Conversation conversation, UserMessage message, String customMessage, String timeText, String dateText) {
        PastMessage pastMessage = new PastMessage(
                Arrays.asList(conversations).indexOf(conversation),
                Arrays.asList(conversation.userMessages).indexOf(message),
                true,
                customMessage,
                timeText, dateText
        );
        pastMessages.add(pastMessage);
    }

    public void addPastSenderMessage(Conversation conversation, SenderMessage message, String timeText, String dateText) {
        PastMessage pastMessage = new PastMessage(
                Arrays.asList(conversations).indexOf(conversation),
                Arrays.asList(conversation.senderMessages).indexOf(message),
                false,
                null,
                timeText, dateText
        );
        pastMessages.add(pastMessage);
    }

    /**
     * Makes the conversation containing the specified user message current.
     * @param userMessage
     * @return {@link int} index of the user message in the conversation, -1 if not found
     */
    public int makeCurrent(String userMessage) {
        String typed = userMessage.trim();
        if(typed.isEmpty())
            return -1;      // dont accept empty string
        boolean isAnswerMedia = userMessage.startsWith(Globals.PHOTOROLL_PREFIX);

        Conversation bestConversation = null;
        int bestMessageIndex = -1;

        for(Conversation conversation : available) {
            for(int c = 0; c < conversation.userMessages.length; c++) {
                UserMessage message = conversation.userMessages[c];
                boolean isMessageMedia = message.message.startsWith(Globals.PHOTOROLL_PREFIX);
                boolean isWildcard = !isMessageMedia && message.message.startsWith(Globals.KEYBOARD_WILDCARD);

                if(isAnswerMedia != isMessageMedia)
                    continue;       // cant match different types

                if(isWildcard) {
                    if(bestConversation == null) {          // Only accept wildcard matches if there are no other matches
                        if(message.message.equals(Globals.KEYBOARD_NUMERIC_WILDCARD)) {
                            // Make sure message is all numeric
                            int length = typed.length();
                            boolean isNumeric = true;
                            for(int i = 0; i < length; i++) {
                                char ch = typed.charAt(i);
                                if (!Character.isDigit(ch) && !Character.isWhitespace(ch)) {
                                    isNumeric = false;
                                    break;
                                }
                            }
                            if(isNumeric) {
                                bestConversation = conversation;
                                bestMessageIndex = c;
                            }
                        }
                        else {
                            // Else accepts any input
                            bestConversation = conversation;
                            bestMessageIndex = c;
                        }
                    }
                }
                else if(isMessageMedia) {
                    // Is media, check for match
                    String name = message.message.split(Globals.ATTACHMENT_TITLE_TOKEN)[0].trim();
                    if(name.equals(Globals.ATTACHMENT_WILDCARD)) {
                        // This is a media wildcard, just remember for now and try better matches
                        if(bestConversation == null) {
                            bestConversation = conversation;
                            bestMessageIndex = c;
                        }
                    }
                    else if(name.compareTo(userMessage) == 0) {
                        // Matches this media, accept
                        bestConversation = conversation;
                        bestMessageIndex = c;
                        break;
                    }
                }
                else if(compareIgnoreCaseAndWhitespace(message.message, typed)) {
                    // Else just text and it matches
                    bestConversation = conversation;
                    bestMessageIndex = c;
                    break;
                }
            }
        }

        // Check if found a match
        if(bestConversation != null) {
            current = bestConversation;
            available.clear();
            availableUserMessages.clear();
            timedUserMessageIndex = -1;
            timedUserMessageDelay = 0f;
            return bestMessageIndex;
        }

        // Else no match was found
        return -1;
    }

    /**
     * Finishes current conversation, {@link #current} must not be null
     */
    public void finishCurrent() {
        // Lock tags
        for(String tag : current.tagsToLock)
            setTagState(tag, false);
        // Unlock tags
        for(String tag : current.tagsToUnlock)
            setTagState(tag, true);
        // Trigger
        try {
            if(current.trigger != null && !current.trigger.isEmpty())
                Globals.grid.eval(namespace, current.trigger);
        } catch (Throwable e) {
            Sys.error(TAG, "Unable to perform trigger for current conversation " + Arrays.asList(conversations).indexOf(current), e);
        }
        // Finish current
        current = null;
    }

    private boolean getTagState(String tag) {
        String qualifiedName;
        if(tag.contains("."))
            qualifiedName = tag;
        else
            qualifiedName = namespace + "." + tag;
        return Globals.grid.state.get(qualifiedName, false);
    }

    private void setTagState(String tag, boolean state) {
        if(tag.contains("."))
            Globals.grid.state.set(tag, state, true);         // notify only when modifying states of other namespaces, 20180818: replaced !tag.startsWith(namespace) with just true as it conflicts with similar namespace names
        else
            Globals.grid.state.set(namespace + "." + tag, state, false);            // no need to modify as we already know
    }


    /**
     * Refresh current conversation status, updates current, available and availableUserMessages fields.<br />
     * Must be called after {@link #unpack(ScriptState)}
     */
    public void refreshCurrent() {
        // Clear
        current = null;
        available.clear();
        tempIdle.clear();
        availableUserMessages.clear();
        timedUserMessageIndex = -1;
        timedUserMessageDelay = 0f;
        // Evaluate conversations in order
        for(int c = 0; c < conversations.length; c++) {
            Conversation conversation = conversations[c];
            // Check if tags are all unlocked
            boolean tagsUnlocked = true;
            boolean isIdleUsed = false;
            for(String tag : conversation.tags) {
                if(tag == null || tag.isEmpty())
                    continue;       // Invalid
                boolean isNot = tag.charAt(0) == '!';
                if(isNot)
                    tag = tag.substring(1);
                if(tag.equals(TAG_IDLE))
                    isIdleUsed = true;      // Ignore the value of idle, for now, but remember to assess again later
                else {
                    boolean value = getTagState(tag);
                    if (isNot) {
                        if (value) {
                            tagsUnlocked = false;
                            break;
                        }
                    } else if (!value) {
                        tagsUnlocked = false;
                        break;
                    }
                }
            }
            if(!tagsUnlocked)
                continue;       // Tags for this conversation is still locked
            // Check code condition
            if(conversation.condition != null && !conversation.condition.isEmpty()) {
                try {
                    Boolean allowed = (Boolean) Globals.grid.eval(namespace, conversation.condition);
                    if(!allowed)
                        continue;       // not allowed
                } catch (Throwable e) {
                    Sys.error(TAG, "Unable to check condition '" + conversation.condition + "' for conversation " + c, e);
                    continue;
                }
            }
            // Okay this conversation is allowed
            if(isIdleUsed)
                tempIdle.add(conversation);
            else
                available.add(conversation);
        }
        // Re-evaluate idles
        if(!tempIdle.isEmpty()) {
            boolean isIdle = available.isEmpty();
            for(int c = 0; c < tempIdle.size(); c++) {
                Conversation conversation = tempIdle.get(c);
                // Check if tags are all unlocked
                boolean tagsUnlocked = true;
                for (String tag : conversation.tags) {
                    if(tag == null || tag.isEmpty())
                        continue;       // Invalid
                    boolean isNot = tag.charAt(0) == '!';
                    if(isNot)
                        tag = tag.substring(1);
                    if(tag.equals(TAG_IDLE)) {
                        if (isNot) {
                            if (isIdle) {
                                tagsUnlocked = false;
                                break;
                            }
                        } else if (!isIdle) {
                            tagsUnlocked = false;
                            break;
                        }
                    }
                }
                if(tagsUnlocked)
                    available.add(conversation);            // this was unlocked by idle tag
            }
            // Clear
            tempIdle.clear();
        }
        if(available.isEmpty())
            return;     // no conversations available
        // Now for each available conversation, double check idle state

        // Now, find the first conversation that does not require user messages (sender sends first)
        for(Conversation conversation : available) {
            if(conversation.userMessages.length == 0) {
                // Found a conversation where sender sends first, automatically use this as current
                current = conversation;
                available.clear();
                availableUserMessages.clear();
                break;
            }
            // Else, add user messages
            availableUserMessages.addAll(Arrays.asList(conversation.userMessages));
        }
        // Done

        // If there are user messages, check for timed messages
        for(int c = 0; c < availableUserMessages.size(); c++) {
            String userMessage = availableUserMessages.get(c).message;
            if(!userMessage.startsWith(Globals.DIALOG_TIMER))
                continue;       // not timed message
            // Parse time
            float time;
            try {
                time = Float.parseFloat(userMessage.substring(Globals.DIALOG_TIMER.length()));
            } catch (Throwable e) {
                // Ignore
                Sys.error(TAG, "Failed to parse timed user message: " + userMessage);
                continue;
            }
            // Find the earliest timed reply
            if(timedUserMessageIndex == -1 || time < timedUserMessageDelay) {
                timedUserMessageIndex = c;
                timedUserMessageDelay = time;
            }
        }
    }


    /**
     * Helper method equivalent to availableUserMessages.size() > 0
     * @return boolean
     */
    public boolean isUserMessagesAvailable() {
        return availableUserMessages.size() > 0;
    }

    /**
     * Initializes the keyboard with available user messages that can be typed.
     * @param keyboard
     */
    public void refreshAvailableUserMessages(Keyboard keyboard) {
        // If there are user messages, populate keyboard's auto complete
        if (!availableUserMessages.isEmpty()) {
            // Compile a list of visible and hidden autocompletes
            ArrayList<String> autoCompletes = new ArrayList<>();
            ArrayList<String> hiddenAutoCompletes = new ArrayList<>();
            for (int c = 0; c < availableUserMessages.size(); c++) {
                UserMessage message = availableUserMessages.get(c);
                if (!message.message.startsWith(Globals.DIALOG_TIMER)) {
                    if (message.isHidden)
                        hiddenAutoCompletes.add(message.message);
                    else
                        autoCompletes.add(message.message);
                }
            }
            keyboard.resetAutoComplete(autoCompletes, hiddenAutoCompletes);
        }
        else
           keyboard.resetAutoComplete();           // Else clear keyboard auto completes
    }

    public DialogueTree(String filename) {
        DialogueTreeDescriptor descriptor = DialogueTreeDescriptor.load(filename);

        namespace = descriptor.namespace;
        conversations = descriptor.conversations;
    }

    /**
     * Unpacks past messages from state and registers to state
     */
    public void unpack(ScriptState state) {

        // Reset main tag if doesnt exist
        if(state.get(namespace + ".main", null) == null)
            state.set(namespace + ".main", true, false);

        // Refresh past messages
        pastMessages = state.get(namespace + ".pastMessages", null);
        if(pastMessages == null)
            pastMessages = new ArrayList<>();
    }

    /**
     * Packs past messages into state
     */
    public boolean pack(ScriptState state) {
        if(current != null)
            return false;           // cannot pack as there is a conversation going on
        state.set(namespace + ".pastMessages", pastMessages);
        return true;
    }


}

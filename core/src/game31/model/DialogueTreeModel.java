package game31.model;

import com.badlogic.gdx.utils.Array;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import game31.Globals;
import game31.VoiceProfile;
import sengine.calc.Range;
import sengine.utils.SheetsParser;

/**
 * Created by Azmi on 18/7/2016.
 */

@SheetsParser.Row(fields = { "namespace", "conversations" })
public class DialogueTreeModel {
    static final String TAG = "DialogueTreeModel";

    private static final String defaultEmptyString = "";
    private static final String defaultAutoString = "auto";
    private static final String defaultSenderString = "sender";
    private static final UserMessageModel[] defaultEmptyUserMessages = new UserMessageModel[0];
    private static final SenderMessageModel[] defaultEmptySenderMessages = new SenderMessageModel[0];
    private static final ConversationModel[] defaultEmptyConversationModel = new ConversationModel[0];

    @SheetsParser.Row(fields = { "message", "is_hidden" })
    public static class UserMessageModel {
        public String message = defaultEmptyString;
        public boolean is_hidden = false;
    }

    @SheetsParser.Row(fields = { "message", "origin", "date_text", "time_text", "idle_time", "typing_time", "trigger", "trigger_time" })
    public static class SenderMessageModel {
        public String message = defaultEmptyString;
        public String origin = defaultSenderString;

        public String date_text = defaultAutoString;
        public String time_text = defaultAutoString;

        public float idle_time = 0;
        public float typing_time = 0;

        public String trigger = defaultEmptyString;
        public float trigger_time = 0;
    }

    @SheetsParser.Row(fields = { "tags", "condition", "user_messages", "is_user_ignored", "sender_messages", "tags_to_unlock", "tags_to_lock", "trigger" })
    public static class ConversationModel {


        public String tags = defaultEmptyString;
        public String condition = defaultEmptyString;

        public UserMessageModel[] user_messages = defaultEmptyUserMessages;

        public boolean is_user_ignored = false;

        public SenderMessageModel[] sender_messages = defaultEmptySenderMessages;

        public String tags_to_unlock = defaultEmptyString;
        public String tags_to_lock = defaultEmptyString;

        public String trigger = defaultEmptyString;
    }

    public String namespace = defaultEmptyString;

    public ConversationModel[] conversations = defaultEmptyConversationModel;


    public DialogueTreeModel() {
        ConversationBuilder.linkCounter = 0;                // Reset links for each dialogue tree

        // Reset timers
        ConversationBuilder.switchTime = ConversationBuilder.defaultSwitchTime;
        ConversationBuilder.sentenceTime = ConversationBuilder.defaultSentenceTime;
        ConversationBuilder.wordsPerMinute = ConversationBuilder.defaultWordsPerMinute;
    }


    @SheetsParser.Row(fields = { "tags" })
    public static class ConversationBuilder {

        private static String appendString(String source, String string) {
            if(source == null || source.isEmpty())
                return string;
            return source + ", " + string;
        }

        private static String[] splitCSV(String csv) {
            String[] tags = csv.trim().split("\\s*,\\s*");
            if(tags.length == 1 && tags[0].isEmpty())
                return new String[0];
            return tags;
        }


        public static int linkCounter = 0;
        public static String linkPrefix = "__LINK";

        public static SimpleDateFormat parseFormat = new SimpleDateFormat("dd MMMM yyyy h:mm a", Locale.US);
        public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        public static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a",Locale.US);
        public static TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");

        public static float pastTimeMultiplier = 10f;

        public static Range defaultSwitchTime = new Range(0.3f, 0.3f);
        public static Range defaultSentenceTime = new Range(0.9f, 0.8f);                 // 0.9f, 0.8f
        public static float defaultWordsPerMinute = 6f;                 // 12f

        private static Range switchTime = defaultSwitchTime;
        private static Range sentenceTime = defaultSentenceTime;
        private static float wordsPerMinute = defaultWordsPerMinute;

        // Initialize statics
        static {
            parseFormat.setTimeZone(timeZone);
            dateFormat.setTimeZone(timeZone);
            timeFormat.setTimeZone(timeZone);
            DateFormatSymbols lowercaseAMPM = new DateFormatSymbols(Locale.US);
            lowercaseAMPM.setAmPmStrings(new String[] { "am", "pm" });
            timeFormat.setDateFormatSymbols(lowercaseAMPM);
        }


        public String tags = null;

        // Current
        // Conversations
        private final Array<ConversationModel> conversations = new Array<ConversationModel>(ConversationModel.class);
        private ConversationModel current = null;
        private boolean isTagsModified = false;
        private String currentSender = "";

        private final Array<ConversationBuilder> splits = new Array<ConversationBuilder>(ConversationBuilder.class);
        private final Array<String> splitTags = new Array<String>(String.class);


        // Time
        private Date time = null;
        private float queuedIdleTime = 0;
        private float queuedTypingTime = 0;

        public void requires(String tags) {
            push();
            current.tags = appendString(current.tags, tags);
        }

        public void split_add(String list, ConversationBuilder builder) {
            if(builder.conversations.size == 0)
                return;         // UB
            // Get a unique start and end name for this section
            String sectionOpenName = linkPrefix + linkCounter;
            linkCounter++;
            String sectionFinishedName = linkPrefix + linkCounter;
            linkCounter++;
            // Add open and finish links to this section
            ConversationModel firstConversation = builder.conversations.first();
            firstConversation.tags = appendString(firstConversation.tags, sectionOpenName);
            firstConversation.tags_to_lock = appendString(firstConversation.tags_to_lock, sectionOpenName);
            ConversationModel lastConversation = builder.conversations.peek();
            lastConversation.tags_to_unlock = appendString(lastConversation.tags_to_unlock, sectionFinishedName);
            // Add section
            conversations.addAll(builder.conversations);

            // Now link to specified splits
            String[] indices = splitCSV(list);
            for(int c = 0; c < indices.length; c++) {
                int index = Integer.valueOf(indices[c]) - 1;
                // Create new conversation to the last one
                ConversationBuilder split = splits.items[index];
                split.unlocks(sectionOpenName);
                split.push();
                ConversationModel conversation = split.conversations.peek();
                conversation.tags = appendString(conversation.tags, sectionFinishedName);
                conversation.tags_to_lock = appendString(conversation.tags_to_lock, sectionFinishedName);
            }
        }

        public void split(ConversationBuilder builder) {
            if(builder == null)
                builder = new ConversationBuilder();
            if(builder.conversations.size == 0)
                builder.push();
            // Get a unique name for this split
            String splitName = linkPrefix + linkCounter;
            linkCounter++;
            splitTags.add(splitName);
            // Unlock this split
            unlocks(splitName);
            // Label this split
            ConversationModel firstConversation = builder.conversations.first();
            firstConversation.tags = appendString(firstConversation.tags, splitName);
            // Add this split
            splits.add(builder);
        }

        /**
         * Waits for all of the previous splits to end
         */
        public void join_all() {
            if(splits.size == 0)
                return;         // UB

            // Create new tag for the join
            String joinName = linkPrefix + linkCounter;
            linkCounter++;
            unlocks(joinName);          // unlock the join automatically, but it will only unlock on IDLE

            // Compile all split tags into one
            String allTags = null;
            for(String tag : splitTags)
                allTags = appendString(allTags, tag);
            allTags = appendString(allTags, joinName);          // also include the join name

            // For all splits, keep track of another set of links so that each split can only be entered once
            for(ConversationBuilder split : splits) {
                // Create a new tag
                String lockName = linkPrefix + linkCounter;
                linkCounter++;

                // Label this split with the absence of the new tag, so that this conversation does not recurse
                conversations.addAll(split.conversations);
                ConversationModel conversation = split.conversations.first();
                conversation.tags = appendString(conversation.tags, "!" + lockName);

                // Lock all other splits (and the join) while this conversation is going on
                conversation.tags_to_lock = appendString(conversation.tags_to_lock, allTags);

                // In the last conversation, unlock all back, including the lock tag so that this split cannot be entered again
                conversation = split.conversations.peek();
                conversation.tags_to_unlock = appendString(conversation.tags_to_unlock, allTags);
                conversation.tags_to_unlock = appendString(conversation.tags_to_unlock, lockName);
            }

            // Clear
            splits.clear();
            splitTags.clear();

            // Create join
            current = new ConversationModel();
            current.tags = appendString(joinName, "IDLE");
            current.tags_to_lock = joinName;
            conversations.add(current);
        }

        /**
         * Waits for either one of the previous split to end, persist choices
         */
        public void join_persist() {
            if(splits.size == 0)
                return;         // UB

            // Get a unique name for this join
            String joinName = linkPrefix + linkCounter;
            linkCounter++;

            // Compile all split tags into one
            String allTags = null;
            for(String tag : splitTags)
                allTags = appendString(allTags, tag);

            // For all splits, automatically unlock this join if either of the splits were unlocked
            for(ConversationBuilder split : splits) {
                // Create a new tag
                String lockName = linkPrefix + linkCounter;
                linkCounter++;
                // In the first conversation, lock all other splits
                conversations.addAll(split.conversations);
                ConversationModel conversation = split.conversations.first();
                conversation.tags = appendString(conversation.tags, "!" + lockName);
                conversation.tags_to_lock = appendString(conversation.tags_to_lock, allTags);
                // In the last conversation, unlock all, join tag and also this conversation's lock tag
                conversation = split.conversations.peek();
                conversation.tags_to_unlock = appendString(conversation.tags_to_unlock, joinName);
                conversation.tags_to_unlock = appendString(conversation.tags_to_unlock, lockName);
            }

            // Clear
            splits.clear();
            splitTags.clear();

            // Create join
            current = new ConversationModel();
            current.tags = joinName;
            current.tags_to_lock = joinName;
            conversations.add(current);
        }

        /**
         * Waits for either one of the previous split to end
         */
        public void join() {
            if(splits.size == 0)
                return;         // UB

            // Get a unique name for this join
            String joinName = linkPrefix + linkCounter;
            linkCounter++;

            // Compile all split tags into one
            String allTags = null;
            for(String tag : splitTags)
                allTags = appendString(allTags, tag);

            // For all splits, automatically unlock this join if either of the splits were unlocked
            for(ConversationBuilder split : splits) {
                // In the first conversation, lock all other splits
                conversations.addAll(split.conversations);
                ConversationModel conversation = split.conversations.first();
                conversation.tags_to_lock = appendString(conversation.tags_to_lock, allTags);
                // In the last conversation, unlock join tag
                conversation = split.conversations.peek();
                conversation.tags_to_unlock = appendString(conversation.tags_to_unlock, joinName);
            }

            // Clear
            splits.clear();
            splitTags.clear();

            // Create join
            current = new ConversationModel();
            current.tags = joinName;
            current.tags_to_lock = joinName;
            conversations.add(current);
        }


        // Timing
        public void conversation_speed(float minTime, float randomTime) {
            ConversationBuilder.switchTime = new Range(minTime, randomTime);
        }

        public void typing_speed(float minTime, float randomTime, float wordsPerMinute) {
            ConversationBuilder.sentenceTime = new Range(minTime, randomTime);
            ConversationBuilder.wordsPerMinute = wordsPerMinute;
        }

        public void reset_conversation_speed() {
            ConversationBuilder.switchTime = ConversationBuilder.defaultSwitchTime;
        }

        public void reset_typing_speed() {
            ConversationBuilder.sentenceTime = ConversationBuilder.defaultSentenceTime;
            ConversationBuilder.wordsPerMinute = ConversationBuilder.defaultWordsPerMinute;
        }


        public void past(String date, String time) {
            try {
                this.time = parseFormat.parse(date + " " + time);
            } catch (Throwable e) {
                throw new RuntimeException("Unable to parse time: " + date + " " + time, e);
            }
        }

        public void live() {
            time = null;
        }

        public void wait(float seconds) {
            if(time != null) {
                long timestamp = time.getTime();
                timestamp += Math.round(seconds / 1000.0);
                time.setTime(timestamp);
            }
            else
                queuedIdleTime += seconds;
        }

        public void wait_typing(float seconds) {
            if(time != null) {
                long timestamp = time.getTime();
                timestamp += Math.round(seconds / 1000.0);
                time.setTime(timestamp);
            }
            else
                queuedTypingTime += seconds;
        }


        private void push() {
            if(current == null) {
                current = new ConversationModel();
                current.tags = tags;
                conversations.add(current);
            }
            else if(conversations.size == 1) {
                // Create a new link to close up first conversation
//                String linkName = linkPrefix + linkCounter;
//                linkCounter++;
//                current.tags = appendString(current.tags, "!" + linkName);
                // Create a new link for second conversation
                String nextLinkName = linkPrefix + linkCounter;
                linkCounter++;
//                current.tags_to_unlock = appendString(current.tags_to_unlock, linkName + ", " + nextLinkName);
                current.tags_to_unlock = appendString(current.tags_to_unlock, nextLinkName);

                current = new ConversationModel();
                current.tags = nextLinkName;
                current.tags_to_lock = nextLinkName;
                conversations.add(current);
            }
            else {
                // Create a new link for next conversation
                String nextLinkName = linkPrefix + linkCounter;
                linkCounter++;

                current.tags_to_unlock = appendString(current.tags_to_unlock, nextLinkName);

                current = new ConversationModel();
                current.tags = nextLinkName;
                current.tags_to_lock = nextLinkName;
                conversations.add(current);
            }
            // Indicate tags has not been modified yet
            isTagsModified = false;
        }

        public void choices(UserMessageModel[] messages) {
            live();         // Will always be live if there are choices
            // Choices will always start a new conversation
            if(current == null || current.user_messages != defaultEmptyUserMessages  || current.sender_messages != defaultEmptySenderMessages)
                push();
            current.user_messages = messages;
            currentSender = "user";
            // Cleanup messages
            for(UserMessageModel message : messages)
                message.message = message.message.trim();
        }

        public void response(SenderMessageModel[] messages) {
            // Update message times
            for(SenderMessageModel message : messages) {
                if(time == null) {
                    // Live
                    if (!message.origin.equals("user") && message.idle_time == 0 && message.typing_time == 0) {     // live user responses have no timing
                        // Check if new switching person
                        if(!currentSender.equals(message.origin))
                            queuedIdleTime += switchTime.generate();
                        message.idle_time = queuedIdleTime + sentenceTime.generate();
                        queuedIdleTime = 0;

                        // Typing speed
                        int words = message.message.trim().split("\\s+").length;
                        message.typing_time = (1f / wordsPerMinute) * words;
                        message.typing_time += queuedTypingTime;
                        queuedTypingTime = 0;
                    }
                    // Ignore dates
                }
                else {
                    // Past
                    if (message.date_text == defaultAutoString && message.time_text == defaultAutoString) {
                        // Check if new switching person
                        long delay = 0;
                        if(!currentSender.equals(message.origin))
                            delay = switchTime.generateInt() * 1000;     // milliseconds

                        // Sentence
                        delay += Math.round(sentenceTime.generate() * 1000);

                        // Typing speed
                        int words = message.message.trim().split("\\s+").length;
                        delay += Math.round(((1f / wordsPerMinute) * words) * 1000);
                        delay = Math.round(delay * (double)pastTimeMultiplier);
                        time.setTime(time.getTime() + delay);

                        // Update texts
                        message.date_text = dateFormat.format(time);
                        message.time_text = timeFormat.format(time);
                    }
                }
                // Compile text voice if needed
                if(Globals.compileVoiceProfiles && message.message.startsWith(Globals.VOICE_PREFIX)) {
                    // Message is corrupted, extract actual message
                    String[] data = message.message.substring(Globals.VOICE_PREFIX.length()).split("=", 2);
                    String path = data[0].trim();
                    String actual = data[1].trim();

                    // Compile text voice
                    VoiceProfile.load(path);
                }
                currentSender = message.origin;
            }

            if(current == null || isTagsModified)
                push();         // first conversation or tags has been modified
            else if(current.sender_messages != null) {
                // Tags has not been modified and there are existing responses, just append
                SenderMessageModel[] array = new SenderMessageModel[current.sender_messages.length + messages.length];
                System.arraycopy(current.sender_messages, 0, array, 0, current.sender_messages.length);
                System.arraycopy(messages, 0, array, current.sender_messages.length, messages.length);
                current.sender_messages = array;
                return;
            }
            // Else just use this one
            current.sender_messages = messages;
        }

        public void ignore_choices() {
            if(current != null && current.user_messages != defaultEmptyUserMessages)
                current.is_user_ignored = true;
        }

        public void trigger(String trigger) {
            if(current == null)
                push();
            current.trigger = trigger;
            push();
        }

        public void unlocks(String tags) {
            if(current == null)
                push();
            current.tags_to_unlock = appendString(current.tags_to_unlock, tags);
            isTagsModified = true;          // indicate that cannot append sender messages together
        }

        public void locks(String tags) {
            if(current == null)
                push();
            current.tags_to_lock = appendString(current.tags_to_lock, tags);
            isTagsModified = true;          // indicate that cannot append sender messages together
        }
    }


    public void build(ConversationBuilder[] builders) {
        // Compile all conversations
        Array<ConversationModel> conversations = new Array<ConversationModel>(ConversationModel.class);

        for(ConversationBuilder builder : builders) {
            conversations.addAll(builder.conversations);
        }

        this.conversations = conversations.toArray();
    }

}

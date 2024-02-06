package game31.app.flapee;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

import java.util.Locale;

import game31.AppCrashDialog;
import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.ScriptState;
import game31.VoiceProfile;
import game31.app.chats.WhatsupContact;
import game31.app.homescreen.Homescreen;
import game31.gb.flapee.GBFlapeeBirdScreen;
import game31.glitch.MpegGlitch;
import game31.renderer.LightingCompositorMaterial;
import game31.renderer.LightingGeneratorMaterial;
import game31.renderer.SaraRenderer;
import game31.triggers.ACT1;
import sengine.Entity;
import sengine.File;
import sengine.Streamable;
import sengine.Sys;
import sengine.animation.Animation;
import sengine.animation.ScaleAnim;
import sengine.audio.Audio;
import sengine.audio.Stream;
import sengine.calc.Graph;
import sengine.calc.Range;
import sengine.calc.SetDistributedSelector;
import sengine.calc.SetRandomizedSelector;
import sengine.calc.SetSelector;
import sengine.graphics2d.Font;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;
import sengine.mass.MassSerializable;
import sengine.materials.ColorAttribute;
import sengine.ui.Clickable;
import sengine.ui.HorizontalProgressBar;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.OnPressed;
import sengine.ui.PatchedTextBox;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.Toast;
import sengine.ui.UIElement;
import sengine.utils.Builder;
import sengine.utils.StreamablePrecacher;

public class FlapeeBirdScreen extends Menu<Grid> implements OnClick<Grid>, OnPressed<Grid>, Homescreen.App {
    private static final String TAG = "FlapeeBird";

    private static final String FONT_NUMBERS = "0123456789";

    private static final Vector3 vec1 = new Vector3();
    private static final Vector3 vec2 = new Vector3();

    public static class Internal {
        public UIElement.Group window;
        public ScreenBar bars;

        public Animation barsShowAnim;
        public Animation barsHideAnim;

        public Clickable tapView;

        public StaticSprite tutorialView;

        public Toast readyView;

        public StaticSprite titleView;
        public StaticSprite royaleTitleView;

        public StaticSprite loadingView;
        public float tLoadingTime;

        public StaticSprite findingFriendsGroup;
        public float tFindingFriendsTime;

        public StaticSprite sendingEggsGroup;
        public float tSendingEggsTime;

        public StaticSprite menuGroup;
        public StaticSprite[] friendRows;
        public Sprite friendsRowPlayer;
        public TextBox friendRowNameView;
        public TextBox friendRowScoreView;
        public Clickable playButton;
        public Clickable highScoreButton;

        public Toast dieSplash;

        // Basic menu group
        public StaticSprite basicGroup;
        public TextBox basicScoreView;
        public Clickable basicPlayButton;

        public StaticSprite newHighScoreView;
        public float tNewHighScoreTime;
        public float tNewHighScoreRoyaleTime;
        public TextBox rankingView;
        public String rankingFormat;
        public StaticSprite rankingRoyaleView;

        // High scores
        public StaticSprite scoreGroup;
        public TextBox scoreRowNameView;
        public TextBox scoreRowHoursPlayedView;
        public String scoreRowHoursPlayedFormat;
        public TextBox scoreRowScoreView;
        public StaticSprite[] scoreRows;
        public Sprite scoreRowPlayer;
        public Clickable scoreCloseButton;
        public String scorePlayerName;
        public Font scorePlayerFont;
        public Font scoreNormalFont;

        // Invite friends group
        public StaticSprite inviteGroup;
        public StaticSprite inviteRowProfileView;
        public TextBox inviteRowNameView;
        public Clickable inviteRowSendButton;
        public Clickable inviteRowSentButton;
        public UIElement.Group[] inviteRows;
        public Clickable inviteCloseButton;

        // Shop button
        public StaticSprite shopGroup;
        public TextBox shopOfferView;
        public String shopOfferFormat;
        public int[] shopEggsAmount;
        public int shopPurchasedThreshold;
        public int[] shopLifeAmount;
        public Clickable shopPurchaseButton;
        public String shopPurchaseButtonFormat;
        public Clickable shopCloseButton;

        public MpegGlitch shopBuyGlitch;
        public UIElement.Group shopSigilsGroup;
        public SetSelector<Toast> shopSigils;
        public SetSelector<Animation> shopSigilAnim;
        public Range shopSigilSize;
        public Range tShopSigilInterval;

        // More eggs
        public StaticSprite moreEggsGroup;
        public UIElement.Group moreEggsAdOnlyGroup;
        public UIElement.Group moreEggsAdAndInviteGroup;
        public Clickable moreEggsWatchAdSingleButton;
        public StaticSprite moreEggsWatchAdNewSticker;
        public Clickable moreEggsWatchAdButton;
        public Clickable moreEggsInviteButton;
        public StaticSprite moreEggsInviteNewSticker;
        public Clickable moreEggsShopButton;
        public StaticSprite moreEggsShopNewSticker;

        public TextBox eggsView;
        public Animation eggsNotEnoughAnim;

        public TextBox lifehoursView;

        public Graph resourceTransitionGraph;

        public Clickable ingamePowerupButton;
        public TextBox ingamePowerupCostView;
        public StaticSprite ingamePowerupChargingView;
        public Animation ingamePowerupChargingAnim;
        public Toast ingamePowerupAvailableView;
        public Animation ingamePowerupBirdAnim;

        public StaticSprite gameOverView;
        public float tNormalGameOverDelay;

        public UIElement.Group chanceMenu;
        public Clickable chanceReviveButton;
        public TextBox chanceReviveCostView;
        public StaticSprite chanceReviveTimerView;
        public Animation chanceReviveTimerAnim;
        public Clickable chanceDieButton;
        public Animation reviveBirdAnim;

        public TextBox scoreView;
        public Animation scoredAnim;

        public StaticSprite inputBlockerView;

        public StaticSprite rewardGroup;
        public StaticSprite rewardVisualView;
        public Sprite rewardVisualClosed;
        public Sprite rewardVisualOpen;
        public Animation rewardIdleAnim;
        public Animation rewardOpenAnim;
        public TextBox rewardTextView;
        public Clickable rewardAcceptButton;
        public TextBox rewardAcceptText;
        public float tRewardStartDelay;
        public float tRewardEndDelay;
        public Audio.Sound rewardAppearSound;
        public Audio.Sound rewardOpenSound;

        public StaticSprite permissionGroup;
        public Clickable permissionAcceptButton;
        public Clickable permissionDenyButton;

        public Audio.Sound eggsNotEnoughSound;
        public Audio.Sound newHighScoreSound;


        // Dialog box
        public PatchedTextBox dialogContainer;
        public UIElement.Group dialogGroup;
        public TextBox dialogTextView;
        public UIElement.Group dialogStarGroup;
        public Clickable[] dialogStarViews;
        public Animation dialogStarHintAnim;
        public Sprite dialogStarSelectedMat;
        public Sprite dialogStarDeselectedMat;
        public Sprite dialogStarTopMat;
        public TextBox dialogStartLeftTextView;
        public TextBox dialogStartRightTextView;
        public Clickable dialogPositiveButton;
        public Clickable dialogNegativeButton;
        public Clickable dialogSingleButton;
        public float dialogLengthPadding;
        public float tDialogRefreshDelay;

        // Update screen
        public StaticSprite updatingGroup;
        public TextBox updatingTextView;
        public String updatingFormat;
        public Range updatingNumber;
        public float tUpdatingNumberInterval;
        public Clickable updatingCloseButton;

        // Showdown
        public final ObjectMap<String, ShowdownInternal> showdowns = new ObjectMap<>();

        // Stats
        public float tFlapInterval;
        public Animation pipeHitVerticalAnim;
        public Animation pipeHitHorizontalAnim;

        public Graph lightingChangeStartGraph;
        public Graph lightingChangeEndGraph;
        public Graph lightingVoiceGraph;
        public float tLightingEndCooldown;

        // Demon
        public float demonVoiceBgMusicVolume;
        public float demonVoiceSfxVolume;
        public Animation demonVoiceScreenBgStartAnim;
        public Animation demonVoiceScreenBgLoopAnim;
        public Animation demonVoiceScreenBgEndAnim;

        public float gameplayThemeVolume;

        public SetRandomizedSelector<DemonVoice> demonOpenEggshopVoices;
        public SetRandomizedSelector<DemonVoice> demonBuyEggsVoices;
        public SetRandomizedSelector<DemonVoice> demonRejectEggshopVoices;

        public float tDemonHitTauntMinInterval;

        public int cheatMaxPipes;
        public String[] cheatUnlockTags;
        public AppCrashDialog cheatCrashDialog;

        public float gameTimeMultiplier;
    }

    public static class ShowdownInternal {
        // Showdown Giveup
        public StaticSprite giveupGroup;
        public Clickable giveupYesButton;
        public Clickable giveupNoButton;
        public SetRandomizedSelector<DemonVoice> giveupVoice;

        // Showdown dialog
        public StaticSprite showdownGroup;
        public Clickable showdownAcceptButton;
        public Clickable showdownLaterButton;
        public DemonVoice showdownVoice;

        // Subscription
        public StaticSprite subscribeGroup;
        public Clickable subscribeYesButton;
        public Clickable subscribeNoButton;
        public SetRandomizedSelector<DemonVoice> subscribeVoice;
        public DemonVoice subscribedVoice;

        public float tLifehoursDrainInterval;
        public int lifehoursDrainAmount;
        public int lifehoursMinDrainCap;
        public int lifehoursDrainEggs;
    }

    public interface LevelSource {
        Level buildLevel();
    }

    public static class PropType {
        public final Sprite mat;
        public final float scale;

        public PropType(Sprite mat, float scale) {
            this.mat = mat;
            this.scale = scale;
        }
    }

    private static class PipeInstance implements Pool.Poolable {
        static final Pool<PipeInstance> pool = new Pool<PipeInstance>() {
            @Override
            protected PipeInstance newObject() {
                return new PipeInstance();
            }

            @Override
            protected void reset(PipeInstance object) {
                object.reset();
            }
        };

        float x;
        float gapY;
        float gapSize;
        float actualGapY;
        float actualGapSize;
        float bottomY;
        float topY;
        Sprite topPipe;
        Sprite bottomPipe;
        final BoundingBox topBox = new BoundingBox();
        final BoundingBox bottomBox = new BoundingBox();
        boolean scored = false;
        Animation.Handler topPipeAnim;
        Animation.Handler bottomPipeAnim;

        float tStarted;
        Graph gapGraph;
        Graph yGraph;

        @Override
        public void reset() {
            scored = false;
            topPipeAnim = null;
            bottomPipeAnim = null;
            gapGraph = null;
            yGraph = null;
        }
    }

    private static class GroundInstance {
        static final Pool<GroundInstance> pool = new Pool<GroundInstance>() {
            @Override
            protected GroundInstance newObject() {
                return new GroundInstance();
            }

            @Override
            protected void reset(GroundInstance object) {
                // nothing
            }
        };

        float x;
        Sprite ground;
    }

    private static class PropInstance {
        static final Pool<PropInstance> pool = new Pool<PropInstance>() {
            @Override
            protected PropInstance newObject() {
                return new PropInstance();
            }

            @Override
            protected void reset(PropInstance object) {
                // nothing
            }
        };

        float x;
        float y;
        float size;
        float speedX;
        Sprite mat;
        Animation.Loop anim;
        float flipped;
    }

    public static class InviteInfo implements MassSerializable {
        final String profileFilename;
        final String name;
        final String tag;

        @MassConstructor
        public InviteInfo(String profileFilename, String name, String tag) {
            this.profileFilename = profileFilename;
            this.name = name;
            this.tag = tag;
        }

        @Override
        public Object[] mass() {
            return new Object[] { profileFilename, name, tag };
        }
    }

    private final Builder<Object> builder;
    private Internal s;

    public FlapeeAdScreen adScreen;

    // Rendering
    private final LightingCompositorMaterial lightingCompositorMaterial;
    private final Sprite gameplayPlane;
    private final LightingGeneratorMaterial lightingGeneratorMaterial;
    private final Sprite lightingGenerationPlane;
    private final Sprite belowGroundMat;

    private Animation.Loop powerupBirdAnim;
    private Animation.Loop reviveBirdAnim;

    // Menus
    private float tLoadingEndScheduled = Float.MAX_VALUE;
    private float tFindingFriendsEndScheduled = Float.MAX_VALUE;
    private float tSendingEggsEndScheduled = Float.MAX_VALUE;
    private float tNewHighScoreEndScheduled = Float.MAX_VALUE;
    private float tShopSigilScheduled = Float.MAX_VALUE;
    private float tRewardStartScheduled = Float.MAX_VALUE;
    private float tRewardEndScheduled = Float.MAX_VALUE;
    private float tDialogShowScheduled = Float.MAX_VALUE;
    private float tUpdatingNumberScheduled = Float.MAX_VALUE;

    // Dialog menu
    private DialogConfig queuedDialog = null;
    private int dialogStarsSelected = -1;

    // Level and game
    public Class<? extends LevelSource> levelSource;
    private Level level = null;
    private final Array<PipeInstance> pipes = new Array<>(PipeInstance.class);
    private final Array<GroundInstance> grounds = new Array<>(GroundInstance.class);
    private final Array<PropInstance> mountains = new Array<>(PropInstance.class);
    private final Array<PropInstance> trees = new Array<>(PropInstance.class);
    private final Array<PropInstance> bushes = new Array<>(PropInstance.class);
    private final Array<PropInstance> clouds = new Array<>(PropInstance.class);
    private float generatedX = 0;
    private float groundGeneratedX = 0;
    private float mountainGeneratedX = 0;
    private float treeGeneratedX = 0;
    private float bushGeneratedX = 0;
    private float tNextCloudScheduled = -1;
    private Stage stage;
    private int stageIndex;
    private int stageGeneratedRemaining;
    private int stageScoreRemaining;
    private int stageReviveCostIndex;
    private int stagePowerupCostIndex;

    // Bird stats
    private int generatedDirectionRemaining = 0;
    private int levelDirection = 0;
    private float levelDirectionY;
    private float birdY;
    private float birdSafeY;
    private boolean isBirdHovering = false;
    private boolean isBirdAutopilot = false;
    private boolean isBirdReviving = false;

    private final BoundingBox birdBox = new BoundingBox();
    private float birdVelocityY;
    private float tBirdPissVelocityRefreshScheduled = 0;
    private float birdPissVelocity;
    private float tBirdPissStateRefreshScheduled = 0;
    private boolean birdPissState = false;
    private float tBirdPissIntermittentStateRefreshScheduled = 0;
    private boolean birdPissIntermittentState = false;
    private float accumulatedCollisionTime = 0;
    private float tNextFlapAllowed = 0;
    private float tDeathStarted = -1;
    private boolean isAcceptedDeath = false;
    private float tPowerupEndScheduled = Float.MAX_VALUE;
    private float tPowerupChargingStarted = -1;

    private float cameraX = 0;

    // Piss effect
    private PissEffect pissEffect;
    private Stream pissEffectSound;
    private Stream revivingSound;
    private float tPissEffectStarted;


    // Lighting and demon voice
    private float tDemonIdleChatterScheduled = Float.MAX_VALUE;
    private float tLightingChangeStarted = -1;
    private float tLightingEndStarted = -1;
    private VoiceProfile demonVoiceProfile = null;
    private Music demonVoiceTrack = null;
    private DemonVoice demonVoice;
    private boolean demonVoiceStopsLevelGeneration = false;

    private float sfxVolume = 1f;
    private float tLastDemonHitTaunt = -Float.MAX_VALUE;

    // Shop and eggs
    private int shopOfferIndex = 0;

    private int queuedReward = 0;
    private int queuedRewardPurchased = 0;

    // Player stats
    private int score = 0;
    private int totalScore = 0;
    private float hoursPlayed = 0;

    private int eggs = 50;
    private int eggsPurchased = 0;
    private int eggsTransitionCurrent = 50;
    private int eggsTransitionFrom = 50;
    private float tEggsTransitionStarted = -1;

    private int lifehours = 0;
    private int lifehoursTransitionCurrent = 0;
    private int lifehoursTransitionFrom = 0;
    private float tLifehoursTransitionStarted = -1;
    private float tNextLifehoursDrainScheduled = Float.MAX_VALUE;

    private boolean isAdvancedPlayer = false;
    private boolean isEggShopAllowed = false;

    // New sticker status
    private boolean hasWatchedAd = false;
    private boolean hasInvitedFriend = false;
    private boolean hasSpentLifehours = false;

    // Updating gb size
    private float updatingSize = -1;

    // Invites
    private InviteInfo[] inviteInfos = null;
    private final Array<UIElement<?>> inviteButtons = new Array<>(UIElement.class);

    // Scores
    private final Array<LeaderboardScore> leaderboard = new Array<>(LeaderboardScore.class);
    private LeaderboardScore playerScore = null;
    private boolean isLeaderboardRefreshed = false;

    // Showdown
    private ShowdownInternal si;

    // Cheating
    private int cheatingLevel = 0;
    private int cheatingCurrentPipes = 0;

    // Precacher list
    private final Array<Streamable> precacherList = new Array<>(Streamable.class);

    public void resetEggShopIndex() {
        shopOfferIndex = 0;
    }

    public void setEggShopAllowed(boolean isEggShopAllowed) {
        this.isEggShopAllowed = isEggShopAllowed;
    }

    public void startUpdating(float updateSize) {
        updatingSize = updateSize;
    }

    public void stopUpdating() {
        updatingSize = -1;
    }

    public boolean isUsingSubscription() {
        return tNextLifehoursDrainScheduled != Float.MAX_VALUE;
    }

    private void showSubscriptionWindow() {
        // Hide title for this
        s.titleView.detachWithAnim();
        s.royaleTitleView.detachWithAnim();
        // Show
        si.subscribeGroup.attach();
        // Voice
        startDemonVoice(si.subscribeVoice, false);
    }

    public void startShowdown(String name) {
        si = s.showdowns.get(name);
        if(si == null)
            throw new RuntimeException("Unknown showdown: " + name);
    }

    public void clearShowdown() {
        si = null;
        tNextLifehoursDrainScheduled = Float.MAX_VALUE;
    }

    public int getDialogStarsSelected() {
        return dialogStarsSelected;
    }

    public void queueDialog(DialogConfig dialog) {
        queuedDialog = dialog;
    }

    private boolean showDialogMenu() {
        if(queuedDialog == null)
            return false;           // nothing to show

        // Reset
        dialogStarsSelected = -1;

        // Build ui
        s.dialogTextView.autoLengthText(queuedDialog.text);
        if(queuedDialog.leftStarText == null && queuedDialog.rightStarText == null)
            s.dialogStarGroup.detach();     // not required to show stars
        else {
            s.dialogStarGroup.attach();
            for(Clickable starView : s.dialogStarViews)
                starView.visuals(s.dialogStarDeselectedMat);
            s.dialogStartLeftTextView.text(queuedDialog.leftStarText);
            s.dialogStartRightTextView.text(queuedDialog.rightStarText);
        }

        // Buttons
        if(queuedDialog.negativeButtonText == null) {
            // Only a single button
            s.dialogPositiveButton.detach();
            s.dialogNegativeButton.detach();
            s.dialogSingleButton.text(queuedDialog.positiveButtonText).attach();
        }
        else {
            // Has 2 buttons
            s.dialogPositiveButton.text(queuedDialog.positiveButtonText).attach();
            s.dialogNegativeButton.text(queuedDialog.negativeButtonText).attach();
            s.dialogSingleButton.detach();
        }

        // Measure
        float length = s.dialogGroup.autoLength().getLength();

        s.dialogContainer.minSize(1f, length + s.dialogLengthPadding).refresh();

        s.dialogContainer.attach();

        return true;
    }

    private void stopPissEffectSound() {
        if(pissEffectSound != null) {
            pissEffectSound.stop();
            pissEffectSound = null;
        }
        tPissEffectStarted = Float.MAX_VALUE;
    }

    public void stopDemonVoice() {
        if(demonVoiceTrack == null)
            return;
        Globals.grid.notification.stopSubtitle(demonVoice.voiceFilename);
        demonVoiceTrack.dispose();
        demonVoiceTrack = null;
        demonVoice = null;
        if(tLightingChangeStarted != -1 && tLightingEndStarted == Float.MAX_VALUE)
            tLightingEndStarted = getRenderTime() + s.tLightingEndCooldown;
    }

    public VoiceProfile getDemonVoiceProfile() {
        return demonVoiceProfile;
    }

    public boolean startDemonVoice(SetSelector<DemonVoice> chatter, boolean stopLevelGeneration) {
        DemonVoice voice = DemonVoice.select(chatter);
        return voice != null && startDemonVoice(voice, stopLevelGeneration);
    }

    public boolean startDemonVoice(DemonVoice voice, boolean stopLevelGeneration) {
        if(voice == null)
            return false;
        // Check if can repeat voice
        if(voice.doNotRepeat && voice.hasPlayed())
            return false;       // voice cannot be repeated, and was played before
        if(demonVoiceTrack != null) {
            if(!voice.interrupt)
                return false;     // not allowed to interrupt
            demonVoiceTrack.dispose();      // Else can interrupt
        }
        // Else allowed
        // Trigger
        Globals.grid.trigger(Globals.TRIGGER_FLAPEE_DEMON_SPEAKS);
        demonVoice = voice;
        demonVoiceStopsLevelGeneration = stopLevelGeneration;
        demonVoiceTrack = Gdx.audio.newMusic(File.open(voice.voiceFilename));
        demonVoiceProfile = VoiceProfile.load(voice.voiceFilename);
        demonVoiceTrack.play();

        // Subtitles
        Globals.grid.notification.startSubtitle(demonVoice.voiceFilename, demonVoiceTrack);

        sfxVolume = s.demonVoiceSfxVolume;
        if(Audio.musicVolume > 0f)
            Audio.setMusicVolume((Audio.getMusicVolume() / Audio.musicVolume) * s.demonVoiceBgMusicVolume);
        Globals.grid.screen.animateBackground(s.demonVoiceScreenBgStartAnim, s.demonVoiceScreenBgLoopAnim, false);
        return true;
    }

    private void stopRevivingSound() {
        if(revivingSound != null) {
            revivingSound.stop();
            revivingSound = null;
        }
    }

    public void setPlayerScore(int score) {
        totalScore = score;
        isLeaderboardRefreshed = false;
    }

    public int getCurrentScore() {
        return score;
    }

    public int getPlayerScore() {
        return totalScore;
    }

    public int getOpponentScore(String name) {
        for(LeaderboardScore e : leaderboard) {
            if(e.name.equals(name))
                return e.score;
        }
        return -1;      // not found
    }

    public int getLeaderboardHighestScore() {
        return leaderboard.items[0].score;
    }

    public int getLeaderboardLength() {
        return leaderboard.size;
    }

    public int getPlayerRank() {
        refreshLeaderboard();
        return leaderboard.indexOf(playerScore, true);
    }

    private void refreshLeaderboard() {
        if(isLeaderboardRefreshed)
            return;     // already valid
        isLeaderboardRefreshed = true;

        // Basic score
        s.basicScoreView.text(Integer.toString(totalScore));

        // Update player score
        if(playerScore != null)
            leaderboard.removeValue(playerScore, true);
        playerScore = new LeaderboardScore(s.scorePlayerName, totalScore, hoursPlayed + (getRenderTime() / 3600f), true);
        if(isAdvancedPlayer)
            leaderboard.add(playerScore);
        // Sort positions
        for(int start = 0; start < leaderboard.size; start++) {
            int bestIndex = -1;
            int bestScore = Integer.MIN_VALUE;
            for(int c = start; c < leaderboard.size; c++) {
                LeaderboardScore current = leaderboard.items[c];
                if(current.score > bestScore) {
                    bestScore = current.score;
                    bestIndex = c;
                }
            }
            // Move this up if needed
            if(bestIndex != start) {
                LeaderboardScore select = leaderboard.removeIndex(bestIndex);
                leaderboard.insert(start, select);
            }
        }

        // Update friends list
        boolean isPlayerScoreShown = false;
        for(int r = 0, c = 0; r < s.friendRows.length; r++) {
            boolean isLastRow = r == s.friendRows.length - 1;
            LeaderboardScore select = null;
            if(isLastRow && !isPlayerScoreShown)
                select = playerScore;           // always show player in last row if not yet
            else {
                // Find next candiate
                for(; c < leaderboard.size; c++) {
                    LeaderboardScore current = leaderboard.items[c];
                    if(current.isFriend) {
                        select = current;
                        c++;
                        break;
                    }
                }
            }

            if(select == null) {
                // No more rows to show
                for(; r < s.friendRows.length; r++) {
                    s.friendRows[r].find(s.friendRowNameView).text("");
                    s.friendRows[r].find(s.friendRowScoreView).text("");
                    s.friendRows[r].visual(null).length(s.friendsRowPlayer.length);
                }
                break;
            }
            else if(select == playerScore) {
                isPlayerScoreShown = true;
                s.friendRows[r].visual(s.friendsRowPlayer);
            }
            else {
                s.friendRows[r].visual(null).length(s.friendsRowPlayer.length);
            }
            // Show
            s.friendRows[r].find(s.friendRowNameView).text(select.name);
            s.friendRows[r].find(s.friendRowScoreView).text(Integer.toString(select.score));
        }

        // Update high scores list
        for(int r = 0; r < s.scoreRows.length; r++) {
            if(r >= leaderboard.size) {
                // No more scores to show, clear rest of the rows
                for(; r < s.scoreRows.length; r++) {
                    s.scoreRows[r].find(s.scoreRowNameView).text("");
                    s.scoreRows[r].find(s.scoreRowHoursPlayedView).text("");
                    s.scoreRows[r].find(s.scoreRowScoreView).text("");
                }
                break;
            }
            LeaderboardScore current = leaderboard.items[r];
            Font font;
            if(current == playerScore) {
                font = s.scorePlayerFont;
                s.scoreRows[r].visual(s.scoreRowPlayer);
            }
            else {
                font = s.scoreNormalFont;
                s.scoreRows[r].visual(null).length(s.scoreRowPlayer.length);
            }
            s.scoreRows[r].find(s.scoreRowNameView).text().font(font).text(current.name);
            s.scoreRows[r].find(s.scoreRowHoursPlayedView).text(String.format(Locale.US, s.scoreRowHoursPlayedFormat, current.hoursPlayed));
            s.scoreRows[r].find(s.scoreRowScoreView).text().font(font).text(Integer.toString(current.score));
        }
    }


    public void setAdvancedPlayer(boolean isAdvancedPlayer) {
        this.isAdvancedPlayer = isAdvancedPlayer;
    }

    public void configureInvites(InviteInfo[] invites) {
        inviteInfos = invites;
    }

    private boolean consumeEggs(int amount) {
        if(eggs < amount) {
            // Not enough
            s.eggsView.windowAnimation(s.eggsNotEnoughAnim.startAndReset(), true, false);
            s.eggsNotEnoughSound.play(sfxVolume);
            return false;
        }
        boolean isAbleToShop = eggsPurchased < s.shopPurchasedThreshold;
        // Else consume
        eggs -= amount;
        eggsPurchased -= amount;
        if(eggsPurchased < 0)
            eggsPurchased = 0;
        if(eggsPurchased < s.shopPurchasedThreshold && !isAbleToShop)
            hasSpentLifehours = false;
        eggsTransitionFrom = eggsTransitionCurrent;
        tEggsTransitionStarted = getRenderTime();
        return true;
    }

    private boolean consumeLifehours(int amount) {
        if(lifehours < amount)
            return false;
        // Else consume
        lifehours -= amount;
        lifehoursTransitionFrom = lifehoursTransitionCurrent;
        tLifehoursTransitionStarted = getRenderTime();
        return true;
    }

    public int getEggs() {
        return eggs;
    }

    public int getEggsPurchased() {
        return eggsPurchased;
    }

    public void setEggs(int amount) {
        eggsTransitionCurrent = eggsTransitionFrom = eggs = amount;
        if(eggsPurchased > eggs)
            eggsPurchased = eggs;
        tEggsTransitionStarted = -1;
        s.eggsView.text(Integer.toString(eggs));
    }

    public void setLifehours(int amount) {
        lifehoursTransitionCurrent = lifehoursTransitionFrom = lifehours = amount;
        tLifehoursTransitionStarted = -1;
        s.lifehoursView.text(Integer.toString(lifehours));
    }

    public int getLifehours() {
        return lifehours;
    }

    public void queueReward(int reward) {
        queueReward(reward, 0);
    }

    public void queueReward(int reward, int purchased) {
        queuedReward += reward;
        queuedRewardPurchased += purchased;
    }

    public int queuedReward() {
        return queuedReward;
    }

    public boolean showRewardMenu() {
        if(queuedReward == 0)
            return false;
        if(tRewardStartScheduled != Float.MAX_VALUE)
            return true;        // already showing
        s.inputBlockerView.attach();
        if(queuedReward > 0)
            s.rewardTextView.text("+" + queuedReward).detach();
        else
            s.rewardTextView.text(Integer.toString(queuedReward)).detach();
        s.rewardGroup.attach();
        s.rewardVisualView.visual(s.rewardVisualClosed).windowAnimation(s.rewardIdleAnim.loopAndReset(), true, true);
        s.rewardAcceptButton.attach();
        s.rewardAcceptText.attach();

        tRewardStartScheduled = getRenderTime() + s.tRewardStartDelay;
        tRewardEndScheduled = Float.MAX_VALUE;

        // Sound
        s.rewardAppearSound.play();

        // Show eggs if havent
        if(!s.eggsView.isAttached())
            s.eggsView.attach();

        // Hide menu
        s.menuGroup.detach();
        s.moreEggsGroup.detach();
        return true;
    }

    private void showInviteMenu() {
        inviteButtons.clear();

        if(inviteInfos == null)
            return;     // no invites

        // Else populate rows
        for(int c = 0; c < s.inviteRows.length; c++) {
            UIElement.Group row = s.inviteRows[c];

            if(c >= inviteInfos.length)
                row.detach();       // extra rows, detach
            else {
                InviteInfo info = inviteInfos[c];
                // Fill
                row.attach();
                row.find(s.inviteRowNameView).text(info.name);
                row.find(s.inviteRowProfileView).visual(Sprite.load(info.profileFilename));
                if(Globals.grid.isStateUnlocked(info.tag)) {
                    // Already sent invite
                    row.find(s.inviteRowSendButton).windowAnimation(ScaleAnim.gone.startAndReset(), true, true);
                    row.find(s.inviteRowSentButton).windowAnimation(null, false, false);
                    inviteButtons.add(null);
                }
                else {
                    // Haven't sent an invite yet
                    Clickable button = row.find(s.inviteRowSendButton).windowAnimation(null, false, false);
                    row.find(s.inviteRowSentButton).windowAnimation(ScaleAnim.gone.startAndReset(), true, true);
                    inviteButtons.add(button);
                }
            }
        }

        s.inviteGroup.attach();
    }

    public void showMenu(boolean playTheme) {
        // Trigger
        Globals.grid.trigger(Globals.TRIGGER_FLAPEE_SHOW_MENU);

        // Update friends score
        refreshLeaderboard();

        if(isAdvancedPlayer) {
            if(!s.gameOverView.isAttached() && !s.royaleTitleView.isAttached())
                s.royaleTitleView.attach();

            s.titleView.detach();
            s.menuGroup.attach();
            s.moreEggsGroup.attach();
            if(inviteInfos != null) {
                s.moreEggsAdAndInviteGroup.attach();
                s.moreEggsAdOnlyGroup.detach();
                if(!hasWatchedAd)
                    s.moreEggsWatchAdNewSticker.viewport(s.moreEggsWatchAdButton).attach();
                else
                    s.moreEggsWatchAdNewSticker.detach();
                if(!hasInvitedFriend)
                    s.moreEggsInviteNewSticker.attach();
                else
                    s.moreEggsInviteNewSticker.detach();
            }
            else {
                s.moreEggsAdAndInviteGroup.detach();
                s.moreEggsAdOnlyGroup.attach();
                if(!hasWatchedAd)
                    s.moreEggsWatchAdNewSticker.viewport(s.moreEggsWatchAdSingleButton).attach();
                else
                    s.moreEggsWatchAdNewSticker.detach();
            }
            if(isEggShopAllowed && eggsPurchased < s.shopPurchasedThreshold)
                s.moreEggsShopButton.attach();
            else
                s.moreEggsShopButton.detach();

            s.basicGroup.detach();

            if(!hasSpentLifehours || si != null)
                s.moreEggsShopNewSticker.attach();
            else
                s.moreEggsShopNewSticker.detach();

            // Show eggs
            s.eggsView.attach();
        }
        else {
            if(!s.gameOverView.isAttached() && !s.titleView.isAttached())
                s.titleView.attach();

            s.royaleTitleView.detach();
            s.basicGroup.attach();
            s.menuGroup.detach();
            s.moreEggsGroup.detach();

            s.eggsView.detach();
        }

        // Show navbar if hidden
        if(s.bars.navbar().windowAnim != null && s.bars.navbar().windowAnim.anim == s.barsHideAnim)
            s.bars.navbar().windowAnimation(s.barsShowAnim.startAndReset(), true, false);

        // Play theme
        if(playTheme) {
            Audio.playMusic(level.themeMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);
        }

        // Reset charging
        tPowerupChargingStarted = -1;
        s.ingamePowerupChargingView.detach();
        s.ingamePowerupButton.enable();
    }

    private void flap() {
        if(getRenderTime() < tNextFlapAllowed)
            return;     // too soon
        tNextFlapAllowed = getRenderTime() + s.tFlapInterval;
        birdVelocityY = level.birdFlapY;

        birdPissIntermittentState = false;

        level.flapSound.play(sfxVolume);
    }

    private void die(boolean isHitGround) {
        if(tDeathStarted != -1)
            return;
        tDeathStarted = getRenderTime();
        isAcceptedDeath = false;
        if(birdVelocityY > 0f)
            birdVelocityY = -birdVelocityY;
        else
            birdVelocityY = level.birdDeathVelocityY;

        tBirdPissVelocityRefreshScheduled = Float.MAX_VALUE;
        birdPissVelocity = level.birdDeathPissVelocity;

        s.tapView.detach();     // stop response
        s.readyView.detach();       // Detach cuz might die before ready
        s.ingamePowerupButton.detachWithAnim();

        // Give chance if advanced
        if(isAdvancedPlayer) {
            s.chanceMenu.attach();

            if(tNextLifehoursDrainScheduled != Float.MAX_VALUE) {
                // If on life drain, don't need to show die button
                s.chanceDieButton.detach();
                s.chanceReviveTimerView.detach();
            }
            else {
                // Else normal death, show die and revive timer
                s.chanceDieButton.attach();
                s.chanceReviveTimerView.attach();
                s.chanceReviveTimerView.windowAnimation(s.chanceReviveTimerAnim.startAndReset(), false, true);
            }
            // Die voice
            if((getRenderTime() - tLastDemonHitTaunt) > s.tDemonHitTauntMinInterval) {
                startDemonVoice(DemonVoice.select(stage.demonPlayerHitChatter), false);
                tLastDemonHitTaunt = getRenderTime();
            }
            // Reviving sound
            if(revivingSound == null)
                revivingSound = level.revivingSound.loop();
        }
        else
            s.gameOverView.attach();

        s.dieSplash.attach();
        if(isHitGround)
            level.hitGroundSound.play(sfxVolume);
        else
            level.hitPipeSound.play(sfxVolume);

        Audio.pauseMusic();

        // Achievement
        ACT1.unlockAchievement(Globals.Achievement.DIE_IN_FB);
    }

    private void increaseScore() {
        if(score == 0)
            s.scoreView.attach();

        if(isAdvancedPlayer)
            Globals.grid.trigger(Globals.TRIGGER_FLAPEE_SCORED_POINT);      // trigger

        score++;

        s.scoreView.text(Integer.toString(score)).windowAnimation(s.scoredAnim.startAndReset(), true, false);
        level.scoreSound.play(sfxVolume);
        // Check if reached #1
        if(isAdvancedPlayer && score > leaderboard.items[0].score) {
            // Start autopilot and win game
            isBirdAutopilot = true;

            // Analytics
            Game.analyticsEndLevel(levelSource.getSimpleName(), score, true);

            // Stop idle chatter
            tDemonIdleChatterScheduled = Float.MAX_VALUE;

            // Cancel powerup
            tPowerupEndScheduled = Float.MAX_VALUE;
            timeMultiplier = 1f;
            s.ingamePowerupButton.detachWithAnim();

            // Show royale win
            s.newHighScoreSound.play(sfxVolume);
            s.newHighScoreView.attach();
            s.rankingRoyaleView.attach();
            tNewHighScoreEndScheduled = getRenderTime() + s.tNewHighScoreRoyaleTime;
            Audio.playMusic(level.themeMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);

            // Stop any demon voice
            stopDemonVoice();

            // Add excess as reward TODO: parameterize and balancing
            queueReward(score - totalScore);

            // Ranking
            totalScore = score;
            isLeaderboardRefreshed = false;         // updated player score

            // Trigger
            Globals.grid.trigger(Globals.TRIGGER_FLAPEE_NEW_HIGH_SCORE);        // return ignored

            if(tNextLifehoursDrainScheduled != Float.MAX_VALUE) {
                // Drain all the way
                consumeLifehours(lifehours);
            }
            return;
        }

        // Else check if stage has ended
        stageScoreRemaining--;
        if(stageScoreRemaining <= 0 && stageIndex < (level.stages.length - 1)) {
            // Change stage
            stageIndex++;
            if(stageIndex >= level.stages.length)
                stageIndex = level.stages.length - 1;                       // Reached end of level, no more stages left
            prepareStage(stageIndex);
            generatedX = -cameraX + 1f + level.stages[stageIndex].stageStartX;
            // Reschedule idle chatter
            tDemonIdleChatterScheduled = getRenderTime() + stage.tDemonIdleChatterDelay.generate();
        }
    }

    public void startBirdHovering() {
        isBirdHovering = true;

        // Inform to start
        s.tutorialView.attach();

        // Remove powerup button
        s.ingamePowerupButton.detachWithAnim();
    }

    private void gameStep() {
        // Game logic


        // Powerup
        if(getRenderTime() > tPowerupEndScheduled) {
            // Skip generating pipes
            generatedX = -cameraX + level.pipeStartX;

            // Wait till there are no more pipes in the screen
            if(pipes.isEmpty()) {
                // Ended
                tPowerupEndScheduled = Float.MAX_VALUE;

                // Return back to normal
                timeMultiplier = 1f;
                isBirdAutopilot = false;
                isBirdHovering = true;

                // Inform to start
                s.tutorialView.attach();

                // Gameplay music
                Audio.playMusic(level.gameplayMusic, true, (sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume) * s.gameplayThemeVolume);
            }
        }

        if(tPowerupChargingStarted != -1) {
            float elapsed = getRenderTime() - tPowerupChargingStarted;
            if(elapsed > level.powerupChargingTime) {
                tPowerupChargingStarted = -1;
                // Indicate charged
                s.ingamePowerupChargingView.detachWithAnim();
                s.ingamePowerupChargingView.windowAnimation(null, false, false);
                s.ingamePowerupAvailableView.attach();
                s.ingamePowerupButton.enable();
            }
            else {
                // Animate
                s.ingamePowerupChargingView.windowAnim.setProgress(elapsed / level.powerupChargingTime);
            }
        }

        // Physics & autopilot
        if(!isBirdHovering) {
            birdVelocityY -= getRenderDeltaTime() * level.birdGravityY;
            birdY += birdVelocityY * getRenderDeltaTime();

            if (birdY < level.groundY && !isBirdAutopilot) {
                birdY = level.groundY;
                die(true);
            }

            // Stop cheating if not allowed
            if((si != null || cheatingLevel == -1) && (birdY > (Globals.LENGTH / 2f)))
                birdY = Globals.LENGTH / 2f;

            if(isBirdAutopilot) {
                // sy - gy*t = 0
                // t = sy/gy
                // y = sy * (sy / gy) / 2
                // y = sy2 / 2gy

                float flapHeight = (level.birdFlapY * level.birdFlapY) / (2 * level.birdGravityY);

                // find minY
                float y = 0;
                float bestX = Float.MAX_VALUE;
                float minX = level.birdX - (level.birdSize / 2f) - (level.pipeSize / 2f);
                float pipeHalfLength = level.pipeSize * stage.pipes.set[0].length / 2f;
                float birdHalfLength = level.birdSize / 2f;

                flapHeight += birdHalfLength;

                for(int c = 0; c < pipes.size; c++) {
                    PipeInstance pipe = pipes.items[c];
                    if(pipe.x < bestX && pipe.x > (-cameraX + minX)) {
                        bestX = pipe.x;
                        float gapY = (pipe.topY - pipeHalfLength) - (pipe.bottomY + pipeHalfLength);
                        if(gapY < flapHeight) {
                            Sys.error(TAG, "Insufficient flap space " + gapY + ", required " + flapHeight);
                            gapY = 0;
                        }
                        else
                            gapY = (gapY - flapHeight) / 2f;

                        y = pipe.bottomY + level.groundY + pipeHalfLength + birdHalfLength + gapY;
                    }
                }
                if(birdY < y) {
                    // Flap up
                    flap();
                }
            }
        }
        else        // waiting for first tap
            generatedX = -cameraX + level.pipeStartX;

        // Death
        if(tDeathStarted == -1)
            cameraX += getRenderDeltaTime() * -level.speedX;

        // Revival
        if(isBirdReviving) {
            if(!pipes.isEmpty()) {
                // There are still pipes, stop generating and allow autopilot to clear those
                generatedX = -cameraX + level.pipeStartX;
            }
            else if(birdY < 0) {
                // Else there are no pipes, but bird is too low, stop generating and flap bird up
                generatedX = -cameraX + level.pipeStartX;
                flap();
            }
            else {
                // No more pipes, handover control
                isBirdHovering = true;
                isBirdAutopilot = false;
                isBirdReviving = false;
                birdVelocityY = 0;
                timeMultiplier = 1f;

                // Tutorial
                s.tutorialView.attach();

                // Allow input
                s.tapView.attach();
            }
        }

        // Check if demon voice stops level generation
        if(tDemonIdleChatterScheduled != Float.MAX_VALUE || (demonVoice != null && demonVoiceStopsLevelGeneration))
            generatedX = -cameraX + level.pipeStartX;

        // Piss effect
        SetDistributedSelector<Float> velocitySelector;
//        if(timeMultiplier == level.powerupSpeedMultiplier)
        if(tPowerupEndScheduled != Float.MAX_VALUE)
            velocitySelector = level.birdPowerupPissVelocity;
        else
            velocitySelector = level.birdPissVelocity;
        if(getRenderTime() >= tBirdPissVelocityRefreshScheduled) {
            int index = velocitySelector.selectIndex();
            tBirdPissVelocityRefreshScheduled = getRenderTime() + velocitySelector.distribution[index];
            birdPissVelocity = velocitySelector.set[index];
        }
        if(getRenderTime() >= tBirdPissStateRefreshScheduled) {
            birdPissState = !birdPissState;
            if(birdPissState)
                tBirdPissStateRefreshScheduled = getRenderTime() + level.birdPissActiveTime.generate();
            else
                tBirdPissStateRefreshScheduled = getRenderTime() + level.birdPissInactiveTime.generate();
        }
        if(getRenderTime() >= tBirdPissIntermittentStateRefreshScheduled) {
            birdPissIntermittentState = !birdPissIntermittentState;
            if(birdPissIntermittentState)
                tBirdPissIntermittentStateRefreshScheduled = getRenderTime() + level.birdPissIntermittentActiveTime.generate();
            else
                tBirdPissIntermittentStateRefreshScheduled = getRenderTime() + level.birdPissIntermittentTime.generate();
        }


        float angle;
        boolean isDead = false;
        if(tDeathStarted == -1) {
            if(birdVelocityY < level.birdRotateMaxVelocityY)
                angle = level.birdRotateMax + ((birdVelocityY - level.birdRotateMaxVelocityY) * level.birdRotateAngle);
            else
                angle = level.birdRotateMax;
            angle += level.birdPissAngle;
        }
        else {
            float elapsed = getRenderTime() - tDeathStarted;
            if(elapsed > level.birdDeathRotateGraph.getLength()) {
                angle = -level.birdDeathRotateGraph.getEnd();
                isDead = true;
            }
            else
                angle = -level.birdDeathRotateGraph.generate(elapsed);
            birdPissState = true;       // enable on death
        }
        float volume;
        if(birdPissState && birdPissIntermittentState)
            volume = level.birdPissVolume.select() + level.birdPissVolumeModifier.generate(getRenderTime());
        else
            volume = 0;
        boolean isPissBelowGround = pissEffect.updatePosition(
                getRenderTime(),
                getRenderDeltaTime(),
                -cameraX + level.birdPissX,
                birdY + level.birdPissY,
                angle,
                birdPissVelocity + level.birdPissVelocityModifier.generate(getRenderTime()),
                volume,
//                level.birdGravityY,
                level.groundY,
                isDead
        );
        if(isPissBelowGround && tDeathStarted == -1) {
            if(pissEffectSound == null)
                pissEffectSound = level.pissEffectSound.loop(sfxVolume);
            tPissEffectStarted = getRenderTime();
        }

        if(getRenderTime() > tPissEffectStarted) {
            float elapsed = getRenderTime() - tPissEffectStarted;
            if(elapsed > level.tPissEffectTimeout) {
                elapsed -= level.tPissEffectTimeout;
                if(elapsed > level.tPissEffectFade)
                    stopPissEffectSound();
                else
                    pissEffectSound.volume((1f - (elapsed / level.tPissEffectFade)) * sfxVolume);
            }
        }


        // Clear pipes to the left
        float cameraLeftX = -cameraX - 0.5f - (level.pipeSize / 2f);
        for(int c = 0; c < pipes.size; c++) {
            PipeInstance pipe = pipes.items[c];
            if(pipe.x < cameraLeftX) {
                // Remove this pipe as its past left
                if(pipes.size == 1) {
                    PipeInstance.pool.freeAll(pipes);
                    pipes.clear();
                    break;
                }
                // Replace with last
                PipeInstance.pool.free(pipes.items[c]);
                pipes.items[c] = pipes.items[pipes.size - 1];
                pipes.size--;
            }
        }

        // Check if generated enough
        float pipeHalfSize = (level.pipeSize / 2f) - stage.pipeBoundingXAdjust;
        float pipeHalfLength = level.pipeSize * stage.pipes.set[0].length / 2f;
        // Stop generating if stage has ended
        if(stageGeneratedRemaining <= 0 && stageIndex < (level.stages.length - 1))
            generatedX = -cameraX + 1f + level.stages[stageIndex + 1].stageStartX;
        else {
            while ((-cameraX + 1f) >= (generatedX - (level.pipeSize / 2f))) {
                // Check direction change
                if (generatedDirectionRemaining <= 0) {
                    levelDirection = -levelDirection;
                    generatedDirectionRemaining = stage.pipeDirectionAmount.generateInt();
                }

                float y = levelDirectionY + stage.pipeMinYOffsetRange.generate() + (stage.pipeYOffsetRange.generate() * levelDirection);
                float gap = stage.pipeGapRange.generate();
                float gapTopY = y + (gap / 2f);
                float gapBottomY = y - (gap / 2f);

                if (gapTopY > stage.pipeMaxY)
                    y -= gapTopY - stage.pipeMaxY;
                else if (gapBottomY < stage.pipeMinY)
                    y += stage.pipeMinY - gapBottomY;

                generatedX += stage.pipeIntervalDistance.generate();
                levelDirectionY = y;
                generatedDirectionRemaining--;
                stageGeneratedRemaining--;

                PipeInstance pipe = PipeInstance.pool.obtain();
                pipe.x = generatedX;
                pipe.actualGapSize = pipe.gapSize = gap;
                pipe.actualGapY = pipe.gapY = y;
                pipe.topPipe = stage.pipes.select();
                pipe.bottomPipe = stage.pipes.select();

                pipe.tStarted = getRenderTime();
                if (stage.pipeGapGraphs != null)
                    pipe.gapGraph = stage.pipeGapGraphs.select();
                if (stage.pipeYGraphs != null)
                    pipe.yGraph = stage.pipeYGraphs.select();

                pipes.add(pipe);
            }
        }

        // Process pipe movements TODO: autopilot happens before pipe movement
        for(int c = 0; c < pipes.size; c++) {
            PipeInstance pipe = pipes.items[c];
            float elapsed = getRenderTime() - pipe.tStarted;

            if(pipe.gapGraph != null)
                pipe.gapSize = pipe.actualGapSize * pipe.gapGraph.generate(elapsed);
            if(pipe.yGraph != null)
                pipe.gapY = pipe.actualGapY + pipe.yGraph.generate(elapsed);

            // Clamp gap position to stage bounds
            float gapTopY = pipe.gapY + (pipe.gapSize / 2f);
            float gapBottomY = pipe.gapY - (pipe.gapSize / 2f);
            if(gapTopY > stage.pipeMaxY)
                pipe.gapY -= gapTopY - stage.pipeMaxY;
            else if(gapBottomY < stage.pipeMinY)
                pipe.gapY += stage.pipeMinY - gapBottomY;

            // Set bounds
            pipe.topY = pipe.gapY + (pipe.gapSize / 2f) + pipeHalfLength;
            pipe.bottomY = pipe.gapY - (pipe.gapSize / 2f) - pipeHalfLength;
            vec1.set(pipe.x - pipeHalfSize, pipe.topY + level.groundY - pipeHalfLength, 0);
            vec2.set(pipe.x + pipeHalfSize, pipe.topY + level.groundY + pipeHalfLength, 0);
            pipe.topBox.set(vec1, vec2);
            vec1.set(pipe.x - pipeHalfSize, pipe.bottomY + level.groundY - pipeHalfLength, 0);
            vec2.set(pipe.x + pipeHalfSize, pipe.bottomY + level.groundY + pipeHalfLength, 0);
            pipe.bottomBox.set(vec1, vec2);
        }

        // Check collisions (only if not dead or in autopilot, OR cheating not allowed or bird is within screen)
        if(tDeathStarted == -1 && !isBirdAutopilot && (cheatingLevel == -1 || birdY <= (Globals.LENGTH / 2f))) {
            boolean hasCollided = false;
            float birdHalfSize = level.birdSize / 2f;
            float birdHalfLength = level.birdSize * level.bird.length / 2f;
            vec1.set(level.birdX - cameraX - birdHalfSize, birdY - birdHalfLength, 0);
            vec2.set(level.birdX - cameraX + birdHalfSize, birdY + birdHalfLength, 0);
            birdBox.set(vec1, vec2);
            for (int c = 0; c < pipes.size; c++) {
                PipeInstance pipe = pipes.items[c];
                boolean collidedTop = pipe.topBox.intersects(birdBox);
                boolean collidedBottom = pipe.bottomBox.intersects(birdBox);
                if (collidedTop || collidedBottom) {
                    hasCollided = true;
                    accumulatedCollisionTime += getRenderDeltaTime();
                    if (accumulatedCollisionTime > level.collisionMaxTime) {
                        // Collide
                        die(false);
                        // Animate
                        if (collidedTop) {
                            if (pipe.topBox.min.y > birdSafeY)
                                pipe.topPipeAnim = s.pipeHitVerticalAnim.startAndReset();
                            else
                                pipe.topPipeAnim = s.pipeHitHorizontalAnim.startAndReset();
                        } else { // if (collidedBottom) {
                            if (pipe.bottomBox.max.y < birdSafeY)
                                pipe.bottomPipeAnim = s.pipeHitVerticalAnim.startAndReset();
                            else
                                pipe.bottomPipeAnim = s.pipeHitHorizontalAnim.startAndReset();
                        }
                    }
                    break;  // no need to check anymore
                }
            }

            // Reset timer if haven't collided
            if(!hasCollided) {
                accumulatedCollisionTime = 0;
                birdSafeY = birdY;
            }
        }

        // Check score
        if(!isBirdAutopilot || tPowerupEndScheduled != Float.MAX_VALUE) {
            float birdX = level.birdX - cameraX;
            for (int c = 0; c < pipes.size; c++) {
                PipeInstance pipe = pipes.items[c];
                if(pipe.x < birdX && !pipe.scored) {
                    pipe.scored = true;
                    increaseScore();

                    // Check if cheating
                    if(birdY > (Globals.LENGTH / 2f)) {
                        // Yes we're cheating
                        cheatingCurrentPipes++;
                        if(cheatingCurrentPipes >= s.cheatMaxPipes) {
                            // Unlock content
                            Globals.grid.unlockState(s.cheatUnlockTags[cheatingLevel]);
                            cheatingLevel++;
                            if(cheatingLevel >= s.cheatUnlockTags.length) {
                                cheatingLevel = -1;     // no more cheating allowed

                                // Achievement
                                ACT1.unlockAchievement(Globals.Achievement.CHEATED_FB);
                            }
                            // Crash
                            Globals.grid.homescreen.transitionBack(this, Globals.grid);
                            s.cheatCrashDialog.open();
                        }
                    }

                    break;
                }
            }
        }


        // Clear ground to the left
        cameraLeftX = -cameraX - 0.5f - 0.5f;
        for(int c = 0; c < grounds.size; c++) {
            GroundInstance ground = grounds.items[c];
            if(ground.x < cameraLeftX) {
                // Remove this pipe as its past left
                if(grounds.size == 1) {
                    GroundInstance.pool.freeAll(grounds);
                    grounds.clear();
                    break;
                }
                GroundInstance.pool.free(grounds.removeIndex(c));
                c--;
            }
        }
        // Generate enough ground
        while((-cameraX + 0.5f) >= (groundGeneratedX - 0.5f)) {
            GroundInstance ground = GroundInstance.pool.obtain();
            ground.x = groundGeneratedX;
            ground.ground = level.grounds.select();
            groundGeneratedX += 1f;
            grounds.add(ground);
        }

        // Mountain generation
        float offsetZ = -cameraX * level.mountainZ;
        for(int c = 0; c < mountains.size; c++) {
            PropInstance prop = mountains.items[c];
            if((prop.x + offsetZ) < cameraLeftX) {
                if(mountains.size == 1) {
                    PropInstance.pool.freeAll(mountains);
                    mountains.clear();
                    break;
                }
                PropInstance.pool.free(mountains.removeIndex(c));
                c--;
            }
        }
        while((-cameraX + 0.5f) >= (mountainGeneratedX - 0.5f + offsetZ)) {
            PropInstance prop = PropInstance.pool.obtain();
            PropType type = level.mountains.select();
            prop.mat = type.mat;
            prop.size = level.mountainSize.generate() * type.scale;
            prop.x = mountainGeneratedX;
            prop.y = prop.mat.length * prop.size / 2f;
            prop.flipped = Math.random() < 0.5f ? +1 : -1;
            mountainGeneratedX += prop.size + level.mountainIntervalX.generate();
            mountains.add(prop);
        }

        // Tree generation
        offsetZ = -cameraX * level.treeZ;
        for(int c = 0; c < trees.size; c++) {
            PropInstance prop = trees.items[c];
            if((prop.x + offsetZ) < cameraLeftX) {
                if(trees.size == 1) {
                    PropInstance.pool.freeAll(trees);
                    trees.clear();
                    break;
                }
                PropInstance.pool.free(trees.removeIndex(c));
                c--;
            }
        }
        while((-cameraX + 0.5f) >= (treeGeneratedX - 0.5f + offsetZ)) {
            PropInstance prop = PropInstance.pool.obtain();
            PropType type = level.trees.select();
            prop.mat = type.mat;
            prop.anim = level.treeAnims.select().loopAndReset();
            if(level.treeAnimSyncX == 0)
                prop.anim.setProgress((float) Math.random());
            else {
                prop.anim.setProgress(((getRenderTime() + treeGeneratedX * level.treeAnimSyncX) % prop.anim.getLength()) / prop.anim.getLength());
            }
            prop.size = level.treeSize.generate() * type.scale;
            prop.x = treeGeneratedX;
            prop.y = prop.mat.length * prop.size / 2f;
            prop.flipped = Math.random() < 0.5f ? +1 : -1;
            treeGeneratedX += prop.size + level.treeIntervalX.generate();
            trees.add(prop);
        }


        // Bush generation
        offsetZ = -cameraX * level.bushZ;
        for(int c = 0; c < bushes.size; c++) {
            PropInstance prop = bushes.items[c];
            if((prop.x + offsetZ) < cameraLeftX) {
                if(bushes.size == 1) {
                    PropInstance.pool.freeAll(bushes);
                    bushes.clear();
                    break;
                }
                PropInstance.pool.free(bushes.removeIndex(c));
                c--;
            }
        }
        while((-cameraX + 0.5f) >= (bushGeneratedX- 0.5f + offsetZ)) {
            PropInstance prop = PropInstance.pool.obtain();
            PropType type = level.bushes.select();
            prop.mat = type.mat;
            prop.anim = level.bushAnims.select().loopAndReset();
            prop.anim.setProgress((float) Math.random());
            prop.size = level.bushSize.generate() * type.scale;
            prop.x = bushGeneratedX;
            prop.y = prop.mat.length * prop.size / 2f;
            prop.flipped = Math.random() < 0.5f ? +1 : -1;
            bushGeneratedX += prop.size + level.bushIntervalX.generate();
            bushes.add(prop);
        }

        // Cloud generation
        offsetZ = -cameraX * level.cloudsZ;
        for(int c = 0; c < clouds.size; c++) {
            PropInstance prop = clouds.items[c];
            if((prop.x + offsetZ) < cameraLeftX) {
                if(clouds.size == 1) {
                    PropInstance.pool.freeAll(clouds);
                    clouds.clear();
                    break;
                }
                PropInstance.pool.free(clouds.removeIndex(c));
                c--;
            }
            else {
                // Process speed
                prop.x += prop.speedX * getRenderDeltaTime();
            }
        }
        if(getRenderTime() > tNextCloudScheduled) {
            PropInstance prop = PropInstance.pool.obtain();
            PropType type = level.clouds.select();
            prop.mat = type.mat;
            prop.anim = level.cloudAnims.select().loopAndReset();
            prop.anim.setProgress((float) Math.random());
            prop.size = level.cloudsSize.generate() * type.scale;
            prop.x = -cameraX + 1f + (prop.size / 2f) - offsetZ;
            prop.y = level.cloudsY.generate();
            prop.speedX = level.cloudsSpeed.generate();
            prop.flipped = Math.random() < 0.5f ? +1 : -1;
            clouds.add(prop);
            tNextCloudScheduled = getRenderTime() + level.tCloudInterval.generate();
        }

    }

    public void loadLevel(Class<? extends LevelSource> source) {
        levelSource = source;
        try {
            level = levelSource.newInstance().buildLevel();
        } catch (Throwable e) {
            throw new RuntimeException("Unable to build level source: " + source, e);
        }

        // Add all for precaching
        StreamablePrecacher precacher = Globals.grid.precacher;
        if(precacherList.size > 0) {
            precacher.removeAll(precacherList.toArray());
            precacherList.clear();
        }
        for(int c = 0; c < level.mountains.set.length; c++)
            precacherList.add(level.mountains.set[c].mat);
        for(int c = 0; c < level.clouds.set.length; c++)
            precacherList.add(level.clouds.set[c].mat);
        for(int c = 0; c < level.trees.set.length; c++)
            precacherList.add(level.trees.set[c].mat);
        for(int c = 0; c < level.bushes.set.length; c++)
            precacherList.add(level.bushes.set[c].mat);
        for(Stage stage : level.stages) {
            precacherList.addAll(stage.pipes.set);
        }
        precacherList.addAll(level.birdPissEffect.splashMats.set);
        precacherList.add(level.birdPissEffect.mesh.getMaterial());
        precacherList.add(level.bird);
        precacherList.addAll(level.grounds.set);
        precacherList.add(level.baseLighting.bg.getMaterial());
        precacherList.add(level.demonLighting.bg.getMaterial());
        precacherList.addAll(level.baseLighting.sunMat.getMaterial());
        precacherList.addAll(level.demonLighting.sunMat.getMaterial());
        precacher.addAll(precacherList.toArray());


        if(pissEffect != null)
            pissEffect.clear();
        pissEffect = level.birdPissEffect;
        pissEffect.clear();

        // Set new leaderboard
        leaderboard.clear();
        leaderboard.addAll(level.leaderboard);
        isLeaderboardRefreshed = false;

        open();
    }

    private void startLevel(boolean isForMenu) {
        // Clear map
        pipes.clear();
        grounds.clear();
        mountains.clear();
        trees.clear();
        bushes.clear();
        clouds.clear();
        cameraX = 0;
        groundGeneratedX = 0;
        mountainGeneratedX = 0;
        treeGeneratedX = 0;
        bushGeneratedX = 0;
        tNextCloudScheduled = 0;

        // Reset bird
        birdY = 0;
        birdVelocityY = 0;
        tNextFlapAllowed = 0;
        tBirdPissVelocityRefreshScheduled = 0;
        tBirdPissStateRefreshScheduled = 0;
        tBirdPissIntermittentStateRefreshScheduled = 0;
        tDeathStarted = -1;
        tPowerupEndScheduled = Float.MAX_VALUE;
        tPowerupChargingStarted = -1;
        pissEffect.clear();
        stopPissEffectSound();

        // Score
        score = 0;

        // Cheating
        cheatingCurrentPipes = 0;

        if(isForMenu) {
            generatedX = 1f;
            isBirdHovering = false;
            isBirdAutopilot = true;
            isBirdReviving = false;
        }
        else {
            generatedX = level.pipeStartX;
            isBirdHovering = true;      // Wait for first tap
            isBirdAutopilot = false;
            isBirdReviving = false;

            // Analytics
            Game.analyticsStartLevel(levelSource.getSimpleName());
        }

        // Stage
        prepareStage(0);
    }

    private void prepareStage(int index) {
        Stage prevStage = stage;
        int prevPowerupCostIndex = stagePowerupCostIndex;
        int prevReviveCostIndex = stageReviveCostIndex;

        stageIndex = index;
        stage = level.stages[index];
        stageGeneratedRemaining = stageScoreRemaining = stage.stageLength.generateInt();
        stagePowerupCostIndex = 0;
        stageReviveCostIndex = 0;

        // Update cost indices to be equal or higher but never cheaper
        if(index > 0) {
            // Check with previous stage costs
            int prevPowerupCost = prevStage.powerupCosts[prevPowerupCostIndex];
            int prevReviveCost = prevStage.reviveCosts[prevReviveCostIndex];

            while(stagePowerupCostIndex < (stage.powerupCosts.length - 1) && stage.powerupCosts[stagePowerupCostIndex] < prevPowerupCost)
                stagePowerupCostIndex++;
            while(stageReviveCostIndex < (stage.reviveCosts.length - 1) && stage.reviveCosts[stageReviveCostIndex] < prevReviveCost)
                stageReviveCostIndex++;
        }

        // Update cost texts
        s.ingamePowerupCostView.text(Integer.toString(stage.powerupCosts[stagePowerupCostIndex]));
        s.chanceReviveCostView.text(Integer.toString(stage.reviveCosts[stageReviveCostIndex]));
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            s.window.detach();
            s.bars.detach();
            s.shopBuyGlitch.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
        s.bars.attach(this);

        powerupBirdAnim = s.ingamePowerupBirdAnim.loopAndReset();
        reviveBirdAnim = s.reviveBirdAnim.loopAndReset();

        if(isAttached())
            showMenu(false);
    }


    public boolean pack(ScriptState state) {
        if(si != null)
            return false;           // can't save when on showdown

        state.set("flapeebird.eggs", eggs);
        state.set("flapeebird.eggsPurchased", eggsPurchased);
        state.set("flapeebird.lifehours", lifehours);
        state.set("flapeebird.isAdvancedPlayer", isAdvancedPlayer);
        state.set("flapeebird.isEggShopAllowed", isEggShopAllowed);
        state.set("flapeebird.inviteInfos", inviteInfos);
        state.set("flapeebird.level", levelSource);

        state.set("flapeebird.totalScore", totalScore);
        state.set("flapeebird.hoursPlayed", hoursPlayed);

        state.set("flapeebird.shopOfferIndex", shopOfferIndex);

        state.set("flapeebird.hasWatchedAd", hasWatchedAd);
        state.set("flapeebird.hasInvitedFriend", hasInvitedFriend);
        state.set("flapeebird.hasSpentLifehours", hasSpentLifehours);

        state.set("flapeebird.updatingSize", updatingSize);

        state.set("flapeebird.cheatingLevel", cheatingLevel);

        // Can save
        return true;
    }

    public void unpack(ScriptState state) {
        eggs = state.get("flapeebird.eggs", Globals.g_flapeeDefaultEggs);
        eggsPurchased = state.get("flapeebird.eggsPurchased", 0);
        lifehours = state.get("flapeebird.lifehours", 100);         // default value ignored
        isAdvancedPlayer = state.get("flapeebird.isAdvancedPlayer", false);
        isEggShopAllowed = state.get("flapeebird.isEggShopAllowed", false);
        inviteInfos = state.get("flapeebird.inviteInfos", null);
        levelSource = state.get("flapeebird.level", Globals.g_flapeeDefaultLevel);
        loadLevel(levelSource);

        // Update UI
        setEggs(eggs);
        setLifehours(lifehours);
        configureInvites(inviteInfos);

        totalScore = state.get("flapeebird.totalScore", 0);
        hoursPlayed = state.get("flapeebird.hoursPlayed", 0f);
        isLeaderboardRefreshed = false;     // updated player stats from save

        shopOfferIndex = state.get("flapeebird.shopOfferIndex", 0);

        hasWatchedAd = state.get("flapeebird.hasWatchedAd", false);
        hasInvitedFriend = state.get("flapeebird.hasInvitedFriend", false);
        hasSpentLifehours = state.get("flapeebird.hasSpentLifehours", false);

        updatingSize = state.get("flapeebird.updatingSize", -1f);

        cheatingLevel = state.get("flapeebird.cheatingLevel", 0);
    }

    public FlapeeBirdScreen() {
        builder = new Builder<Object>(GBFlapeeBirdScreen.class, this);
        builder.build();

        lightingCompositorMaterial = new LightingCompositorMaterial();
        gameplayPlane = new Sprite(Globals.LENGTH, lightingCompositorMaterial);
        lightingGeneratorMaterial = new LightingGeneratorMaterial();
        lightingGenerationPlane = new Sprite(Globals.LENGTH, lightingGeneratorMaterial);

        belowGroundMat = new Sprite(Globals.LENGTH, SaraRenderer.renderer.coloredMaterial);

        adScreen = new FlapeeAdScreen(this);

        // Load save
        unpack(Globals.grid.state);
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        // Request max framerate
        Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);

        // Delay idle scare
        grid.idleScare.reschedule();

        // Cache numbers
        s.scoreView.text().font.prepare(FONT_NUMBERS);          // only needed for super high resolution font

        // Refresh resources counting
        if(eggsTransitionCurrent != eggs) {
            int current = eggsTransitionCurrent;
            if(tEggsTransitionStarted != -1) {
                float elapsed = renderTime - tEggsTransitionStarted;
                if(elapsed > s.resourceTransitionGraph.getLength())
                    current = eggs;
                else
                    current = eggsTransitionFrom + Math.round((float)(eggs - eggsTransitionFrom) * s.resourceTransitionGraph.generate(elapsed));

            }
            if(current != eggsTransitionCurrent) {
                s.eggsView.text(Integer.toString(current));
                eggsTransitionCurrent = current;
            }
        }
        if(lifehoursTransitionCurrent != lifehours) {
            int current = lifehoursTransitionCurrent;
            if(tLifehoursTransitionStarted != -1) {
                float elapsed = renderTime - tLifehoursTransitionStarted;
                if(elapsed > s.resourceTransitionGraph.getLength())
                    current = lifehours;
                else
                    current = lifehoursTransitionFrom + Math.round((float)(lifehours - lifehoursTransitionFrom) * s.resourceTransitionGraph.generate(elapsed));

            }
            if(current != lifehoursTransitionCurrent) {
                s.lifehoursView.text(Integer.toString(current));
                lifehoursTransitionCurrent = current;
            }
        }

        // Subscription lifehours drain
        if(renderTime > tNextLifehoursDrainScheduled) {
            // Drain lifehours up to min drain cap
            int drain = lifehours - si.lifehoursMinDrainCap;
            if(drain > 0) {
                if(drain > si.lifehoursDrainAmount)
                    drain = si.lifehoursDrainAmount;
                consumeLifehours(drain);
            }
            // Give reward
            int reward = si.lifehoursDrainEggs - eggs;
            if(reward > 0)
                consumeEggs(-reward);

            // Schedule next
            tNextLifehoursDrainScheduled = renderTime + si.tLifehoursDrainInterval;
        }


        // Theme will react to glitches stopping time
        if(getEffectiveTimeMultiplier() == 0)
            Audio.pauseMusic();
        else if(tDeathStarted == -1 || isAcceptedDeath)
            Audio.resumeMusic();

        // Update UI
        if(renderTime > tLoadingEndScheduled) {
            tLoadingEndScheduled = Float.MAX_VALUE;

            s.loadingView.detachWithAnim();

            if(updatingSize != -1) {
                s.updatingGroup.attach();
                tUpdatingNumberScheduled = -1;        // refresh
            }
            else if(si != null) {
                // Hide title for this
                s.titleView.detachWithAnim();
                s.royaleTitleView.detachWithAnim();

                // Show showdown dialog
                si.showdownGroup.attach();

                // Voice
                startDemonVoice(si.showdownVoice, false);
            }
            else if(!Globals.grid.isStateUnlocked(Globals.STATE_FLAPEE_PERMISSIONS_ACCEPTED)) {
                s.permissionGroup.attach();
                // Switch button positions (for each time they click deny)
                UIElement.Metrics metrics = s.permissionAcceptButton.metrics;
                s.permissionAcceptButton.metrics = s.permissionDenyButton.metrics;
                s.permissionDenyButton.metrics = metrics;
            }
            else if(isAdvancedPlayer && Globals.grid.unlockState(Globals.STATE_FLAPEE_SHOWN_FINDING_FRIENDS)) {
                // Show finding friends
                s.findingFriendsGroup.attach();
                s.findingFriendsGroup.iterate(null, HorizontalProgressBar.class, false, null).progress(0).seek(1f, s.tFindingFriendsTime);

                tFindingFriendsEndScheduled = getRenderTime() + s.tFindingFriendsTime;
            }
            else if(!showDialogMenu())
                showMenu(false);
        }

        if(renderTime > tFindingFriendsEndScheduled) {
            tFindingFriendsEndScheduled = Float.MAX_VALUE;

            s.findingFriendsGroup.detachWithAnim();
            showMenu(false);
        }

        if(renderTime > tSendingEggsEndScheduled) {
            tSendingEggsEndScheduled = Float.MAX_VALUE;

            s.sendingEggsGroup.detachWithAnim();
            showMenu(false);
        }

        if (renderTime > tShopSigilScheduled)  {
            tShopSigilScheduled = renderTime + s.tShopSigilInterval.generate();     // Reschedule

            // Create new sigil effect
            Toast sigil = s.shopSigils.select().instantiate();
            sigil.metrics.anchorWindowX = Range.generateFor(0f, 0.5f, true);
            sigil.metrics.anchorY = Range.generateFor(0, Globals.LENGTH / 2f, true);
            float size = s.shopSigilSize.generate();
            sigil.metrics.scale(size, size);
            sigil.animation(null, null, s.shopSigilAnim.select());
            sigil.viewport(s.window).attach();
        }

        if(renderTime > tRewardEndScheduled) {
            tRewardEndScheduled = Float.MAX_VALUE;
            tRewardStartScheduled = Float.MAX_VALUE;

            s.inputBlockerView.detachWithAnim();
            s.rewardGroup.detachWithAnim();

//            if(updatingSize != -1) {          // TODO: doesnt seem to work and i feel its fine
//                // Game is updating
//                s.updatingGroup.attach();
//                // Show navbar if hidden
//                if(s.bars.navbar().windowAnim != null && s.bars.navbar().windowAnim.anim == s.barsHideAnim)
//                    s.bars.navbar().windowAnimation(s.barsShowAnim.startAndReset(), true, false);
//                tUpdatingNumberScheduled = -1;        // refresh
//            }
//            else if(!showDialogMenu())                  // If there is a dialog queued, show that, if not show menu
            if(!showDialogMenu())                  // If there is a dialog queued, show that, if not show menu
                showMenu(false);
        }

        if(renderTime > tDialogShowScheduled) {
            tDialogShowScheduled = Float.MAX_VALUE;
            if(!showDialogMenu())
                showMenu(false);
        }

        if(renderTime > tUpdatingNumberScheduled) {
            s.updatingTextView.text(String.format(Locale.US, s.updatingFormat, updatingSize, s.updatingNumber.generate()));
            tUpdatingNumberScheduled = renderTime + s.tUpdatingNumberInterval;
        }

        if(tDeathStarted != -1 && tNextLifehoursDrainScheduled == Float.MAX_VALUE) {        // Only recognize chance if not in life drain
            float elapsed = renderTime - tDeathStarted;
            if(((isAdvancedPlayer && elapsed > level.chanceTime) || (!isAdvancedPlayer && elapsed > s.tNormalGameOverDelay)) && !isAcceptedDeath) {
                isAcceptedDeath = true;
                // Analytics
                Game.analyticsEndLevel(levelSource.getSimpleName(), score, false);
                // Reschedule idle chatter
                tDemonIdleChatterScheduled = Float.MAX_VALUE;
                // Stop reviving sound
                stopRevivingSound();
                Audio.playMusic(level.themeMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);
                // TODO: update hours played
                s.chanceMenu.detachWithAnim();
                // Update score
                if(isAdvancedPlayer && score > totalScore && si == null) {  // Show highscore if not on showdown
                    // Add excess as reward TODO: parameterize and balancing
                    queueReward(score - totalScore);
                    // Show new high score notification
                    s.newHighScoreView.attach();
                    // New high score sound
                    s.newHighScoreSound.play(sfxVolume);
                    tNewHighScoreEndScheduled = renderTime + s.tNewHighScoreTime;
                    // Ranking
                    totalScore = score;
                    isLeaderboardRefreshed = false;         // updated player score
                    int playerRank = getPlayerRank();
                    if(playerRank == 0) {       // This will never happen as royale win will hapen during increaseScore()
                        s.rankingRoyaleView.attach();
                        tNewHighScoreEndScheduled = renderTime + s.tNewHighScoreRoyaleTime;
                    }
                    else {
                        s.rankingView.text(String.format(Locale.US, s.rankingFormat, playerRank + 1));
                        s.rankingView.attach();
                        tNewHighScoreEndScheduled = renderTime + s.tNewHighScoreTime;
                    }

                    // Demon voice
                    startDemonVoice(stage.demonPlayerHighscoreChatter, false);

                    // Trigger
                    grid.trigger(Globals.TRIGGER_FLAPEE_NEW_HIGH_SCORE);        // return ignored
                }
                else {
                    if(score > totalScore) {
                        totalScore = score;
                        isLeaderboardRefreshed = false;
                    }

                    if(si != null)
                        showSubscriptionWindow();       // Show subscription first
                    else {
                        // Game over
                        s.gameOverView.attach();

                        // Show ad if possible
                        if(!Globals.g_showRealAds || !Game.game.platform.showInterstitialAd())
                            showMenu(true);
                    }
                }
                // Trigger session end
                grid.trigger(Globals.TRIGGER_FLAPEE_FINISHED_SESSION);

                // Achievement
                if(score > 0)
                    ACT1.unlockAchievement(Globals.Achievement.SCORED_IN_FB, score);
            }
            else if(isAdvancedPlayer) {
                // Update progress
                s.chanceReviveTimerView.windowAnim.setProgress(elapsed / level.chanceTime);
            }
        }

        if(renderTime > tNewHighScoreEndScheduled) {
            tNewHighScoreEndScheduled = Float.MAX_VALUE;

//            if(tNextLifehoursDrainScheduled != Float.MAX_VALUE)
            if(si != null && getPlayerRank() == 0)          // Wins showdown
                grid.trigger(Globals.TRIGGER_FLAPEE_SHOWDOWN_WON);
            else {
                // Else proceed normally
                s.rankingView.detachWithAnim();
                s.rankingRoyaleView.detachWithAnim();
                s.newHighScoreView.detachWithAnim();

                if (!showRewardMenu())
                    showMenu(true);
            }
        }

        // Demon voice
        if(demonVoiceTrack != null) {
            if(!demonVoiceTrack.isPlaying()) {
                Globals.grid.notification.stopSubtitle(demonVoice.voiceFilename);
                // Finished
                demonVoiceTrack.dispose();
                demonVoiceTrack = null;
                demonVoiceProfile = null;
                // Inform finished demon voice
                demonVoice.notifyPlayed();
                demonVoice = null;
                // Reschedule idle chatter
                if(tDemonIdleChatterScheduled != Float.MAX_VALUE)
                    tDemonIdleChatterScheduled = renderTime + stage.tDemonIdleChatterDelay.generate();
                // Schedule end if lighting was changed
                if(tLightingChangeStarted != -1 && tLightingEndStarted == Float.MAX_VALUE)
                    tLightingEndStarted = renderTime + s.tLightingEndCooldown;
                sfxVolume = 1f;
                if(!(s.basicGroup.isAttached() || s.menuGroup.isAttached()) && tPowerupEndScheduled == Float.MAX_VALUE)
                    Audio.setMusicVolume(s.gameplayThemeVolume);        // Playing gameplay music, set volume
                else
                    Audio.setMusicVolume(1f);       // menu music, full volume
                // Restore screen background
                if(Globals.grid.screen.backgroundAnim() != null && Globals.grid.screen.backgroundAnim().anim != s.demonVoiceScreenBgEndAnim)
                    Globals.grid.screen.animateBackground(s.demonVoiceScreenBgEndAnim, null, false);
            }
            else {
                // Else demon voice is playing, make sure lighting has changed
                if(tLightingChangeStarted == -1) {
                    tLightingChangeStarted = renderTime;
                    tLightingEndStarted = Float.MAX_VALUE;
                }
                else if(tLightingEndStarted != Float.MAX_VALUE) {
                    // Else lighting change has started but is ending now, reverse it
                    float elapsed = renderTime - tLightingEndStarted;
                    tLightingChangeStarted = renderTime - (1f - (elapsed / s.lightingChangeEndGraph.getLength())) * s.lightingChangeStartGraph.getLength();
                    tLightingEndStarted = Float.MAX_VALUE;
                }
            }
        }

        // Idle chatter scheduled
        if(renderTime > tDemonIdleChatterScheduled) {
            DemonVoice voice = DemonVoice.select(stage.demonIdleChatter);
            if(voice == null)
                tDemonIdleChatterScheduled = Float.MAX_VALUE;       // no chat available, cancel for this stage
            else if(!startDemonVoice(voice, true))
                tDemonIdleChatterScheduled = renderTime + stage.tDemonIdleChatterDelay.generate();      // reschedule
            else
                tDemonIdleChatterScheduled = Float.MAX_VALUE;       // is being played now
        }

        float lightingBlendR = 0;

        // Determine lighting change
        if(tLightingChangeStarted != -1) {
            // Start animation
            float elapsed = renderTime - tLightingChangeStarted;
            if(elapsed > s.lightingChangeStartGraph.getLength())
                lightingBlendR = s.lightingChangeStartGraph.getEnd();
            else
                lightingBlendR = s.lightingChangeStartGraph.generate(elapsed);
            // End animation
            if(renderTime > tLightingEndStarted) {
                elapsed = renderTime - tLightingEndStarted;
                if(elapsed > s.lightingChangeEndGraph.getLength()) {
                    lightingBlendR -= 1.0f - s.lightingChangeEndGraph.getEnd();
                    tLightingChangeStarted = -1;
                    tLightingEndStarted = Float.MAX_VALUE;
                }
                else
                    lightingBlendR -= 1.0f - s.lightingChangeEndGraph.generate(elapsed);
            }

            // Sample voice if available
            if(demonVoiceTrack != null) {
                float profile = demonVoiceProfile.sample(demonVoiceTrack.getPosition());
                lightingBlendR += s.lightingVoiceGraph.generate(profile);
            }

            // Clamp
            if(lightingBlendR < 0)
                lightingBlendR = 0;
//            else if(lightingBlendR > 1f)
//                lightingBlendR = 1f;
        }


        // Process game
        gameStep();

        // Start render
        Matrix4 m = Matrices.model;

        Matrices.push();
        Matrices.camera = grid.compositor.camera;

        level.baseLighting.render(
                lightingBlendR > 0 ? level.demonLighting : null,
                lightingBlendR,
                level.groundY,
                lightingGenerationPlane, lightingGeneratorMaterial,
                gameplayPlane, lightingCompositorMaterial
        );

        int flapeeBgTarget;
        int flapeeGameplayTarget;

        if(Globals.r_highQuality) {
            flapeeBgTarget = SaraRenderer.TARGET_FLAPEE_BG;
            flapeeGameplayTarget = SaraRenderer.TARGET_FLAPEE_GAMEPLAY;
        }
        else {
            flapeeBgTarget = SaraRenderer.TARGET_INTERACTIVE_SUB;
            flapeeGameplayTarget = SaraRenderer.TARGET_INTERACTIVE_SUB;
        }

        // Render mountains
        float offsetZ = -cameraX * level.mountainZ;
        for(int c = 0; c < mountains.size; c++) {
            PropInstance prop = mountains.items[c];
            Matrices.push();
            Matrices.target = flapeeBgTarget;

            m.translate(prop.x + cameraX + offsetZ + (prop.size / 2f), (+Globals.LENGTH / 2f) + level.groundY + level.mountainY + prop.y, 0);
            m.scale(prop.size * prop.flipped, prop.size, prop.size);
            prop.mat.render();
            Matrices.pop();
        }

        // Render clouds
        offsetZ = -cameraX * level.cloudsZ;
        for(int c = 0; c < clouds.size; c++) {
            PropInstance prop = clouds.items[c];
            Matrices.push();
            Matrices.target = flapeeBgTarget;

            m.translate(prop.x + cameraX + offsetZ + (prop.size / 2f), (+Globals.LENGTH / 2f) + level.groundY + prop.y, 0);
            m.scale(prop.size, prop.size, prop.size);
            prop.anim.updateAndApply(prop.mat, getRenderDeltaTime());
            if(prop.flipped != +1f)
                m.scale(-1, 1, 1);
            prop.mat.render();
            Matrices.pop();
        }

        // Render trees
        offsetZ = -cameraX * level.treeZ;
        for(int c = 0; c < trees.size; c++) {
            PropInstance prop = trees.items[c];
            Matrices.push();
            Matrices.target = flapeeBgTarget;

            m.translate(prop.x + cameraX + offsetZ + (prop.size / 2f), (+Globals.LENGTH / 2f) + level.groundY + level.treeY + prop.y, 0);
            m.scale(prop.size, prop.size, prop.size);
            prop.anim.updateAndApply(prop.mat, getRenderDeltaTime());
            if(prop.flipped != +1f)
                m.scale(-1, 1, 1);
            prop.mat.render();
            Matrices.pop();
        }

        // Render bushes
        offsetZ = -cameraX * level.bushZ;
        for(int c = 0; c < bushes.size; c++) {
            PropInstance prop = bushes.items[c];
            Matrices.push();
            Matrices.target = flapeeBgTarget;

            m.translate(prop.x + cameraX + offsetZ + (prop.size / 2f), (+Globals.LENGTH / 2f) + level.groundY + level.bushY + prop.y, 0);
            m.scale(prop.size, prop.size, prop.size);
            prop.anim.updateAndApply(prop.mat, getRenderDeltaTime());
            if(prop.flipped != +1f)
                m.scale(-1, 1, 1);
            prop.mat.render();
            Matrices.pop();
        }

        // Render pipes
        for(int c = 0; c < pipes.size; c++) {
            PipeInstance pipe = pipes.items[c];

            // Top pipe
            Matrices.push();
            Matrices.target = flapeeGameplayTarget;
            float x = pipe.x + cameraX;
            m.translate(x, (+Globals.LENGTH / 2f) + level.groundY + pipe.topY, 0);
            m.scale(level.pipeSize, -level.pipeSize, level.pipeSize);
            if(pipe.topPipeAnim != null && !pipe.topPipeAnim.updateAndApply(pipe.topPipe, getRenderDeltaTime()))
                pipe.topPipeAnim = null;
            pipe.topPipe.render();
            Matrices.pop();

            // Bottom pipe
            Matrices.push();
            Matrices.target = flapeeGameplayTarget;
            x = pipe.x + cameraX;
            m.translate(x, (+Globals.LENGTH / 2f) + level.groundY + pipe.bottomY, 0);
            m.scale(level.pipeSize, level.pipeSize, level.pipeSize);
            if(pipe.bottomPipeAnim != null && !pipe.bottomPipeAnim.updateAndApply(pipe.bottomPipe, getRenderDeltaTime()))
                pipe.bottomPipeAnim = null;
            pipe.bottomPipe.render();
            Matrices.pop();
        }



        // Render bird

        Matrices.push();
        Matrices.target = flapeeGameplayTarget;

        m.translate(level.birdX + cameraX, (+Globals.LENGTH / 2f), 0);

        pissEffect.mesh.render();
        pissEffect.render(cameraX);

        Matrices.pop();


        Matrices.push();
        Matrices.target = flapeeGameplayTarget;
        m.translate(level.birdX,(+Globals.LENGTH / 2f) + birdY, 0);
        m.scale(level.birdSize, level.birdSize, level.birdSize);
        float angle;
        if(tDeathStarted == -1) {
            if(birdVelocityY < level.birdRotateMaxVelocityY)
                angle = level.birdRotateMax + ((birdVelocityY - level.birdRotateMaxVelocityY) * level.birdRotateAngle);
            else
                angle = level.birdRotateMax;
        }
        else {
            angle = level.birdDeathRotateGraph.generate(level.birdDeathRotateGraph.clampProgress(renderTime - tDeathStarted));
        }
        m.rotate(0, 0, -1, angle);

        if(isBirdHovering)
            level.birdHoverAnim.updateAndApply(level.bird, getRenderDeltaTime());
        if(tPowerupEndScheduled != Float.MAX_VALUE)
            powerupBirdAnim.updateAndApply(level.bird, getRenderDeltaTime());
        else if(isBirdReviving)
            reviveBirdAnim.updateAndApply(level.bird, getRenderDeltaTime());
        level.bird.render();

        Matrices.pop();

        // Render ground
        for(int c = 0; c < grounds.size; c++) {
            GroundInstance ground = grounds.items[c];
            Matrices.push();
            Matrices.target = SaraRenderer.TARGET_INTERACTIVE_SUB;
            m.translate(ground.x + cameraX + 0.5f, (+Globals.LENGTH / 2f) + level.groundY - (ground.ground.length / 2f), 0);
            ground.ground.render();
            m.translate(0, - (ground.ground.length / 2f) - (belowGroundMat.length / 2f), 0);
            ColorAttribute.of(belowGroundMat).set(level.belowGroundColor);
            belowGroundMat.render();
            Matrices.pop();
        }

        Matrices.pop();
    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Reset
        tNextFlapAllowed = 0;
        tNewHighScoreEndScheduled = Float.MAX_VALUE;
        tShopSigilScheduled = Float.MAX_VALUE;
        tRewardStartScheduled = Float.MAX_VALUE;
        tRewardEndScheduled = Float.MAX_VALUE;
        tDialogShowScheduled = Float.MAX_VALUE;
        tUpdatingNumberScheduled = Float.MAX_VALUE;

        tLastDemonHitTaunt = -Float.MAX_VALUE;

        s.shopSigilsGroup.detachChilds();

        levelDirection = Math.random() < 0.5f ? -1 : +1;

        // Reset lighting
        tLightingChangeStarted = -1;
        tLightingEndStarted = Float.MAX_VALUE;

        if(demonVoiceTrack != null)
            sfxVolume = s.demonVoiceSfxVolume;
        else
            sfxVolume = 1f;

        // Theme music
        Audio.playMusic(level.themeMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);

        // Check queued reward
        showRewardMenu();                   // Maybe not a good place to put it, other views might appear on top
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();

        // Stop music
        Audio.stopMusic();
        stopPissEffectSound();

        s.shopBuyGlitch.detach();

        // Update hours played
        float elapsed = getRenderTime();
        float virtualElapsed = (elapsed * s.gameTimeMultiplier) - elapsed;
        hoursPlayed += (elapsed + virtualElapsed) / 3600f;     // convert to hours
        long millis = (long) (virtualElapsed * 1000);
        grid.setSystemTime(grid.getSystemTime() + millis);

        stopRevivingSound();

        // Restore screen background
        if(Globals.grid.screen.backgroundAnim() != null && Globals.grid.screen.backgroundAnim().anim != s.demonVoiceScreenBgEndAnim)
            Globals.grid.screen.animateBackground(s.demonVoiceScreenBgEndAnim, null, false);
    }

    @Override
    public void refreshNotification(Homescreen homescreen) {
        // Show notification only if in basic mode and never passed a pipe OR is in advanced mode and not updating
        if((!isAdvancedPlayer && totalScore == 0) || (isAdvancedPlayer && updatingSize == -1))
            homescreen.setIndefiniteNotification(Globals.CONTEXT_APP_FLAPEE, true);
        else {
            homescreen.setIndefiniteNotification(Globals.CONTEXT_APP_FLAPEE, false);
            homescreen.clearNotifications(Globals.CONTEXT_APP_FLAPEE);
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if((view == s.bars.backButton() || view == s.bars.homeButton() || view == s.updatingCloseButton) && (s.menuGroup.isAttached() || s.basicGroup.isAttached() || s.updatingGroup.isAttached())) {
            if(!v.trigger(Globals.TRIGGER_FLAPEE_LEAVE_SCREEN))
                return;         // not allowed to leave
            if(si != null) {
                // Hide menu
                s.menuGroup.detachWithAnim();
                s.moreEggsGroup.detachWithAnim();

                // Show showdown giveup dialog
                si.giveupGroup.attach();

                // Voice
                startDemonVoice(si.giveupVoice, false);
                return;
            }

            // Else just return back to homescreen
            v.homescreen.transitionBack(this, v);
            return;
        }


        if(view == s.bars.irisButton() && (s.menuGroup.isAttached() || s.basicGroup.isAttached() || s.updatingGroup.isAttached())) {
            v.notification.openTracker();
            return;
        }

        if(view == s.playButton || view == s.basicPlayButton) {
            // If bought some hours, clear effect
            s.shopBuyGlitch.detachWithAnim();
            tShopSigilScheduled = Float.MAX_VALUE;       // Stop

            // Schedule demon chatter
            tDemonIdleChatterScheduled = getRenderTime() + stage.tDemonIdleChatterDelay.generate();

            if(tNextLifehoursDrainScheduled == Float.MAX_VALUE)
                s.lifehoursView.detachWithAnim();       // dont need to show lifehours

            s.titleView.detachWithAnim();
            s.royaleTitleView.detachWithAnim();
            s.menuGroup.detachWithAnim();
            s.basicGroup.detachWithAnim();
            s.moreEggsGroup.detachWithAnim();
            s.gameOverView.detachWithAnim();
            s.tapView.attach();     // allow input

            s.scoreView.detach();       // from game over screen

            // Hide navbar
            s.bars.navbar().windowAnimation(s.barsHideAnim.startAndReset(), true, true);

            startLevel(false);

            // Queue tutorial
            s.tutorialView.attach();

            // Gameplay music
            Audio.playMusic(level.gameplayMusic, true, (sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume) * s.gameplayThemeVolume);
            return;
        }

        if(view == s.moreEggsInviteButton) {
            // Close menu
            s.menuGroup.detachWithAnim();
            s.moreEggsGroup.detachWithAnim();

            // Open invite menu
            showInviteMenu();

            // Stop showing new sticker
            hasInvitedFriend = true;
            return;
        }

        int index = inviteButtons.indexOf(view, true);
        if(index != -1) {
            // Sending invite to this character
            InviteInfo info = inviteInfos[index];
            v.unlockState(info.tag);

            // Open chat
            WhatsupContact contact = v.whatsupApp.findContact(info.name);
            v.whatsupApp.threadScreen.open(contact);
            // Transition
            ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(this, v.whatsupApp.threadScreen, v.screensGroup);
            transition.attach(v.screensGroup);

            // Queue reward
            queueReward(90);

            return;
        }

        if(view == s.moreEggsShopButton) {
            // Close menu
            s.menuGroup.detachWithAnim();
            s.moreEggsGroup.detachWithAnim();

            // Show subscription if on showdown
            if(si != null) {
                showSubscriptionWindow();
                return;
            }

            // Configure shop menu
            int lifeAmount = s.shopLifeAmount[shopOfferIndex];
            if(lifeAmount > lifehours)
                lifeAmount = lifehours;     // limit to available hours
            s.shopOfferView.text(String.format(Locale.US, s.shopOfferFormat, s.shopEggsAmount[shopOfferIndex], lifeAmount));
            s.shopPurchaseButton.text(String.format(Locale.US, s.shopPurchaseButtonFormat, lifeAmount));

            // Open shop menu
            s.shopGroup.attach();

            // Shop life remaining
            s.lifehoursView.attach();

            // Demon voice
            startDemonVoice(s.demonOpenEggshopVoices.select(), false);

            // Remember to remove sticker
            hasSpentLifehours = true;

            // Trigger
            v.trigger(Globals.TRIGGER_FLAPEE_SHOP_OPENED);

            return;
        }

        // Purchase buttons
        if(view == s.shopPurchaseButton) {
            int lifeAmount = s.shopLifeAmount[shopOfferIndex];
            if(lifeAmount > lifehours)
                lifeAmount = lifehours;
            consumeLifehours(lifeAmount);       // ignore, should deplete to zero

            // Analytics
            Game.analyticsValue(Globals.ANALYTICS_EVENT_FLAPEEBIRD_BUY, Globals.ANALYTICS_EVENT_FLAPEEBIRD_BUY_FIELD, lifeAmount);

            // Update hours spent
            hoursPlayed += lifeAmount;
            isLeaderboardRefreshed = false;

            // Reward
            int purchased = s.shopEggsAmount[shopOfferIndex];
            queueReward(purchased, purchased);

            // Make it more expensive next time
            if(shopOfferIndex < (s.shopLifeAmount.length - 1))
                shopOfferIndex++;

            // Close menu
            s.shopGroup.detachWithAnim();

            // Glitch and sigils
            s.shopBuyGlitch.attach(this);
            tShopSigilScheduled = -1;       // Start now

            // Demon music
            float position = Audio.getMusicPosition();
            Audio.playMusic(level.storeMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);
            Audio.setMusicPosition(position);

            // Demon voice
            startDemonVoice(s.demonBuyEggsVoices.select(), false);

            // Show reward
            showRewardMenu();

            // Trigger
            v.trigger(Globals.TRIGGER_FLAPEE_HOURS_PURCHASED);

            return;
        }

        if(view == s.shopCloseButton) {
            s.shopGroup.detachWithAnim();

            // Show back menu
            showMenu(false);

            // Demon voice
            startDemonVoice(s.demonRejectEggshopVoices.select(), false);

            return;
        }

        if(view == s.inviteCloseButton) {
            // Close invite menu
            s.inviteGroup.detachWithAnim();

            // Open back menu
            showMenu(false);
            return;
        }

        if(view == s.highScoreButton) {
            // Close menu
            s.menuGroup.detachWithAnim();
            s.basicGroup.detachWithAnim();
            s.moreEggsGroup.detachWithAnim();

            // Open high scores
            s.scoreGroup.attach();
            return;
        }

        if(view == s.scoreCloseButton) {
            // Close high scores
            s.scoreGroup.detachWithAnim();

            // Open back menu
            showMenu(false);
            return;
        }

        if(view == s.ingamePowerupButton) {
            if(!consumeEggs(stage.powerupCosts[stagePowerupCostIndex])) {
                flap();
                return;     // not enough eggs
            }

            // Increase cost
            if(stagePowerupCostIndex < (stage.powerupCosts.length - 1)) {
                stagePowerupCostIndex++;
                s.ingamePowerupCostView.text(Integer.toString(stage.powerupCosts[stagePowerupCostIndex]));
            }

            // Trigger
            v.trigger(Globals.TRIGGER_FLAPEE_POWERUPS_USED);        // return ignored

            // Analytics
            Game.analyticsEvent(Globals.ANALYTICS_EVENT_FLAPEEBIRD_JETSTREAM);

            // Disable for charging
            s.ingamePowerupButton.disable();
            tPowerupEndScheduled = getRenderTime() + level.powerupEffectTime;

            // Enable powerup piss and reschedule to refresh at the end of powerup
            tBirdPissStateRefreshScheduled = tPowerupEndScheduled;
            tBirdPissVelocityRefreshScheduled = getRenderTime();
            birdPissState = true;

            // Charging
            s.ingamePowerupChargingView.attach();
            s.ingamePowerupChargingView.windowAnimation(s.ingamePowerupChargingAnim.startAndReset(), false, true);

            // Effect
            timeMultiplier = level.powerupSpeedMultiplier;
            isBirdAutopilot = true;

            // Play theme again
            Audio.playMusic(level.powerupMusic, true, sfxVolume == 1f ? 1f : s.demonVoiceBgMusicVolume);

            // Achievement
            ACT1.unlockAchievement(Globals.Achievement.JETSTREAM_IN_FB);

            return;
        }

        if(view == s.chanceReviveButton) {
            if(!consumeEggs(stage.reviveCosts[stageReviveCostIndex]))
                return;     // not enough eggs

            // Increase cost
            if(stageReviveCostIndex < (stage.reviveCosts.length - 1)) {
                stageReviveCostIndex++;
                s.chanceReviveCostView.text(Integer.toString(stage.reviveCosts[stageReviveCostIndex]));
            }

            // Trigger
            v.trigger(Globals.TRIGGER_FLAPEE_POWERUPS_USED);        // return ignored

            // Analytics
            Game.analyticsEvent(Globals.ANALYTICS_EVENT_FLAPEEBIRD_REVIVED);

            // Revive bird
            birdVelocityY = 0;
            tBirdPissVelocityRefreshScheduled = 0;
            birdPissState = false;
            pissEffect.clear();
            stopPissEffectSound();

            tDeathStarted = -1;
            isBirdAutopilot = true;
            isBirdReviving = true;
            timeMultiplier = level.powerupSpeedMultiplier;

            s.chanceMenu.detachWithAnim();

            stageGeneratedRemaining = stageScoreRemaining;      // Generate up to score

            // Sound
            stopRevivingSound();
            level.revivedSound.play(sfxVolume);
            Audio.resumeMusic();

            // Achievement
            ACT1.unlockAchievement(Globals.Achievement.REVIVE_IN_FB);

            return;
        }

        if(view == s.chanceDieButton) {
            tDeathStarted = -Float.MAX_VALUE;       // Skip to death

            return;
        }

        if(view == s.moreEggsWatchAdButton || view == s.moreEggsWatchAdSingleButton) {
            // Watch ad
            if(!Globals.g_showRealAds || !Game.game.platform.showRewardedVideoAd()) {
                adScreen.show(false);
                adScreen.open(false);

                queueReward(30);        // TODO: parameterize and balancing
            }
            else {
                // Close menu and wait for ad network to show reward menu
                s.menuGroup.detachWithAnim();
                s.moreEggsGroup.detachWithAnim();
                // showRewardMenu();
            }

            // Remember to remove sticker
            hasWatchedAd = true;

            return;
        }

        if(view == s.rewardAcceptButton) {
            if(tRewardEndScheduled == Float.MAX_VALUE) {
                if(getRenderTime() > tRewardStartScheduled) {
                    boolean isAbleToShop = eggsPurchased < s.shopPurchasedThreshold;
                    eggs += queuedReward;
                    if(queuedReward < 0)
                        eggsPurchased += queuedReward;
                    eggsPurchased += queuedRewardPurchased;
                    if(eggsPurchased < 0)
                        eggsPurchased = 0;
                    if(eggsPurchased < s.shopPurchasedThreshold && !isAbleToShop)
                        hasSpentLifehours = false;

                    eggsTransitionFrom = eggsTransitionCurrent;
                    tEggsTransitionStarted = getRenderTime();
                    queuedReward = 0;
                    queuedRewardPurchased = 0;

                    // Animate
                    s.rewardVisualView.visual(s.rewardVisualOpen).windowAnimation(s.rewardOpenAnim.startAndReset(), true, false);

                    s.rewardTextView.attach();
                    s.rewardAcceptText.detach();

                    // Sound
                    s.rewardOpenSound.play();

                    // End
                    tRewardEndScheduled = getRenderTime() + s.tRewardEndDelay;
                }
            }
            else {
                // Close immediately
                tRewardEndScheduled = Float.MAX_VALUE;
                tRewardStartScheduled = Float.MAX_VALUE;

                s.inputBlockerView.detachWithAnim();
                s.rewardGroup.detachWithAnim();
                s.rewardAcceptButton.detach();

                if(!showDialogMenu())
                    showMenu(false);
            }

            return;
        }

        if(view == s.permissionAcceptButton) {
            // Permissions accepted, mark as done
            v.unlockState(Globals.STATE_FLAPEE_PERMISSIONS_ACCEPTED);
            s.permissionGroup.detachWithAnim();
            // Show send eggs
            s.sendingEggsGroup.attach();
            s.sendingEggsGroup.iterate(null, HorizontalProgressBar.class, false, null).progress(0).seek(1f, s.tSendingEggsTime);

            tSendingEggsEndScheduled = getRenderTime() + s.tSendingEggsTime;
            return;
        }

        if(view == s.permissionDenyButton) {
            // Close permissions group and show back loading (which will show the permissions group again)
            s.permissionGroup.detachWithAnim();
            tLoadingEndScheduled = getRenderTime() + s.tLoadingTime;
            s.loadingView.attach();
            return;
        }

        // Dialog box
        for(int c = 0; c < s.dialogStarViews.length; c++) {
            Clickable starView = s.dialogStarViews[c];
            if(view == starView) {
                // Clicked on star
                // Update visuals
                for(int i = 0; i < s.dialogStarViews.length; i++) {
                    starView = s.dialogStarViews[i];
                    if(i <= c) {
                        if(i == (s.dialogStarViews.length - 1))
                            starView.visuals(s.dialogStarTopMat);       // Special for last
                        else
                            starView.visuals(s.dialogStarSelectedMat);
                    }
                    else
                        starView.visuals(s.dialogStarDeselectedMat);
                }
                // Remember
                dialogStarsSelected = c + 1;
                return;
            }
        }

        // Dialog buttons
        if(view == s.dialogPositiveButton || view == s.dialogNegativeButton || view == s.dialogSingleButton) {
            // If stars are shown, require stars
            if(s.dialogStarGroup.isAttached() && dialogStarsSelected == -1) {
                // Animate hint
                for(Clickable starView : s.dialogStarViews)
                    starView.windowAnimation(s.dialogStarHintAnim.startAndReset(), true, false);
                // Sound
                s.eggsNotEnoughSound.play();
                return;
            }
            Runnable action;
            if(view == s.dialogPositiveButton || view == s.dialogSingleButton)
                action = queuedDialog.positiveButtonAction;
            else //  if(view == s.dialogNegativeButton)
                action = queuedDialog.negativeButtonAction;

            // Clear dialog and run action
            queuedDialog = null;
            if(action != null)
                action.run();

            // Check effects after action
            if(queuedReward != 0)
                showRewardMenu();       // A reward was queued
            else if(queuedDialog != null)
                tDialogShowScheduled = getRenderTime() + s.tDialogRefreshDelay;     // Another dialog has to show
            else {
                // Finished, show loading and back to menu
                s.loadingView.attach();
                tLoadingEndScheduled = getRenderTime() + s.tLoadingTime;
            }
            s.dialogContainer.detachWithAnim();
            return;
        }

        // Showdown giveup
        if(si != null) {
            if(view == si.giveupYesButton) {
                // Trigger
                v.trigger(Globals.TRIGGER_FLAPEE_SHOWDOWN_GIVEUP);
                return;
            }

            if(view == si.giveupNoButton) {
                si.giveupGroup.detachWithAnim();

                // Show back menu
                showMenu(false);
                return;
            }

            if(view == si.subscribeYesButton) {
                // Hide subscribe menu
                si.subscribeGroup.detachWithAnim();

                // Start life drain
                tNextLifehoursDrainScheduled = 0;
                s.lifehoursView.attach();

                // Start level again
                onClick(v, s.playButton, 0);

                // Play subscribed voice
                startDemonVoice(si.subscribedVoice, false);
                return;
            }

            if(view == si.subscribeNoButton) {
                // Hide subscribe menu
                si.subscribeGroup.detachWithAnim();

                // Back to main menu
                showMenu(false);
                return;
            }

            if(view == si.showdownAcceptButton) {
                // Hide accept menu
                si.showdownGroup.detachWithAnim();

                // Proceed to main menu
                showMenu(false);
            }

            if(view == si.showdownLaterButton) {
                // Return to homescreen
                v.homescreen.transitionBack(this, v);
                return;
            }
        }
    }

    @Override
    public void onPressed(Grid v, UIElement<?> view, float x, float y, int button) {
        if(view == s.tapView && !isBirdAutopilot) {
            // Flap bird
            flap();

            // First tap
            if(isBirdHovering) {
                isBirdHovering = false;
                s.tutorialView.detach();
                s.readyView.attach();
                if(isAdvancedPlayer) {
                    s.ingamePowerupButton.attach();
                }

                // Start charging
                if(s.ingamePowerupChargingView.isAttached()) {
                    tPowerupChargingStarted = getRenderTime();
                }
            }

            return;
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(key == Input.Keys.SPACE && inputType == INPUT_KEY_DOWN) {
            // Keyboard to flap
            onPressed(v, s.tapView, 0, 0, 0);
            return false;
        }

//            if(viewport.isAttached())
//                viewport.detach();
//            else
//                viewport.attach(this);
//            return true;
//        }


//        if(key == Input.Keys.F10 && inputType == INPUT_KEY_UP) {     // disable on release
//            if(viewport.isAttached())
//                viewport.detach();
//            else
//                viewport.attach(this);
//            return true;
//        }
//        if(key == Input.Keys.F9 && inputType == INPUT_KEY_UP) {     // disable on release
//            isBirdAutopilot = !isBirdAutopilot;
//            return true;
//        }
        if(!viewport.isAttached() && s.tapView.isAttached() && inputType == INPUT_TOUCH_DOWN) {
            onPressed(v, s.tapView, 0, 0, 0);
            return false;
        }
        return super.input(v, inputType, key, character, scrolledAmount, pointer, x, y, button);
    }

    @Override
    public Entity<?> open() {
        // Reset to main menu
        s.window.detachChilds();        // detach all
        if(isAdvancedPlayer)
            s.royaleTitleView.attach();
        else
            s.titleView.attach();
        s.bars.navbar().windowAnimation(null, false, false);

        // Reset menus
        if(queuedReward != 0)
            tLoadingEndScheduled = Float.MAX_VALUE;
        else {
            tLoadingEndScheduled = getRenderTime() + s.tLoadingTime;
            s.loadingView.attach();
        }
        tFindingFriendsEndScheduled = Float.MAX_VALUE;
        tSendingEggsEndScheduled = Float.MAX_VALUE;

        tDemonIdleChatterScheduled = Float.MAX_VALUE;

        timeMultiplier = 1f;        // Reset powerup

        startLevel(true);

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_TYPE_FLAPEEBIRD);

        return this;            // Open this screen
    }

}

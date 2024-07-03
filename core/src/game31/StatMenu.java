package game31;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.Locale;

import game31.gb.GBStatMenu;
import game31.gb.flapee.GBDemonLevel;
import game31.triggers.ACT1;
import sengine.Sys;
import sengine.calc.Range;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 9/25/2017.
 */

public class StatMenu extends Menu<Grid> implements OnClick<Grid> {
    private static final String TAG = "StatMenu";

    private static final String POLL_URL = "http://simulacragame.appspot.com/?";
    private static final int POLL_MAX_TRIES = 3;

    private static final String[] POLL_KEYS = {
            "pd_teddy_trusts",
            "pd_used_more_than_halflife",
            "pd_support_pay_to_win",
            "pd_shared_to_all",
            "pd_used_subscription"
    };

    public static class StatModel {

        public static class EntryModel {
            public int yes;
            public int no;

            public float calculatePercentage(boolean side) {
                int total = yes + no;
                if(side)
                    return (float)yes / (float)total;       // sided with yes
                else
                    return (float)no / (float)total;       // sided with no
            }
        }

        public EntryModel pd_teddy_trusts;
        public EntryModel pd_used_more_than_halflife;
        public EntryModel pd_support_pay_to_win;
        public EntryModel pd_shared_to_all;
        public EntryModel pd_used_subscription;
    }


    public static class Internal {
        public UIElement<?> window;

        public StaticSprite bgView;
        public Sprite[] bgs;

        public Sprite[] endIcons;
        public Sprite[] endIconsDisabled;

        public StaticSprite[] endIconViews;
        public StaticSprite[] endIconArrows;

        public TextBox endTitleView;
        public String[] endTitles;

        public TextBox endNoticeView;
        public String[] endNotice;

        public TextBox opinionView;
        public Clickable opinionYesButton;
        public Clickable opinionNoButton;
        public TextBox reviewView;
        public Clickable reviewYesButton;
        public Clickable reviewNoButton;

        public TextBox socialView;
        public Clickable socialTwitterButton;
        public Clickable socialFbButton;
        public Clickable socialNextButton;

        public UIElement[] statViews;
        public TextBox[] statTextViews;
        public StaticSprite[] statProgressViews;
        public String[] statYesTexts;
        public String[] statNoTexts;

        public Clickable nextButton;
        public Clickable endButton;

        public StaticSprite loadingView;
        public float tMinLoadingTime;
    }


    private final Builder<Object> builder;
    private Internal s;

    private Runnable onFinish = null;

    private boolean[] pollChoices;
    private int pollTries = 0;
    private float[] pollResults = null;

    private void sendPollRequest() {
        // Build URL
        StringBuilder sb = new StringBuilder();
        sb.append(POLL_URL);
        for (int c = 0; c < POLL_KEYS.length; c++) {
            if (c > 0)
                sb.append('&');
            sb.append(POLL_KEYS[c]);
            sb.append('=');
            sb.append(pollChoices[c] ? "true" : "false");
        }
        String url = sb.toString();

        Sys.info(TAG, "Requesting \"" + url + "\"");

        // Send net request
        Net.HttpRequest request = new Net.HttpRequest();
        request.setUrl(url);
        request.setMethod(Net.HttpMethods.GET);
        Gdx.net.sendHttpRequest(request, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                try {
                    if(httpResponse.getStatus().getStatusCode() != HttpStatus.SC_OK)
                        throw new RuntimeException("Unexpected status code " + httpResponse.getStatus().getStatusCode());
                    String response = httpResponse.getResultAsString();
                    Sys.info(TAG, "Received poll results\n" + response);
                    StatModel model = Globals.gson.fromJson(response, StatModel.class);
                    pollReceiveRequest(model);
                } catch (Throwable e) {
                    failed(e);
                }
            }

            @Override
            public void failed(Throwable t) {
                Sys.error(TAG, "Request failed", t);
                pollRequestFailed();
            }

            @Override
            public void cancelled() {
                failed(new RuntimeException("Request cancelled"));
            }
        });
    }

    private void pollReceiveRequest(StatModel model) {
        pollResults = new float[] {
                model.pd_teddy_trusts.calculatePercentage(pollChoices[0]),
                model.pd_used_more_than_halflife.calculatePercentage(pollChoices[1]),
                model.pd_support_pay_to_win.calculatePercentage(pollChoices[2]),
                model.pd_shared_to_all.calculatePercentage(pollChoices[3]),
                model.pd_used_subscription.calculatePercentage(pollChoices[4]),
        };
    }

    private void pollRequestFailed() {
        // Try again if can
        pollTries++;
        if(pollTries < POLL_MAX_TRIES) {
            // Queue try again
            Globals.grid.scheduleRunnable(new Runnable() {
                @Override
                public void run() {
                    sendPollRequest();
                }
            }, 3f);     // Try again after 3 seconds
            return;
        }
        // Else accept failure, randomly generate stats
        pollResults = new float[POLL_KEYS.length];
        for(int c = 0; c < pollResults.length; c++) {
            pollResults[c] = Range.generateFor(0.4f, 0.2f, false);
        }
    }

    public void setInternal(Internal internal) {
        if (s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }


    public void show(Runnable onFinish,
                     boolean usedSubscription,
                     boolean givesUp
    ) {
        this.onFinish = onFinish;

        Grid v = Globals.grid;

        boolean teddyTrusts = ACT1.getTeddyTrustPoints() > Globals.g_teddyTrustSplit;

        int endingType;
        int outroType;

        if(teddyTrusts) {
            if(usedSubscription) {
                // teddy lives
                endingType = 1;
                outroType = 0;
            }
            else if(givesUp) {
                // player lives
                endingType = 2;
                outroType = 2;
            }
            else {
                // both live
                endingType = 3;
                outroType = 1;
            }
        }
        else {
            if(usedSubscription) {
                // both die
                endingType = 0;
                outroType = 3;
                if(Globals.grid.flapeeBirdApp.levelSource != GBDemonLevel.class) {
                    outroType = 6;      // Special ending, consumed all lifehours before demon level
                    usedSubscription = false;
                }
            }
            else if(givesUp) {
                // player lives
                endingType = 2;
                outroType = 5;
            }
            else {
                // player lives
                endingType = 2;
                outroType = 4;
            }
        }

        // Achievement
        if(endingType == 0) {
            ACT1.unlockAchievement(Globals.Achievement.ENDING_BOTH_DIE);
            Game.analyticsString("Ending", "Type", "Both Die");
        }
        else if(endingType == 1) {
            ACT1.unlockAchievement(Globals.Achievement.ENDING_PLAYER_DIES);
            Game.analyticsString("Ending", "Type", "Player Dies");
        }
        else if(endingType == 2) {
            ACT1.unlockAchievement(Globals.Achievement.ENDING_TEDDY_DIES);
            Game.analyticsString("Ending", "Type", "Teddy Dies");
        }
        else { // if(endingType == 3) {
            ACT1.unlockAchievement(Globals.Achievement.ENDING_BOTH_SURVIVE);
            Game.analyticsString("Ending", "Type", "Both Survive");
        }


        // Stats
        int startingLifehours = v.state.get("stats.startingLifehours", 1);
        int currentLifehours = v.flapeeBirdApp.getLifehours();
        float percentageLifehoursUsed = (float)(startingLifehours - currentLifehours) / (float)startingLifehours;
        boolean usedMoreThanHalfLife = percentageLifehoursUsed > 0.5f;

        boolean supportPayToWin = v.isStateUnlocked("stats.supportPayToWin");

        boolean sharedToAll = v.isStateUnlocked("chats.jenny.fp_invited") &&
                v.isStateUnlocked("chats.auntkaren.fp_invited") &&
                v.isStateUnlocked("chats.liam.fp_invited") &&
                v.isStateUnlocked("chats.max.fp_invited") &&
                v.isStateUnlocked("chats.chad.fp_invited");

        Sprite bgSprite = s.bgs[endingType];
        if (bgSprite.length != Globals.LENGTH) {              // Crop according to length
            bgSprite = new Sprite(bgSprite.length, bgSprite.getMaterial());
            bgSprite.crop(Globals.LENGTH);
        }
        s.bgView.visual(bgSprite);

        // Win condition icon, with arrow
        for(int c = 0; c < s.endIcons.length; c++) {
            if(c == endingType) {
                s.endIconViews[c].visual(s.endIcons[c]);        // enabled
                s.endIconArrows[c].attach();
            }
            else {
                s.endIconViews[c].visual(s.endIconsDisabled[c]);        // disabled
                s.endIconArrows[c].detach();
            }
        }

        // End title
        s.endTitleView.text(s.endTitles[endingType]);

        // Reset
        for(int c = 0; c < s.statViews.length; c++)
            s.statViews[c].detach();

        // Update stats
        pollChoices = new boolean[] {
                teddyTrusts,
                usedMoreThanHalfLife,
                supportPayToWin,
                sharedToAll,
                usedSubscription
        };
        pollTries = 0;

        // Send request
        sendPollRequest();

        // Show loading
        s.loadingView.attach();

        // Reset notice
        s.endNoticeView.detach();
        s.endNoticeView.text(s.endNotice[outroType]);

        // End button
        s.nextButton.attach();
        s.endButton.detach();

        // Opinion and review
        s.opinionView.detach();
        s.opinionYesButton.detach();
        s.opinionNoButton.detach();
        s.reviewView.detach();
        s.reviewYesButton.detach();
        s.reviewNoButton.detach();

        // Social
        s.socialView.detach();
        s.socialTwitterButton.detach();
        s.socialFbButton.detach();
        s.socialNextButton.detach();
    }

    private void showPollStats() {
        for(int c = 0; c < s.statViews.length; c++) {
            // Convert to percentage
            float progress = Math.max(Math.min(pollResults[c], 100), 0);
            int percentage = Math.round(progress * 100f);
            // Update text view
            String format = pollChoices[c] ? s.statYesTexts[c] : s.statNoTexts[c];
            s.statTextViews[c].text(String.format(Locale.US, format, percentage));
            // Update progress
            s.statProgressViews[c].metrics.scaleX = progress;
            // Show
            s.statViews[c].attach();
        }
    }

    public StatMenu() {
        builder = new Builder<Object>(GBStatMenu.class, this);
        builder.build();


    }

    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        // Save finished game state
        try {
            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_FINISHED_GAME, true).flush();
        } catch (Throwable e) {
            Sys.error(TAG, "Unable to save finished game state", e);
        }

        // Analytics
        Game.analyticsView(Globals.ANALYTICS_CONTENT_MAIN_MENU_STATS, Globals.ANALYTICS_CONTENT_TYPE_MAIN_MENU);
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(s.loadingView.isAttached() && renderTime > s.tMinLoadingTime && pollResults != null) {
            s.loadingView.detach();
            showPollStats();
        }
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int button) {
        if(view == s.nextButton) {
            // Hide all stats
            for(int c = 0; c < s.statViews.length; c++)
                s.statViews[c].detachWithAnim();
            s.nextButton.detachWithAnim();
            s.loadingView.detach();
            s.endNoticeView.attach();
            s.endButton.attach();
            return;
        }

        if(view == s.endButton) {
            // Ask for opinion
            s.endNoticeView.detachWithAnim();
            s.endButton.detachWithAnim();
            s.opinionView.attach();
            s.opinionYesButton.attach();
            s.opinionNoButton.attach();
            return;
        }

        if(view == s.opinionYesButton) {
            // Liked the game, should ask to review
            s.opinionView.detachWithAnim();
            s.opinionYesButton.detachWithAnim();
            s.opinionNoButton.detachWithAnim();
            s.reviewView.attach();
            s.reviewYesButton.attach();
            s.reviewNoButton.attach();

            // Remember opinion (to know if want to open discord link)
            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_OPINION_ENJOYED, true).flush();
            return;
        }


        if(view == s.opinionNoButton || view == s.reviewYesButton || view == s.reviewNoButton) {

            if(view == s.opinionNoButton) {
                // Remember opinion (to know if want to open discord link)
                Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_OPINION_ENJOYED, false).flush();
            }

            // Would want to review
            if(view == s.reviewYesButton) {
                // Analytics
                Game.analyticsEvent(Globals.ANALYTICS_EVENT_RATED);

                Game.game.platform.openReviewPage();
            }

            // Show social button
            s.opinionView.detachWithAnim();
            s.opinionYesButton.detachWithAnim();
            s.opinionNoButton.detachWithAnim();
            s.reviewView.detachWithAnim();
            s.reviewYesButton.detachWithAnim();
            s.reviewNoButton.detachWithAnim();

            s.socialView.attach();
            s.socialTwitterButton.attach();
            s.socialFbButton.attach();
            s.socialNextButton.attach();
            return;
        }

        if(view == s.socialTwitterButton) {
            // Analytics
            Game.analyticsString(Globals.ANALYTICS_EVENT_SHARED, Globals.ANALYTICS_EVENT_SHARED_FIELD, Globals.ANALYTICS_EVENT_SHARED_TWITTER);
            Game.game.platform.openURI(Globals.helpTwitterURL);

//            Gdx.net.openURI(Globals.helpTwitterURL);
            return;
        }

        if(view == s.socialFbButton) {
            // Analytics
            Game.analyticsString(Globals.ANALYTICS_EVENT_SHARED, Globals.ANALYTICS_EVENT_SHARED_FIELD, Globals.ANALYTICS_EVENT_SHARED_FB);
            Game.game.platform.openURI(Globals.helpFacebookURL);

//            Gdx.net.openURI(Globals.helpFacebookURL);
            return;
        }

        if(view == s.socialNextButton) {
            if(onFinish != null)
                onFinish.run();
            return;
        }
    }

}

package game31;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Matrix4;

import game31.gb.GBMainMenu;
import game31.triggers.MusicFadeOutEntity;
import sengine.Entity;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.audio.Stream;
import sengine.calc.Range;
import sengine.calc.SetRandomizedSelector;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Mesh;
import sengine.graphics2d.Sprite;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;
import sengine.utils.LoadingMenu;

/**
 * Created by Azmi on 9/4/2017.
 */

public class MainMenu extends Menu<Grid> implements OnClick<Grid> {


    public static class ParticleType {
        public SetRandomizedSelector<Sprite> mats;
        public int target;
        public Range scale;
        public Range x;
        public Range y;
        public Range xSpeed;
        public Range ySpeed;
        public Range rotateSpeed;
        public Animation startAnim;
        public Animation idleAnim;
        public Animation endAnim;

        public Range tLifeTime;
    }

    private static class Particle extends Entity<Grid> {
        private final ParticleType type;
        private final Mesh mat;
        private final float scale;
        private final float x;
        private final float y;
        private final float rotate;
        private final float xSpeed;
        private final float ySpeed;
        private final float rotateSpeed;
        private final Animation.Instance startAnim;
        private final Animation.Loop idleAnim;
        private final Animation.Instance endAnim;

        private float tEndScheduled;

        public Particle(ParticleType type) {
            this.type = type;

            mat = type.mats.select();
            scale = type.scale.generate();
            x = type.x.generate();
            y = type.y.generate();
            rotate = (float) (Math.random() * 360f);
            xSpeed = type.xSpeed.generate();
            ySpeed = type.ySpeed.generate();
            rotateSpeed = type.rotateSpeed.generate();

            startAnim = type.startAnim.startAndReset();
            idleAnim = type.idleAnim.loopAndReset();
            idleAnim.setProgress((float) Math.random());
            endAnim = type.endAnim.start();

            tEndScheduled = type.tLifeTime.generate();
        }

        @Override
        protected void render(Grid v, float r, float renderTime) {
            Matrices.push();
            Matrices.target = type.target;
            Matrix4 m = Matrices.model;

            m.translate(
                    x + xSpeed * renderTime,
                    y + ySpeed * renderTime,
                    0
            );
            m.scale(scale, scale, scale);
            m.rotate(0, 0, -1, rotate + rotateSpeed * renderTime);

            if(startAnim.isActive())
                startAnim.updateAndApply(mat, getRenderDeltaTime());
            idleAnim.updateAndApply(mat, getRenderDeltaTime());
            if(endAnim.isActive() && !endAnim.updateAndApply(mat, getRenderDeltaTime())) {
                // Finished, spawn another
                Particle p = new Particle(type);
                p.attach(getEntityParent());
                detach();
            }

            if(renderTime > tEndScheduled) {
                endAnim.reset();
                tEndScheduled = Float.MAX_VALUE;
            }

            mat.render();

            Matrices.pop();
        }
    }

    public static class Internal {
        public UIElement<?> window;

        public UIElement<?> screenGroup;
        public StaticSprite offShadow;
        public StaticSprite baseGlow;
        public StaticSprite fullGlow;
        public StaticSprite screen;

        public ParticleType particle;
        public int numParticles;


        public StaticSprite title;
        public Clickable continueButton;
        public Clickable restartButton;
        public Clickable newGameButton;
        public Clickable newGamePlusButton;
        public Clickable creditsButton;
        public Clickable quitButton;
        public TextBox versionView;
        public float tTitleHideTime;
        public float tNewGameMusicDelay;

        public Clickable simAdButton;

        public UIElement.Group betaFeedbackGroup;
        public Clickable betaFeedbackButton;

        public TextBox newGameWarningView;
        public Clickable newGameYesButton;
        public Clickable newGameNoButton;

        // Google play
        public Clickable googlePlayButton;

        public UIElement<?> googlePlayPrompt;
        public UIElement<?> googlePlayBg;
        public StaticSprite googlePlayLoadingView;
        public Clickable googlePlayPromptLoginButton;
        public Clickable googlePlayPromptContinueButton;

        public Clickable newGamePlusYesButton;
        public Clickable newGamePlusNoButton;

        // Help menu
        public Clickable helpButton;
        public TextBox helpTitleView;
        public Clickable twitterButton;
        public Clickable mailButton;
        public Clickable fbButton;
        public Clickable helpBackButton;

        // Discord
        public Clickable discordButton;
        public Animation discordNewAnim;

        // Remove ads
        public Clickable removeAdsButton;

        public UIElement<?> headphonesGroup;
        public float tHeadphonesTime;

        public UIElement<?> kaiganGroup;
        public float tKaiganTime;

        public String themeMusic;
        public float tThemeTime;
        public float tThemeFadeOutTime;

        // Loading indicator
        public UIElement<?> loadingView;
        public float tLoadingViewDelay;

        // Loading menu
        public LoadingMenu loadingMenu;

        // Timing
        public float tLeaveTitleDelay;

        // Subtitles
        public Clickable subtitleButton;
        public UIElement<?> subtitleEnabledView;
        public TextBox subtitleLabelView;

        // High Quality Videos
        public Clickable highQualityVideosButton;
        public UIElement<?> highQualityVideosEnabledView;
        public TextBox highQualityVideosLabelView;


        public Clickable privacyPolicyButton;

        public Animation doneBgAnim;
        public Animation doneBgLoopAnim;
    }



    // UI
    private final Builder<Object> builder;
    private Internal s;

    private final Group particleGroup;

    private float tHeadphonesEndScheduled = Float.MAX_VALUE;
    private float tKaiganEndScheduled = Float.MAX_VALUE;
    private float tThemeScheduled = Float.MAX_VALUE;
    private float tLoadingViewScheduled = Float.MAX_VALUE;
    private boolean showingCredits = false;
    private boolean doneLoading = false;
    private boolean isFoundSave = false;
    private boolean isNewGamePlus = false;

    private boolean isShowingSimAd = false;

    private boolean isQueuedOpenAchievements = false;
    private boolean isQueuedStartGame = false;

    private boolean isLoadedSaves = false;

    private void showGooglePrompt() {
        s.googlePlayBg.attach();
        s.googlePlayPrompt.attach();
        s.googlePlayLoadingView.detach();
    }

    private void hideGooglePrompt() {
        s.googlePlayBg.detachWithAnim();
        s.googlePlayPrompt.detachWithAnim();
        s.googlePlayLoadingView.detachWithAnim();
    }

    private void showTitleMenu() {
        if(Gdx.app.getType() == Application.ApplicationType.Desktop)
            s.quitButton.attach();
        if(Game.game.platform.showGameCenter()) {
            s.googlePlayButton.attach();
        }


        if(Globals.d_showBetaFeedback)
            s.betaFeedbackGroup.attach();

        s.creditsButton.attach();
        s.title.attach();

        if(Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HAS_PLAYED, false)) {
            s.simAdButton.attach();
            isShowingSimAd = true;
        }
        else
            isShowingSimAd = false;

        // Check save
        if(isFoundSave) {
            if(isNewGamePlus) {
                s.newGamePlusButton.attach();
                s.restartButton.detach();
            }
            else {
                s.restartButton.attach();
                s.newGamePlusButton.detach();
            }
            s.continueButton.attach();
            s.newGameButton.detach();
        }
        else {
            s.newGameButton.attach();
            s.continueButton.detach();
            s.restartButton.detach();
            s.newGamePlusButton.detach();
        }

        s.versionView.attach();
        s.helpButton.attach();

        s.discordButton.attach();
        if(Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HAS_DISCORD_OPENED, false))
            s.discordButton.windowAnimation(null, false, false);
        else
            s.discordButton.windowAnimation(s.discordNewAnim.loopAndReset(), true, true);

        refreshAdsRemoved();
    }

    private void hideTitleMenu() {
        s.title.detachWithAnim();
        s.continueButton.detachWithAnim();
        s.restartButton.detachWithAnim();
        s.newGameButton.detachWithAnim();
        s.newGamePlusButton.detachWithAnim();
        s.creditsButton.detachWithAnim();
        s.quitButton.detachWithAnim();

        s.simAdButton.detachWithAnim();

        s.versionView.detachWithAnim();
        s.helpButton.detachWithAnim();
        s.discordButton.detachWithAnim();

        s.googlePlayButton.detachWithAnim();
        s.removeAdsButton.detachWithAnim();

        s.betaFeedbackGroup.detachWithAnim();
    }

    private void showHelpMenu() {
        s.helpTitleView.attach();
        s.twitterButton.attach();
        s.mailButton.attach();
        s.fbButton.attach();
        s.helpBackButton.attach();

        s.subtitleLabelView.attach();
        s.subtitleButton.attach();
        refreshSubtitleStatus();

//        if(Gdx.app.getType() == Application.ApplicationType.Desktop) {
            s.highQualityVideosButton.attach();
            s.highQualityVideosLabelView.attach();
            refreshHighQualityVideosStatus();
//        }

        s.privacyPolicyButton.attach();
    }

    private void refreshSubtitleStatus() {
        boolean showSubtitles = Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_SUBTITLE_ENABLED, false);
        if(showSubtitles)
            s.subtitleEnabledView.attach();
        else
            s.subtitleEnabledView.detach();
    }

    private void refreshHighQualityVideosStatus() {
        Globals.r_highQuality = Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HQ_VIDEOS_ENABLED, true);      // By default is on
        if(Globals.r_highQuality)
            s.highQualityVideosEnabledView.attach();
        else
            s.highQualityVideosEnabledView.detach();
    }

    private void refreshAdsRemoved() {
        if(Gdx.app.getType() == Application.ApplicationType.Desktop)
            Globals.g_showRealAds = false;
        else {
            Globals.g_showRealAds = !Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_HAS_ADS_REMOVED, false);      // By default is on
            if(Globals.g_showRealAds)
                Game.game.platform.checkRemovedAds();
        }
        if(Globals.g_showRealAds && isShowingSimAd)
            s.removeAdsButton.attach();
        else
            s.removeAdsButton.detach();
    }

    public void informHasRemovedAds() {
        Globals.g_showRealAds = false;
        Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_ADS_REMOVED, true).flush();
        s.removeAdsButton.detach();
    }

    private void hideHelpMenu() {
        s.helpTitleView.detachWithAnim();
        s.twitterButton.detachWithAnim();
        s.mailButton.detachWithAnim();
        s.fbButton.detachWithAnim();
        s.subtitleLabelView.detachWithAnim();
        s.subtitleButton.detachWithAnim();
        s.highQualityVideosLabelView.detachWithAnim();
        s.highQualityVideosButton.detachWithAnim();
        s.privacyPolicyButton.detachWithAnim();
        s.helpBackButton.detachWithAnim();
    }


    private void showNewGameWarning() {
        s.newGameYesButton.attach();
        s.newGameNoButton.attach();
        s.newGameWarningView.attach();
    }

    private void hideNewGameWarning() {
        s.newGameYesButton.detachWithAnim();
        s.newGameNoButton.detachWithAnim();
        s.newGameWarningView.detachWithAnim();
    }

    private void startGameLoad() {
        hideTitleMenu();

        // Mark as has played, show simulacra promo button next time
        Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_PLAYED, true).flush();

        tLoadingViewScheduled = getRenderTime() + s.tLoadingViewDelay;
        Globals.grid.startLoad(s.loadingMenu);
    }

    public void doneLogin(boolean success, boolean savegameFound) {
        isLoadedSaves = true;

        hideGooglePrompt();

        if(isFoundSave != savegameFound) {
            // Upate title menu and done
            isQueuedOpenAchievements = false;
            isQueuedStartGame = false;
            isFoundSave = savegameFound;
            if(s.title.isAttached())
                showTitleMenu();
            return;
        }

        if(!success) {
            isQueuedOpenAchievements = false;
            isQueuedStartGame = false;
            return;
        }

        if(isQueuedOpenAchievements) {
            isQueuedOpenAchievements = false;
            Game.game.platform.openGameCenter();
        }
        else if(isQueuedStartGame) {
            isQueuedStartGame = false;
            startGameLoad();
        }
    }

    void doneLoading() {
        doneLoading = true;
        s.offShadow.detachWithAnim();

        if(!isFoundSave)
            Globals.grid.screen.animateBackground(s.doneBgAnim, s.doneBgLoopAnim);

        tLoadingViewScheduled = Float.MAX_VALUE;
        s.loadingView.detachWithAnim();
    }


    private void refreshParticles() {
        particleGroup.detachChilds();
        for(int c = 0; c < s.numParticles; c++) {
            Particle p = new Particle(s.particle);
            p.startAnim.stop();
            p.attach(particleGroup);
        }
    }

    public void internal(Internal internal) {
        if(s != null) {
            s.window.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();

        if(isAttached())
            refreshParticles();
    }

    public MainMenu() {
        builder = new Builder<Object>(GBMainMenu.class, this);
        builder.build();

        particleGroup = new Group();
        particleGroup.attach(this);
    }


    @Override
    protected void recreate(Grid grid) {
        super.recreate(grid);

        builder.start();

        refreshParticles();
//        if(Gdx.app.getType() == Application.ApplicationType.Desktop)
            refreshHighQualityVideosStatus();

        // Check save
        isFoundSave = Game.game.platform.existsSaveGame();

        // Stop all sound effects
        Stream.stopAllStreams();

        if(grid.skipIntoMainMenu) {
            isLoadedSaves = true;
            s.screenGroup.attach();
            showTitleMenu();
            tThemeScheduled = getRenderTime();      // Start theme immediately
        }
        else {
            // Else start from beginning
            isLoadedSaves = false;
            tHeadphonesEndScheduled = getRenderTime() + s.tHeadphonesTime;
            tKaiganEndScheduled = Float.MAX_VALUE;
            s.headphonesGroup.attach();
        }

        isQueuedOpenAchievements = false;
        isQueuedStartGame = false;

        showingCredits = false;
    }

    @Override
    protected void release(Grid grid) {
        super.release(grid);

        builder.stop();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(!s.offShadow.isAttached() && tHeadphonesEndScheduled == Float.MAX_VALUE) {
            if(tKaiganEndScheduled != Float.MAX_VALUE) {
                // Showing kaigan logo
                s.headphonesGroup.detach();
                s.kaiganGroup.attach();
                s.screenGroup.attach();
            }
            else if(showingCredits) {
                // Show credits
                s.screenGroup.detach();
                detach();
                Globals.grid.creditsMenu.show(new Runnable() {
                    @Override
                    public void run() {
                        // Detach credits, enter main menu
                        Globals.grid.creditsMenu.detach();
                        Globals.grid.skipIntoMainMenu = true;
                        Globals.grid.mainMenu.attach(Globals.grid.compositor);
                    }
                });
                return;
            }
            else if(doneLoading) {
                s.screenGroup.detach();
                detach();
//                float tStartDelay = isFoundSave ? 0 : s.tNewGameMusicDelay;
//                MusicFadeOutEntity fadeOutEntity = new MusicFadeOutEntity(tStartDelay, s.tThemeFadeOutTime);
                MusicFadeOutEntity fadeOutEntity = new MusicFadeOutEntity(0, s.tThemeFadeOutTime);
                fadeOutEntity.attach(grid);
                grid.start();
                return;
            }
            else {
                // Showing title
                s.kaiganGroup.detach();
                showTitleMenu();
            }
            s.offShadow.attach();
        }

        if(renderTime > tHeadphonesEndScheduled && isLoadedSaves) {
            tHeadphonesEndScheduled = Float.MAX_VALUE;
            s.offShadow.attach();
            s.offShadow.detachWithAnim();
            tKaiganEndScheduled = getRenderTime() + s.tKaiganTime;
            tThemeScheduled = getRenderTime() + s.tThemeTime;
        }

        if(renderTime > tKaiganEndScheduled) {
            tKaiganEndScheduled = Float.MAX_VALUE;
            s.offShadow.detachWithAnim();
        }

        if(renderTime > tThemeScheduled) {
            tThemeScheduled = Float.MAX_VALUE;
            Audio.playMusic(s.themeMusic, true, 1.0f);
        }

        if(renderTime > tLoadingViewScheduled) {
            tLoadingViewScheduled = Float.MAX_VALUE;
            s.loadingView.attach();
        }
    }


    @Override
    public void onClick(final Grid v, UIElement<?> view, int button) {

        if(view == s.continueButton || view == s.newGameButton) {
            if(Game.game.platform.promptGameCenterLogin()) {
                isQueuedStartGame = true;
                showGooglePrompt();
            }
            else {
                // Else no need to login, just start game
                startGameLoad();
            }
            return;
        }

        if(view == s.restartButton) {
            hideTitleMenu();
            showNewGameWarning();
            return;
        }

        if(view == s.newGameYesButton) {
            Game.game.platform.deleteSaveGame();
            isFoundSave = false;
            hideNewGameWarning();
            startGameLoad();
            return;
        }

        if(view == s.newGameNoButton) {
            hideNewGameWarning();
            showTitleMenu();
            return;
        }

        if(view == s.creditsButton) {
            hideTitleMenu();
            showingCredits = true;
            s.offShadow.detachWithAnim();
            return;
        }

        if(view == s.quitButton) {
            Game.game.platform.exitGame();
            return;
        }

        if(view == s.betaFeedbackButton) {
            Gdx.net.openURI(Globals.d_betaFeedbackLink);
            return;
        }

        // Google play buttons
        if(view == s.googlePlayButton) {
            if(Game.game.platform.promptGameCenterLogin()) {
                // Need to login first
                isQueuedOpenAchievements = true;
                showGooglePrompt();
            }
            else {
                // Open game center
                Game.game.platform.openGameCenter();
            }
            return;
        }

        if(view == s.googlePlayPromptLoginButton) {
            // Login
            s.googlePlayPrompt.detachWithAnim();
            s.googlePlayLoadingView.attach();
            Game.game.platform.loginGameCenter();
            return;
        }

        if(view == s.googlePlayPromptContinueButton) {
            doneLogin(true, isFoundSave);            // skipped
            return;
        }

        if(view == s.twitterButton) {
            // Analytics
            Game.analyticsString(Globals.ANALYTICS_EVENT_SHARED, Globals.ANALYTICS_EVENT_SHARED_FIELD, Globals.ANALYTICS_EVENT_SHARED_TWITTER);

            Gdx.net.openURI(Globals.helpTwitterURL);
            return;
        }

        if(view == s.mailButton) {
            Gdx.net.openURI(Globals.helpMailURL);
            return;
        }

        if(view == s.fbButton) {
            // Analytics
            Game.analyticsString(Globals.ANALYTICS_EVENT_SHARED, Globals.ANALYTICS_EVENT_SHARED_FIELD, Globals.ANALYTICS_EVENT_SHARED_FB);

            Gdx.net.openURI(Globals.helpFacebookURL);
            return;
        }

        if(view == s.helpButton) {
            hideTitleMenu();
            showHelpMenu();
            return;
        }

        if(view == s.discordButton) {
            Gdx.net.openURI(Globals.helpDiscordURL);
            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HAS_DISCORD_OPENED, true).flush();
            s.discordButton.windowAnimation(null, false, false);
            return;
        }

        if(view == s.removeAdsButton) {
            Game.game.platform.removeAds();
            return;
        }

        if(view == s.subtitleButton) {
            // Toggle
            boolean showSubtitles = Gdx.app.getPreferences(Globals.PREF_FILENAME).getBoolean(Globals.STATE_SUBTITLE_ENABLED, false);
            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_SUBTITLE_ENABLED, !showSubtitles).flush();
            refreshSubtitleStatus();
            return;
        }

        if(view == s.highQualityVideosButton) {
            // Toggle
            Gdx.app.getPreferences(Globals.PREF_FILENAME).putBoolean(Globals.STATE_HQ_VIDEOS_ENABLED, !Globals.r_highQuality).flush();
            refreshHighQualityVideosStatus();
            return;
        }


        if(view == s.privacyPolicyButton) {
            Gdx.net.openURI(Globals.helpPrivacyPolicyURL);
            return;
        }

        if(view == s.helpBackButton) {
            hideHelpMenu();
            showTitleMenu();
            return;
        }

        if(view == s.simAdButton) {
            // Play trailer
            detach();
            v.simTrailerMenu.setOnClose(new Runnable() {
                @Override
                public void run() {
                    v.simTrailerMenu.detach();
                    v.skipIntoMainMenu = true;
                    attach(Globals.grid.compositor);
                }
            });
            v.simTrailerMenu.attach(v.screensGroup);
            // Fadeout music
            MusicFadeOutEntity fadeOutEntity = new MusicFadeOutEntity(0, s.tThemeFadeOutTime);
            fadeOutEntity.attach(v);
            return;
        }
    }

    @Override
    protected boolean input(Grid v, int inputType, int key, char character, int scrolledAmount, int pointer, float x, float y, int button) {
        if(inputType == INPUT_KEY_UP && key == Input.Keys.BACK) {
            Game.game.platform.exitGame();          // Only on android, go back to homescreen
            return true;
        }
        return false;
    }
}

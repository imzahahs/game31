package game31.app.phone;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;

import game31.Game;
import game31.Globals;
import game31.Grid;
import game31.ScreenBar;
import game31.ScreenTransition;
import game31.ScreenTransitionFactory;
import game31.VoiceProfile;
import game31.gb.phone.GBPhoneCallScreen;
import game31.model.PhoneAppModel;
import sengine.Entity;
import sengine.File;
import sengine.Sys;
import sengine.audio.Audio;
import sengine.calc.Range;
import sengine.graphics2d.Material;
import sengine.graphics2d.Sprite;
import sengine.materials.MaskMaterial;
import sengine.ui.Clickable;
import sengine.ui.Menu;
import sengine.ui.OnClick;
import sengine.ui.StaticSprite;
import sengine.ui.TextBox;
import sengine.ui.UIElement;
import sengine.utils.Builder;

/**
 * Created by Azmi on 24/8/2016.
 */
public class PhoneCallScreen extends Menu<Grid> implements OnClick<Grid> {
    private static final String TAG = "PhoneCallScreen";


    public static class Internal {
        // Window
        public UIElement<?> window;
        public ScreenBar bars;
        public String incomingTitle;
        public String callingTitle;

        public StaticSprite profileView;
        public Material maskMaterial;
        public Sprite defaultProfileSprite;
        public UIElement<?> callingIndicatorGroup;
        public UIElement<?> incallIndicatorGroup;
        public UIElement<?> callEndIndicatorGroup;

        public float incallStartSize;
        public float incallMaxSize;

        public TextBox nameView;
        public TextBox deviceView;
        public float tStartInterval;
        public float tEndInterval;
        public Range tCallTime;
        public Range tUnansweredCallTime;

        public String dialingText;
        public String lineBusyText;
        public String callEndedText;

        // Controls
        public UIElement decisionGroup;
        public Clickable declineButton;
        public Clickable acceptButton;
        public UIElement controlGroup;
        public Clickable endButton;

        public UIElement callOptionsGroup;
        public Sprite padActiveSprite;
        public Sprite padInactiveSprite;
        public Clickable padButton;
        public StaticSprite padIndicator;
        public Clickable muteButton;
        public Sprite mutedSprite;
        public Sprite unmutedSprite;
        public String muteText;
        public String unmuteText;

        public UIElement padGroup;
        public Audio.Sound padShowSound;
        public Audio.Sound padHideSound;
        public TextBox dialedView;
        public Clickable[] keyButtons;
        public Audio.Sound[] keySounds;
        public char[] keyCharacters;
        public int maxDialedLength;

        // Sounds
        public Music dialSound;
        public Music busySound;
        public Audio.Sound endCallSound;
    }

    public interface InterfaceSource {
        String convertSeconds(int seconds);
    }


    // App
    private final PhoneApp app;

    // Interface source
    private final Builder<InterfaceSource> interfaceSource;
    private Internal s;

    // Working
    private Entity<?> transitionFrom;
    private Music ringtone = null;
    private boolean hasStarted = false;
    private boolean isReceiving = false;

    private Music callAudio = null;
    private boolean isAudioPlaying = false;
    private VoiceProfile voiceProfile = null;
    private float tPlayingTime = 0;
    private float tAudioPlayingTime = 0;

    private float tAcceptScheduled = Float.MAX_VALUE;
    private float tEndScheduled = Float.MAX_VALUE;
    private float tSeconds = 0;
    private int seconds = -1;
    private String name = null;
    private PhoneContact contact = null;
    private boolean isIncomming = false;
    private boolean isUserInputObserved = false;

    private boolean isMuted = false;
    private boolean isPadShowing = false;
    private String answer = "";

    public PhoneContact contact() {
        return contact;
    }

    public void clearAnswer() {
        answer = "";
        s.dialedView.text().text(answer);
        isUserInputObserved = false;
    }

    public void clear() {
        stopAllAudio();

        // Reset
        s.decisionGroup.detach();
        s.controlGroup.detach();
        s.callOptionsGroup.detach();

        s.callingIndicatorGroup.detach();
        s.incallIndicatorGroup.detach();
        s.callEndIndicatorGroup.detach();

        isMuted = false;
        s.muteButton.visuals(s.unmutedSprite);
        s.muteButton.text().text(s.muteText);

        isPadShowing = false;
        s.padButton.visuals(s.padInactiveSprite);
        s.padGroup.detach();
        s.padIndicator.detach();

        clearAnswer();

        isAudioPlaying = false;

        tAcceptScheduled = Float.MAX_VALUE;
        tEndScheduled = Float.MAX_VALUE;

        tSeconds = 0;
        seconds = -1;

        contact = null;
    }

    public void showCalling(Sprite profilePic, String name, PhoneContact contact) {
        clear();

        this.contact = contact;
        this.name = name;
        isIncomming = false;

        // Replace name
        s.nameView.text().text(name);
        s.deviceView.text().text(s.dialingText);
        if(profilePic != null)
            s.profileView.visual(new Sprite(new MaskMaterial(profilePic.getMaterial(), s.maskMaterial)));
        else
            s.profileView.visual(s.defaultProfileSprite);

        s.bars.showAppbar(s.callingTitle, name);

        s.callingIndicatorGroup.attach();

        if(contact != null) {
            String audioPath = contact.message();
            if(audioPath == null) {
                // No messages at the moment, try inform call
                contact.call(app);
                audioPath = contact.message();
            }
            // Check if contact is receiving or
            if (audioPath != null) {
                callAudio = Gdx.audio.newMusic(File.open(audioPath));
                Globals.grid.notification.startSubtitle(audioPath);
                voiceProfile = VoiceProfile.load(audioPath);
                tAcceptScheduled = s.tCallTime.generate();
            } else {
                callAudio = null;
                tAcceptScheduled = s.tUnansweredCallTime.generate();
            }

            // Analytics
            if(contact.tree != null)
                Game.analyticsView(contact.tree.namespace, Globals.ANALYTICS_CONTENT_TYPE_CALLS);
        }
        else {
            callAudio = null;
            tAcceptScheduled = s.tUnansweredCallTime.generate();
        }

        hasStarted = false;
        isReceiving = false;
    }

    public void showReceiving(Sprite profilePic, String name, String device, PhoneContact contact) {
        // Reset
        clear();

        this.contact = contact;
        this.name = name;
        isIncomming = true;

        // Replace name
        s.nameView.text().text(name);
        s.deviceView.text().text(device);
        if(profilePic != null)
            s.profileView.visual(new Sprite(new MaskMaterial(profilePic.getMaterial(), s.maskMaterial)));
        else
            s.profileView.visual(s.defaultProfileSprite);

        s.bars.showAppbar(s.incomingTitle, name);

        s.callingIndicatorGroup.attach();

        ringtone = Gdx.audio.newMusic(File.open(app.ringtonePath()));
        ringtone.setLooping(true);

        String message = contact.message();
        callAudio = Gdx.audio.newMusic(File.open(message));
        Globals.grid.notification.startSubtitle(message);
        voiceProfile = VoiceProfile.load(message);


        // Analytics
        if(contact.tree != null)
            Game.analyticsView(contact.tree.namespace, Globals.ANALYTICS_CONTENT_TYPE_CALLS);

        hasStarted = false;
        isReceiving = true;
    }


    public void open(Entity<?> transitionFrom, Entity<?> target) {
        if(!isAttached()) {
            this.transitionFrom = transitionFrom;
            ScreenTransition transition = ScreenTransitionFactory.createSwipeLeft(transitionFrom, this, target);
            transition.attach(target);
        }

        // Close keyboard
        Globals.grid.keyboard.hideNow();
    }

    public void openAbrupt(Entity<?> transitionFrom, Entity<?> target) {
        if(!isAttached()) {
            this.transitionFrom = transitionFrom;
            ScreenTransition transition = ScreenTransitionFactory.createCallTransition(transitionFrom, this, target);
            transition.attach(target);
        }

        // Close keyboard
        Globals.grid.keyboard.hideNow();
    }

    public void setInternal(Internal internal) {
        if(s != null) {
            stopAllAudio();
            s.window.detach();
            s.bars.detach();
        }

        s = internal;

        s.window.viewport(viewport).attach();
    }

    public PhoneCallScreen(PhoneApp app) {
        this.app = app;

        // Initialize
        interfaceSource = new Builder<InterfaceSource>(GBPhoneCallScreen.class, this);
        interfaceSource.build();
    }



    @Override
    protected void recreate(Grid v) {
        super.recreate(v);

        interfaceSource.start();

        // Pause all audio
//        v.musicApp.playerScreen.pauseTrack();          TODO
        v.photoRollApp.videoScreen.pause();
        v.stopAmbiance();
        v.idleScare.stop();

        if(ringtone != null)
            ringtone.play();
    }

    @Override
    protected void render(Grid grid, float r, float renderTime) {
        super.render(grid, r, renderTime);

        if(!hasStarted && renderTime > s.tStartInterval) {
            hasStarted = true;

            if(isReceiving) {
                s.decisionGroup.attach();
            }
            else {
                s.dialSound.play();
                s.controlGroup.attach();
                s.callOptionsGroup.attach();
            }
        }

        if(renderTime > tAcceptScheduled) {
            tAcceptScheduled = Float.MAX_VALUE;
            s.dialSound.stop();

            if(callAudio != null) {
                callAudio.play();
                isAudioPlaying = true;

                s.callingIndicatorGroup.detachWithAnim();
                s.incallIndicatorGroup.attach();
            }
            else {
                s.busySound.play();
                s.deviceView.text().text(s.lineBusyText);

                s.callingIndicatorGroup.detachWithAnim();
                s.callEndIndicatorGroup.attach();
            }
        }

        if(renderTime > tEndScheduled) {
            // Ended, simulate pressing end button
            tEndScheduled = Float.MAX_VALUE;
            onClick(grid, s.endButton, Input.Buttons.LEFT);
            return;
        }

        // Animation
        if(s.callingIndicatorGroup.isAttached())
            Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);

        if(contact == null || contact.tree == null)
            return;

        // Observe user input
        if(contact.tree.isUserMessagesAvailable()) {
            if(!isUserInputObserved) {
                clearAnswer();
                isUserInputObserved = true;
            }
        }
        else if(isUserInputObserved) {
            s.padIndicator.detach();
            isUserInputObserved = false;
        }

        // Timer
        if(isAudioPlaying) {
            // Update seconds view
            tSeconds += getRenderDeltaTime();
            int newSeconds = (int) tSeconds;
            if (newSeconds != seconds) {
                seconds = newSeconds;
                String timeText = interfaceSource.build().convertSeconds(seconds);
                s.deviceView.text().text(timeText);
            }

            // Check current audio
            if(callAudio != null) {
                if(!callAudio.isPlaying())
                    stopCallAudio();            // Audio ended
                else if(voiceProfile != null) {
                    float currentAudioPosition = callAudio.getPosition();
                    if (currentAudioPosition != tAudioPlayingTime) {
                        if(tPlayingTime < currentAudioPosition) {
                            tPlayingTime = currentAudioPosition;
                            tAudioPlayingTime = currentAudioPosition;
                        }
                    } else if(tAudioPlayingTime != 0) {
                        // Manually calculate audio position, since there is no movement (on some devices, audio position has a very low resolution)
                        tPlayingTime += Gdx.graphics.getRawDeltaTime();
                    }
                    // Subtitles
                    Globals.grid.notification.updateSubtitles(tPlayingTime, false);
                    // Sample voice profile
                    float sample = voiceProfile.sample(tPlayingTime);
                    // Indicate sample
                    float size = s.incallStartSize + ((s.incallMaxSize - s.incallStartSize) * sample);
                    s.incallIndicatorGroup.metrics.scaleX = size;
                    s.incallIndicatorGroup.metrics.scaleY = size;
                    // Request max framerate
                    Sys.system.requestMaxFramerate(Sys.system.renderChangeMaxFramerateTime);
                }
            }


            if (callAudio == null) {
                // No audio at the moment, check if next message is available
                String audioPath = contact.nextMessage(app);
                if(audioPath != null) {
                    callAudio = Gdx.audio.newMusic(File.open(audioPath));
                    callAudio.play();
                    Globals.grid.notification.startSubtitle(audioPath);
                    voiceProfile = VoiceProfile.load(audioPath);
                }
                else if(contact.tree.isUserMessagesAvailable()) {
                    // Indicate that user input is required
                    if(!isPadShowing)
                        s.padIndicator.attach();
                }
                else { // if(!contact.tree.isUserMessagesAvailable()) {
                    // No next message and no user messages available, call ended
                    isAudioPlaying = false;
                    s.deviceView.text().text(s.callEndedText);
                    tEndScheduled = renderTime + s.tEndInterval;

                    // Indicator
                    s.incallIndicatorGroup.detachWithAnim();
                    s.callEndIndicatorGroup.attach();
                }
            }
        }
    }

    @Override
    protected void release(Grid v) {
        super.release(v);

        interfaceSource.stop();

        clear();

        v.resumeAmbiance();
    }

    private void stopAllAudio() {
        stopRingtone();
        stopCallAudio();

        s.dialSound.stop();
        s.busySound.stop();
    }

    private void stopRingtone() {
        if(ringtone != null) {
            ringtone.stop();
            ringtone.dispose();
            ringtone = null;
        }
    }

    private void stopCallAudio() {
        if(callAudio != null) {
            callAudio.stop();
            callAudio.dispose();
            callAudio = null;
            // Clear subtitle
            Globals.grid.notification.stopSubtitle(voiceProfile.filename);
        }
        // Clear voice profile
        voiceProfile = null;
        tPlayingTime = 0;
        tAudioPlayingTime = 0;
        // Reset size
        s.incallIndicatorGroup.metrics.scaleX = s.incallStartSize;
        s.incallIndicatorGroup.metrics.scaleY = s.incallStartSize;
    }

    private void showPad() {
        s.padButton.visuals(s.padActiveSprite);
        s.padGroup.attach();
        s.padShowSound.play();
        s.padIndicator.detach();
        isPadShowing = true;
    }

    private void hidePad() {
        s.padButton.visuals(s.padInactiveSprite);
        s.padGroup.detachWithAnim();
        s.padHideSound.play();
        if(isUserInputObserved)
            s.padIndicator.attach();
        isPadShowing = false;
    }

    @Override
    public void onClick(Grid v, UIElement<?> view, int b) {

        if(view == s.declineButton) {
            if(!v.trigger(Globals.TRIGGER_PHONECALL_DECLINED) || transitionFrom == null)
                return;     // not accepted or already closing
            // Add to recents
            app.addRecentCall(name, PhoneAppModel.RECENT_MISSED);
            if(transitionFrom == v.screensGroup) {
                ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, v.screensGroupContainer);
                transition.attach(v);
            }
            else {
                ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, v.screensGroup);
                transition.attach(v);
            }
            transitionFrom = null;
            stopAllAudio();
            s.endCallSound.play();
            // Inform contact call ended
            if(contact != null)
                contact.endCall(app);
            return;
        }

        if(view == s.acceptButton) {
            if(!v.trigger(Globals.TRIGGER_PHONECALL_ACCEPTED))
                return;     // not accepted
            // Accept button clicked
            stopRingtone();

            // Switch controls
            s.decisionGroup.detachWithAnim();
            s.controlGroup.attach();
            s.callOptionsGroup.attach();

            s.callingIndicatorGroup.detach();
            s.incallIndicatorGroup.attach();

            // Accepted call
            callAudio.play();
            isAudioPlaying = true;
            return;
        }

        if(view == s.endButton) {
            if(transitionFrom == null)
                return;     // already closing
            // Check if can end
            if(isAudioPlaying) {
                if(!v.trigger(Globals.TRIGGER_PHONECALL_CANCELLED))
                    return;     // not allowed to cancel call
            }
            else
                v.trigger(Globals.TRIGGER_PHONECALL_ENDED);     // call ended
            // Add to recents
            app.addRecentCall(name, isIncomming ? PhoneAppModel.RECENT_INCOMING : PhoneAppModel.RECENT_OUTGOING);
            // Inform contact call ended
            if(contact != null)
                contact.endCall(app);
            if(transitionFrom == v.screensGroup) {
                ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, v.screensGroupContainer);
                transition.attach(v);
            }
            else {
                ScreenTransition transition = ScreenTransitionFactory.createSwipeRight(this, transitionFrom, v.screensGroup);
                transition.attach(v);
            }
            transitionFrom = null;
            stopAllAudio();
            s.endCallSound.play();
            return;
        }

        // Mute
        if(view == s.muteButton) {
            // Toggle mute
            isMuted = !isMuted;
            s.muteButton.visuals(isMuted ? s.mutedSprite : s.unmutedSprite);
            s.muteButton.text().text(isMuted ? s.unmuteText : s.muteText);
            return;
        }

        // Pad
        if(view == s.padButton) {
            if(!isPadShowing)
                showPad();
            else
                hidePad();
            return;
        }

        for(int c = 0; c < s.keyButtons.length; c++) {
            Clickable button = s.keyButtons[c];

            if(view == button) {
                // Stop call audio if there are user messages
                if(contact != null && contact.tree != null && !contact.tree.availableUserMessages.isEmpty())
                    stopCallAudio();

                // Sound
                s.keySounds[c].play();

                // Typed
                answer += s.keyCharacters[c];
                if(answer.length() > s.maxDialedLength)
                    answer = answer.substring(answer.length() - s.maxDialedLength);         // limit
                s.dialedView.text().text(answer);

                // Check answer
                if(contact != null && contact.tree != null) {
                    // Stop call audio if there are user messages
                    if(!contact.tree.availableUserMessages.isEmpty())
                        stopCallAudio();
                    // Try to answer
                    if(contact.answer(app, answer)) {
                        // Accepted
                        clearAnswer();
                        // Hide pad
                        hidePad();
                    }
                }
            }
        }
    }
}

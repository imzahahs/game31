package game31.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Globals;
import game31.Grid;
import game31.Screen;
import game31.VoiceProfile;
import game31.renderer.DiffuseGeneratorMaterial;
import game31.renderer.LsdScreenMaterial;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.File;
import sengine.animation.Animation;
import sengine.audio.Audio;
import sengine.calc.Graph;
import sengine.graphics2d.Sprite;

public class BlissEffect extends Entity<Grid> {

    private final Music ambient;
    private final VoiceProfile ambientProfile;
    private final Animation overlayStartAnim;
    private final Animation overlayLoopAnim;
    private final Animation overlayEndAnim;

    private final float baseLsdSpeed;
    private final float dynamicLsdSpeed;

    private final Sprite effectBufferGenerator;
    private final Sprite overlay;

    private float ambientVolume;

    private Graph diffuseGraph;
    private Graph lsdGraph;
    private float tCustomGraphStarted = -1;

    private Music voice;
    private VoiceProfile voiceProfile;

    public VoiceProfile getVoiceProfile() {
        return voiceProfile;
    }

    public void playVoice(String filename) {
        voice = Gdx.audio.newMusic(File.open(filename));
        voice.play();
        voiceProfile = VoiceProfile.load(filename);
    }

    public void pauseAmbient() {
        ambient.pause();
    }

    public void resumeAmbient() {
        ambient.play();
    }

    public void setAmbientVolume(float volume) {
        ambientVolume = volume;
        Screen screen = Globals.grid.screen;
        if(screen.overlayAnim() == null || screen.overlayAnim().anim != overlayEndAnim)
            ambient.setVolume(volume);

    }

    public void resetScreenOverlayAnim(boolean skipStart) {
        Globals.grid.screen.animateOverlay(skipStart ? null : overlayStartAnim, overlayLoopAnim);
    }

    public void animateEffect(Graph diffuseGraph, Graph lsdGraph) {
        this.diffuseGraph = diffuseGraph;
        this.lsdGraph = lsdGraph;
        tCustomGraphStarted = getRenderTime();
    }

    public BlissEffect(String ambientName, boolean loop, float ambientVolume, float diffuseAmount, float lsdAmount, float baseLsdSpeed, float dynamicLsdSpeed, Animation overlayStartAnim, Animation overlayLoopAnim, Animation overlayEndAnim) {
        this.ambient = Gdx.audio.newMusic(File.open(ambientName));
        ambient.setVolume(ambientVolume);
        ambient.setLooping(loop);

        this.ambientVolume = ambientVolume;

        ambientProfile = VoiceProfile.load(ambientName);

        // Diffusion buffer feedback generator
        DiffuseGeneratorMaterial generatorMaterial = new DiffuseGeneratorMaterial();
        generatorMaterial.diffuseAmount = diffuseAmount;
        effectBufferGenerator = new Sprite(Globals.LENGTH, generatorMaterial);

        // LSD overlay renderer
        LsdScreenMaterial overlayMaterial = new LsdScreenMaterial();
        overlayMaterial.amount = lsdAmount;
        overlay = new Sprite(Globals.LENGTH, overlayMaterial);

        this.baseLsdSpeed = baseLsdSpeed;
        this.dynamicLsdSpeed = dynamicLsdSpeed;

        this.overlayStartAnim = overlayStartAnim;
        this.overlayLoopAnim = overlayLoopAnim;
        this.overlayEndAnim = overlayEndAnim;
    }

    @Override
    protected void recreate(Grid v) {
        // Stop music
        Audio.musicVolume = 0.0f;
        Audio.setMusicVolume(0f);

        // Starting
        ambient.play();

        // Request effect buffer generation and set overlay
        SaraRenderer.renderer.requestEffectBuffer(Globals.r_diffusionEffectResolution);
        v.screen.effectBufferGenerator = effectBufferGenerator;
        v.screen.overlay = overlay;
        resetScreenOverlayAnim(false);
    }

    @Override
    protected void release(Grid v) {
        Audio.musicVolume = 1.0f;
        Audio.setMusicVolume(1f);

        // Ending
        ambient.dispose();

        // Stop voice
        if(voice != null) {
            voice.dispose();
            voice = null;
            voiceProfile = null;
        }

        // Stop effect buffer generator
        v.screen.effectBufferGenerator = null;
        v.screen.overlay = null;
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        if(v.screen.overlayAnim() == null)
            detach();
        else if(v.screen.overlayAnim().anim == overlayEndAnim) {
            // Animating end
            float progress = v.screen.overlayAnim().getProgress();
            Audio.setMusicVolume(progress);
            ambient.setVolume((1f - progress) * ambientVolume);
        }

        if(tCustomGraphStarted != -1) {
            float elapsed = renderTime - tCustomGraphStarted;
            DiffuseGeneratorMaterial diffuseGeneratorMaterial = effectBufferGenerator.getMaterial();
            LsdScreenMaterial lsdScreenMaterial = overlay.getMaterial();

            if(elapsed > diffuseGraph.getLength() && elapsed > lsdGraph.getLength()) {
                diffuseGeneratorMaterial.diffuseAmount = diffuseGraph.getEnd();
                lsdScreenMaterial.amount = lsdGraph.getEnd();
                tCustomGraphStarted = -1;     // finished
            }
            else {
                if(elapsed < diffuseGraph.getLength())
                    diffuseGeneratorMaterial.diffuseAmount = diffuseGraph.generate(elapsed);
                else
                    diffuseGeneratorMaterial.diffuseAmount = diffuseGraph.getEnd();
                if(elapsed < lsdGraph.getLength())
                    lsdScreenMaterial.amount = lsdGraph.generate(elapsed);
                else
                    lsdScreenMaterial.amount = lsdGraph.getEnd();
            }
        }

        // Animate lsd material
        float profile = 0;

        // Sample ambient
        if(ambient != null && ambient.isPlaying())
            profile = ambientProfile.sample(ambient.getPosition());

        // Sample voice
        if(voice != null) {
            if(!voice.isPlaying()) {
                voice.dispose();
                voice = null;
                voiceProfile = null;
            }
            else {
                profile += voiceProfile.sample(voice.getPosition());
                if(profile > 1f)
                    profile = 1f;
            }
        }

        LsdScreenMaterial overlayMaterial = overlay.getMaterial();
        overlayMaterial.progress += getRenderDeltaTime() * (baseLsdSpeed + (profile * dynamicLsdSpeed));
        overlayMaterial.progress %= 1.0f;
    }

    public void detachWithAnim() {
        if(Globals.grid.screen.overlayAnim() != null && Globals.grid.screen.overlayAnim().anim != overlayEndAnim)
            Globals.grid.screen.animateOverlay(overlayEndAnim, null);
    }
}

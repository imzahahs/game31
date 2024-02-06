package game31.triggers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import game31.Globals;
import game31.Grid;
import game31.VoiceProfile;
import game31.renderer.DiffuseGeneratorMaterial;
import game31.renderer.GloomScreenMaterial;
import game31.renderer.SaraRenderer;
import sengine.Entity;
import sengine.File;
import sengine.audio.Audio;
import sengine.calc.Graph;
import sengine.graphics2d.Sprite;

public class GloomEffect extends Entity<Grid> {

    private final Music ambient;
    private final VoiceProfile ambientProfile;
    private final Graph diffuseLoopGraph;
    private final Graph startGraph;
    private final Graph endGraph;

    private final float activeDiffuseAmount;
    private final float activeBlackThreshold;
    private final float activeWhiteThreshold;
    private final float activeProfileScale;

    private final Sprite effectBufferGenerator;
    private final Sprite screen;

    private float ambientVolume;

    private float tEndStarted = -1;

    private Music voice;
    private VoiceProfile voiceProfile;

    public void playVoice(String filename) {
        voice = Gdx.audio.newMusic(File.open(filename));
        voice.play();
        voiceProfile = VoiceProfile.load(filename);
    }

    public GloomEffect(
            String ambientName, boolean loop,
            float ambientVolume, float diffuseAmount, float gloomThreshold,
            float activeDiffuseAmount, float activeBlackThreshold, float activeWhiteThreshold, float activeProfileScale,
            Graph diffuseLoopGraph, Graph startGraph, Graph endGraph

    ) {
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
        GloomScreenMaterial overlayMaterial = new GloomScreenMaterial();
        overlayMaterial.threshold = gloomThreshold;
        screen = new Sprite(Globals.LENGTH, overlayMaterial);

        this.activeDiffuseAmount = activeDiffuseAmount;
        this.activeBlackThreshold = activeBlackThreshold;
        this.activeWhiteThreshold = activeWhiteThreshold;
        this.activeProfileScale = activeProfileScale;

        this.diffuseLoopGraph = diffuseLoopGraph;
        this.startGraph = startGraph;
        this.endGraph = endGraph;
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
        v.screen.screen = v.screen.defaultScreen;
    }

    @Override
    protected void render(Grid v, float r, float renderTime) {
        v.screen.screen = screen;       // Set custom screen render

        DiffuseGeneratorMaterial diffuseGeneratorMaterial = effectBufferGenerator.getMaterial();
        GloomScreenMaterial overlayMaterial = screen.getMaterial();

        // Loop graph
        diffuseGeneratorMaterial.diffuseAmount = diffuseLoopGraph.generate(renderTime);

        // Start
        if(renderTime < startGraph.getLength())
            overlayMaterial.alpha = startGraph.generate(renderTime);
        else if(tEndStarted != -1) {
            float elapsed = renderTime - tEndStarted;
            if(elapsed > endGraph.getLength()) {
                detach();
                overlayMaterial.alpha = endGraph.getEnd();
            }
            else {
                overlayMaterial.alpha = endGraph.generate(elapsed);
                float progress = elapsed / endGraph.getLength();
                Audio.setMusicVolume(progress);
                ambient.setVolume((1f - progress) * ambientVolume);
            }
        }
        else
            overlayMaterial.alpha = startGraph.getEnd();

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

        diffuseGeneratorMaterial.diffuseAmount += profile * activeDiffuseAmount;

        overlayMaterial.blackThreshold = activeBlackThreshold * profile;
        overlayMaterial.whiteThreshold = 1.0f - ((1.0f - activeWhiteThreshold) * profile);

        // Clamp
        if(overlayMaterial.blackThreshold > 1f)
            overlayMaterial.blackThreshold = 1f;
        if(overlayMaterial.whiteThreshold < 0f)
            overlayMaterial.whiteThreshold = 0f;
        if(overlayMaterial.whiteThreshold < overlayMaterial.blackThreshold)
            overlayMaterial.whiteThreshold = overlayMaterial.blackThreshold;

        // Clamp diffuse
        if(diffuseGeneratorMaterial.diffuseAmount > 1f)
            diffuseGeneratorMaterial.diffuseAmount = 1f;
        else if(diffuseGeneratorMaterial.diffuseAmount < 0f)
            diffuseGeneratorMaterial.diffuseAmount = 0f;
    }

    public void detachWithAnim() {
        if(tEndStarted == -1)
            tEndStarted = getRenderTime();
    }
}

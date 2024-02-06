package game31.app.flapee;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;

import game31.Globals;
import game31.renderer.LightingCompositorMaterial;
import game31.renderer.LightingGeneratorMaterial;
import game31.renderer.SaraRenderer;
import sengine.Sys;
import sengine.calc.Graph;
import sengine.graphics2d.Matrices;
import sengine.graphics2d.Sprite;
import sengine.materials.ColorAttribute;

public class Lighting {
    public Sprite bg;

    public Sprite sunMat;
    public final Color sunColor = new Color();
    public float sunSize;
    public Graph sunX;
    public Graph sunY;

    public final Color lightColor = new Color(0);
    public float lightEdge;
    public float lightScale;
    public float lightGamma;
    public final Color godraysColor = new Color(0);
    public float godraysScale;
    public float godraysGamma;
    public float godraysFog;

    public void render(Lighting blend, float blendR, float groundY,
                       Sprite lightingGenerationPlane, LightingGeneratorMaterial lightingGeneratorMaterial,
                       Sprite gameplayRenderPlane, LightingCompositorMaterial lightingCompositorMaterial
    ) {
        // Reset attributes
        float time = Sys.getTime();
        float currentSunX = sunX.generate(time);
        float currentSunY = sunY.generate(time);
        float currentSunSize = sunSize;

        ColorAttribute.of(bg).current.a = 1f;

        ColorAttribute sunColorAttrib = ColorAttribute.of(sunMat).set(sunColor);

//        ColorAttribute lightingColorAttrib = ColorAttribute.of(lightingPlane).set(lightColor);
        lightingCompositorMaterial.lightingColor.set(lightColor);
        float currentLightEdge = lightEdge;
        float currentLightScale = lightScale;
        float currentLightGamma = lightGamma;

//        ColorAttribute godraysColorAttrib = ColorAttribute.of(godraysPlane).set(godraysColor);
        lightingCompositorMaterial.raysColor.set(godraysColor);
        float currentGodraysScale = godraysScale;
        float currentGodraysGamma = godraysGamma;
        float currentGodraysFog = godraysFog;

        if(blend != null) {
            float clampR = blendR;
            if(clampR < 0f)
                clampR = 0f;
            else if(clampR > 1f)
                clampR = 1f;
            // Lerp to blended lighting
            ColorAttribute.of(blend.bg).current.a = clampR;

            float blendSunX = blend.sunX.generate(time);
            float blendSunY = blend.sunY.generate(time);
            currentSunX = currentSunX + ((blendSunX - currentSunX) * blendR);
            currentSunY = currentSunY + ((blendSunY - currentSunY) * blendR);
            currentSunSize = currentSunSize + ((blend.sunSize - currentSunSize) * blendR);

            sunColorAttrib.lerp(blend.sunColor, clampR);

            lightingCompositorMaterial.lightingColor.lerp(blend.lightColor, clampR);
            currentLightEdge = currentLightEdge + ((blend.lightEdge - currentLightEdge) * blendR);
            currentLightScale = currentLightScale + ((blend.lightScale - currentLightScale) * blendR);
            currentLightGamma = currentLightGamma + ((blend.lightGamma - currentLightGamma) * blendR);

            lightingCompositorMaterial.raysColor.lerp(blend.godraysColor, clampR);
            currentGodraysScale = currentGodraysScale + ((blend.godraysScale - currentGodraysScale) * blendR);
            currentGodraysGamma = currentGodraysGamma + ((blend.godraysGamma - currentGodraysGamma) * blendR);
            currentGodraysFog = currentGodraysFog + ((blend.godraysFog - currentGodraysFog) * blendR);
        }

        // Commit
        lightingGeneratorMaterial.lightingEdge = currentLightEdge;
        lightingGeneratorMaterial.lightingScale = currentLightScale;
        lightingGeneratorMaterial.lightingGamma = currentLightGamma;
        lightingGeneratorMaterial.raysScale = currentGodraysScale;
        lightingGeneratorMaterial.raysGamma = currentGodraysGamma;
        lightingCompositorMaterial.raysFog = currentGodraysFog;

        // Update lighting position
        float lightY = ((+Globals.LENGTH / 2f) + groundY + currentSunY) / Globals.LENGTH;
        lightingGeneratorMaterial.center.set(currentSunX, lightY);

        // Start render
        Matrix4 m = Matrices.model;

        int flapeeSkyTarget;

        if(Globals.r_highQuality) {
            // Queue lighting and god rays generation and compositor instructions
            Matrices.push();
            m.translate(0.5f, +Globals.LENGTH / 2f, 0);
            m.scale(1, -1, 1);
            // Lighting
            Matrices.target = SaraRenderer.TARGET_FLAPEE_LIGHTING_GENERATOR;
            lightingGenerationPlane.render();
            Matrices.target = SaraRenderer.TARGET_INTERACTIVE;
            gameplayRenderPlane.render();
            Matrices.pop();

            flapeeSkyTarget = SaraRenderer.TARGET_FLAPEE_SKY;
        }
        else
            flapeeSkyTarget = SaraRenderer.TARGET_INTERACTIVE_SUB;

        // Render background
        Matrices.push();
        Matrices.target = flapeeSkyTarget;
        m.translate(0.5f, +Globals.LENGTH / 2f, 0);
        bg.render();
        if(blend != null)
            blend.bg.render();
        Matrices.pop();

        // Sun
        Matrices.push();
        Matrices.target = flapeeSkyTarget;
        m.translate(currentSunX, (+Globals.LENGTH / 2f) + groundY + currentSunY, 0);
        m.scale(currentSunSize, currentSunSize, currentSunSize);
        sunMat.render();
        Matrices.pop();
    }
}

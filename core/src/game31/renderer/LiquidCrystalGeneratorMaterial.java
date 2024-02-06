package game31.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import game31.Globals;
import sengine.Sys;

public class LiquidCrystalGeneratorMaterial extends ScreenMaterial {

    public static final String u_backBuffer = "u_backBuffer";
//    public static final String u_resolution = "u_resolution";
    public static final String u_acid = "u_acid";
    public static final String u_amount = "u_amount";

    public float amount = 0; // 0.95f;
    public float acid = 0; // 1.25f;

    public LiquidCrystalGeneratorMaterial() {
        super("shaders/LiquidCrystalGenerator.glsl", SaraRenderer.RENDER_FINAL, GL20.GL_ONE, GL20.GL_ZERO);
    }

    @Override
    protected void program(ShaderProgram program) {
        // Bind backbuffer
        FrameBuffer backBuffer = SaraRenderer.renderer.renderBuffers[SaraRenderer.RENDER_EFFECT2];
        backBuffer.getColorBufferTexture().bind(1);
        program.setUniformi(u_backBuffer, 1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

//        if((Sys.getTime() * 0.05) < 0.95f) {
//            amount = Sys.getTime() * 0.05f;
//        }
//        else
//            amount = 0.95f;
//
//
//        if(Sys.getTime() > 35f) {
//            float excess = Sys.getTime() - 35f;
//            acid = 2.0f + (excess * 0.2f);
//            amount = 1f;
//        }

        // Uniforms
        program.setUniformf(u_resolution, backBuffer.getWidth(), backBuffer.getHeight());
        program.setUniformf(u_acid, acid);
        program.setUniformf(u_amount, amount);
    }


    /*


#define PI 3.14159265359

void mainImage( out vec4 fragColor, in vec2 fragCoord ){


    float feedback = 0.99;
    float acid = 1.09;
    float baseWarp = 0.001;
    float baseWarpSpeed = 0.1;
    float warp = 30.0;

    vec2 vUv = fragCoord.xy / iResolution.xy;
    vec2 texel = vec2(1.0, 1.0) / iResolution.xy;

    float s = sin(mod(iTime * baseWarpSpeed, PI * 2.0));
    float c = cos(mod(iTime * baseWarpSpeed, PI * 2.0));
    vec2 rotate = normalize(vec2(s, c)) * baseWarp;
    vec3 uv = texture(iChannel0, vUv + rotate).xyz;

    float gt = -vUv.x * vUv.y * warp;

    vec2 d1 = vec2(uv.x * vec2(texel.x*sin(gt * uv.z), texel.y*cos(gt*uv.z)));
    vec2 d2 = vec2(uv.y * vec2(texel.x*cos(gt * uv.x), texel.y*sin(gt*uv.x)));
    vec2 d3 = vec2(uv.z * vec2(texel.x*sin(gt * uv.y), texel.y*cos(gt*uv.y)));

    float bright = length(uv);

    float r = texture(iChannel0, vUv+ d1 * bright).x;
    float g = texture(iChannel0, vUv+ d2 * bright).y;
    float b = texture(iChannel0, vUv+ d3 * bright).z;

    vec3 uvMix = mix(uv, vec3(r,g,b), acid);

    vec3 orig = texture(iChannel1, vUv).xyz;


    fragColor = vec4(mix(orig, uvMix, feedback), 1.0);

}

     */
}

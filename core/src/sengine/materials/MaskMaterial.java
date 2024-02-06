package sengine.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import sengine.GarbageCollector;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Shader;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 8/16/2017.
 */

public class MaskMaterial extends Material implements MassSerializable {

    public static final String DEFAULT_SHADER = "shaders/MaskMaterial.glsl";

    public final Material image;
    public final Material mask;
    public final float length;

    public final Shader shader;

    public final Vector2 scale;
    public final Vector2 offset;

    // Uniforms
    public static final String u_texture = "u_texture";
    public static final String u_mask = "u_mask";
    public static final String u_scale = "u_scale";
    public static final String u_offset = "u_offset";

    public MaskMaterial(String image, String mask) {
        this(Material.load(image), Material.load(mask));
    }

    public MaskMaterial(Material image, Material mask) {
        this(image, mask, mask.getLength(), Shader.load(DEFAULT_SHADER));
    }

    public MaskMaterial(Material image, Material mask, float length) {
        this(image, mask, length, Shader.load(DEFAULT_SHADER));
    }

    @MassConstructor
    public MaskMaterial(Material image, Material mask, float length, Shader shader) {
        super(
                mask.depthFunc,
                mask.depthMask,
                mask.faceCullingMode,
                mask.srcBlendFunc,
                mask.destBlendFunc
        );

        this.image = image;
        this.mask = mask;
        this.length = length;
        this.shader = shader;

        // Calculate scale and offset
        scale = new Vector2(1, 1);
        offset = new Vector2(0, 0);
        float imageLength = image.getLength();
        if(imageLength < length) {
            // Image is shorter than mask
            scale.x = imageLength / length;
            offset.x = (1.0f - scale.x) / 2f;
        }
        else if(imageLength > length) {
            // Image is longer than mask
            scale.y = length / imageLength;
            offset.y = (1.0f - scale.y) / 2f;
        }
    }

    @Override
    public Object[] mass() {
        return new Object[] { image, mask, length, shader };
    }

    @Override
    public void load() {
        image.load();
        mask.load();
    }

    @Override
    public boolean isLoaded() {
        return image.isLoaded() && mask.isLoaded();
    }

    @Override
    public void ensureLoaded() {
        image.ensureLoaded();
        mask.ensureLoaded();
    }

    @Override
    public void initialize(MaterialInstance m) {
        // Ensure attributes
        m.getAttribute(ColorAttribute.class, 0);                // Supported
    }

    @Override
    public Shader bind() {
        // Bind textures
        image.bindTexture(0);
        mask.bindTexture(1);

        // Reset
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        // Shader
        ShaderProgram program = shader.bind();
        program.setUniformi(u_texture, 0);
        program.setUniformi(u_mask, 1);
        program.setUniformf(u_scale, scale);
        program.setUniformf(u_offset, offset);

        // Return primary shader
        return shader;
    }

    @Override
    public void unbind() {
        // nothing
    }

    @Override
    public float getLength() {
        return length;
    }


}

package game31.renderer;

import com.badlogic.gdx.math.Vector2;

import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.MaterialInstance;
import sengine.mass.MassSerializable;

/**
 * Created by Azmi on 7/12/2017.
 */

public class PixelationAttribute extends MaterialAttribute implements MassSerializable {

    public static PixelationAttribute of(MaterialInstance instance) {
        return of(instance, 0);
    }

    public static PixelationAttribute of(MaterialInstance instance, int layer) {
        return instance.getAttribute(PixelationAttribute.class, layer);
    }

    public static final String u_textureResolution = "u_textureResolution";
    public static final String u_pixelation = "u_pixelation";

    public final Vector2 textureResolution = new Vector2();
    public float pixelation = 1.0f;      // no pixelation

    public PixelationAttribute resolution(float width, float height) {
        textureResolution.set(width, height);
        return this;
    }

    public PixelationAttribute pixelation(float pixelation) {
        if(pixelation < 1f)
            pixelation = 1f;
        this.pixelation = pixelation;
        return this;
    }

    public PixelationAttribute() {
    }

    @MassConstructor
    public PixelationAttribute(Vector2 textureResolution, float pixelation) {
        this.textureResolution.set(textureResolution);
        this.pixelation = pixelation;
    }

    @Override
    public Object[] mass() {
        return new Object[] { textureResolution, pixelation};
    }

    @Override
    protected void configure(MaterialConfiguration config) {
        config.setVector2(u_textureResolution, textureResolution);
        config.setFloat(u_pixelation, pixelation);
    }

    @Override
    protected void copy(MaterialAttribute from) {
        PixelationAttribute f = (PixelationAttribute)from;
        textureResolution.set(f.textureResolution);
        pixelation = f.pixelation;
    }
}

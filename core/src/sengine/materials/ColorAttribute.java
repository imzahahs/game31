package sengine.materials;

import com.badlogic.gdx.graphics.Color;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.MaterialInstance;
import sengine.mass.MassSerializable;

public class ColorAttribute extends MaterialAttribute implements MassSerializable {

	public static ColorAttribute of(MaterialInstance instance) {
		return of(instance, 0);
	}

	public static ColorAttribute of(MaterialInstance instance, int layer) {
		return instance.getAttribute(ColorAttribute.class, layer);
	}

	public static final String u_color = "u_color";
	
	public final Color base = new Color(1, 1, 1, 1);
	public final Color current = new Color(1, 1, 1, 1);
	
	public ColorAttribute() {
	}
	
	@MassConstructor
	public ColorAttribute(Color color) {
		base.set(color);
		current.set(color);
	}
	@Override
	public Object[] mass() {
		return new Object[] { base };
	}
	
	@Override
	protected void configure(MaterialConfiguration config) {
		config.setColor(u_color, current);
		// Reset current color to base color
		current.set(base);
	}

	@Override
	protected void copy(MaterialAttribute from) {
		ColorAttribute f = (ColorAttribute)from;
		base.set(f.base);
		current.set(f.current);
	}
	
	public ColorAttribute set(float r, float g, float b, float a) {
		current.set(r, g, b, a);
		base.set(r, g, b, a);
		return this;
	}
	
	public ColorAttribute set(Color color) {
		current.set(color);
		base.set(color);
		return this;
	}

	public ColorAttribute set(int rgba) {
		base.set(rgba);
		current.set(base);
		return this;
	}

	public ColorAttribute lerp(Color color, float r) {
		base.lerp(color, r);
		current.lerp(color, r);
		return this;
	}
	
	public ColorAttribute setInt(int r, int g, int b, int a) {
		set((float)r / 255f, (float)g / 255f, (float)b / 255f, (float)a / 255f);
		return this;
	}

	public ColorAttribute mul(float amount) {
		base.mul(amount);
		current.mul(amount);
		return this;
	}

	public ColorAttribute alpha(float a) {
		base.a = current.a = a;
		return this;
	}
}

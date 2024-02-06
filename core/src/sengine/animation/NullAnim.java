package sengine.animation;

import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class NullAnim extends Animation implements MassSerializable {
	
	public static final NullAnim one = new NullAnim(1f);
	public static final NullAnim zero = new NullAnim(0f);

	@MassConstructor
	public NullAnim(float length) {
		super(length);
	}
	@Override
	public Object[] mass() {
		return new Object[] { length };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		// nothing
	}
}

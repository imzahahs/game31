package sengine.animation;

import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class CompoundAnim extends Animation implements MassSerializable {

	final Animation[] anims;
	final float[] multipliers;
	
	static float calculateLength(Animation[] anims, float[] multipliers) {
		float length = 0;
		// Find the longest animation
		for(int c = 0; c < anims.length; c++) {
			if(multipliers[c] < 0.0f)
				continue;
			Animation a = anims[c];
			if(a.length > length)
				length = a.length;
		}
		return length;
	}
	
	static float[] defaultMultipliers(Animation[] anims) {
		float[] multipliers = new float[anims.length];
		float length = calculateLength(anims, multipliers);
		for(int c = 0; c < anims.length; c++)
			multipliers[c] = length / anims[c].length;
		return multipliers;
	}
	
	public CompoundAnim(float length, Animation... anims) {
		this(length, anims, defaultMultipliers(anims));
	}
	
	public CompoundAnim(Animation[] anims, float[] multipliers) {
		this(calculateLength(anims, multipliers), anims, multipliers);
	}
	
	@MassConstructor
	public CompoundAnim(float length, Animation[] anims, float[] multipliers) {
		super(length);
		this.anims = anims;
		this.multipliers = multipliers;
	}
	@Override
	public Object[] mass() {
		return new Object[] { length, anims, multipliers };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		for(int c = 0; c < anims.length; c++) {
			float animProgress;
			if(multipliers[c] < 0.0f) {
				// This animation is a loop
				animProgress = (renderTime % anims[c].length) / anims[c].length;
				animProgress = (animProgress * -multipliers[c]) % 1.0f;		// Does not need accurate modulus since its a loop
			}
			else
				animProgress = Graph.remainderf(progress * multipliers[c], 1.0f);
			anims[c].apply(renderTime, animProgress, a);
		}
	}
}

package sengine.animation;

import sengine.calc.ConstantGraph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;

public class SequenceAnim extends Animation implements MassSerializable {

	final Animation[] anims;
	final float[] milestones;
	
	static float calculateLength(Animation[] anims) {
		float length = 0.0f;
		for(Animation anim : anims)
			length += anim.length;
		return length;
	}
	
	static final ConstantGraph invisibleGraph = new ConstantGraph(0f, 1f);
	
	public SequenceAnim(Animation animation, float tDelay, boolean makeInvisible) {
		this(animation, tDelay, makeInvisible, true);
	}
	
	public SequenceAnim(Animation animation, float tDelay, boolean makeInvisible, boolean delayAtFront) {
		this(
			delayAtFront ? (
				makeInvisible ?
				(new Animation[] {
					new ScaleAnim(tDelay, invisibleGraph),
					animation,
				}) :
				(new Animation[] {
					new NullAnim(tDelay),
					animation,
				})
			) : (
				makeInvisible ?
				(new Animation[] {
					animation,
					new ScaleAnim(tDelay, invisibleGraph),
				}) :
				(new Animation[] {
					animation,
					new NullAnim(tDelay),
				})
			)
		);
	}
	
	@MassConstructor
	public SequenceAnim(Animation... anims) {
		super(calculateLength(anims));

		this.anims = anims;
		// calculate milestones
		this.milestones = new float[anims.length];
		for(int c = 0; c < anims.length; c++)
			milestones[c] = anims[c].length / length;
	}
	@Override
	public Object[] mass() {
		return new Object[] { anims };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
        if(anims.length == 0)
            return;     // nothing
		int c;
		if(progress < 0.5f) {
			c = 0;
			while(progress > milestones[c]) {
				progress -= milestones[c];
				c++;
			}
			progress = progress / milestones[c];
		}
		else {
			c = milestones.length - 1;
			progress = 1.0f - progress;
			while(progress > milestones[c]) {
				progress -= milestones[c];
				c--;
			}
			progress = 1.0f - (progress / milestones[c]);
		}
		anims[c].apply(renderTime, progress, a);
	}
}

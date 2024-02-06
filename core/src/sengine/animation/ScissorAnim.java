package sengine.animation;

import sengine.graphics2d.Animatable2D;
import sengine.graphics2d.Matrices;

public class ScissorAnim extends CompoundAnim {

	public ScissorAnim(float length, Animation ... anims) {
		super(length, anims);
	}
	
	public ScissorAnim(Animation[] anims, float[] multipliers) {
		super(anims, multipliers);
	}

	public ScissorAnim(float length, Animation[] anims, float[] multipliers) {
		super(length, anims, multipliers);
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D a) {
		Matrices.ScissorBox scissor = Matrices.scissorsPool.obtain();
		Matrices.push();
		a.applyGlobalMatrix();
		super.apply(renderTime, progress, Matrices.getModelMatrixAnimator(a.getLength(), a));
		scissor.set(0, 0, 0, 1f, a.getLength(), 0f);
		Matrices.pop();
		a.scissor(scissor.x, scissor.y, scissor.width, scissor.height);
		Matrices.scissorsPool.free(scissor);
	}

}

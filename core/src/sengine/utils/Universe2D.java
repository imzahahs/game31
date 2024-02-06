package sengine.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;

import sengine.Sys;
import sengine.Universe;
import sengine.graphics2d.Matrices;

public class Universe2D extends Universe {
	
	// Length 
	public final float lengthHint;
	public float length;
	// Camera
	public OrthographicCamera camera;

	public Universe2D(float fixedProcessInterval, float lengthHint) {
		super(fixedProcessInterval);
		
		this.lengthHint = lengthHint;
		this.length = lengthHint > 0.0f ? lengthHint : 1;
	}

	@Override
	protected float resize(int width, int height) {
		// Viewport
		Gdx.gl.glViewport(0, 0, width, height);
		
		length = lengthHint > 0.0f ? lengthHint : ((float)Sys.system.getHeight() / (float)Sys.system.getWidth());
		if(camera == null || lengthHint <= 0f) {
			// Create camera
			camera = new OrthographicCamera(1.0f, length);
			// Camera will be positioned at bottom center
			// So for modelview matrix, objects can be placed from x: 0.0f ~ 1.0f, and y: 0.0f (bottom) to length paramter (top)
			camera.position.set(0.5f, length / 2.0f, 0.0f);
		}

		return length;	
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	@Override
	protected void render(Universe v, float r, float renderTime) {
        // Update camera
		camera.update();
		Matrices.camera = camera;
	}

	// User implementation
	@Override
	protected void pause() {
		
	}

	@Override
	protected void resume() {

	}

	@Override
	protected void stopped() {

	}
}

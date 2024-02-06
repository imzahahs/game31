package sengine.model;

import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.Matrices;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;

public class ModelInstance {
	
	public static final Matrix4 identityMatrix = new Matrix4();
	public static final BoundingBox renderingBounds = new BoundingBox();
	
	public final Model model;
	
	public final SkeletalFrame skeleton;
	public final float[] movementVectors;

	public final SkinnedMesh[] meshes;
	
	public final BoundingBox approxBounds;
	
	public void apply() {
		// Convert to unstructured
		skeleton.convertToUnstructured();
		// Calculate movement matrices
		skeleton.calculateMovementVectors(movementVectors, model.bindPoseInverse);
		// Calculate bounding box
		skeleton.calculateBoundingBox(model.jointRadius, approxBounds);
	}

	public void apply(SkeletalAnimator layer) {
		layer.apply(skeleton);
		apply();
	}

	
	public void applyLayers(SkeletalAnimator ... layers) {
		// Apply all layers
		for(int c = 0; c < layers.length; c++)
			layers[c].apply(skeleton);
		apply();
	}
	
	public void render() {
		renderingBounds.set(approxBounds);			// TODO: performance
		renderingBounds.mul(Matrices.model);
		if(!Matrices.camera.frustum.boundsInFrustum(renderingBounds))
			return;		// not in view
		// Render
		for(int c = 0; c < meshes.length; c++)
			meshes[c].render();
	}
	
	public ModelInstance(Model model) {
		this.model = model;
		
		// Copy bind pose skeleton for interpolation
		this.skeleton = new SkeletalFrame(model.bindPoseStructured);
		// Joint matrices to upload to skinned mesh
		this.movementVectors = new float[12 * skeleton.numJoints];
		for(int c = 0; c < skeleton.numJoints; c++)
			MaterialConfiguration.setTransformVectors(movementVectors, c, identityMatrix);
		
		// Copy all meshes
		this.meshes = new SkinnedMesh[model.meshes.length];
		for(int c = 0; c < meshes.length; c++) {
			meshes[c] = model.meshes[c].instantiate();
			// Set joints data for uploading to GPU
			meshes[c].movementVectors = movementVectors;
		}
		
		this.approxBounds = new BoundingBox(model.approxBounds);
	}
}
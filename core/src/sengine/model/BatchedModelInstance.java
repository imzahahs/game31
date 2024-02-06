package sengine.model;

import sengine.graphics2d.MaterialConfiguration;

public class BatchedModelInstance {
	
	public final Model model;
	
	public final SkeletalFrame skeleton;
	public final float[] movementVectors;

	public final InstancedSkinnedMesh[] meshes;

	public void apply(SkeletalAnimator layer) {
		layer.apply(skeleton);
		// Convert to unstructured
		skeleton.convertToUnstructured();
		// Calculate movement matrices
		skeleton.calculateMovementVectors(movementVectors, model.bindPoseInverse);
	}

	public void applyLayers(SkeletalAnimator ... layers) {
		// Apply all layers
		for(int c = 0; c < layers.length; c++)
			layers[c].apply(skeleton);
		// Convert to unstructured
		skeleton.convertToUnstructured();
		// Calculate movement matrices
		skeleton.calculateMovementVectors(movementVectors, model.bindPoseInverse);
	}
	
	public void render() {
		// Render
		for(int c = 0; c < meshes.length; c++)
			meshes[c].render();
	}

	public BatchedModelInstance(BatchedModel batchedModel) {
		this.model = batchedModel.model;
		
		// Copy bind pose skeleton for interpolation
		this.skeleton = new SkeletalFrame(model.bindPose);
		// Joint matrices to upload to skinned mesh
		this.movementVectors = new float[12 * skeleton.numJoints];			// Save as transform vectors to save space
		for(int c = 0; c < skeleton.numJoints; c++)
			MaterialConfiguration.setTransformVectors(movementVectors, c, ModelInstance.identityMatrix);
		
		// Create meshes
		this.meshes = new InstancedSkinnedMesh[model.meshes.length];
		for(int c = 0; c < meshes.length; c++) {
			meshes[c] = batchedModel.meshes[c].instantiate();
			// Set joints data for uploading to GPU
			meshes[c].movementVectors = movementVectors;
		}
	}
}

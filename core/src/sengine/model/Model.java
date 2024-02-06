package sengine.model;

import sengine.graphics2d.Mesh;
import sengine.mass.MassSerializable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Model implements MassSerializable {

	private static final Vector3 tempVec1 = new Vector3();
	
	public static float defaultMinJointWeight = 0.5f;
	
	public static Model load(String filename) {
		return MD5Loader.loadModel(filename);
	}
	
	public final SkeletalFrame bindPose;
	public final SkeletalFrame bindPoseStructured;
	
	public final Matrix4[] bindPoseInverse;
	
	public final SkinnedMesh[] meshes;
	
	// Bounding box
	public final float[] jointRadius;
	public final BoundingBox approxBounds;
	public final BoundingBox accurateBounds;
	
	@SuppressWarnings("deprecation")
	public float calculateSize(boolean useX, boolean useY, boolean useZ) {
		Vector3 dimensions = accurateBounds.getDimensions(tempVec1);
		float size = 0.0f;
		if(useX)
			size = dimensions.x;
		if(useY && size < dimensions.y)
			size = dimensions.y;
		if(useZ && size < dimensions.z)
			size = dimensions.z;
		return size;
	}
	
	public Model instantiate() {
		// Instantiate all mesh
		SkinnedMesh[] meshCopies = new SkinnedMesh[meshes.length];
		for(int c = 0; c < meshCopies.length; c++)
			meshCopies[c] = meshes[c].instantiate();
		
		return new Model(
			bindPose, 
			bindPoseStructured, 
			bindPoseInverse, 
			meshCopies, 
			jointRadius, 
			approxBounds, 
			accurateBounds
		);
	}
	
	public Model(SkeletalFrame bindPose, SkinnedMesh[] meshes) {
		this(bindPose, meshes, defaultMinJointWeight);
	}
	
	public Model(SkeletalFrame bindPose, SkinnedMesh[] meshes, float minJointWeight) {
		this(bindPose, meshes, bindPose.calculateJointRadius(meshes, minJointWeight));
	}
	

	@MassConstructor
	public Model(SkeletalFrame bindPose, SkinnedMesh[] meshes, float[] jointRadius) {
		this.bindPose = bindPose;
		this.bindPoseStructured = new SkeletalFrame(bindPose);
		bindPoseStructured.convertToStructured();
		
		this.meshes = meshes;
		
		this.bindPoseInverse = new Matrix4[bindPose.numJoints];
		
		// Calculate and invert all the bindPose matrices
		for(int c = 0; c < bindPose.numJoints; c++) {
			Matrix4 jointMat = new Matrix4();
			bindPose.calculateJointMatrix(c, jointMat);
			bindPoseInverse[c] = jointMat.inv();
		}
		
		// Joint radius
		this.jointRadius = jointRadius;
		
		// Bounding box
		this.approxBounds = new BoundingBox();
		bindPose.calculateBoundingBox(jointRadius, approxBounds);
		
		// Calculate accurate bounds
		this.accurateBounds = new BoundingBox();
		for(int c = 0; c < meshes.length; c++)
			Mesh.accumulateBounds(meshes[c], accurateBounds);
	}
	@Override
	public Object[] mass() {
		return new Object[] { bindPose, meshes, jointRadius };
	}
	

	public Model(SkeletalFrame bindPose, SkeletalFrame bindPoseStructured,
			Matrix4[] bindPoseInverse, SkinnedMesh[] meshes,
			float[] jointRadius, BoundingBox approxBounds,
			BoundingBox accurateBounds
	) {
		this.bindPose = bindPose;
		this.bindPoseStructured = bindPoseStructured;
		this.bindPoseInverse = bindPoseInverse;
		this.meshes = meshes;
		this.jointRadius = jointRadius;
		this.approxBounds = approxBounds;
		this.accurateBounds = accurateBounds;
	}
}

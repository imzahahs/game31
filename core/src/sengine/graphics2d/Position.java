package sengine.graphics2d;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Position {
	public static final Vector3 vec1 = new Vector3();
	public static final Quaternion quat1 = new Quaternion();
	public static final Matrix4 mat1 = new Matrix4();
	
	public final Vector3 translation = new Vector3();
	public final Quaternion rotation = new Quaternion();
	public float scale = 1f;
	
	public void idt() {
		translation.set(0, 0, 0);
		rotation.idt();
		scale = 1f;
	}
	
	public Position translate(Vector3 vec) {
		return translate(vec.x, vec.y, vec.z);
	}
	
	public Position translate(float x, float y, float z) {
		vec1.set(x, y, z);
		rotation.transform(vec1);
		vec1.scl(scale);
		translation.add(vec1);
		return this;
	}
	
	public Position rotate(float x, float y, float z, float degrees) {
		quat1.setFromAxis(x, y, z, degrees);
		rotation.mul(quat1);
		return this;
	}
	
	public Position rotate(Quaternion rotate) {
		rotation.mul(rotate);
		return this;
	}
	
	public Position rotateRad(float x, float y, float z, float radians) {
		quat1.setFromAxisRad(x, y, z, radians);
		rotation.mul(quat1);
		return this;
	}
	
	public Position rotateCross(Vector3 from, Vector3 to) {
		quat1.setFromCross(from, to);
		rotation.mul(quat1);
		return this;
	}
	
	public Position scale(float scalar) {
		scale *= scalar;
		return this;
	}
	
	public Position mul(Position position) {
		translate(position.translation);
		rotation.mul(position.rotation);
		scale *= position.scale;
		return this;
	}
	
	public Position set(Vector3 newTranslation) {
		translation.set(newTranslation);
		return this;
	}
	
	public Position set(Quaternion newRotation) {
		rotation.set(newRotation);
		return this;
	}

	public Position set(float newScale) {
		scale = newScale;
		return this;
	}

	public Position set(Position pos) {
		translation.set(pos.translation);
		rotation.set(pos.rotation);
		scale = pos.scale;
		return this;
	}
	
	public Position lerp(Position target, float alpha) {
		translation.lerp(target.translation, alpha);
		rotation.slerp(target.rotation, alpha);
		scale = scale + ((target.scale - scale) * alpha);
		return this;
	}
	
	public void transform(BoundingBox bounds) {
		mat1.set(
			translation.x, translation.y, translation.z, 
			rotation.x, rotation.y, rotation.z, rotation.w, 
			scale, scale, scale
		);
		bounds.mul(mat1);
	}
	
	public void transform(Matrix4 m) {
		mat1.set(
			translation.x, translation.y, translation.z, 
			rotation.x, rotation.y, rotation.z, rotation.w, 
			scale, scale, scale
		);
		m.mul(mat1);
	}
}

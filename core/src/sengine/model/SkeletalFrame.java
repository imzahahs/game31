package sengine.model;

import sengine.graphics2d.MaterialConfiguration;
import sengine.mass.MassSerializable;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.FloatArray;

public class SkeletalFrame implements MassSerializable {

	// x, y, z, ox, oy, oz, ow
	public static final int STRIDE = 7;

	public static float deltaTolerance = 0.00001f;

	public static final Quaternion quat1 = new Quaternion();
	public static final Quaternion quat2 = new Quaternion();
	
	public static final Vector3 vec1 = new Vector3();
	public static final Vector3 vec2 = new Vector3();
	public static final Vector3 vec3 = new Vector3();
	
	public static final Matrix4 mat4 = new Matrix4();
	
	public static final float[] verts1 = new float[STRIDE];
	public static final float[] verts2 = new float[STRIDE];

	// Delta functions
	public static void getJointDelta(Vector3 vec, Quaternion quat, SkeletalAnimation anim, int frame, int joint) {
		float[] deltas;
		int startOffset;
		int endOffset;
		if(anim == null) {
			deltas = null;
			startOffset = -1;
			endOffset = -1;
		}
		else {
			deltas = anim.deltas;
			startOffset = anim.frameOffsets[frame];
			endOffset = anim.frameOffsets[frame + 1];
		}
		getJointDelta(vec, quat, deltas, startOffset, endOffset, joint);
	}
	
	public static void getJointDelta(Vector3 vec, Quaternion quat, float[] deltas, int startOffset, int endOffset, int joint) {
		int currentJoint = -1;
		while(startOffset < endOffset && currentJoint < joint) {
			int flag = (int)deltas[startOffset++];
			currentJoint = flag >> STRIDE;
			for(int c = 0; c < STRIDE; c++) {
				if((flag & (1 << c)) != 0)
					verts1[c] = deltas[startOffset++];
				else
					verts1[c] = 0.0f;
			}
		}
		if(currentJoint == joint) {
			// Joint was found
			vec.set(verts1[0], verts1[1], verts1[2]);
			quat.set(verts1[3], verts1[4], verts1[5], verts1[6] + 1.0f);
		}
		else {
			// If no deltas available or joint didn't change
			vec.setZero();
			quat.idt();
		}
	}

	public static void calculateJointDelta(Vector3 vec, Quaternion quat, SkeletalAnimation fromAnim, int fromAnimFrame, SkeletalAnimation toAnim, int toAnimFrame, int joint, Vector3 toOffsetVec, Quaternion toOffsetQuat, float frameProgress) {
		float[] fromDeltas;
		int fromStartOffset;
		int fromEndOffset;
		float[] toDeltas;
		int toStartOffset;
		int toEndOffset;
		// Get From parameters
		if(fromAnim == null) {
			fromDeltas = null;
			fromStartOffset = -1;
			fromEndOffset = -1;
		}
		else {
			fromDeltas = fromAnim.deltas;
			fromStartOffset = fromAnim.frameOffsets[fromAnimFrame];
			fromEndOffset = fromAnim.frameOffsets[fromAnimFrame + 1];
		}
		// Get To parameters
		if(toAnim == null) {
			toDeltas = null;
			toStartOffset = -1;
			toEndOffset = -1;
		}
		else {
			toDeltas = toAnim.deltas;
			toStartOffset = toAnim.frameOffsets[toAnimFrame];
			toEndOffset = toAnim.frameOffsets[toAnimFrame + 1];
		}

		calculateJointDelta(vec, quat, fromDeltas, fromStartOffset, fromEndOffset, toDeltas, toStartOffset, toEndOffset, joint, toOffsetVec, toOffsetQuat, frameProgress);
	}
	
	public static void calculateJointDelta(Vector3 vec, Quaternion quat, float[] fromDeltas, int fromStartOffset, int fromEndOffset, float[] toDeltas, int toStartOffset, int toEndOffset, int joint, Vector3 toOffsetVec, Quaternion toOffsetQuat, float frameProgress) {
		// Get From joint
		getJointDelta(vec, quat, fromDeltas, fromStartOffset, fromEndOffset, joint);
		// Get To joint
		getJointDelta(vec2, quat2, toDeltas, toStartOffset, toEndOffset, joint);
		// Adjust to joint offset if needed
		if(toOffsetVec != null)
			vec2.add(toOffsetVec);
		if(toOffsetQuat != null)
			quat2.mul(toOffsetQuat);
		// Lerp
		vec.lerp(vec2, frameProgress);
		quat.slerp(quat2, frameProgress);
	}

	
	// Skeleton data
	public final int numJoints;
	public final float[] data;
	// Structure
	public String[] names;		// joint names
	public int[] parents;
	
	
	public SkeletalFrame(int numJoints) {
		this.numJoints = numJoints;
		this.names = new String[numJoints];
		this.parents = new int[numJoints];
		this.data = new float[STRIDE * numJoints];
	}

	@MassConstructor
	public SkeletalFrame(String[] names, int[] parents, float[] data) {
		this.numJoints = names.length;
		this.names = names;
		this.parents = parents;
		this.data = data;
	}
	@Override
	public Object[] mass() {
		return new Object[] { names, parents, data };
	}
	
	public SkeletalFrame(SkeletalFrame copyOf) {
		this(copyOf.names, copyOf.parents, new float[STRIDE * copyOf.numJoints]);
		
		// Copy data
		System.arraycopy(copyOf.data, 0, data, 0, data.length);
	}
	
	public SkeletalFrame(SkeletalFrame copyOf, String ... joints) {
		this(joints.length);

		// Rebuild joint hierarchy
		// Copy joint names and order from copyOf hierarchy
		for(int c = 0; c < numJoints; c++)
			names[c] = joints[c];
		// Temporarily copy order into parents array, for subsequent processing
		for(int c = 0; c < numJoints; c++) {
			parents[c] = copyOf.findJoint(this, c);
			if(parents[c] == -1)
				throw new IllegalArgumentException("joints[" + c + "]: " + joints[c] + " not found in copyOf");
		}
		// Reorder joints
		while(true) {
			boolean ordered = true;
			for(int c = 1; c < numJoints; c++) {
				// Get current order
				int order = parents[c];
				// Find a replacementIdx if previous indices order is higher than current
				int replacementIdx = c;
				for(int i = c - 1; i >= 0; i--) {
					if(order < parents[i])
						replacementIdx = i;
				}
				// Switch index if needed
				if(replacementIdx != c) {
					String replacementName = names[replacementIdx];
					names[replacementIdx] = names[c];
					names[c] = replacementName;
					int replacementOrder = parents[replacementIdx];
					parents[replacementIdx] = order;
					parents[c] = replacementOrder;
					ordered = false;
				}
			}
			if(ordered)
				break;		// no switching needed, hierarchy is ordered
		}
		// Find new hierarchy indices
		for(int c = 0; c < numJoints; c++) {
			int copyOfParentIdx = copyOf.parents[copyOf.findJoint(this, c)];
			if(copyOfParentIdx == -1)
				parents[c] = -1;		// root joint
			else
				parents[c] = findJoint(copyOf, copyOfParentIdx);	// Find parent joint in new frame, will be -1 if not found in new frame
		}
		
		// Copy joint data
		set(copyOf);
	}
	
	
	public float[] calculateJointRadius(SkinnedMesh[] meshes, float minWeight) {
		float[] radius = new float[numJoints];
		
		for(int c = 0; c < numJoints; c++) {
			// Get joint position
			getJointPosition(c, vec1);
			
			// Find the farthest vertex in all meshes
			for(int mi = 0; mi < meshes.length; mi++) {
				SkinnedMesh m = meshes[mi];
				
				for(int v = 0; v < m.maxVertices; v++) {
					boolean affected = false;
					for(int w = 0; w < 4; w++) {
						int joint = (int)m.vertices[m.indexBI(v) + w];
						float weight = m.vertices[m.indexBW(v) + w];
						if(joint == c && weight >= minWeight) {
							affected = true;
							break;
						}
					}
					if(!affected)
						continue;			// this vertex is unaffected, next vertex
					
					// Else this vertex is affected by this joint find the distance
					vec2.set(m.vertices[m.indexX(v)], m.vertices[m.indexY(v)], m.vertices[m.indexZ(v)]);
					vec2.sub(vec1);
					
					float r = vec2.len();
					
					// If this vertex is even farther than any other, set as new radius
					if(radius[c] < r)
						radius[c] = r;
				}
			}
		}
		
		return radius;
	}
	
	public void calculateJointBoundingBox(int joint, float radius, BoundingBox bb) {
		getJointPosition(joint, vec1);
		vec2.set(vec1);
		vec1.sub(radius);
		vec2.add(radius);
		bb.set(vec1, vec2);
	}
	
	public void calculateBoundingBox(float[] radius, BoundingBox bb) {
		// Get first joint
		getJointPosition(0, vec1);
		vec2.set(vec1);
		vec1.sub(radius[0]);
		vec2.add(radius[0]);
		
		// Extend to additional joints
		for(int c = 1; c < radius.length; c++) {
			getJointPosition(c, vec3);
			float r = radius[c];
			
			float minx = vec3.x - r;
			float miny = vec3.y - r;
			float minz = vec3.z - r;
			float maxx = vec3.x + r;
			float maxy = vec3.y + r;
			float maxz = vec3.z + r;
			
			if(minx < vec1.x) vec1.x = minx;
			if(miny < vec1.y) vec1.y = miny;
			if(minz < vec1.z) vec1.z = minz;
			if(maxx > vec2.x) vec2.x = maxx;
			if(maxy > vec2.y) vec2.y = maxy;
			if(maxz > vec2.z) vec2.z = maxz;
		}
		
		bb.set(vec1, vec2);
	}
	
	
	public boolean isSameStructure(SkeletalFrame with) {
		if(names == with.names && parents == with.parents)
			return true;
		else if(numJoints != with.numJoints)
			return false;
		// Else possibly same structure, perform per joint comparison
		for(int c = 0; c < numJoints; c++) {
			if(!compareNames(names, c, with.names, c) || parents[c] != with.parents[c])
				return false;		// different structure
		}
		// Else it is the same structure, optimize by setting the same reference for both
		if(System.identityHashCode(names) < System.identityHashCode(with.names))
			with.names = names;
		else
			names = with.names;
		if(System.identityHashCode(parents) < System.identityHashCode(with.parents))
			with.parents = parents;
		else
			parents = with.parents;
		return true;
	}
	
	public String[] findRootJoints() {
		int numRootJoints = 0;
		for(int c = 0; c < numJoints; c++) {
			if(parents[c] == -1)
				numRootJoints++;
		}
		String[] rootJoints = new String[numRootJoints];
		for(int c = 0, i = 0; c < numJoints; c++) {
			if(parents[c] == -1)
				rootJoints[i++] = names[c];
		}
		return rootJoints;
	}
	
	public int findJoint(String name) {
		for(int c = 0; c < numJoints; c++) {
			if(names[c] == name || (names[c].hashCode() == name.hashCode() && names[c].equals(name)))
				return c;
		}
		// Else not found
		return -1;
	}
	
	public int findJoint(SkeletalFrame from, int joint) {
		// If both frames are of the same structure, return joint as it would be the same index
		if(isSameStructure(from))
			return joint;
		// Else both frames are not of the same build, have to find this joint
		for(int c = 0; c < numJoints; c++) {
			if(compareNames(from.names, joint, names, c))
				return c;		// found the joint
		}
		// Else not found
		return -1;
	}
	
	public void convertToUnstructured() {
		for(int joint = 0; joint < numJoints; joint++) {
			int parent = parents[joint];
			
			if(parent == -1)
				continue;
			
			getJointQuaternion(parent, quat1);
			getJointQuaternion(joint, quat2);
			getJointPosition(parent, vec1);
			getJointPosition(joint, vec2);
			
			vec2.mul(quat1);
			vec1.add(vec2);
			quat1.mul(quat2);
			
			setJoint(joint, vec1.x, vec1.y, vec1.z, quat1.x, quat1.y, quat1.z, quat1.w);
		}
	}
	
	public void convertToStructured() {
		for(int joint = numJoints - 1; joint >= 0; joint--) {
			int parent = parents[joint];
			
			if(parent == -1)
				continue;

			getJointQuaternion(parent, quat1);
			getJointPosition(parent, vec1);
			getJointQuaternion(joint, quat2);
			getJointPosition(joint, vec2);
			
			// Invert parent quaternion
			quat1.conjugate();
			
			vec2.sub(vec1);
			vec2.mul(quat1);
			quat1.mul(quat2);
			
			setJoint(joint, vec2.x, vec2.y, vec2.z, quat1.x, quat1.y, quat1.z, quat1.w);
		}
	}
	
	public void calculateJointDelta(SkeletalFrame from, int joint, Vector3 vec, Quaternion quat) {
		from.getJoint(joint, vec1, quat);
		getJoint(joint, vec, quat2);
		
		quat.conjugate();
		quat.mul(quat2);
		
		vec.sub(vec1);
	}
	
	public void packDeltas(FloatArray output, SkeletalFrame base) {
		for(int joint = 0; joint < numJoints; joint++) {
			int flag = 0;

			// Get joints
			base.getJoint(joint, vec1, quat1);
			getJoint(joint, vec2, quat2);
			
			// Invert base quaternion
			quat1.conjugate();
			
			vec2.sub(vec1);
			quat1.mul(quat2);
			
			// Compute stride
			verts1[0] = vec2.x;
			verts1[1] = vec2.y;
			verts1[2] = vec2.z;
			verts1[3] = quat1.x;
			verts1[4] = quat1.y;
			verts1[5] = quat1.z;
			verts1[6] = quat1.w - 1.0f;			// To negate identity quaternion
			
			// Identify only non-empty fields
			for(int c = 0; c < STRIDE; c++) {
				if(Math.abs(verts1[c]) > deltaTolerance)
					flag |= 1 << c;
			}
			
			// Do not output if there is no delta
			if(flag == 0)
				continue;
			
			// Else this joint has changed, identify joint
			flag |= joint << STRIDE;
			
			// Output flag
			output.add(flag);
			
			// Output non-empty fields
			for(int c = 0; c < STRIDE; c++) {
				if((flag & (1 << c)) != 0)
					output.add(verts1[c]);
			}
		}
	}
	
	public void unpackDeltas(float[] deltas, int startOffset, int endOffset, SkeletalFrame baseFrame) {
		while(startOffset < endOffset) {
			// Read deltas
			int flag = (int)deltas[startOffset++];
			int joint = flag >> STRIDE;
			for(int c = 0; c < STRIDE; c++) {
				if((flag & (1 << c)) != 0)
					verts1[c] = deltas[startOffset++];
				else
					verts1[c] = 0.0f;
			}
		
			// Convert joint index to local joint index
			joint = findJoint(baseFrame, joint);
			if(joint == -1)
				continue;		// joint not found
			
			// Else found joint, unpack
			getJoint(joint, vec1, quat1);

			// Update with delta
			if((flag & 7) != 0)
				vec1.add(verts1[0], verts1[1], verts1[2]);

			if((flag & 120) != 0)
				quat1.mul(verts1[3], verts1[4], verts1[5], verts1[6] + 1.0f);
			
			// Save joint
			setJoint(joint, vec1, quat1);
		}
	}
	
	public void lerpDeltas(SkeletalAnimation fromAnim, int fromAnimFrame, SkeletalAnimation toAnim, int toAnimFrame, float r) {
		float[] fromDeltas;
		int fromStartOffset;
		int fromEndOffset;
		SkeletalFrame fromBaseFrame;
		float[] toDeltas;
		int toStartOffset;
		int toEndOffset;
		SkeletalFrame toBaseFrame;
		
		// Get From parameters
		if(fromAnim == null) {
			fromDeltas = null;
			fromStartOffset = -1;
			fromEndOffset = -1;
			fromBaseFrame = null;
		} else {
			fromDeltas = fromAnim.deltas;
			fromStartOffset = fromAnim.frameOffsets[fromAnimFrame];
			fromEndOffset = fromAnim.frameOffsets[fromAnimFrame + 1];
			fromBaseFrame = fromAnim.baseFrame;
		}
		
		// Get To parameters
		if(toAnim == null) {
			toDeltas = null;
			toStartOffset = -1;
			toEndOffset = -1;
			toBaseFrame = null;
		} else {
			toDeltas = toAnim.deltas;
			toStartOffset = toAnim.frameOffsets[toAnimFrame];
			toEndOffset = toAnim.frameOffsets[toAnimFrame + 1];
			toBaseFrame = toAnim.baseFrame;
		}
		
		lerpDeltas(fromDeltas, fromStartOffset, fromEndOffset, fromBaseFrame, toDeltas, toStartOffset, toEndOffset, toBaseFrame, r);
	}
	
	public void lerpDeltas(float[] fromDeltas, int fromStartOffset, int fromEndOffset, SkeletalFrame fromBaseFrame, float[] toDeltas, int toStartOffset, int toEndOffset, SkeletalFrame toBaseFrame, float r) {
		// Check if there is any data to lerp
		if(fromStartOffset == fromEndOffset && toStartOffset == toEndOffset)
			return;		// No data to lerp
		// Check if lerping between the same frame
		if(fromDeltas == toDeltas && fromStartOffset == toStartOffset && fromEndOffset == toEndOffset) {
			// Lerping between the same frames, so just unpack either one
			unpackDeltas(fromDeltas, fromStartOffset, fromEndOffset, fromBaseFrame);
			return;
		}
		
		int fromFlag = 0;
		int fromJoint = Integer.MAX_VALUE;
		int toFlag = 0;
		int toJoint = Integer.MAX_VALUE;
		
		// Keep processing until we've run out of data
		while(true) {
			// Identify which deltas to load
			boolean loadFrom = fromJoint <= toJoint && fromStartOffset < fromEndOffset;
			boolean loadTo = toJoint <= fromJoint && toStartOffset < toEndOffset;
			if(!loadFrom && !loadTo)
				break;		// nothing to load

			// Load deltas
			if(loadFrom) {
				// Keep loading untill we've found a usable joint
				do {
					fromFlag = (int)fromDeltas[fromStartOffset++];
					fromJoint = fromFlag >> STRIDE;
					for(int c = 0; c < STRIDE; c++) {
						if((fromFlag & (1 << c)) != 0)
							verts1[c] = fromDeltas[fromStartOffset++];
						else
							verts1[c] = 0.0f;
					}
					// Convert joint index to current frame index
					fromJoint = findJoint(fromBaseFrame, fromJoint); 
				} while(fromJoint == -1 && fromStartOffset < fromEndOffset);
				// If reached end of deltas and still not found any usable joint, mark as finished
				if(fromJoint == -1)
					fromJoint = Integer.MAX_VALUE;
			}
			if(loadTo) {
				do {
					toFlag = (int)toDeltas[toStartOffset++];
					toJoint = toFlag >> STRIDE;
					for(int c = 0; c < STRIDE; c++) {
						if((toFlag & (1 << c)) != 0)
							verts2[c] = toDeltas[toStartOffset++];
						else
							verts2[c] = 0.0f;
					}
					toJoint = findJoint(toBaseFrame, toJoint);
				} while(toJoint == -1 && toStartOffset < toEndOffset);
				if(toJoint == -1)
					toJoint = Integer.MAX_VALUE;
			}
			
			// If both From and To joint was not found, nothing to lerp
			if(fromJoint == Integer.MAX_VALUE && toJoint == Integer.MAX_VALUE)
				continue;
			
			// Pick the lowest joint
			int joint = fromJoint <= toJoint ? fromJoint : toJoint;
				
			// Get joint
			getJointPosition(joint, vec1);
			getJointQuaternion(joint, quat1);
			vec2.set(vec1);
			quat2.set(quat1);
			
			// Update with delta
			boolean positionUpdated = false;
			boolean orientationUpdated = false;
			
			if(joint == fromJoint) {
				if((fromFlag & 7) != 0) {
					vec1.add(verts1[0], verts1[1], verts1[2]);
					positionUpdated = true;
				}
				if((fromFlag & 120) != 0) {
					quat1.mul(verts1[3], verts1[4], verts1[5], verts1[6] + 1.0f);
					orientationUpdated = true;
				}
				if(fromStartOffset >= fromEndOffset)
					fromJoint = Integer.MAX_VALUE;		// End of data, do not use this joint again
			}
			
			if(joint == toJoint) {
				if((toFlag & 7) != 0) {
					vec2.add(verts2[0], verts2[1], verts2[2]);
					positionUpdated = true;
				}
				if((toFlag & 120) != 0) {
					quat2.mul(verts2[3], verts2[4], verts2[5], verts2[6] + 1.0f);
					orientationUpdated = true;
				}
				if(toStartOffset >= toEndOffset)
					toJoint = Integer.MAX_VALUE;		// End of data, do not use this joint again
			}
			
			// Lerp position if needed
			if(positionUpdated)
				vec1.lerp(vec2, r);
			
			// Slerp orientation if needed
			if(orientationUpdated)
				quat1.slerp(quat2, r);
			
			// Save to current skeleton
			int idx = joint * STRIDE;
			data[idx + 0] = vec1.x;
			data[idx + 1] = vec1.y;
			data[idx + 2] = vec1.z;
			data[idx + 3] = quat1.x;
			data[idx + 4] = quat1.y;
			data[idx + 5] = quat1.z;
			data[idx + 6] = quat1.w;
		}
	}
	
	public void setJoint(int joint, float positionX, float positionY, float positionZ, float quaternionX, float quaternionY, float quaternionZ, boolean invertW) {
		// Calculate quaternionW
		float quaternionW = invertW ? -1 : +1;
		float t = 1.0f - (quaternionX * quaternionX) - (quaternionY * quaternionY) - (quaternionZ * quaternionZ);

		if (t < 0.0f)
			quaternionW = 0;
		else
			quaternionW *= Math.sqrt(t);
		setJoint(joint, positionX, positionY, positionZ, quaternionX, quaternionY, quaternionZ, quaternionW);
	}
	
	public void setJoint(int joint, Vector3 pos, Quaternion quat) {
		setJoint(joint, pos.x, pos.y, pos.z, quat.x, quat.y, quat.z, quat.w);
	}
	
	public void setJoint(int joint, float positionX, float positionY, float positionZ, float quaternionX, float quaternionY, float quaternionZ, float quaternionW) {
		int idx = joint * STRIDE;
		
		// Position
		data[idx + 0] = positionX;
		data[idx + 1] = positionY;
		data[idx + 2] = positionZ;
		// Orientation
		data[idx + 3] = quaternionX;
		data[idx + 4] = quaternionY;
		data[idx + 5] = quaternionZ;
		data[idx + 6] = quaternionW;
	}
	
	public void setJoint(int joint, SkeletalFrame from, int fromJoint) {
		int idx = joint * STRIDE;
		int fromIdx = fromJoint * STRIDE;
		
		// Position
		data[idx + 0] = from.data[fromIdx + 0];
		data[idx + 1] = from.data[fromIdx + 1];
		data[idx + 2] = from.data[fromIdx + 2];
		// Orientation
		data[idx + 3] = from.data[fromIdx + 3];
		data[idx + 4] = from.data[fromIdx + 4];
		data[idx + 5] = from.data[fromIdx + 5];
		data[idx + 6] = from.data[fromIdx + 6];
	}
	
	public void transformToJoint(int joint, Matrix4 m) {
		getJoint(joint, vec1, quat1);
		m.translate(vec1);
		m.rotate(quat1);
	}

	public void getJoint(int joint, Vector3 vec, Quaternion quat) {
		int idx = joint * STRIDE;
		
		vec.set(data[idx + 0], data[idx + 1], data[idx + 2]);
		quat.set(data[idx + 3], data[idx + 4], data[idx + 5], data[idx + 6]);
	}
	
	
	public void getJointQuaternion(int joint, Quaternion quat) {
		int idx = joint * STRIDE;
		
		quat.set(data[idx + 3], data[idx + 4], data[idx + 5], data[idx + 6]);
	}
	
	public void getJointPosition(int joint, Vector3 vec) {
		int idx = joint * STRIDE;
		
		vec.set(data[idx + 0], data[idx + 1], data[idx + 2]);
	}

	public void setJointQuaternion(int joint, Quaternion quat) {
		int idx = joint * STRIDE;
		
		data[idx + 3] = quat.x;
		data[idx + 4] = quat.y;
		data[idx + 5] = quat.z;
		data[idx + 6] = quat.w;
	}
	public void setJointQuaternion(int joint, float x, float y, float z, float w) {
		int idx = joint * STRIDE;
		
		data[idx + 3] = x;
		data[idx + 4] = y;
		data[idx + 5] = z;
		data[idx + 6] = w;
	}
	
	public void setJointPosition(int joint, Vector3 vec) {
		int idx = joint * STRIDE;
		
		data[idx + 0] = vec.x;
		data[idx + 1] = vec.y;
		data[idx + 2] = vec.z;
	}
	public void setJointPosition(int joint, float x, float y, float z) {
		int idx = joint * STRIDE;
		
		data[idx + 0] = x;
		data[idx + 1] = y;
		data[idx + 2] = z;
	}
	
	public void calculateJointMatrix(int joint, Matrix4 mat) {
		int idx = joint * STRIDE;
		
		mat.set(
			data[idx + 0], data[idx + 1], data[idx + 2], 
			data[idx + 3], data[idx + 4], data[idx + 5], data[idx + 6]
		);
	}
	
	public void calculateMovementVectors(float[] movementVectors, Matrix4[] bindPoseInverse) {
		for(int c = 0; c < numJoints; c++) {
			// Get current joint matrix
			calculateJointMatrix(c, mat4);
			// Multiply with bind pose's inverse matrix to find the delta
			mat4.mul(bindPoseInverse[c]);
			// Now we have the movement of the joint, save as movement vectors
			MaterialConfiguration.setTransformVectors(movementVectors, c, mat4);
		}
	}
	
	public SkeletalFrame add(SkeletalFrame with) {
		// Add each joint 
		for(int joint = 0; joint < numJoints; joint++) {
			// Find the joint index in the With skeleton
			int withJointIndex = with.findJoint(this, joint);
			if(withJointIndex == -1)
				continue;		// joint not found
			// Get both joints
			getJoint(joint, vec1, quat1);
			with.getJoint(withJointIndex, vec2, quat2);
			// Add
			vec1.add(vec1);
			quat1.mul(quat2);
			// Set
			setJoint(joint, vec1, quat1);
		}
		return this;	// for chaining
	}
	
	public SkeletalFrame sub(SkeletalFrame with) {
		// Sub each joint 
		for(int joint = 0; joint < numJoints; joint++) {
			// Find the joint index in the With skeleton
			int withJointIndex = with.findJoint(this, joint);
			if(withJointIndex == -1)
				continue;		// joint not found
			// Get both joints
			getJoint(joint, vec1, quat1);
			with.getJoint(withJointIndex, vec2, quat2);
			// Sub
			vec1.sub(vec1);
			quat2.conjugate();
			quat2.mul(quat1);
			// Set
			setJoint(joint, vec1, quat2);
		}
		return this;	// for chaining
	}
	
	public SkeletalFrame set(SkeletalFrame from) {
		if(from == this)
			return this;		// Same frame, so no point copying
		// Else check if same structure
		if(isSameStructure(from)) {
			// Is exactly same structure, so just raw copy
			System.arraycopy(from.data, 0, data, 0, data.length);
			return this;
		}
		// Else not of the same structure, need to copy each joint individually
		for(int joint = 0; joint < numJoints; joint++) {
			// Find the joint index in the From skeleton
			int fromJointIndex = from.findJoint(this, joint);
			if(fromJointIndex == -1)
				continue;		// joint not found
			// Else copy joint
			setJoint(joint, from, fromJointIndex);
		}
		return this;
	}
	
	public void rotate(Quaternion rotation, boolean isStructured) {
		if(!isStructured)
			convertToStructured();
		getJoint(0, vec1, quat1);
		quat2.set(rotation);
		quat2.transform(vec1);
		quat2.mul(quat1);
		setJoint(0, vec1, quat2);
		if(!isStructured)
			convertToUnstructured();
	}
	
	public void lerp(SkeletalFrame from, SkeletalFrame to, float r) {
		if(from == null)
			from = this;
		if(to == null)
			to = this;
		// If lerping between same frames, just copy joint data either one
		if(from == to) {
			set(from);
			return;
		}

		for(int joint = 0; joint < numJoints; joint++) {
			// Find the joint index in the From and To skeleton
			int fromJointIndex = from.findJoint(this, joint);
			int toJointIndex = to.findJoint(this, joint);

			// Both From and To frames does not have this joint, so nothing to lerp, continue next joint
			if(fromJointIndex == -1 && toJointIndex == -1)
				continue;
			
			// Else need to lerp, load position and orientation
			if(fromJointIndex != -1) {
				from.getJointPosition(fromJointIndex, vec1);
				from.getJointQuaternion(fromJointIndex, quat1);
			}
			else {
				getJointPosition(joint, vec1);
				getJointQuaternion(joint, quat1);
			}
			if(toJointIndex != -1) {
				to.getJointPosition(toJointIndex, vec2);
				to.getJointQuaternion(toJointIndex, quat2);
			} else {
				getJointPosition(joint, vec2);
				getJointQuaternion(joint, quat2);
			}
			
			// Lerp position if necessary
			if(vec1.x != vec2.x || vec1.y != vec2.y || vec1.z != vec2.z)
				vec1.lerp(vec2, r);
			
			// Slerp orientation if necessary
			if(quat1.x != quat2.x || quat1.y != quat2.y || quat1.z != quat2.z || quat1.w != quat2.w)
				quat1.slerp(quat2, r);
			
			// Set joint
			setJoint(joint, vec1, quat1);
		}
	}
	
	public static boolean compareNames(String names1[], int index1, String names2[], int index2) {
		String s1 = names1[index1];
		String s2 = names2[index2];
		if(s1 == s2)
			return true;
		// Else try optimize
		if(s1.hashCode() == s2.hashCode() && s1.equals(s2)) {
			// If identical strings, replace with the 'older' duplicate for faster access and save memory
			if(System.identityHashCode(s1) < System.identityHashCode(s2))
				names2[index2] = s1;
			else
				names1[index1] = s2;
			return true;
		}
		// Not found
		return false;
	}
}

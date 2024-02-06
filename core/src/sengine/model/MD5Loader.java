package sengine.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

import sengine.File;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.Mesh;

public class MD5Loader {
	static final String TAG = "MD5Loader";

	public static final int VERSION = 10;
	
	enum Operation {
		md5version,
		commandline,
		
		numjoints,
		nummeshes,
		
		sortpoint,
		flatnormals,
		maxweights,
		rotation,
		
		joints,
		
		mesh,
		shader,
		numverts,
		vert,
		numtris,
		tri,
		numweights,
		weight,
		
		// md5anim
		numframes,
// 		numJoints,
		framerate,
		numanimatedcomponents,
		
		hierarchy,
		bounds,
		
		baseframe,
		
		frame,
		
		// tags and timing
		tag,
		normaltransitiontime,
		interruptedtransitiontime,
		
		unknown,
	}
	
	static Operation identify(String op) {
		try {
			return Operation.valueOf(op.toLowerCase());
		} catch(IllegalArgumentException e) {
			return Operation.unknown;
		}
	}

	static String parse(BufferedReader reader, List<String> tokens) throws IOException {
		do {
			tokens.clear();
			String line = reader.readLine();
			if(line == null || line.contains("}"))
				return null;
			String regex = "\"([^\"]*)\"|(\\S+)";
			Matcher m = Pattern.compile(regex).matcher(line);
			while (m.find()) {
				String token = m.group(1);
				if(token == null)
					token = m.group(2);
				// Check for comments
				int commentIdx = token.indexOf("//");
				if(commentIdx != -1) {
					// Split token into comments
					if(commentIdx > 0) {
						token = token.substring(0, commentIdx);
						tokens.add(token);
					}
					break;		// ignore tokens after comment
				}
				// Add token
				tokens.add(token);
			}
		} while(tokens.size() == 0);
		return tokens.get(0);
	}
	
	static class MeshCompiler {
		static final int VERT_STRIDE = 4;
		static final int WEIGHT_STRIDE = 5;
		
		public final String shader;
		public final boolean flatNormals;

		int numVerts = 0;
		int prevNumVerts = 0;
		float[] verts = null;
		
		int numTris = 0;
		int prevNumTris = 0;
		short[] indices = null;
		
		int numWeights = 0;
		int prevNumWeights = 0;
		float[] weights = null;
		
		public MeshCompiler(String shader, boolean flatNormals) {
			this.shader = shader;
			this.flatNormals = flatNormals;
		}
		
		public SkinnedMesh compile(Matrix4[] jointMatrices, int maxWeights) {
			Sys.info(TAG, "Compiling mesh: " + shader + ", " + numVerts + " verts, " + numTris + " tris, " + 
				(flatNormals ? "flat normals" : "soft normals") + ", " + maxWeights + " weights");
			SkinnedMesh m = new SkinnedMesh(numVerts, numTris * 3);
			
			Vector3 vec1 = new Vector3();
			Vector3 vec2 = new Vector3();
			
			IntArray jointIndices = new IntArray(false, 4);
			FloatArray jointWeights = new FloatArray(false, 4);
			
			// Copy indices
			m.replaceIndices(indices);
			
			// Process vertices 
			for(int c = 0; c < numVerts; c++) {
				int idx = c * VERT_STRIDE;
				
				// Set UV
				m.vertices[m.indexU(c)] = verts[idx + 0];
				m.vertices[m.indexV(c)] = verts[idx + 1];
				
				// Calculate position
				vec2.set(0, 0, 0);
				int weightOffset = (int)verts[idx + 2];
				int weightCount = (int)verts[idx + 3];
				
				for(int w = 0; w < weightCount; w++) {
					int idx2 = (weightOffset + w) * WEIGHT_STRIDE;
					
					// Get joint
					int joint = (int)weights[idx2 + 0];
					float jointWeight = weights[idx2 + 1];
					
					// Get weight
					vec1.set(weights[idx2 + 2], weights[idx2 + 3], weights[idx2 + 4]);
					
					// Transform weight to model space
					vec1.mul(jointMatrices[joint]);
					
					// Adjust to joint weight and add to final position 
					vec1.scl(jointWeight);
					vec2.add(vec1);

					jointIndices.add(joint);
					jointWeights.add(jointWeight);
				}
				
				// Set position
				m.vertices[m.indexX(c)] = vec2.x;
				m.vertices[m.indexY(c)] = vec2.y;
				m.vertices[m.indexZ(c)] = vec2.z;
				
				// Set weights
				if(jointIndices.size <= 4) {
					for(int w = 0; w < jointIndices.size; w++) {
						m.vertices[m.indexBI(c) + w] = jointIndices.items[w];
						m.vertices[m.indexBW(c) + w] = jointWeights.items[w];
					}
				}
				else {
					Sys.error(TAG, "Clamping number of joints from " + jointIndices.size + " to 4 for mesh " + shader + " vertex-" + c);
					float totalWeight = 0.0f;
					for(int saved = 0; saved < 4; saved++) {
						int bestJoint = -1;
						float bestJointWeight = -Float.MAX_VALUE;
						
						// Find best joint first
						for(int w = 0; w < jointIndices.size; w++) {
							if(jointWeights.items[w] > bestJointWeight) {
								bestJoint = w;
								bestJointWeight = jointWeights.items[w];
							}
						}
						
						m.vertices[m.indexBI(c) + saved] = jointIndices.removeIndex(bestJoint);
						m.vertices[m.indexBW(c) + saved] = jointWeights.removeIndex(bestJoint);
						totalWeight += bestJointWeight;
					}
					
					// Saved 4 best joints, now normalize weights to equal one
					if(totalWeight > 0.0f) {
						for(int w = 0; w < 4; w++)
							m.vertices[m.indexBW(c) + w] /= totalWeight;
					}
				}
				
				jointIndices.clear();
				jointWeights.clear();
			}
			
			// If flatNormals, flatten indices
			if(flatNormals) {
				SkinnedMesh f = new SkinnedMesh(numTris * 3, numTris * 3);
				for(int c = 0; c < f.maxVertices; c++) {
					int sourceOffset = m.indices[c] * 16;
					int destOffset = c * 16;
					for(int i = 0; i < 16; i++)
						f.vertices[destOffset + i] = m.vertices[sourceOffset + i];
				}
				m = f;
			}
			
			// If less than 4 max weights, resample skin
			if(maxWeights < 4)
				SkinnedMesh.resampleSkin(m, maxWeights);
			
			// Load material
			m.setMaterial(Material.load(shader));
			
			return m;
		}
		
		public void finish() {
			prevNumVerts = numVerts;
			prevNumTris = numTris;
			prevNumWeights = numWeights;
		}
		
		public void addNumVerts(int newNumVerts) {
			numVerts += newNumVerts;
			float[] newVerts = new float[VERT_STRIDE * numVerts];
			if(verts != null)
				System.arraycopy(verts, 0, newVerts, 0, verts.length);
			verts = newVerts;
		}
		
		public void addNumTris(int newNumTris) {
			numTris += newNumTris;
			short[] newIndices = new short[numTris * 3];
			if(indices != null)
				System.arraycopy(indices, 0, newIndices, 0, indices.length);
			indices = newIndices; 
		}

		public void addNumWeights(int newNumWeights) {
			numWeights += newNumWeights;
			float[] newWeights = new float[WEIGHT_STRIDE * numWeights];
			if(weights != null)
				System.arraycopy(weights, 0, newWeights, 0, weights.length);
			weights = newWeights;
		}
		
		public void setVert(int c, float u, float v, int weightOffset, int weightCount) {
			int idx = (prevNumVerts + c) * VERT_STRIDE;
			verts[idx + 0] = u;
			verts[idx + 1] = v;
			verts[idx + 2] = prevNumWeights + weightOffset;
			verts[idx + 3] = weightCount;
		}
		
		public void setTri(int c, short v1, short v2, short v3) {
			int idx = (prevNumTris + c) * 3;
			indices[idx + 0] = (short)(prevNumVerts + v1);
			indices[idx + 1] = (short)(prevNumVerts + v2);
			indices[idx + 2] = (short)(prevNumVerts + v3);
		}
		
		public void setWeight(int c, int joint, float jointBias, float weightX, float weightY, float weightZ) {
			int idx = (prevNumWeights + c) * WEIGHT_STRIDE;
			weights[idx + 0] = joint;
			weights[idx + 1] = jointBias;
			weights[idx + 2] = weightX;
			weights[idx + 3] = weightY;
			weights[idx + 4] = weightZ;
		}
	}
		
	
	public static Model loadModel(String filename) {
		Model model = File.getHints(filename, false);
		if(model != null)
			return model;
		
		// Open stream
		InputStream stream = File.open(filename).read();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 1024);

		// Parsing data
		List<String> tokens = new ArrayList<String>(10);
		String op = null;

		// Joints
		int numJoints = 0;
		SkeletalFrame bindPose = null;
		Matrix4[] jointMatrices = null;
		
		// Meshes
		int numMeshes = 0;
		MeshCompiler[] compilers = null;
		int currentMesh = 0;
		
		// Additional info
		Vector3 sortPoint = null;
		int maxWeights = 4;
		Quaternion rotation = new Quaternion();
		Quaternion quat = new Quaternion();
		boolean defaultFlatNormals = false;

		try {
			while((op = parse(reader, tokens)) != null) {
				switch(identify(op)) {
				case md5version: {
					int version = Integer.parseInt(tokens.get(1));
					if (version != VERSION)
						throw new IllegalArgumentException("Unsupported version " + version + ", supported only version " + VERSION);
					break;
				}
				
				case commandline: 
					break;		// ignored
				
				case numjoints: {
					numJoints = Integer.parseInt(tokens.get(1));
					bindPose = new SkeletalFrame(numJoints);
					jointMatrices = new Matrix4[numJoints];
					break;
				}
				
				case nummeshes: {
					numMeshes = Integer.parseInt(tokens.get(1));
					compilers = new MeshCompiler[numMeshes];
					break;
				}
				
				case sortpoint: {
					sortPoint = new Vector3(
						Float.parseFloat(tokens.get(1)),
						Float.parseFloat(tokens.get(2)),
						Float.parseFloat(tokens.get(3))
					);
					break;
				}
					
				case maxweights:
					maxWeights = Integer.parseInt(tokens.get(1));
					break;

				case flatnormals:
					defaultFlatNormals = tokens.get(1).equals("true");
					break;

				case rotation:
					quat.setFromAxis(
						Float.parseFloat(tokens.get(1)), 
						Float.parseFloat(tokens.get(2)), 
						Float.parseFloat(tokens.get(3)), 
						Float.parseFloat(tokens.get(4)) 
					);
					rotation.mul(quat);
					break;
				
				case joints: {
					int c = 0;
					while((op = parse(reader, tokens)) != null) {
						// Name
						bindPose.names[c] = tokens.get(0);
						// Hierarchy
						bindPose.parents[c] = Integer.parseInt(tokens.get(1));
						// Position and orientation
						bindPose.setJoint(
							c, 
							Float.parseFloat(tokens.get(3)),
							Float.parseFloat(tokens.get(4)),
							Float.parseFloat(tokens.get(5)),
							Float.parseFloat(tokens.get(8)),
							Float.parseFloat(tokens.get(9)),
							Float.parseFloat(tokens.get(10)),
							true
						);
						c++;
					}
					
					// Calculate all joint matrices
					for(int joint = 0; joint < numJoints; joint++) {
						Matrix4 mat = new Matrix4();
						bindPose.calculateJointMatrix(joint, mat);
						jointMatrices[joint] = mat;
					}
						
					break;
				}
				
				case mesh: {
					// Read mesh section
					MeshCompiler compiler = null;
					String shader = null;
					boolean flatNormals = defaultFlatNormals;
					while((op = parse(reader, tokens)) != null) {
						Operation operation = identify(op);
						switch(operation) {
						case shader:
							// Identify shader
							shader = tokens.get(1);
							break;
							
						case flatnormals:
							flatNormals = tokens.get(1).equals("true");
							break;
							
						// Preparation for data entry, make sure compiler is identified 
						case numverts:
						case numtris:
						case numweights: {
							// Make sure compiler is identified
							if(compiler == null) {
								// Find an existing compiler which matches the description, so we can batch similar meshes together
								for(int c = 0; c < currentMesh; c++) {
									// Check name and flatnormals
									if(compilers[c].shader.equals(shader) && (compilers[c].flatNormals == flatNormals)) {
										compiler = compilers[c];
										break;
									}
								}
								// Else if totally different mesh, create new compiler
								if(compiler == null) {
									compiler = new MeshCompiler(shader, flatNormals);
									compilers[currentMesh] = compiler;
									currentMesh++;
								}
							}
							
							// Prepare for entry data
							switch(operation) {
							case numverts:
								compiler.addNumVerts(Integer.parseInt(tokens.get(1)));
								break;
								
							case numtris:
								compiler.addNumTris(Integer.parseInt(tokens.get(1)));
								break;
								
							case numweights:
							default:
								compiler.addNumWeights(Integer.parseInt(tokens.get(1)));
								break;
							}
							
							break;
						}
						
						// Data entry operations
						case vert:
							compiler.setVert(
								Integer.parseInt(tokens.get(1)), 
								Float.parseFloat(tokens.get(3)), 
								Float.parseFloat(tokens.get(4)), 
								Integer.parseInt(tokens.get(6)), 
								Integer.parseInt(tokens.get(7)) 
							);
							break;
							
						case tri:
							compiler.setTri(
								Integer.parseInt(tokens.get(1)),
								Short.parseShort(tokens.get(2)),
								Short.parseShort(tokens.get(3)),
								Short.parseShort(tokens.get(4))
							);
							break;
							
						case weight:
							compiler.setWeight(
								Integer.parseInt(tokens.get(1)),
								Integer.parseInt(tokens.get(2)),
								Float.parseFloat(tokens.get(3)),
								Float.parseFloat(tokens.get(5)),
								Float.parseFloat(tokens.get(6)),
								Float.parseFloat(tokens.get(7))
							);
							break;
							
						case unknown:
						default:
							Sys.debug(TAG, "Unexpected operation in mesh section: " + op);					
						}
					}
					
					// Finished reading mesh section
					compiler.finish();
					
					break;
				}
				
				case unknown:
				default:
					Sys.debug(TAG, "Unexpected operation: " + op);					
				}
			}

			// Finished reading md5mesh, create Model
			// Create actual SkinnedMeshes 
			SkinnedMesh[] meshes = new SkinnedMesh[currentMesh];
			for(int c = 0; c < currentMesh; c++)
				meshes[c] = compilers[c].compile(jointMatrices, maxWeights);

			// Apply rotation
			bindPose.rotate(rotation, false);
			for(int c = 0; c < currentMesh; c++)
				Mesh.rotate(meshes[c], rotation);

			// Calculate normals
			for(int c = 0; c < currentMesh; c++)
				Mesh.accumulateNormals(meshes[c], false);
			
			// Spread normals across all meshes
			Mesh.spreadNormals(meshes);
			
			for(int c = 0; c < currentMesh; c++) {
				// Recalculate normals for flatNormal meshes (spreadNormals() would have smoothed them)
				if(compilers[c].flatNormals) {
					Mesh.clearNormals(meshes[c]);
					Mesh.accumulateNormals(meshes[c], false);
				}
				// Normalize all normals as well
				Mesh.normalizeNormals(meshes[c]);
			}
			
			// Sort triangles to sort point
			if(sortPoint != null ) {
				for(int c = 0; c < currentMesh; c++)
					Mesh.sortTrisDistance(meshes[c], sortPoint);
			}
			
			Sys.info(TAG, "Parsed " + currentMesh + " meshes from " + filename);
			
			model = new Model(bindPose, meshes);
			File.saveHints(filename, model);
			
			reader.close();

			return model;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to load model: " + filename, e);
		}
	}
	
	public static SkeletalAnimation loadAnimation(String filename) {
		SkeletalAnimation anim = File.getHints(filename, false);
		if(anim != null)
			return anim;
		
		// Open stream
		InputStream stream = File.open(filename).read();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream), 1024);
		
		// Parsing data
		List<String> tokens = new ArrayList<String>(10);
		String op = null;
		
		// Additional info
		Quaternion rotation = new Quaternion();
		Quaternion quat = new Quaternion();

		// Joints
		int numJoints = 0;
		SkeletalFrame baseFrame = null;
		SkeletalFrame frame = null;
		int jointFlags[] = null;
		int jointDataOffset[] = null;
		
		// Animation data
		int frameRate = 0;
		int numAnimatedComponents = 0;
		float[] frameData = null;

		int numFrames = 0;
		FloatArray animData = new FloatArray();
		IntArray animOffsets = new IntArray();
		animOffsets.add(0);
		
		// Timing			
		ArrayList<String> tagsList = new ArrayList<String>();
		HashMap<String, Float> tInterruptedTransitionTimes = new HashMap<String, Float>();
		HashMap<String, Float> tNormalTransitionTimes = new HashMap<String, Float>();
		
		Vector3 vec1 = new Vector3();
		Quaternion quat1 = new Quaternion();

		try {
			while((op = parse(reader, tokens)) != null) {
				switch(identify(op)) {
				case md5version: {
					int version = Integer.parseInt(tokens.get(1));
					if (version != VERSION)
						throw new IllegalArgumentException("Unsupported version " + version + ", supported only version " + VERSION);
					break;
				}
				
				case commandline: 
					break;		// ignored
					
				case rotation:
					quat.setFromAxis(
						Float.parseFloat(tokens.get(1)), 
						Float.parseFloat(tokens.get(2)), 
						Float.parseFloat(tokens.get(3)), 
						Float.parseFloat(tokens.get(4)) 
					);
					rotation.mul(quat);
					break;

				case numjoints: {
					numJoints = Integer.parseInt(tokens.get(1));
					baseFrame = new SkeletalFrame(numJoints);
					jointFlags = new int[numJoints];
					jointDataOffset = new int[numJoints];
					break;
				}
				
				case numanimatedcomponents: {
					numAnimatedComponents = Integer.parseInt(tokens.get(1));
					frameData = new float[numAnimatedComponents];
					break;
				}

				case framerate: {
					frameRate = Integer.parseInt(tokens.get(1));
					break;
				}

				case numframes: {
					numFrames = Integer.parseInt(tokens.get(1));
					break;
				}

				case hierarchy: {
					int c = 0;
					while((op = parse(reader, tokens)) != null) {
						baseFrame.names[c] = tokens.get(0);
						baseFrame.parents[c] = Integer.parseInt(tokens.get(1));
						jointFlags[c] = Integer.parseInt(tokens.get(2));
						jointDataOffset[c] = Integer.parseInt(tokens.get(3));
						c++;
					}
					// Create a copy for processing					
					frame = new SkeletalFrame(baseFrame);
					break;
				}
				
				case bounds: {
					// not storing per-frame bounds
					while((op = parse(reader, tokens)) != null) {
						// ignore
					}
					break;
				}
				
				case baseframe: {
					int c = 0;
					while((op = parse(reader, tokens)) != null) {
						// Position and orientation
						baseFrame.setJoint(
							c, 
							Float.parseFloat(tokens.get(1)),
							Float.parseFloat(tokens.get(2)),
							Float.parseFloat(tokens.get(3)),
							Float.parseFloat(tokens.get(6)),
							Float.parseFloat(tokens.get(7)),
							Float.parseFloat(tokens.get(8)),
							true
						);
						c++;
					}
					
					// Apply rotation
					baseFrame.rotate(rotation, true);
					
					break;
				}
				
				case frame: {
					// Reset frame
					frame.set(baseFrame);
					
					// Load entire frame data
					int c = 0;
					while((op = parse(reader, tokens)) != null) {
						int size = tokens.size();
						for(int i = 0; i < size; i++, c++)
							frameData[c] = Float.parseFloat(tokens.get(i));
					}
					
					// Process frame data into SkeletalFrame
					for(int joint = 0; joint < numJoints; joint++) {
						int offset = jointDataOffset[joint];
						int flag = jointFlags[joint];
						
						// Retrieve base frame position and quaternion
						frame.getJointPosition(joint, vec1);
						frame.getJointQuaternion(joint, quat1);
						
						// Update base frame with modified frame data
						c = 0;
						if((flag & 1) != 0)
							vec1.x = frameData[offset + (c++)];
						if((flag & 2) != 0)
							vec1.y = frameData[offset + (c++)];
						if((flag & 4) != 0)
							vec1.z = frameData[offset + (c++)];
						if((flag & 8) != 0)
							quat1.x = frameData[offset + (c++)];
						if((flag & 16) != 0)
							quat1.y = frameData[offset + (c++)];
						if((flag & 32) != 0)
							quat1.z = frameData[offset + (c++)];
						
						// Set updated joint
						frame.setJoint(joint, vec1.x, vec1.y, vec1.z, quat1.x, quat1.y, quat1.z, true);
					}

					// Apply rotation
					frame.rotate(rotation, true);

					// Done loading frame, convert to to SkeletalAnim data
					frame.packDeltas(animData, baseFrame);
					animOffsets.add(animData.size);
					
					break;
				}
				
				// Tags and timing
				case tag:
					tagsList.add(tokens.get(1));
					break;
					
				case normaltransitiontime:
					tNormalTransitionTimes.put(tokens.get(1), Float.parseFloat(tokens.get(2)));
					break;

				case interruptedtransitiontime:
					tInterruptedTransitionTimes.put(tokens.get(1), Float.parseFloat(tokens.get(2)));
					break;

				case unknown:
				default:
					Sys.debug(TAG, "Unexpected operation: " + op);					
				}
			}


			// Finished reading md5anim, convert to SkeletalAnim
			float[] deltas = new float[animData.size];
			System.arraycopy(animData.items, 0, deltas, 0, animData.size);
			int[] frameOffsets = new int[animOffsets.size];
			System.arraycopy(animOffsets.items, 0, frameOffsets, 0, animOffsets.size);
			
			Sys.info(TAG, "Parsed " + (deltas.length * 4) + " bytes animation (" + (frame.data.length * 4 * numFrames) + " bytes raw) from " + filename);
			
			// Convert to array
			String[] tags = new String[tagsList.size()];
			tagsList.toArray(tags);
			
			anim = new SkeletalAnimation(
				1.0f / (float)frameRate, 
				baseFrame, 
				deltas, 
				frameOffsets,
				tags,
				tNormalTransitionTimes,
				tInterruptedTransitionTimes
			);
			
			File.saveHints(filename, anim);

			reader.close();

			return anim;
		} catch (Throwable e) {
			throw new RuntimeException("Failed to load animation: " + filename, e);
		}
	}
	
}

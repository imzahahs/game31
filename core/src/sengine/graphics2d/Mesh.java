package sengine.graphics2d;

import sengine.File;
import sengine.Streamable;
import sengine.Sys;
import sengine.GarbageCollector;
import sengine.mass.MassSerializable;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

public class Mesh extends MaterialInstance implements Animatable2D, Streamable, GarbageCollector.Collectible, MassSerializable {
	static final String TAG = "Mesh";

	public static abstract class AbstractMeshRenderInstruction<T extends Mesh> extends MaterialConfiguration {
		// Base
		public T source = null;
		public int offset = 0;
		public int count = 0;
	}
	
	public static class MeshRenderInstruction extends AbstractMeshRenderInstruction<Mesh> {
		// Cached instructions
		static Array<MeshRenderInstruction> cache = new Array<MeshRenderInstruction>(MeshRenderInstruction.class);

		@Override
		public void render(Shader shader) {
			apply(shader);

			source.mesh.render(shader.getProgram(), source.getPrimitiveType(), offset, count);
		}

		@Override
		public void bind(Shader shader) {
			// Make sure mesh is loaded
			source.tLastUsed = Sys.getTime();
			if(source.mesh == null) {
				// Create mesh
				source.mesh = new com.badlogic.gdx.graphics.Mesh(source.isStatic(), source.maxVertices, source.indices.length, source.getVertexAttributes());
				source.mesh.setAutoBind(false);
				// Require garbage cycle
				GarbageCollector.add(source);
				// Reupload
				source.upload();
			}
			// Update mesh if needed
			if(!source.meshUploaded) {
				source.mesh.setIndices(source.indices);
				source.mesh.setVertices(source.vertices);
				source.meshUploaded = true;
			}
			source.mesh.bind(shader.getProgram(), shader.getAttributeLocations(source.getVertexAttributes()));
		}

		@Override
		public void unbind(Shader shader) {
			source.mesh.unbind(shader.getProgram(), shader.getAttributeLocations(source.getVertexAttributes()));
		}

		@Override
		public void clear() {
			super.clear();
			// Return back to cache
			cache.add(this);
		}
	}
	
	public static float tDefaultGCTime = 15.0f;

	// Identity
	public final float[] vertices;
	public final short[] indices;
	public final int maxVertices;
	public final Mesh source;
	// Internal mesh data
	protected com.badlogic.gdx.graphics.Mesh mesh = null;
	protected boolean meshUploaded = false;
	// GC
	public float tLastUsed = -1;

	// Subclasses should override these
	public int indexX(int vertex) { return (vertex * 8) + 0; }
	public int indexY(int vertex) { return (vertex * 8) + 1; }
	public int indexZ(int vertex) { return (vertex * 8) + 2; }
	public int indexU(int vertex) { return (vertex * 8) + 3; }
	public int indexV(int vertex) { return (vertex * 8) + 4; }
	public int indexNX(int vertex) { return (vertex * 8) + 5; }
	public int indexNY(int vertex) { return (vertex * 8) + 6; }
	public int indexNZ(int vertex) { return (vertex * 8) + 7; }
	
	// Plain mesh can be contain 3d geometry, texture coordinates and normals for lighting
	public static final VertexAttributes vertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
		new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE)
	);

	public VertexAttributes getVertexAttributes() {
		return vertexAttributes;
	}
	
	public boolean isStatic() {
		return true;
	}
	
	public int getPrimitiveType() {
		return GL20.GL_TRIANGLES;
	}
	
	protected AbstractMeshRenderInstruction<?> getRenderInstruction() {
		MeshRenderInstruction r = MeshRenderInstruction.cache.size == 0 ? new MeshRenderInstruction() : MeshRenderInstruction.cache.pop();
		r.bindObject = source;
		return r;
	}
	
	public Mesh instantiate() {
		return new Mesh(this);
	}
	
	public Mesh(int maxVertices, int maxIndices) {
		this.maxVertices = maxVertices;
		this.vertices = new float[maxVertices * (getVertexAttributes().vertexSize / 4)];
		this.indices = new short[maxIndices];
		// Populate default indice values
		for(short c = 0; c < maxVertices && c < maxIndices; c++)
			indices[c] = c;
		this.source = this;
	}
	
	@MassConstructor
	public Mesh(Mesh copy) {
		this.maxVertices = copy.source.maxVertices;
		this.vertices = copy.source.vertices;
		this.indices = copy.source.indices;
		this.source = copy.source;
		// Copy material info
		setMaterial(copy.getMaterial());
		copyAttributes(copy);
	}
	
	@MassConstructor
	public Mesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		this.source = this;
		this.maxVertices = vertices.length / (getVertexAttributes().vertexSize / 4);
		this.vertices = vertices;
		this.indices = indices;
		setMaterial(material, attribs);
	}
	@Override
	public Object[] mass() {
		if(source != this)
			return new Object[] { source };
		else 
			return new Object[] { vertices, indices, material, attribs };
	}
	
	/**
	 * <p>Replaces existing vertex data with specifed vertex data.</p>
	 * <p>If vertex array size is not equal to existing vertex array size, vertices will not be copied</p>
	 * <p>{@link #upload()} will be automatically implied aupon successfull replacement.</p>
	 * @param vertices - float[], new vertex array
	 */
	public void replace(float vertices[]) {
		// Copy entire array
		System.arraycopy(vertices, 0, this.vertices, 0, this.vertices.length);
	}
	
	public void replaceIndices(short indices[]) {
		// Copy entire array
		System.arraycopy(indices, 0, this.indices, 0, this.indices.length);
	}
	
	/**
	 * <p>Uploads {@link #vertices} to libGDX internal vertex buffers. Should be called whenever mesh is modified.</p>
	 * <p>Is not dependent on OpenGL thread.</p>
	 */
	public void upload() {
		source.meshUploaded = false;
	}
	
	public void render() {
		render(0, indices.length);
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void render(int offset, int count) {
		// Get a render instruction
		AbstractMeshRenderInstruction r = getRenderInstruction(); 
		// Set parameters
		r.source = source;
		r.material = material;
		r.offset = offset;
		r.count = count;
		// Upload render config
		configure(r);
		// Add to renderer
		material.load();
		Renderer.renderer.addInstruction(r);
	}
	
	// Streamable
	@Override
	public void load() {
		material.load();
	}
	
	@Override
	public boolean isLoaded() {
		return material.isLoaded();
	}
	
	@Override
	public void ensureLoaded() {
		material.ensureLoaded();
	}
	
	// GarbageCollector.Collectible
	@Override
	public boolean performGC(boolean forced) {
		float elapsed = Sys.getTime() - tLastUsed;
		if(elapsed < Mesh.tDefaultGCTime && !forced)
			return false;		// no GC
		if(mesh == null)
			return true;		// already unloaded
		// Else unload now
		mesh.dispose();
		mesh = null;
		return true;
	}
	
	// Animatable2D
	@Override
	public void translate(float x, float y) {
		Matrices.model.translate(x, y, 0.0f);
	}
	
	@Override
	public void rotate(float rotate) {
		Matrices.model.rotate(0, 0, -1, rotate);
	}
	
	@Override
	public void scale(float x, float y) {
		Matrices.model.scale(x, y, 1.0f);
	}

	@Override
	public void shear(float sx, float sy) {
		Matrices.shear(sx, sy);
	}

	@Override
	public void scissor(float x, float y, float width, float height) {
		Matrices.scissor.set(x, y, width, height);
	}
	
	@Override
	public void applyGlobalMatrix() {
		// no special matrix to apply
	}
	
	@Override
	public float getLength() {
		return material.getLength();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + material;
	}
	
	
	// Tools
	public static void rotate(Mesh m, Quaternion rotation) {
		Vector3 vec = new Vector3();
		
		for(int c = 0; c < m.maxVertices; c++) {
			vec.set(m.vertices[m.indexX(c)], m.vertices[m.indexY(c)], m.vertices[m.indexZ(c)]);
			rotation.transform(vec);
			m.vertices[m.indexX(c)] = vec.x;
			m.vertices[m.indexY(c)] = vec.y;
			m.vertices[m.indexZ(c)] = vec.z;
		}
	}
	
	public static void accumulateBounds(Mesh m, BoundingBox bounds) { 
		for(int c = 0; c < m.maxVertices; c++)
			bounds.ext(m.vertices[m.indexX(c)], m.vertices[m.indexY(c)], m.vertices[m.indexZ(c)]);
	}
	
	public static void sortTrisDistance(Mesh m, Vector3 point) {
		Vector3 vec = new Vector3();
		
		int replacing = 0;
		int numTris = m.indices.length / 3;
		
		for(; replacing < numTris; replacing++) {
			
			int bestTri = 0;
			float bestDistance = Float.MAX_VALUE;
			
			for(int c = replacing; c < numTris; c++) {
				int i1 = m.indices[(c * 3) + 0];
				int i2 = m.indices[(c * 3) + 1];
				int i3 = m.indices[(c * 3) + 2];
				
				// Find mid point of triangle 
				vec.set(m.vertices[m.indexX(i1)], m.vertices[m.indexY(i1)], m.vertices[m.indexZ(i1)]);
				vec.add(m.vertices[m.indexX(i2)], m.vertices[m.indexY(i2)], m.vertices[m.indexZ(i2)]);
				vec.add(m.vertices[m.indexX(i3)], m.vertices[m.indexY(i3)], m.vertices[m.indexZ(i3)]);
				vec.scl(1.0f / 3.0f);
				float distance = vec.dst2(point);
				
				// Found a closer triangle than any previously found, remember it
				if(distance < bestDistance) {
					bestTri = c;
					bestDistance = distance;
				}
			}
			
			// Replace next triangle with closer triangle
			short ri1 = m.indices[(replacing * 3) + 0];
			short ri2 = m.indices[(replacing * 3) + 1];
			short ri3 = m.indices[(replacing * 3) + 2];
			m.indices[(replacing * 3) + 0] = m.indices[(bestTri * 3) + 0];
			m.indices[(replacing * 3) + 1] = m.indices[(bestTri * 3) + 1];
			m.indices[(replacing * 3) + 2] = m.indices[(bestTri * 3) + 2];
			m.indices[(bestTri * 3) + 0] = ri1;
			m.indices[(bestTri * 3) + 1] = ri2;
			m.indices[(bestTri * 3) + 2] = ri3;
		}
	}
	
	public static void accumulateNormals(Mesh m, boolean invert) {
		Vector3 vec1 = new Vector3();
		Vector3 vec2 = new Vector3();
		Vector3 vec3 = new Vector3();
		Vector3 vecA = new Vector3();
		Vector3 vecB = new Vector3();

		for(int c = 0; c < m.indices.length; c += 3) {
			int i1 = m.indices[c];
			int i2 = m.indices[c + 1];
			int i3 = m.indices[c + 2];
			
			// Get triangle positions
			vec1.set(m.vertices[m.indexX(i1)], m.vertices[m.indexY(i1)], m.vertices[m.indexZ(i1)]);
			vec2.set(m.vertices[m.indexX(i2)], m.vertices[m.indexY(i2)], m.vertices[m.indexZ(i2)]);
			vec3.set(m.vertices[m.indexX(i3)], m.vertices[m.indexY(i3)], m.vertices[m.indexZ(i3)]);
			
			// Calculate normal
			vecA.set(vec2);
			vecA.sub(vec1);
			vecB.set(vec3);
			vecB.sub(vec2);
			vecB.crs(vecA);
			// Calculate triangle area, to average normals according to their surface size
			float size = vecB.len() / 2.0f;
			vecB.nor();
			vecB.scl(invert ? -size : +size);
			
			// Add normal
			m.vertices[m.indexNX(i1)] = m.vertices[m.indexNX(i2)] = m.vertices[m.indexNX(i3)] += vecB.x;
			m.vertices[m.indexNY(i1)] = m.vertices[m.indexNY(i2)] = m.vertices[m.indexNY(i3)] += vecB.y;
			m.vertices[m.indexNZ(i1)] = m.vertices[m.indexNZ(i2)] = m.vertices[m.indexNZ(i3)] += vecB.z;
		}
	}
	
	public static void normalizeNormals(Mesh m) {
		Vector3 vecN = new Vector3();
		
		// Normalize all normals
		for(int c = 0; c < m.maxVertices; c++) {
			vecN.set(m.vertices[m.indexNX(c)], m.vertices[m.indexNY(c)], m.vertices[m.indexNZ(c)]);
			vecN.nor();
			m.vertices[m.indexNX(c)] = vecN.x;
			m.vertices[m.indexNY(c)] = vecN.y;
			m.vertices[m.indexNZ(c)] = vecN.z;
		}
	}
	
	public static void clearNormals(Mesh m) {
		for(int c = 0; c < m.maxVertices; c++) {
			m.vertices[m.indexNX(c)] = 0.0f;
			m.vertices[m.indexNY(c)] = 0.0f;
			m.vertices[m.indexNZ(c)] = 0.0f;
		}
	}
	
	public static float spreadNormalsPositionTolerance = 0.01f;
	
	public static void spreadNormals(Mesh... meshes) {
		Vector3 vec1 = new Vector3();
		Vector3 vec2 = new Vector3();
		Vector3 vecN = new Vector3();

		// Accumulate duplicate vertex normals
		for(int mi = 0; mi < meshes.length; mi++) {
			Mesh m = meshes[mi];
			
			for(int c = 0; c < m.indices.length; c++) {
				int i1 = m.indices[c];
				
				vec1.set(m.vertices[m.indexX(i1)], m.vertices[m.indexY(i1)], m.vertices[m.indexZ(i1)]);
				vecN.set(m.vertices[m.indexNX(i1)], m.vertices[m.indexNY(i1)], m.vertices[m.indexNZ(i1)]);
				boolean duplicatesFound = false;

				// Check with the rest of the vertices
				for(int mi2 = mi; mi2 < meshes.length; mi2++) {
					Mesh m2 = meshes[mi2];
					
					for(int c2 = mi2 == mi ? (c + 1) : 0; c2 < m2.indices.length; c2++) {
						int i2 = m2.indices[c2];
						
						vec2.set(m2.vertices[m2.indexX(i2)], m2.vertices[m2.indexY(i2)], m2.vertices[m2.indexZ(i2)]);
						
						// Check if same position
						if(!vec1.epsilonEquals(vec2, spreadNormalsPositionTolerance))
							continue;
						
						// Same position, accumulate normal
						vecN.add(m2.vertices[m2.indexNX(i2)], m2.vertices[m2.indexNY(i2)], m2.vertices[m2.indexNZ(i2)]);
						duplicatesFound = true;
					}
				}

				// If no duplicates, no need adjustment
				if(!duplicatesFound)
					continue;

				// Update origin vertex
				m.vertices[m.indexNX(i1)] = vecN.x;
				m.vertices[m.indexNY(i1)] = vecN.y;
				m.vertices[m.indexNZ(i1)] = vecN.z;
				
				
				// Else set the same for the rest of the duplicate vertices
				for(int mi2 = mi; mi2 < meshes.length; mi2++) {
					Mesh m2 = meshes[mi2];
					
					for(int c2 = mi2 == mi ? (c + 1) : 0; c2 < m2.indices.length; c2++) {
						int i2 = m2.indices[c2];
						
						vec2.set(m2.vertices[m2.indexX(i2)], m2.vertices[m2.indexY(i2)], m2.vertices[m2.indexZ(i2)]);
						
						// Check if same position
						if(!vec1.epsilonEquals(vec2, spreadNormalsPositionTolerance))
							continue;
						
						// Save new normal
						m2.vertices[m2.indexNX(i2)] = vecN.x; 
						m2.vertices[m2.indexNY(i2)] = vecN.y;
						m2.vertices[m2.indexNZ(i2)] = vecN.z;
					}
				}
			}
		}
	}
	
	
//	static float[] outVerts = new float[8];
	//static short[] outIndices = new short[6];
//	static int outVertsIndex = 0;
//	static int outIndicesIndex = 0;
	
	public static float sameNormalVariance = 0.000001f;
	public static float samePositionVariance = 0.01f;
	

	static short getVertIndex(FloatArray outVertices, Vector3 position, Vector3 normal) {
		for(int i = 0; i < outVertices.size; i += 8) {
			// Find distance
			if(position.epsilonEquals(outVertices.items[i + 0], outVertices.items[i + 1], outVertices.items[i + 2], samePositionVariance) &&
				normal.epsilonEquals(outVertices.items[i + 5], outVertices.items[i + 6], outVertices.items[i + 7], sameNormalVariance)) 
			{
				// Found matching vertex
				return (short)(i / 8);
			}
		}
		// Have to add new index
		// Position
		outVertices.add(position.x);
		outVertices.add(position.y);
		outVertices.add(position.z);
		// UV
		outVertices.add(0.0f);
		outVertices.add(0.0f);
		// Normal
		outVertices.add(normal.x);
		outVertices.add(normal.y);
		outVertices.add(normal.z);
		return (short)((outVertices.size / 8) - 1);
	}
	
	static void addDegenerateEdge(FloatArray outVertices, ShortArray outIndices, Vector3 v1, Vector3 v2, Vector3 n1, Vector3 n2) {
		short tl = getVertIndex(outVertices, v1, n2);
		short tr = getVertIndex(outVertices, v2, n2);
		short bl = getVertIndex(outVertices, v1, n1);
		short br = getVertIndex(outVertices, v2, n1);
		
		// Make sure to have enough space
		outIndices.add(tl);
		outIndices.add(br);
		outIndices.add(bl);
		
		outIndices.add(tl);
		outIndices.add(tr);
		outIndices.add(br);
	}
	
	public static Mesh createDegenerateEdgesMesh(Mesh ... meshes) {
		// Reset
		FloatArray outVertices = new FloatArray();
		ShortArray outIndices = new ShortArray();
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		Vector3 v3 = new Vector3();
		Vector3 vn = new Vector3();
		Vector3 w1 = new Vector3();
		Vector3 w2 = new Vector3();
		Vector3 w3 = new Vector3();
		Vector3 wn = new Vector3();
		Vector3 vec = new Vector3();	// temp vector
		
		for(int mi = 0; mi < meshes.length; mi++) {
			Mesh m = meshes[mi];
			int numTris = m.indices.length / 3;
			for(int c = 0; c < numTris; c++) {
				for(int t = 0; t < 3; t++) {
					int i1 = m.indices[(c * 3) + (t % 3)];
					int i2 = m.indices[(c * 3) + ((t + 1) % 3)];
					// Load vertex positions
					v1.set(m.vertices[m.indexX(i1)], m.vertices[m.indexY(i1)], m.vertices[m.indexZ(i1)]);
					v2.set(m.vertices[m.indexX(i2)], m.vertices[m.indexY(i2)], m.vertices[m.indexZ(i2)]);

					// Find adjacent edge
					for(int mi2 = mi; mi2 < meshes.length; mi2++) {
						Mesh m2 = meshes[mi2];
						int numTris2 = m2.indices.length / 3;
						
						for(int c2 = mi2 == mi ? (c + 1) : 0; c2 < numTris2; c2++) {
							for(int t2 = 0; t2 < 3; t2++) {
								int j1 = m2.indices[(c2 * 3) + (t2 % 3)];
								int j2 = m2.indices[(c2 * 3) + ((t2 + 1) % 3)];
								
								// Load position
								w1.set(m2.vertices[m2.indexX(j1)], m2.vertices[m2.indexY(j1)], m2.vertices[m2.indexZ(j1)]);
								w2.set(m2.vertices[m2.indexX(j2)], m2.vertices[m2.indexY(j2)], m2.vertices[m2.indexZ(j2)]);
								
								// Check if adjacent edge
								if(!w1.epsilonEquals(v2, samePositionVariance) || !w2.epsilonEquals(v1, samePositionVariance))
									continue;
								
								// Adjacent triangle, load complete triangles to calculate both triangle normals
								// Origin triangle
								int i3 = m.indices[(c * 3) + ((t + 2) % 3)];
								v3.set(m.vertices[m.indexX(i3)], m.vertices[m.indexY(i3)], m.vertices[m.indexZ(i3)]);
								vn.set(v3);
								vn.sub(v1);
								vec.set(v2);
								vec.sub(v3);
								vn.crs(vec);
								vn.nor();
								// Adjacent triangle
								int j3 = m2.indices[(c2 * 3) + ((t2 + 2) % 3)];
								w3.set(m2.vertices[m2.indexX(j3)], m2.vertices[m2.indexY(j3)], m2.vertices[m2.indexZ(j3)]);
								wn.set(w3);
								wn.sub(w1);
								vec.set(w2);
								vec.sub(w3);
								wn.crs(vec);
								wn.nor();
								
								// Continue only if both normals differ (to ignore triangles of the same face)
								if(wn.epsilonEquals(vn, sameNormalVariance))
									continue;		// same face
								
								// Else is an edge, so create degenerate face
								addDegenerateEdge(outVertices, outIndices, v1, v2, vn, wn);
							}
						}
					}
				}
			}
		}
		
		// Create new shadow volume mesh
		int svVertices = outVertices.size / 8;
		Mesh shadowMesh = new Mesh(svVertices, outIndices.size);
		Sys.info(TAG, "Created shadow volume with " + svVertices + " vertices and " + outIndices.size + " indices");
		shadowMesh.replace(outVertices.items);
		shadowMesh.replaceIndices(outIndices.items);
		shadowMesh.setMaterial((Material)File.getHints("ShadowVolumeMaterial"));
		return shadowMesh;
	}
}

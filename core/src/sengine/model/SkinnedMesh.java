package sengine.model;

import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.Mesh;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class SkinnedMesh extends Mesh {
	public static final String a_jointIndex = "a_jointIndex";
	public static final String a_jointWeight = "a_jointWeight";
	public static final String u_jointVectors = "u_jointVectors";
	
	public float[] movementVectors = null;
	
	// Subclasses should override these
	@Override
	public int indexX(int vertex) { return (vertex * 16) + 0; }
	@Override
	public int indexY(int vertex) { return (vertex * 16) + 1; }
	@Override
	public int indexZ(int vertex) { return (vertex * 16) + 2; }
	@Override
	public int indexU(int vertex) { return (vertex * 16) + 3; }
	@Override
	public int indexV(int vertex) { return (vertex * 16) + 4; }
	@Override
	public int indexNX(int vertex) { return (vertex * 16) + 5; }
	@Override
	public int indexNY(int vertex) { return (vertex * 16) + 6; }
	@Override
	public int indexNZ(int vertex) { return (vertex * 16) + 7; }
	
	public int indexBI(int vertex) { return (vertex * 16) + 8; }
	public int indexBW(int vertex) { return (vertex * 16) + 12; }

	public static final VertexAttributes vertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
		new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
		new VertexAttribute(512, 4, a_jointIndex),
		new VertexAttribute(1024, 4, a_jointWeight)
	);

	@Override
	public VertexAttributes getVertexAttributes() {
		return vertexAttributes;
	}
	
	@Override
	public SkinnedMesh instantiate() {
		return new SkinnedMesh(this);
	}
	
	public SkinnedMesh(int maxVertices, int maxIndices) {
		super(maxVertices, maxIndices);
	}

	@MassConstructor
	public SkinnedMesh(SkinnedMesh source) {
		super(source);
	}
	
	@MassConstructor
	public SkinnedMesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}
	
	@Override
	protected void configure(MaterialConfiguration config) {
		super.configure(config);
		
		// Upload skeletal data
		config.setVector4Array(u_jointVectors, movementVectors);
	}
	
	public static void resampleSkin(SkinnedMesh mesh, int maxWeights) {
		if(maxWeights > 4)
			throw new IllegalArgumentException("Max weights cannot be more than 4, provided: " + maxWeights);
		
		float[] weights = new float[4];
		float[] indices = new float[4];
		
		for(int c = 0; c < mesh.maxVertices; c++) {
			// Clear
			weights[0] = weights[1] = weights[2] = weights[3] = 0;
			indices[0] = indices[1] = indices[2] = indices[3] = 0;
			float totalWeight = 0.0f;
			
			// Sort according to most weight first
			for(int w = 0; w < maxWeights; w++) {
				// Find the highest weight
				float bestWeight = -Float.MAX_VALUE;
				int used = 0;
				for(int ow = 0; ow < 4; ow++) {
					float weight = mesh.vertices[mesh.indexBW(c) + ow];
					if(weight > bestWeight) {
						bestWeight = weight;
						used = ow;
					}
				}
				// Now save identified weight and clear
				weights[w] = bestWeight;
				totalWeight += bestWeight;
				indices[w] = mesh.vertices[mesh.indexBI(c) + used];
				mesh.vertices[mesh.indexBW(c) + used] = 0;
				mesh.vertices[mesh.indexBI(c) + used] = 0;
			}
			
			// Normalize weights
			if(totalWeight > 0.0f) {
				for(int w = 0; w < maxWeights; w++)
					weights[w] /= totalWeight;
			}
			
			// Store new weights
			for(int w = 0; w < 4; w++) {
				mesh.vertices[mesh.indexBW(c) + w] = weights[w];
				mesh.vertices[mesh.indexBI(c) + w] = indices[w];
			}
		}
	}
	
	public static SkinnedMesh skinMesh(Mesh mesh, SkinnedMesh ... skins) {
		SkinnedMesh m = new SkinnedMesh(mesh.maxVertices, mesh.indices.length);
		
		Vector3 vec1 = new Vector3();
		Vector3 vec2 = new Vector3();
		
		for(int c = 0; c < mesh.maxVertices; c++) {
			// Load position
			vec1.set(mesh.vertices[mesh.indexX(c)], mesh.vertices[mesh.indexY(c)], mesh.vertices[mesh.indexZ(c)]);
			
			// Find the closest vertex
			SkinnedMesh closestMesh = null;
			int closestVert = 0;
			float closestDistance = Float.MAX_VALUE;
			
			for(int mi = 0; mi < skins.length; mi++) {
				SkinnedMesh s = skins[mi];
				for(int c2 = 0; c2 < s.maxVertices; c2++) {
					// Load skin position
					vec2.set(s.vertices[s.indexX(c2)], s.vertices[s.indexY(c2)], s.vertices[s.indexZ(c2)]);
					// Find distance
					float distance = vec1.dst2(vec2);
					if(distance < closestDistance) {
						// Found a better match
						closestMesh = s;
						closestVert = c2;
						closestDistance = distance;
					}
				}
			}
			
			// Copy closest vert's joint info
			m.vertices[m.indexX(c)] = vec1.x;
			m.vertices[m.indexY(c)] = vec1.y;
			m.vertices[m.indexZ(c)] = vec1.z;
			m.vertices[m.indexU(c)] = mesh.vertices[mesh.indexU(c)];
			m.vertices[m.indexV(c)] = mesh.vertices[mesh.indexV(c)];
			m.vertices[m.indexNX(c)] = mesh.vertices[mesh.indexNX(c)];
			m.vertices[m.indexNY(c)] = mesh.vertices[mesh.indexNY(c)];
			m.vertices[m.indexNZ(c)] = mesh.vertices[mesh.indexNZ(c)];
			for(int w = 0; w < 4; w++) {
				m.vertices[m.indexBW(c) + w] = closestMesh.vertices[closestMesh.indexBW(closestVert) + w];
				m.vertices[m.indexBI(c) + w] = closestMesh.vertices[closestMesh.indexBI(closestVert) + w];
			}
		}
		
		m.replaceIndices(mesh.indices);
		
		return m;
	}
}

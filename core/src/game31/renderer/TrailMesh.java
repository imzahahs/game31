package game31.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Mesh;

public class TrailMesh extends Mesh {

	// Mesh properties
	@Override
	public int indexX(int vertex) { return (vertex * 6) + 0; }
	@Override
	public int indexY(int vertex) { return (vertex * 6) + 1; }
	@Override
	public int indexZ(int vertex) { return (vertex * 6) + 2; }
	@Override
	public int indexU(int vertex) { return (vertex * 6) + 3; }
	@Override
	public int indexV(int vertex) { return (vertex * 6) + 4; }
	@Override
	public int indexNX(int vertex) { return -1; }		// n/a
	@Override
	public int indexNY(int vertex) { return -1; }		// n/a
	@Override
	public int indexNZ(int vertex) { return -1; }		// n/a

	public int indexColor(int vertex) { return (vertex * 6) + 5; }

	// Sprites only need position and texture coordinates
	public static final VertexAttributes vertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
		new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
	);

	@Override
	public VertexAttributes getVertexAttributes() {
		return vertexAttributes;
	}

	@Override
	public int getPrimitiveType() {
		return GL20.GL_TRIANGLES;
	}

	@Override
	public TrailMesh instantiate() {
		return new TrailMesh(this);
	}

	public TrailMesh(int maxVertices, int maxIndices, Material m) {
		super(maxVertices, maxIndices);

//        // Reset color to white
//		float colorPacked = Color.toFloatBits(255, 255, 255, 255);
//
//		replace(new float[] {
//				-0.5f, -length / 2.0f, 0.0f, 0.0f, 1.0f, colorPacked,		// TODO: replace with 4 vertices and 6 indices
//				+0.5f, -length / 2.0f, 0.0f, 1.0f, 1.0f, colorPacked,
//				-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f, colorPacked,
//				-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f, colorPacked,
//				+0.5f, -length / 2.0f, 0.0f, 1.0f, 1.0f, colorPacked,
//				+0.5f, +length / 2.0f, 0.0f, 1.0f, 0.0f, colorPacked,
//		});

		// Set material
		setMaterial(m);
	}

	@MassConstructor
	public TrailMesh(TrailMesh copy) {
		super(copy);
	}

	@MassConstructor
	public TrailMesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}
	
	@Override
	public Object[] mass() {
		if(source != this)
			return new Object[] { source };
		else 
			return new Object[] { vertices, indices, material, attribs };
	}

	@Override
	public float getLength() {
		return 1f;      // not really supported
	}
}
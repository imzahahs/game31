package sengine.graphics2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class ColoredMesh extends Mesh {

	// Mesh properties
	@Override
	public int indexX(int vertex) { return (vertex * 9) + 0; }
	@Override
	public int indexY(int vertex) { return (vertex * 9) + 1; }
	@Override
	public int indexZ(int vertex) { return (vertex * 9) + 2; }
	@Override
	public int indexU(int vertex) { return (vertex * 9) + 3; }
	@Override
	public int indexV(int vertex) { return (vertex * 9) + 4; }
	@Override
	public int indexNX(int vertex) { return (vertex * 9) + 5; }
	@Override
	public int indexNY(int vertex) { return (vertex * 9) + 6; }
	@Override
	public int indexNZ(int vertex)	{ return (vertex * 9) + 7; }
	public int indexColor(int vertex) { return (vertex * 9) + 8; }
	
	public static final VertexAttributes vertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
		new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE)
	);

	@Override
	public VertexAttributes getVertexAttributes() {
		return vertexAttributes;
	}

	@Override	
	public ColoredMesh instantiate() {
		return new ColoredMesh(this);
	}

	public ColoredMesh(int maxVertices, int maxIndices) {
		super(maxVertices, maxIndices);
		
		// Reset color to white
		float colorPacked = Color.toFloatBits(255, 255, 255, 255);
		for(int c = 0; c < maxVertices; c++)
			vertices[indexColor(c)] = colorPacked;
	}

	@MassConstructor
	public ColoredMesh(ColoredMesh copy) {
		super(copy);
	}

	@MassConstructor
	public ColoredMesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}
}

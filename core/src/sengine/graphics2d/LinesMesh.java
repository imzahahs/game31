package sengine.graphics2d;

import com.badlogic.gdx.graphics.GL20;

public class LinesMesh extends Mesh {
	
	// TODO: add support for glLinesWidth

	@Override	
	public int getPrimitiveType() {
		return GL20.GL_LINES;
	}

	public LinesMesh(int maxVertices, int maxIndices) {
		super(maxVertices, maxIndices);
		
	}
	
	@MassConstructor
	public LinesMesh(LinesMesh copy) {
		super(copy);
	}

	@MassConstructor
	public LinesMesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}
	
	

}

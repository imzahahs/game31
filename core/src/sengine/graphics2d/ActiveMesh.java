package sengine.graphics2d;

import com.badlogic.gdx.graphics.GL20;

public class ActiveMesh extends Mesh {
	
	@Override	
	public int getPrimitiveType() {
		return GL20.GL_TRIANGLES;
	}

	@Override	
	public boolean isStatic() {
		return false;
	}

	@Override	
	public ActiveMesh instantiate() {
		// ActiveMesh was intended to be modified, so should create a copy for instantiation purposes
		ActiveMesh instance = new ActiveMesh(maxVertices, indices.length);
		// Copy mesh data
		instance.replace(vertices);
		instance.replaceIndices(indices);
		// Copy material properties
		instance.setMaterial(getMaterial());
		instance.copyAttributes(this);
		return instance;
	}

	public ActiveMesh(int maxVertices, int maxIndices) {
		super(maxVertices, maxIndices);
	}

	@MassConstructor
	public ActiveMesh(ActiveMesh copy) {
		super(copy);
	}
	
	@MassConstructor
	public ActiveMesh(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}
}
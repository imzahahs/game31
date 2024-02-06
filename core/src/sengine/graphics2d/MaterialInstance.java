package sengine.graphics2d;

public class MaterialInstance implements Animatable {
	
	public static <T extends MaterialAttribute> T createAttribute(Class<T> attribType) {
		try {
			return attribType.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Failed to create attribute: " + attribType +  ": " + e, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to create attribute: " + attribType +  ": " + e, e);
		}
	}

	protected MaterialAttribute[] attribs = null;
	protected Material material = null;
	
	public void setMaterial(Material mat) {
		setMaterial(mat,  null);
	}
	public void setMaterial(Material mat, MaterialAttribute[] attribs) {
		// Clear material attributes
		this.attribs = attribs;
		// Set material
		this.material = mat;
		// Initialize mat
		if(mat == null)
			return;
		mat.initialize(this);
	}
	
	public void clearAttributes() {
		// Clear material attributes
		attribs = null;
	}
	
	public void copyAttributes(MaterialInstance from) {
		// Copy from
		if(from.attribs == null) {
			attribs = null;
			return;
		}
		for(int c = 0; c < from.attribs.length; c++) {
			MaterialAttribute attrib = getAttribute(from.attribs[c].getClass(), 0);
			attrib.copy(from.attribs[c]);
		}
	}
	
	public <T extends Material> T getMaterial() {
		return (T) material;
	}
	
	protected void configure(MaterialConfiguration config) {
		if(attribs != null) {
			for(int c = 0; c < attribs.length; c++)
				attribs[c].configure(config);
		}
		// Set position matrix
		config.modelMatrix.set(Matrices.model);
		config.camera = Matrices.camera;
		// Set scissor
		config.scissor.set(Matrices.scissor);
		// Target
		config.target = Matrices.target;
	}
	
	@Override
	public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
		if(attribs == null) {
			attribs = new MaterialAttribute[1];
			attribs[0] = createAttribute(attribType);
			return (T)attribs[0];
		}
		// Else search existing attribute
		for(int c = 0; c < attribs.length; c++) {
			if(attribs[c].getClass() == attribType)
				return (T)attribs[c];
		}
		// Else no attribute found, extend array to include new attribute
		// Copy existing attributes
		MaterialAttribute[] newAttribs = new MaterialAttribute[attribs.length + 1];
		for(int c = 0; c < attribs.length; c++)
			newAttribs[c] = attribs[c];
		// Now add new one
		MaterialAttribute attrib = createAttribute(attribType);
		newAttribs[attribs.length] = attrib;
		attribs = newAttribs;
		return (T)attrib;
	}

	public MaterialAttribute[] getAttributes() {
		return attribs;
	}
}
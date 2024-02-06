package sengine.materials;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Renderer;
import sengine.graphics2d.Shader;
import sengine.mass.MassSerializable;

public class ColoredMaterial extends Material implements MassSerializable {
	public static final String ID = "ColoredMaterial";
	
	public static class Type extends Material.Type {
		public Type() {
			super(ID);
		}

		@Override
		protected Material create(String name) {
			// No direct scripted hints found, need to infer
			return new ColoredMaterial();
		}
	}
	
	public static final String DEFAULT_SHADER = "shaders/ColoredMaterial.glsl";

	public final Shader shader;
	
	public ColoredMaterial() {
		this(
			GL20.GL_ALWAYS,
			false,
			GL20.GL_NEVER,
			GL20.GL_SRC_ALPHA, 
			GL20.GL_ONE_MINUS_SRC_ALPHA,
			Shader.load(DEFAULT_SHADER) 
		);
	}

	@MassConstructor
	public ColoredMaterial(int depthFunc, boolean depthMask, int faceCullingMode, int srcBlendFunc, int destBlendFunc, Shader shader) {
		super(depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc);
		this.shader = shader;
	}
	@Override
	public Object[] mass() {
		return new Object[] { depthFunc, depthMask, faceCullingMode, srcBlendFunc, destBlendFunc, shader };
	}


	public ColoredMaterial(int srcBlendFunc, int destBlendFunc) {
		this(
			GL20.GL_ALWAYS,
			false,
			GL20.GL_NEVER,
			srcBlendFunc, 
			destBlendFunc,
			Shader.load(DEFAULT_SHADER)
		);
	}



	@Override
	public void load() {
		// nothing
	}

	@Override
	public boolean isLoaded() {
		return true;	// always
	}

	@Override
	public void ensureLoaded() {
		// nothing
	}

	@Override
	public void initialize(MaterialInstance m) {
		// Ensure attributes
		m.getAttribute(ColorAttribute.class, 0);
	}

	@Override
	public Shader bind() {
		// Just a standard shader
		shader.bind();

		return shader;
	}
	
	@Override
	public void unbind() {
		// nothing
	}

	@Override
	public float getLength() {
		return 1.0f;		// No inferred length
	}
}

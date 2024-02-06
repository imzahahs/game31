package sengine.materials;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialInstance;
import sengine.graphics2d.Renderer;
import sengine.graphics2d.Shader;
import sengine.mass.MassSerializable;

public class SpriteBatchMaterial extends Material implements MassSerializable {
	public static final String ID = "SpriteBatchMaterial";
	
	public static class Type extends Material.Type {
		public Type() {
			super(ID);
		}

		@Override
		protected Material create(String name) {
			// No direct scripted hints found, need to infer
			return new SpriteBatchMaterial();
		}
	}
	
	public static final String DEFAULT_SHADER = "shaders/SpriteBatchMaterial.glsl";
	public static final String DEFAULT_NAME = "default.SpriteBatchMaterial";

	public final Shader shader;
	
	public SpriteBatchMaterial() {
		this(
			GL20.GL_ALWAYS,
			false,
			GL20.GL_SRC_ALPHA, 
			GL20.GL_ONE_MINUS_SRC_ALPHA,
			Shader.load(DEFAULT_SHADER) 
		);
	}
	
	public SpriteBatchMaterial(Shader shader) {
		this(
			GL20.GL_ALWAYS,
			false,
			GL20.GL_SRC_ALPHA, 
			GL20.GL_ONE_MINUS_SRC_ALPHA,
			shader
		);
	}

	@MassConstructor
	public SpriteBatchMaterial(int depthFunc, boolean depthMask, int srcBlendFunc, int destBlendFunc, Shader shader) {
		super(depthFunc, depthMask, GL20.GL_NEVER, srcBlendFunc, destBlendFunc);		// Sprites never use face culling
		
		this.shader = shader;
	}
	@Override
	public Object[] mass() {
		return new Object[] { depthFunc, depthMask, srcBlendFunc, destBlendFunc, shader };
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
		// Configure SpriteBatch
		Sys.sb.enableBlending();
		Sys.sb.setBlendFunction(-1, -1);
		ShaderProgram program = shader.bind();
		Sys.sb.setShader(program);
		Sys.sb.begin();
		Gdx.gl.glDepthMask(depthMask);
		return shader;
	}

	@Override
	public void unbind() {
		Sys.sb.end();
		Shader.lastShader = null;
		Gdx.gl.glDepthMask(depthMask);
		Gdx.gl.glEnable(GL20.GL_BLEND);
	}

	@Override
	public float getLength() {
		return 1.0f;		// not really supported
	}
}

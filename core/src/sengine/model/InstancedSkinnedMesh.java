package sengine.model;

import sengine.GarbageCollector;
import sengine.Sys;
import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.MaterialConfiguration;
import sengine.graphics2d.Shader;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class InstancedSkinnedMesh extends SkinnedMesh {
	
	public static final String a_instance = "a_instance";
	public static final String u_instanceModelVectors = "u_instanceModelVectors";

	protected static class InstancedMeshRenderInstruction extends AbstractMeshRenderInstruction<InstancedSkinnedMesh> {
		// Cached instructions
		static Array<InstancedMeshRenderInstruction> cache = new Array<InstancedMeshRenderInstruction>(InstancedMeshRenderInstruction.class);
		
		InstancedSkinnedMesh instance = null;
		
		public void flush(ShaderProgram program) {
			int location = program.fetchUniformLocation(u_instanceModelVectors, false);
			if(location == -1)
				throw new RuntimeException("Material " + instance.getMaterial() + " does not support uniform " + u_instanceModelVectors);
			// Upload instance matrices
			program.setUniform4fv(location, instance.instancedVectors, 0, instance.instances * 12);
			// Render
			source.mesh.render(program, source.getPrimitiveType(), 0, instance.instances * source.indices.length);
			instance.instances = 0;
		}

		@Override
		public void render(Shader shader) {
			// Copy model matrix
			MaterialConfiguration.setTransformVectors(instance.instancedVectors, instance.instances, modelMatrix);
			// Increase batch 
			instance.instances++;
			// First instruction's uniforms are preserved for the rest of the batch
			if(instance.instances == 1)
				apply(shader);
			else if(instance.instances >= source.maxInstances)
				flush(shader.getProgram());			// Batch is full, flush
		}

		@Override
		public void bind(Shader shader) {
			// Make sure mesh is loaded
			source.tLastUsed = Sys.getTime();
			if(source.mesh == null) {
				// Create mesh
				source.mesh = new com.badlogic.gdx.graphics.Mesh(source.isStatic(), source.maxVertices * source.maxInstances, source.indices.length * source.maxInstances, source.getInstancedVertexAttributes());
				source.mesh.setAutoBind(false);
				// Require garbage cycle
				GarbageCollector.add(source);
				// Reupload
				source.upload();
			}
			// Update mesh if needed
			if(!source.meshUploaded) {
				// Duplicate vertices and indices
				int vertexSize = source.getVertexAttributes().vertexSize / 4;
				int instancedVertexSize = vertexSize + 1;
				
				for(int i = 0; i < source.maxInstances; i++) {
					int vo = i * source.maxVertices;
					for(int v = 0; v < source.maxVertices; v++) {
						for(int e = 0; e < vertexSize; e++)
							source.instancedVertices[((vo + v) * instancedVertexSize) + e] = source.vertices[(v * vertexSize) + e];
						source.instancedVertices[((vo + v) * instancedVertexSize) + vertexSize] = i;
					}
					int io = i * source.indices.length;
					for(int c = 0; c < source.indices.length; c++)
						source.instancedIndices[io + c] = (short)(source.indices[c] + vo);
				}

				// Upload to VBO
				source.mesh.setIndices(source.instancedIndices);
				source.mesh.setVertices(source.instancedVertices);
				source.meshUploaded = true;
			}
			source.mesh.bind(shader.getProgram(), shader.getAttributeLocations(source.getVertexAttributes()));
		}

		@Override
		public void unbind(Shader shader) {
			ShaderProgram program = shader.getProgram();
			if(instance.instances != 0)
				flush(program);
			source.mesh.unbind(program, shader.getAttributeLocations(source.getVertexAttributes()));
		}

		@Override
		public void clear() {
			super.clear();
			// Return back to cache
			cache.add(this);
		}
	}
	
	protected AbstractMeshRenderInstruction<?> getRenderInstruction() {
		InstancedMeshRenderInstruction r = InstancedMeshRenderInstruction.cache.size == 0 ? new InstancedMeshRenderInstruction() : InstancedMeshRenderInstruction.cache.pop();
		r.instance = this;
		r.bindObject = this;
		return r;
	}
	
	// Subclasses should override these
	public static final VertexAttributes instancedVertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"),
		new VertexAttribute(VertexAttributes.Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
		// Joint data
		new VertexAttribute(512, 4, a_jointIndex),
		new VertexAttribute(1024, 4, a_jointWeight),
		// Instance
		new VertexAttribute(2048, 1, a_instance)
	);

	public VertexAttributes getInstancedVertexAttributes() {
		return instancedVertexAttributes;
	}
	
	@Override
	public InstancedSkinnedMesh instantiate() {
		return new InstancedSkinnedMesh(this);
	}
	
	// Instanced data
	public final int maxInstances;
	final float[] instancedVertices;
	final short[] instancedIndices;
	final float[] instancedVectors;
	
	// Current
	int instances = 0;
	
	public InstancedSkinnedMesh(int maxVertices, int maxIndices, int maxInstances) {
		super(maxVertices, maxIndices);
		
		this.maxInstances = maxInstances;
		this.instancedVertices = new float[(maxVertices * ((getVertexAttributes().vertexSize / 4) + 1)) * maxInstances];
		this.instancedIndices = new short[indices.length * maxInstances];
		this.instancedVectors = new float[12 * maxInstances];
	}
	
	@MassConstructor
	public InstancedSkinnedMesh(InstancedSkinnedMesh source) {
		super(source);
		
		this.maxInstances = source.maxInstances;
		this.instancedVertices = source.instancedVertices;		// not used
		this.instancedIndices = source.instancedIndices;			// not used
		
		// Copy still keeps a separate matrix array
		this.instancedVectors = new float[12 * maxInstances];
	}

	@MassConstructor
	public InstancedSkinnedMesh(int maxInstances, float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
		
		this.maxInstances = maxInstances;
		this.instancedVertices = new float[(maxVertices * ((getVertexAttributes().vertexSize / 4) + 1)) * maxInstances];
		this.instancedIndices = new short[indices.length * maxInstances];
		this.instancedVectors = new float[12 * maxInstances];
	}
	
	@Override
	public Object[] mass() {
		if(source != this)
			return new Object[] { source };
		else
			return new Object[] { maxInstances, vertices, indices, material, attribs };
	}
}

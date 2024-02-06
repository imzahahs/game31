package sengine.model;

import sengine.File;
import sengine.mass.MassSerializable;

public class BatchedModel implements MassSerializable {
	
	public static final String EXTENSION = ".BatchedModel";

	public static BatchedModel load(String filename, int maxInstances) {
		BatchedModel m = File.getHints(filename + EXTENSION, false);
		if(m == null) {
			m = new BatchedModel(Model.load(filename), maxInstances);
			File.saveHints(filename + EXTENSION, m);
		}
		return m;
	}
	
	public final Model model;
	public final InstancedSkinnedMesh[] meshes;
	
	@MassConstructor
	public BatchedModel(Model model, int maxInstances) {
		this.model = model;
		
		// Create meshes
		this.meshes = new InstancedSkinnedMesh[model.meshes.length];
		for(int c = 0; c < meshes.length; c++) {
			SkinnedMesh source = model.meshes[c];
			InstancedSkinnedMesh dest = new InstancedSkinnedMesh(source.maxVertices, source.indices.length, maxInstances);
			meshes[c] = dest;
			dest.replace(source.vertices);
			dest.replaceIndices(source.indices);
			dest.setMaterial(source.getMaterial());
		}
	}
	@Override
	public Object[] mass() {
		return new Object[] {model, meshes[0].maxInstances};
	}

	public BatchedModel(Model model, InstancedSkinnedMesh[] meshes) {
		this.model = model;
		this.meshes = meshes;
	}
	
	public BatchedModel instantiate() {
		// Instantiate all mesh
		InstancedSkinnedMesh[] meshCopies = new InstancedSkinnedMesh[meshes.length];
		for(int c = 0; c < meshCopies.length; c++)
			meshCopies[c] = meshes[c].instantiate();
		
		return new BatchedModel(model, meshCopies);
		
	}
}

package sengine.graphics2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectIntMap;

import sengine.Sys;

public class Renderer {
	static final String TAG = "Renderer";

	public static final int TARGET_MATERIAL_SORTED = 0;
	public static final int TARGET_BLENDING_SORTED = 1;
	public static final int TARGET_SEQUENCE_SORTED = 2;

	public static int defaultTargetBufferSize = 128;

	public static Renderer renderer;

	// Temporary memory
	protected static float[] tempFloats = new float[0];
	protected static final Matrix4 tempMatrix = new Matrix4();

    protected static final ObjectIntMap<Object> tempIntMap = new ObjectIntMap<Object>();
	

	
	public final GL20 gl;

	public final Array<MaterialConfiguration>[] targets;
	
	// Stack
	protected final Array<MaterialConfiguration> stack = new Array<>(false, defaultTargetBufferSize, MaterialConfiguration.class);
	
	// Clear color
	public final Color clearColor = new Color(0, 0, 0, 1f);
	
	// Current GL states
	protected int depthFunc = GL20.GL_LEQUAL;
	protected boolean depthMask = true;
	protected int faceCullingMode = GL20.GL_BACK;
	protected int srcBlendFunc = GL20.GL_SRC_ALPHA;
	protected int destBlendFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;

    // Viewport
    protected boolean scissorEnabled = false;
	protected final Matrices.ScissorBox scissorBox = new Matrices.ScissorBox();
    protected int viewportWidth = -1;
    protected int viewportHeight = -1;
	
	// Current material
	protected Material material = null;
	protected Shader shader = null;
	protected MaterialConfiguration instruction = null;
	
	public Renderer() {
		this(Renderer.TARGET_SEQUENCE_SORTED + 1);
	}
	
	@SuppressWarnings("unchecked")
	protected Renderer(int targets) {
		this.gl = Gdx.gl20;
		
		// Create targets
		this.targets = new Array[targets];
		for(int c = 0; c < targets; c++)
			this.targets[c] = new Array<MaterialConfiguration>(false, 16, MaterialConfiguration.class); 
	}
	
	public void addInstruction(MaterialConfiguration i) {
		targets[i.target].add(i);
	}
	
	public void clearInstructions() {
		for(int c = 0; c < targets.length; c++) {
            clearInstructions(targets[c]);
		}
	}

    public void clearInstructions(Array<MaterialConfiguration> target) {
        for(int i = 0; i < target.size; i++)
            target.items[i].clear();
        target.clear();
    }
	
	protected void renderInstruction(MaterialConfiguration i) {
		if(shader == null || i.scissor.isZero())
			return;			// cannot render or no need to render
        // Check scissor
        boolean scissorChanged = !i.scissor.contentEquals(scissorBox);
        if(instruction == null) {
            // Scissor for SpriteBatch
            if(scissorChanged && Sys.sb.isDrawing())
                Sys.sb.flush();
            i.bind(shader);
        }
        else if(instruction.bindObject != i.bindObject || i.bindObject == null || scissorChanged) {
			instruction.unbind(shader);
            // Scissor for SpriteBatch
            if(scissorChanged && Sys.sb.isDrawing())
                Sys.sb.flush();
            i.bind(shader);
		}
		instruction = i;
		// Render instruction
        if(scissorChanged) {
            if (i.scissor.isInfinite()) {
                // No need to use scissor
                if (scissorEnabled) {
                    gl.glScissor(0, 0, Sys.system.getWidth(), Sys.system.getHeight());
                    gl.glDisable(GL20.GL_SCISSOR_TEST);
                    scissorEnabled = false;
                }
            } else {
                // Else need to use scissor
                if (!scissorEnabled) {
                    gl.glEnable(GL20.GL_SCISSOR_TEST);
                    scissorEnabled = true;
                }
                float renderLength = i.camera.viewportHeight / i.camera.viewportWidth;
                i.scissor.apply(gl, viewportWidth, viewportHeight, renderLength);
            }
            scissorBox.set(i.scissor);
        }
		try {
			i.render(shader);
		} catch (Throwable e) {
			Sys.error(TAG, "Unable to render instruction: " + i, e);
		}
	}
	
	protected void sortMaterial(Array<MaterialConfiguration> list, Array<MaterialConfiguration> stack) {
		// Render material sorted first
		int size = list.size;
		int remaining = size;
		if(size == 0)
			return;
		int mbidx = 0;
		Material material = null;
		Object bindObject = null;
		while(true) {
			// Select unrendered material
			for(; mbidx < size; mbidx++) {
				MaterialConfiguration render = list.items[mbidx];
				if(render == null)
					continue;
				material = render.material;
				bindObject = render.bindObject;
				break;
			}
			// Render instructions in batches
			int ibidx = mbidx;		// instruction batch index
			do {
				int nibidx = -1;			// next instruction batch index
				for(int c = ibidx; c < size; c++) {
					MaterialConfiguration render = list.items[c];
					if(render == null || render.material != material)
						continue;
					// Check mesh batching
					if(bindObject != render.bindObject) {
						if(c == ibidx)
							bindObject = render.bindObject;
						else {
                            if (nibidx == -1)
                                nibidx = c;
                            continue;
                        }
					}
					// Add to stack
					stack.add(render);
					list.set(c, null);
					remaining--;
					if(remaining == 0) {
						list.clear();
						return;		// Rendered all
					}
				}
				ibidx = nibidx;
			} while(ibidx != -1);
		}
	}

    protected void sortMaterialLast(Array<MaterialConfiguration> list, Array<MaterialConfiguration> stack) {
        // Render material sorted first
        int size = list.size;
        int remaining = size;
        if(size == 0)
            return;
        int mbidx = 0;
        Material material = null;
        Object bindObject = null;
        try {
            // Create profiles on materials
            for (mbidx = 0; mbidx < size; mbidx++) {
                MaterialConfiguration render = list.items[mbidx];
                material = render.material;
                int idx = tempIntMap.get(material, -1);
                if (idx != -1)
                    continue;       // already profiled this material
                // Haven't found the last occurrence of this this material yet, do now
                idx = mbidx;
                for (int ibidx = mbidx + 1; ibidx < size; ibidx++) {
                    render = list.items[ibidx];
                    if (render.material == material)
                        idx = ibidx;
                }
                // Remember the last index of this material
                tempIntMap.put(material, idx);
            }
            while (true) {
                // Select a material which finishes the earliest in the render stack
                int earliestLastIdx = Integer.MAX_VALUE;
                for (mbidx = 0; mbidx < size; mbidx++) {
                    MaterialConfiguration render = list.items[mbidx];
                    if (render == null)
                        continue;
                    int lastIdx = tempIntMap.get(render.material, -1);
                    if (lastIdx < earliestLastIdx) {
                        earliestLastIdx = lastIdx;
                        material = render.material;
                        bindObject = render.bindObject;
                    }
                }
                // Render instructions in batches
                int ibidx = 0;        // instruction batch index
                do {
                    int nibidx = -1;            // next instruction batch index
                    for (int c = ibidx; c < size; c++) {
                        MaterialConfiguration render = list.items[c];
                        if (render == null || render.material != material)
                            continue;
                        // Check mesh batching
                        if (bindObject != render.bindObject) {
                            if (c == ibidx)
                                bindObject = render.bindObject;
                            else {
                                if (nibidx == -1)
                                    nibidx = c;
                                continue;
                            }
                        }
                        // Add to stack
                        stack.add(render);
                        list.set(c, null);
                        remaining--;
                        if (remaining == 0) {
                            list.clear();
                            return;        // Rendered all
                        }
                    }
                    ibidx = nibidx;
                } while (ibidx != -1);
            }
        } finally {
            tempIntMap.clear();
        }
    }
	
	protected void sortBackToFront(Array<MaterialConfiguration> list, Array<MaterialConfiguration> stack) {
		// Render blending sorted
		int size = list.size;
		if(tempFloats.length < size)
			tempFloats = new float[size];
		// Calculate all depth values
		for(int c = 0; c < size; c++) {
			MaterialConfiguration render = list.items[c];
			tempMatrix.set(render.camera.combined);
			tempMatrix.mul(render.modelMatrix);
			tempFloats[c] = tempMatrix.val[Matrix4.M23];
		}
		// Sort according to depth
		for(int c = 0; c < size; c++) {
			float z = -Float.MAX_VALUE;
			int index = 0;
			for(int i = 0; i < size; i++) {
				MaterialConfiguration render = list.items[i];
				if(render == null)
					continue;	// Instruction already rendered
				float iz = tempFloats[i];
				if(iz > z) {
					z = iz;
					index = i;
				}
			}
			// Sort farthest instruction
			stack.add(list.items[index]);
			list.set(index, null);
		}
		list.clear();
	}
	
	protected void sortSequence(Array<MaterialConfiguration> list, Array<MaterialConfiguration> stack) {
		// Render sequence sorted
		stack.addAll(list);
		list.clear();
	}

    protected void initialize() {
        initializeViewport();
        initializeStates();
    }

    protected void initializeViewport() {
        initializeViewport(Sys.system.getWidth(), Sys.system.getHeight());
    }

    protected void initializeViewport(int width, int height) {
        // Scissor
        scissorEnabled = false;
        viewportWidth = width;
        viewportHeight = height;
        gl.glScissor(0, 0, width, height);
        gl.glDisable(GL20.GL_SCISSOR_TEST);
		scissorBox.inf();

    }
	
	protected void initializeStates() {
		// Reset GL states
		depthFunc = GL20.GL_LEQUAL;
		depthMask = true;
		faceCullingMode = GL20.GL_BACK;
		srcBlendFunc = GL20.GL_SRC_ALPHA;
		destBlendFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;

		// Initial GL configuration
//		gl.glEnable(GL20.GL_TEXTURE_2D);		TODO: throws error log on Adreno HTC One M7
		gl.glEnable(GL20.GL_DEPTH_TEST);
		gl.glDepthFunc(depthFunc);
		gl.glDepthMask(depthMask);
		gl.glEnable(GL20.GL_CULL_FACE);
		gl.glCullFace(faceCullingMode);
		gl.glEnable(GL20.GL_BLEND);
		gl.glBlendFunc(srcBlendFunc, destBlendFunc);
		
		// Reset
		material = null;
		shader = null;
		instruction = null;
		Shader.lastShader = null;

		// Clear color
		gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
	}
	
	protected void initializeMaterial(Material newMaterial) {
		// Check if material can be batched
		if(material == newMaterial)
			return;		// Already initialized this material
		// Else using a different material, switch now
		clearMaterial();
		material = newMaterial;
		// Update gl states first, if needed
		// Depth function
		if(material.depthFunc != depthFunc) {
			depthFunc = material.depthFunc;
			gl.glDepthFunc(depthFunc);
		}
		// Depth mask
		if(material.depthMask != depthMask) {
			depthMask = material.depthMask;
			gl.glDepthMask(depthMask);
		}
		// Face culling
		if(material.faceCullingMode != faceCullingMode) {
			if(faceCullingMode == GL20.GL_NEVER)
				gl.glEnable(GL20.GL_CULL_FACE);
			faceCullingMode = material.faceCullingMode;
			if(faceCullingMode == GL20.GL_NEVER)
				gl.glDisable(GL20.GL_CULL_FACE);
			else
				gl.glCullFace(faceCullingMode);
		}
		// Blending
		if(material.srcBlendFunc != srcBlendFunc || material.destBlendFunc != destBlendFunc) {
			if(srcBlendFunc == GL20.GL_ONE && destBlendFunc == GL20.GL_ZERO)
				gl.glEnable(GL20.GL_BLEND);
			srcBlendFunc = material.srcBlendFunc;
			destBlendFunc = material.destBlendFunc;
			if(srcBlendFunc == GL20.GL_ONE && destBlendFunc == GL20.GL_ZERO)
				gl.glDisable(GL20.GL_BLEND);
			else
				gl.glBlendFunc(srcBlendFunc, destBlendFunc);
		}
		// Bind new material
		material.load();
		shader = material.bind();
	}
	
	protected void clearMaterial() {
		if(material != null) {
			if(instruction != null) {
				instruction.unbind(shader);
				instruction = null;
			}
			material.unbind();
			material = null;
			shader = null;
		}
	}
	
	public void render() {
		initialize();
		
		// Clear screen
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Sort instructions
		sortMaterial(targets[Renderer.TARGET_MATERIAL_SORTED], stack);
		sortBackToFront(targets[Renderer.TARGET_BLENDING_SORTED], stack);
		sortSequence(targets[Renderer.TARGET_SEQUENCE_SORTED], stack);
		
		// Render all instructions
		int size = stack.size;
		for(int c = 0; c < size; c++) {
			MaterialConfiguration render = stack.items[c];
			// Initialize material
			initializeMaterial(render.material);
			renderInstruction(render);
		}

        // Flush material
		clearMaterial();

        clearInstructions(stack);
    }
	
	public void resize(int width, int height) {
		// nothing
	}
}

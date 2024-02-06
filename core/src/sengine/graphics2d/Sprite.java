package sengine.graphics2d;

import sengine.File;
import sengine.Sys;
import sengine.model.Model;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Sprite extends Mesh {
	static final String TAG = "Sprite";
	
	public static final String EXTENSION = ".Sprite";


    public static Sprite load(String filename) {
        return load(filename, true);
    }

	public static Sprite load(String filename, boolean compile) {
		// Fetch from FS if available
		Sprite sprite = File.getHints(filename + EXTENSION, false);
		if(sprite != null || !compile)
			return sprite;		// Sprite previously loaded or no need to compile
		// Else first time loading sprite, load material
		Material m = Material.load(filename);
		sprite = new Sprite(m);
		// Save to FS
        sprite.save(filename);
		return sprite;
	}
	
	public float length;
	
	// Mesh properties
	@Override
	public int indexX(int vertex) { return (vertex * 5) + 0; }
	@Override
	public int indexY(int vertex) { return (vertex * 5) + 1; }
	@Override
	public int indexZ(int vertex) { return (vertex * 5) + 2; }
	@Override
	public int indexU(int vertex) { return (vertex * 5) + 3; }
	@Override
	public int indexV(int vertex) { return (vertex * 5) + 4; }
	@Override
	public int indexNX(int vertex) { return -1; }		// n/a
	@Override
	public int indexNY(int vertex) { return -1; }		// n/a
	@Override
	public int indexNZ(int vertex) { return -1; }		// n/a
	
	// Sprites only need position and texture coordinates
	public static final VertexAttributes vertexAttributes = new VertexAttributes(
		new VertexAttribute(VertexAttributes.Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0")
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
	public Sprite instantiate() {
		return new Sprite(this);
	}
	
	public Sprite(Material m) {
		this(m.getLength(), m);
	}

	public Sprite(float length) {
		this(length, null);
	}

	public Sprite(float length, Material m) {
		super(6, 6);
		// Create basic, fixed size sprite
		this.length = length;
		replace(new float[] {
			-0.5f, -length / 2.0f, 0.0f, 0.0f, 1.0f,			// TODO: replace with 4 vertices and 6 indices
			+0.5f, -length / 2.0f, 0.0f, 1.0f, 1.0f,
			-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f,  
			-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f,  
			+0.5f, -length / 2.0f, 0.0f, 1.0f, 1.0f,
			+0.5f, +length / 2.0f, 0.0f, 1.0f, 0.0f, 
		});
		// Set material
		setMaterial(m);
	}

	public Sprite transform(Matrix4 m) {
		Vector3 vec = new Vector3();
		for(int c = 0, o = 0; c < 6; c++, o += 5) {
			vec.set(vertices[o + 0], vertices[o + 1], vertices[o + 2]);
			vec.mul(m);
			vertices[o + 0] = vec.x;
			vertices[o + 1] = vec.y;
			vertices[o + 2] = vec.z;
		}
		return this;
	}

	/**
	 * Tiles this sprite to width and height times, length is calculated by height / width
	 * @param width number of times to repeat in the x dimension
	 * @param height number of times to repeat in the y dimension
     * @return this
     */
	public Sprite tile(float width, float height) {
		tile(width, height, height / width);
		return this;
	}

	/**
	 * Tiles this sprite to width and height times
	 * @param width number of times to repeat in the x dimension
	 * @param height number of times to repeat in the y dimension
	 * @param length new length for this sprite
     * @return this
     */
	public Sprite tile(float width, float height, float length) {
		// Create basic, fixed size sprite
		this.length = length;
        float materialLength = material.getLength();
        float u = width;
        float v = height / materialLength;
		replace(new float[] {
				-0.5f, -length / 2.0f, 0.0f, 0.0f, v,
				+0.5f, -length / 2.0f, 0.0f, u, v,
				-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f,
				-0.5f, +length / 2.0f, 0.0f, 0.0f, 0.0f,
				+0.5f, -length / 2.0f, 0.0f, u, v,
				+0.5f, +length / 2.0f, 0.0f, u, 0.0f,
		});
        upload();
        return this;
	}

	public Sprite crop(float length) {
		return crop(length, 0f, 0f);		// no offset
	}

    public Sprite crop(float length, float offsetX, float offsetY) {
        if(this.length == length)
            return this;            // same length
        this.length = length;
        float materialLength = material.getLength();
        float cropX;
        float cropY;
        if(length > materialLength) {
            cropX = (1f - (materialLength / length)) / 2f;
            cropY = 0f;
        }
        else {
            cropX = 0f;
            cropY = ((materialLength - length) / materialLength) / 2f;
        }
		offsetX *= cropX;
        offsetY *= cropY;
        replace(new float[] {
                -0.5f, -length / 2.0f, 0.0f, cropX + offsetX, 1.0f - cropY + offsetY,
                +0.5f, -length / 2.0f, 0.0f, 1.0f - cropX + offsetX, 1.0f - cropY + offsetY,
                -0.5f, +length / 2.0f, 0.0f, cropX + offsetX, cropY + offsetY,
                -0.5f, +length / 2.0f, 0.0f, cropX + offsetX, cropY + offsetY,
                +0.5f, -length / 2.0f, 0.0f, 1.0f - cropX + offsetX, 1.0f - cropY + offsetY,
                +0.5f, +length / 2.0f, 0.0f, 1.0f - cropX + offsetX, cropY + offsetY,
        });
        upload();
        return this;
    }

    public Sprite save(String filename) {
        // Save to FS
        File.saveHints(filename + EXTENSION, this);
        return this;
    }

	@MassConstructor
	public Sprite(Sprite copy) {
		super(copy);
		
		this.length = copy.length;
	}
	
	@MassConstructor
	public Sprite(float length, float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
		
		this.length = length;
	}
	
	@Override
	public Object[] mass() {
		if(source != this)
			return new Object[] { source };
		else 
			return new Object[] { length, vertices, indices, material, attribs };
	}

	@Override
	public float getLength() {
		return length;
	}
}
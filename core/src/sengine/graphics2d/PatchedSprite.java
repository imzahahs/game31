package sengine.graphics2d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.utils.Config;

/**
 * Created by Azmi on 23/6/2016.
 */
public class PatchedSprite extends Mesh {

    public static final String CFG_MATERIAL_CORNER_SIZE = "materialCornerSize";
    public static final String CFG_MATERIAL_CORNER_WIDTH = "materialCornerWidth";
    public static final String CFG_MATERIAL_CORNER_HEIGHT = "materialCornerHeight";


    public static final String CFG_EXTENSION = ".PatchedSprite";

	public static void load(String filename) {
		Material.load(filename);
		Config.load(filename + CFG_EXTENSION);
	}

	public static PatchedSprite create(String filename, float spriteLength, float cornerSize) {
		return create(filename, spriteLength, cornerSize, cornerSize, cornerSize, cornerSize);
	}

	public static PatchedSprite create(String filename, float spriteLength, float cornerLeftSize, float cornerRightSize, float cornerTopSize, float cornerBottomSize) {
        Material m = Material.load(filename);

		// Get material corner size from config
		Config config = Config.load(filename + CFG_EXTENSION);
        float materialCornerWidth;
        float materialCornerHeight;
        if(config.contains(CFG_MATERIAL_CORNER_SIZE)) {
            materialCornerWidth = config.get(CFG_MATERIAL_CORNER_SIZE);
            materialCornerHeight = materialCornerWidth;
        }
        else {
            materialCornerWidth = config.get(CFG_MATERIAL_CORNER_WIDTH);
            materialCornerHeight = config.get(CFG_MATERIAL_CORNER_HEIGHT);
        }

		return new PatchedSprite(spriteLength, cornerLeftSize, cornerRightSize, cornerTopSize, cornerBottomSize, materialCornerWidth, materialCornerHeight, m);
    }


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


	public final float length;
	public final float cornerLeftSize;
	public final float cornerRightSize;
	public final float cornerTopSize;
	public final float cornerBottomSize;
	public final float materialCornerWidth;
	public final float materialCornerHeight;

	public PatchedSprite(float spriteLength, float cornerLeftSize, float cornerRightSize, float cornerTopSize, float cornerBottomSize, float materialCornerWidth, float materialCornerHeight, Material m) {
		super(16, 54);          // 16 vertices, 56 indices

        this.length = spriteLength;

		this.cornerLeftSize = cornerLeftSize;
		this.cornerRightSize = cornerRightSize;
		this.cornerTopSize = cornerTopSize;
		this.cornerBottomSize = cornerBottomSize;
		this.materialCornerWidth = materialCornerWidth;
		this.materialCornerHeight = materialCornerHeight;

		float halfLength = length / 2f;

        float cornerWidth = materialCornerWidth;
        float cornerHeight = materialCornerHeight;

        float cornerLength = cornerHeight / cornerWidth;

        cornerTopSize *= cornerLength;
        cornerBottomSize *= cornerLength;

        cornerHeight = materialCornerHeight / m.getLength();

        replace(new float[] {
                -0.5f,                      -halfLength,                   		0f, 0f,                      1f,         // bl, 0
                -0.5f + cornerLeftSize,   	-halfLength,                   		0f, cornerWidth,             1f,         // blm, 1
                +0.5f - cornerRightSize,   	-halfLength,                   		0f, 1f - cornerWidth,        1f,         // brm, 2
                +0.5f,                      -halfLength,                   		0f, 1f,                      1f,         // br, 3

                -0.5f,                      -halfLength + cornerBottomSize,     0f, 0f,                      1f - cornerHeight,  // bml, 4
                -0.5f + cornerLeftSize,   	-halfLength + cornerBottomSize,     0f, cornerWidth,             1f - cornerHeight,  // bmlm, 5
                +0.5f - cornerRightSize,   	-halfLength + cornerBottomSize,     0f, 1f - cornerWidth,        1f - cornerHeight,  // bmrm, 6
                +0.5f,                      -halfLength + cornerBottomSize,     0f, 1f,                      1f - cornerHeight,  // bmr, 7

                -0.5f,                      +halfLength - cornerTopSize,   		0f, 0f,                      cornerHeight,   // tml, 8
                -0.5f + cornerLeftSize,   	+halfLength - cornerTopSize,   		0f, cornerWidth,             cornerHeight,   // tmlm, 9
                +0.5f - cornerRightSize,   	+halfLength - cornerTopSize,   		0f, 1f - cornerWidth,        cornerHeight,   // tmrm, 10
                +0.5f,                      +halfLength - cornerTopSize,   		0f, 1f,                      cornerHeight,   // tmr, 11

                -0.5f,                      +halfLength,                   		0f, 0f,                      0f,     // tl, 12
                -0.5f + cornerLeftSize,   	+halfLength,                   		0f, cornerWidth,             0f,     // tlm, 13
                +0.5f - cornerRightSize,   	+halfLength,                   		0f, 1f - cornerWidth,        0f,     // trm, 14
                +0.5f,                      +halfLength,                   		0f, 1f,                      0f,     // tr, 15
        });
        replaceIndices(new short[] {
                // bl, blm, bml, bml, blm, bmlm
                0, 1, 4, 4, 1, 5,
                // blm, brm, bmlm, bmlm, brm, bmrm
                1, 2, 5, 5, 2, 6,
                // brm, br, bmrm, bmrm, br, bmr
                2, 3, 6, 6, 3, 7,

                // bml, bmlm, tml, tml, bmlm, tmlm
                4, 5, 8, 8, 5, 9,
                // bmlm, bmrm, tmlm, tmlm, bmrm, tmrm
                5, 6, 9, 9, 6, 10,
                // bmrm, bmr, tmrm, tmrm, bmr, tmr
                6, 7, 10, 10, 7, 11,

                // tml, tmlm, tl, tl, tmlm, tlm
                8, 9, 12, 12, 9, 13,
                // tmlm, tmrm, tlm, tlm, tmrm, trm
                9, 10, 13, 13, 10, 14,
                // tmrm, tmr, trm, trm, tmr, tr
                10, 11, 14, 14, 11, 15

        });


		// Set material
		setMaterial(m);
	}

	@Override
	public float getLength() {
		return length;
	}


	public PatchedSprite(PatchedSprite copy) {
		super(copy);

		this.length = copy.length;
		this.cornerLeftSize = copy.cornerLeftSize;
		this.cornerRightSize = copy.cornerRightSize;
		this.cornerTopSize = copy.cornerTopSize;
		this.cornerBottomSize = copy.cornerBottomSize;
		this.materialCornerWidth = copy.materialCornerWidth;
		this.materialCornerHeight = copy.materialCornerHeight;
	}


	@Override
	public PatchedSprite instantiate() {
		return new PatchedSprite(this);
	}

	public PatchedSprite instantiate(float spriteLength, float cornerSize) {
		return instantiate(spriteLength, cornerSize, cornerSize, cornerSize, cornerSize);
	}

	public PatchedSprite instantiate(float spriteLength, float cornerLeftSize, float cornerRightSize, float cornerTopSize, float cornerBottomSize) {
		PatchedSprite patch = new PatchedSprite(spriteLength, cornerLeftSize, cornerRightSize, cornerTopSize, cornerBottomSize, materialCornerWidth, materialCornerHeight, getMaterial());
		patch.copyAttributes(this);
		return patch;
	}

	/*
	public PatchedSprite(int maxVertices, int maxIndices) {
		super(maxVertices, maxIndices);
	}

	public PatchedSprite(Mesh copy) {
		super(copy);
	}

	public PatchedSprite(float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
		super(vertices, indices, material, attribs);
	}

    */

}



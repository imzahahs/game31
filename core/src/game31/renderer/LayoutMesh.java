package game31.renderer;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import sengine.graphics2d.Material;
import sengine.graphics2d.MaterialAttribute;
import sengine.graphics2d.Mesh;

/**
 * Created by Azmi on 7/6/2017.
 */

public class LayoutMesh extends Mesh {


    private static final String PNG_SUFFIX = ".png";

    public final float tileSize;
    public final float length;


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

    public static LayoutMesh create(String layoutNamespace, int width, int height, int sectionSize, int[][] indices, int index) {
        // First calculate total number of used tiles
        int count = 0;
        for(int y = 0; y < indices.length; y++) {
            int[] row = indices[y];
            for(int x = 0; x < row.length; x++) {
                if(row[x] == index)
                    count++;
            }
        }
        if(count == 0)
            return null;        // no tiles were used

        // Create mesh
        float tileSize = (float)sectionSize / (float)width;
        float length = (float)height / (float)width;
        Material material = Material.load(layoutNamespace + index + PNG_SUFFIX);
        float materialWidth = 1f / material.getLength();
        LayoutMesh mesh = new LayoutMesh(count, tileSize, length, material);
        float[] vertices = mesh.vertices;

        // Update vertices
        int verticalSections = height / sectionSize;
        if(height % sectionSize != 0)
            verticalSections++;
        int horizontalSections = width / sectionSize;
        if(width % sectionSize != 0)
            horizontalSections++;
        float y = +length / 2f;
        float bottomY = -length / 2f;
        int i = 0;
        for(int v = 0; v < verticalSections; v++) {
            float y2 = y - tileSize;
            if(y2 < bottomY || v == (verticalSections - 1))
                y2 = bottomY;
            float x = -0.5f;
            for(int h = 0; h < horizontalSections; h++) {
                float x2 = x + tileSize;
                float u = 1.0f;
                if(x2 > +0.5f || h == (horizontalSections - 1)) {
                    float overflow = x2 - 0.5f;
                    if(overflow <= 0f)
                        overflow = 0;
                    u = (1.0f - (overflow / tileSize)) / materialWidth;
                    x2 = +0.5f;
                }

                // Check if this tile is visible
                if(indices[v][h] == index) {
                    // Add to vertices
                    // bl
                    vertices[i++] = x;
                    vertices[i++] = y2;
                    vertices[i++] = 0;
                    vertices[i++] = 0.0f;
                    vertices[i++] = 1.0f;
                    // br
                    vertices[i++] = x2;
                    vertices[i++] = y2;
                    vertices[i++] = 0;
                    vertices[i++] = u;
                    vertices[i++] = 1.0f;
                    // tl
                    vertices[i++] = x;
                    vertices[i++] = y;
                    vertices[i++] = 0;
                    vertices[i++] = 0.0f;
                    vertices[i++] = 0.0f;
                    // tl
                    vertices[i++] = x;
                    vertices[i++] = y;
                    vertices[i++] = 0;
                    vertices[i++] = 0.0f;
                    vertices[i++] = 0.0f;
                    // br
                    vertices[i++] = x2;
                    vertices[i++] = y2;
                    vertices[i++] = 0;
                    vertices[i++] = u;
                    vertices[i++] = 1.0f;
                    // tr
                    vertices[i++] = x2;
                    vertices[i++] = y;
                    vertices[i++] = 0;
                    vertices[i++] = u;
                    vertices[i++] = 0.0f;
                }
                x = x2;
            }
            y = y2;
        }
        // Done
        return mesh;
    }

    private LayoutMesh(int count, float tileSize, float length, Material m) {
        super(count * 6, count * 6);

        this.tileSize = tileSize;
        this.length = length;

        // Set material
        setMaterial(m);
    }

    @MassConstructor
    public LayoutMesh(LayoutMesh copy) {
        super(copy);

        this.tileSize = copy.tileSize;
        this.length = copy.length;
    }

    @MassConstructor
    public LayoutMesh(float tileSize, float length, float[] vertices, short[] indices, Material material, MaterialAttribute[] attribs) {
        super(vertices, indices, material, attribs);

        this.tileSize = tileSize;
        this.length = length;
    }

    @Override
    public Object[] mass() {
        if(source != this)
            return new Object[] { source };
        else
            return new Object[] { tileSize, length, vertices, indices, material, attribs };
    }

    @Override
    public LayoutMesh instantiate() {
        return new LayoutMesh(this);
    }


    @Override
    public float getLength() {
        return length;
    }
}

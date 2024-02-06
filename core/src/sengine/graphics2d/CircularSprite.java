package sengine.graphics2d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by Azmi on 10/18/2016.
 */

public class CircularSprite extends Mesh {


    private float length = 1f;
    private float[] verticesReset;
    private float diagonalAngle;

    private float circleStart = 0;
    private float circleEnd = 360;


    public void setLength(float length) {
        this.length = length;
        this.diagonalAngle = (float)Math.toDegrees(Math.atan(1f / length));

        float halfLength = length / 2f;

        verticesReset = new float[] {
                // tr1
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      +halfLength,    0f,     1f,         0f,
                0f,         +halfLength,    0f,     0.5f,       0f,
                // tr2
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      0f,             0f,     1f,         0.5f,
                +0.5f,      +halfLength,    0f,     1f,         0f,
                // br1
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      -halfLength,    0f,     1f,         1f,
                +0.5f,      0f,             0f,     1f,         0.5f,
                // br2
                0f,         0f,             0f,     0.5f,       0.5f,
                0f,         -halfLength,    0f,     0.5f,       1f,
                +0.5f,      -halfLength,    0f,     1f,         1f,
                // bl2
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      -halfLength,    0f,     0f,         1f,
                0f,         -halfLength,    0f,     0.5f,       1f,
                // bl1,
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      0f,             0f,     0f,         0.5f,
                -0.5f,      -halfLength,    0f,     0f,         1f,
                // tl2
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      +halfLength,    0f,     0f,         0f,
                -0.5f,      0f,             0f,     0f,         0.5f,
                // tl1
                0f,         0f,             0f,     0.5f,       0.5f,
                0f,         +halfLength,    0f,     0.5f,       0f,
                -0.5f,      +halfLength,    0f,     0f,         0f,

                // Secondary copy

                // tr1
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      +halfLength,    0f,     1f,         0f,
                0f,         +halfLength,    0f,     0.5f,       0f,
                // tr2
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      0f,             0f,     1f,         0.5f,
                +0.5f,      +halfLength,    0f,     1f,         0f,
                // br1
                0f,         0f,             0f,     0.5f,       0.5f,
                +0.5f,      -halfLength,    0f,     1f,         1f,
                +0.5f,      0f,             0f,     1f,         0.5f,
                // br2
                0f,         0f,             0f,     0.5f,       0.5f,
                0f,         -halfLength,    0f,     0.5f,       1f,
                +0.5f,      -halfLength,    0f,     1f,         1f,
                // bl2
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      -halfLength,    0f,     0f,         1f,
                0f,         -halfLength,    0f,     0.5f,       1f,
                // bl1,
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      0f,             0f,     0f,         0.5f,
                -0.5f,      -halfLength,    0f,     0f,         1f,
                // tl2
                0f,         0f,             0f,     0.5f,       0.5f,
                -0.5f,      +halfLength,    0f,     0f,         0f,
                -0.5f,      0f,             0f,     0f,         0.5f,
                // tl1
                0f,         0f,             0f,     0.5f,       0.5f,
                0f,         +halfLength,    0f,     0.5f,       0f,
                -0.5f,      +halfLength,    0f,     0f,         0f,
        };

        show(circleStart, circleEnd);
    }

    private void hideTriangle(int t) {
        vertices[(((t * 3) + 0) * 5) + 0] = 0;
        vertices[(((t * 3) + 0) * 5) + 1] = 0;
        vertices[(((t * 3) + 1) * 5) + 0] = 0;
        vertices[(((t * 3) + 1) * 5) + 1] = 0;
        vertices[(((t * 3) + 2) * 5) + 0] = 0;
        vertices[(((t * 3) + 2) * 5) + 1] = 0;
    }

    private void updateTriangles(int offset, float circleStart, float circleEnd) {
        float halfLength = length / 2f;

        // tr1
        if(circleStart <= diagonalAngle && circleEnd >= 0f) {
            if(circleStart > 0f) {
                float tan = (float)Math.tan(Math.toRadians(circleStart));
                vertices[((((offset + 0) * 3) + 2) * 5) + 0] = tan * halfLength;
                vertices[((((offset + 0) * 3) + 2) * 5) + 3] = (tan * halfLength) + 0.5f;
            }
            if(circleEnd < diagonalAngle) {
                float tan = (float)Math.tan(Math.toRadians(circleEnd));
                vertices[((((offset + 0) * 3) + 1) * 5) + 0] = tan * halfLength;
                vertices[((((offset + 0) * 3) + 1) * 5) + 3] = (tan * halfLength) + 0.5f;
            }
        }
        else
            hideTriangle(offset + 0);

        // tr2
        if(circleStart <= 90f && circleEnd >= diagonalAngle) {
            if(circleStart > diagonalAngle) {
                float tan = (float)Math.tan(Math.toRadians(90f - circleStart));
                vertices[((((offset + 1) * 3) + 2) * 5) + 1] = tan * 0.5f;
                vertices[((((offset + 1) * 3) + 2) * 5) + 4] = (1f - (tan / length)) * 0.5f;
            }
            if(circleEnd < 90f) {
                float tan = (float)Math.tan(Math.toRadians(90f - circleEnd));
                vertices[((((offset + 1) * 3) + 1) * 5) + 1] = tan * 0.5f;
                vertices[((((offset + 1) * 3) + 1) * 5) + 4] = (1f - (tan / length)) * 0.5f;
            }
        }
        else
            hideTriangle(offset + 1);

        // br1
        if(circleStart <= (180f - diagonalAngle) && circleEnd >= 90f) {
            if(circleStart > 90f) {
                float tan = (float)Math.tan(Math.toRadians(circleStart - 90f));
                vertices[((((offset + 2) * 3) + 2) * 5) + 1] = -tan * 0.5f;
                vertices[((((offset + 2) * 3) + 2) * 5) + 4] = (((tan / length)) * 0.5f) + 0.5f;
            }
            if(circleEnd < (180f - diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(circleEnd - 90f));
                vertices[((((offset + 2) * 3) + 1) * 5) + 1] = -tan * 0.5f;
                vertices[((((offset + 2) * 3) + 1) * 5) + 4] = (((tan / length)) * 0.5f) + 0.5f;
            }
        }
        else
            hideTriangle(offset + 2);

        // br2
        if(circleStart <= 180f && circleEnd >= (180f - diagonalAngle)) {
            if(circleStart > (180f - diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(180f - circleStart));
                vertices[((((offset + 3) * 3) + 2) * 5) + 0] = tan * halfLength;
                vertices[((((offset + 3) * 3) + 2) * 5) + 3] = (tan * halfLength) + 0.5f;
            }
            if(circleEnd < 180f) {
                float tan = (float)Math.tan(Math.toRadians(180f - circleEnd));
                vertices[((((offset + 3) * 3) + 1) * 5) + 0] = tan * halfLength;
                vertices[((((offset + 3) * 3) + 1) * 5) + 3] = (tan * halfLength) + 0.5f;
            }
        }
        else
            hideTriangle(offset + 3);

        // bl2
        if(circleStart <= (180f + diagonalAngle) && circleEnd >= 180f) {
            if(circleStart > 180f) {
                float tan = (float)Math.tan(Math.toRadians(circleStart - 180f));
                vertices[((((offset + 4) * 3) + 2) * 5) + 0] = -tan * halfLength;
                vertices[((((offset + 4) * 3) + 2) * 5) + 3] = 0.5f - (tan * halfLength);
            }
            if(circleEnd < (180f + diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(circleEnd - 180f));
                vertices[((((offset + 4) * 3) + 1) * 5) + 0] = -tan * halfLength;
                vertices[((((offset + 4) * 3) + 1) * 5) + 3] = 0.5f - (tan * halfLength);
            }
        }
        else
            hideTriangle(offset + 4);

        // bl1
        if(circleStart <= 270f && circleEnd >= (180f + diagonalAngle)) {
            if(circleStart > (180f + diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(270f - circleStart));
                vertices[((((offset + 5) * 3) + 2) * 5) + 1] = -tan * 0.5f;
                vertices[((((offset + 5) * 3) + 2) * 5) + 4] = (((tan / length)) * 0.5f) + 0.5f;
            }
            if(circleEnd < 270f) {
                float tan = (float)Math.tan(Math.toRadians(270f - circleEnd));
                vertices[((((offset + 5) * 3) + 1) * 5) + 1] = -tan * 0.5f;
                vertices[((((offset + 5) * 3) + 1) * 5) + 4] = (((tan / length)) * 0.5f) + 0.5f;
            }
        }
        else
            hideTriangle(offset + 5);

        // tl2
        if(circleStart <= (360f - diagonalAngle) && circleEnd >= 270f) {
            if(circleStart > 270f) {
                float tan = (float)Math.tan(Math.toRadians(circleStart - 270f));
                vertices[((((offset + 6) * 3) + 2) * 5) + 1] = tan * 0.5f;
                vertices[((((offset + 6) * 3) + 2) * 5) + 4] = (1f - (tan / length)) * 0.5f;
            }
            if(circleEnd < (360f - diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(circleEnd - 270f));
                vertices[((((offset + 6) * 3) + 1) * 5) + 1] = tan * 0.5f;
                vertices[((((offset + 6) * 3) + 1) * 5) + 4] = (1f - (tan / length)) * 0.5f;
            }
        }
        else
            hideTriangle(offset + 6);

        // tl1
        if(circleStart <= 360f && circleEnd >= (360f - diagonalAngle)) {
            if(circleStart > (360f - diagonalAngle)) {
                float tan = (float)Math.tan(Math.toRadians(360f - circleStart));
                vertices[((((offset + 7) * 3) + 2) * 5) + 0] = -tan * halfLength;
                vertices[((((offset + 7) * 3) + 2) * 5) + 3] = 0.5f - (tan * halfLength);
            }
            if(circleEnd < 360f) {
                float tan = (float)Math.tan(Math.toRadians(360f - circleEnd));
                vertices[((((offset + 7) * 3) + 1) * 5) + 0] = -tan * halfLength;
                vertices[((((offset + 7) * 3) + 1) * 5) + 3] = 0.5f - (tan * halfLength);
            }
        }
        else
            hideTriangle(offset + 7);

    }

    public void show(float circleStart, float circleEnd) {
        // Reset
        replace(verticesReset);

        // Update
        this.circleStart = circleStart;
        this.circleEnd = circleEnd;
        if(circleEnd != 0f) {
            circleEnd %= 360f;
            if(circleEnd < 0f)
                circleEnd += 360f;
            else if(circleEnd == 0f)
                circleEnd = 360f;
        }
        if(circleStart != 0f) {
            circleStart %= 360f;
            if(circleStart < 0f)
                circleStart += 360f;
        }
        if(circleEnd < circleStart) {
            updateTriangles(0, circleStart, 360f);
            updateTriangles(8, 0f, circleEnd);
        }
        else {
            updateTriangles(0, circleStart, circleEnd);
            for(int c = 0; c < 8; c++)
                hideTriangle(8 + c);
        }

        upload();
    }

    public boolean isShowingFull() {
        return circleStart == 0f && circleEnd == 360f;
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

    public CircularSprite(String filename) {
        this(Material.load(filename));
    }

    public CircularSprite(Material material) {
        super(8 * 3 * 2, 8 * 3 * 2);

        setMaterial(material);
    }

    @Override
    public void setMaterial(Material mat, MaterialAttribute[] attribs) {
        super.setMaterial(mat, attribs);

        setLength(mat.getLength());
    }

    @Override
    public float getLength() {
        return length;
    }
}

package sengine.graphics2d;

import sengine.graphics2d.Matrices.ScissorBox;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public abstract class MaterialConfiguration {
	
	/**
	 * Converts a 4x4 matrix to 4 vec3 vectors
	 */
	public static void setTransformVectors(float[] vectors, int index, Matrix4 matrix) {
		index *= 12;
		
		vectors[index++] = matrix.val[Matrix4.M00];
		vectors[index++] = matrix.val[Matrix4.M10];
		vectors[index++] = matrix.val[Matrix4.M20];
		vectors[index++] = matrix.val[Matrix4.M03];		// translation
		vectors[index++] = matrix.val[Matrix4.M01];
		vectors[index++] = matrix.val[Matrix4.M11];
		vectors[index++] = matrix.val[Matrix4.M21];
		vectors[index++] = matrix.val[Matrix4.M13];		// translation
		vectors[index++] = matrix.val[Matrix4.M02];
		vectors[index++] = matrix.val[Matrix4.M12];
		vectors[index++] = matrix.val[Matrix4.M22];
		vectors[index++] = matrix.val[Matrix4.M23];		// translation
	}
	
	public static final String u_modelMatrix = "u_modelMatrix";
	public static final String u_viewMatrix = "u_viewMatrix";
	public static final String u_projectionMatrix = "u_projectionMatrix";
	public static final String u_MVMatrix = "u_MVMatrix";
	public static final String u_VPMatrix = "u_VPMatrix";
	public static final String u_MVPMatrix = "u_MVPMatrix";
	public static final String u_MPMatrix = "u_MPMatrix";
	public static final String u_cameraDirection = "u_cameraDirection";
	public static final String u_cameraPosition = "u_cameraPosition";
	
	public static final Matrix4 temp = new Matrix4();
	
	// Camera matrix calculation optimization
	public static ShaderProgram prevProgram = null;
	public static Camera prevCamera = null;

	// Material
	public Material material = null;
	public Object bindObject = null;
	// Position
	public final Matrix4 modelMatrix = new Matrix4();
	public Camera camera = null;
	public int target = 0;
	// Scissor
	public final ScissorBox scissor = new ScissorBox();
	
	// Floats
	public float[] floats = new float[0];
	public String[] floatNames = new String[0];
	public int floatIdx = 0;
	// Vector2
	public Vector2[] vec2 = new Vector2[0];
	public String[] vec2Names = new String[0];
	public int vec2Idx = 0;
	// Vector3
	public Vector3[] vec3 = new Vector3[0];
	public String[] vec3Names = new String[0];
	public int vec3Idx = 0;
	// Color
	public Color[] colors = new Color[0];
	public String[] colorNames = new String[0];
	public int colorIdx = 0;
	// Matrix4
	public Matrix4[] mat4 = new Matrix4[0];
	public String[] mat4Names = new String[0];
	public int mat4Idx = 0;
	// Float array
	public float[][] floatArrays = new float[0][];
	public String[] floatArrayNames = new String[0];
	public int floatArraysIdx = 0;
	// Vector2 array
	public float[][] vec2Arrays = new float[0][];
	public String[] vec2ArrayNames = new String[0];
	public int vec2ArraysIdx = 0;
	// Vector3 array
	public float[][] vec3Arrays = new float[0][];
	public String[] vec3ArrayNames = new String[0];
	public int vec3ArraysIdx = 0;
	// Vector4 array
	public float[][] vec4Arrays = new float[0][];
	public String[] vec4ArrayNames = new String[0];
	public int vec4ArraysIdx = 0;
	// Matrix4 array
	public float[][] mat4Arrays = new float[0][];
	public String[] mat4ArrayNames = new String[0];
	public int mat4ArraysIdx = 0;
	
	
	public void apply(Shader shader) {
		ShaderProgram program = shader.getProgram();

		// Upload
		// u_modelMatrix
		int location = program.fetchUniformLocation(u_modelMatrix, false);
		if(location != -1)
			program.setUniformMatrix(location, modelMatrix);
		// u_MVMatrix
		location = program.fetchUniformLocation(u_MVMatrix, false);
		if(location != -1) {
			temp.set(camera.view);
			temp.mul(modelMatrix);
			program.setUniformMatrix(location, temp);
		}
		// u_MVPMatrix
		location = program.fetchUniformLocation(u_MVPMatrix, false);
		if(location != -1) {
			temp.set(camera.combined);
			temp.mul(modelMatrix);
			program.setUniformMatrix(location, temp);
		}
		// u_MPMatrix
		location = program.fetchUniformLocation(u_MPMatrix, false);
		if(location != -1) {
			temp.set(camera.projection);
			temp.mul(modelMatrix);
			program.setUniformMatrix(location, temp);
		}
		// Calculate and upload camera dependent matrices 
		if(program != prevProgram || camera != prevCamera) {
			// u_viewMatrix
			location = program.fetchUniformLocation(u_viewMatrix, false);
			if(location != -1)
				program.setUniformMatrix(location, camera.view);
			// u_projectionMatrix
			location = program.fetchUniformLocation(u_projectionMatrix, false);
			if(location != -1)
				program.setUniformMatrix(location, camera.projection);
			// u_VPMatrix
			location = program.fetchUniformLocation(u_VPMatrix, false);
			if(location != -1)
				program.setUniformMatrix(location, camera.combined);
			// u_cameraDirection
			location = program.fetchUniformLocation(u_cameraDirection, false);
			if(location != -1)
				program.setUniformf(location, camera.direction);
			// u_cameraPosition
			location = program.fetchUniformLocation(u_cameraPosition, false);
			if(location != -1)
				program.setUniformf(location, camera.position);
			// Remember
			prevProgram = program;
			prevCamera = camera;
		}
		// float
		for(int c = 0; c < floatIdx; c++) {
			location = program.fetchUniformLocation(floatNames[c], false);
			if(location == -1)
				continue;
			program.setUniformf(location, floats[c]);
		}
		// vec2
		for(int c = 0; c < vec2Idx; c++) {
			location = program.fetchUniformLocation(vec2Names[c], false);
			if(location == -1)
				continue;
			program.setUniformf(location, vec2[c]);
		}
		// vec3
		for(int c = 0; c < vec3Idx; c++) {
			location = program.fetchUniformLocation(vec3Names[c], false);
			if(location == -1)
				continue;
			program.setUniformf(location, vec3[c]);
		}
		// vec4
		for(int c = 0; c < colorIdx; c++) {
			location = program.fetchUniformLocation(colorNames[c], false);
			if(location == -1)
				continue;
			program.setUniformf(location, colors[c]);
		}
		// mat4
		for(int c = 0; c < mat4Idx; c++) {
			location = program.fetchUniformLocation(mat4Names[c], false);
			if(location == -1)
				continue;
			program.setUniformMatrix(location, mat4[c]);
		}
		// float array
		for(int c = 0; c < floatArraysIdx; c++) {
			location = program.fetchUniformLocation(floatArrayNames[c], false);
			if(location == -1)
				continue;
			program.setUniform1fv(location, floatArrays[c], 0, floatArrays[c].length);
		}
		// vec2 array
		for(int c = 0; c < vec2ArraysIdx; c++) {
			location = program.fetchUniformLocation(vec2ArrayNames[c], false);
			if(location == -1)
				continue;
			program.setUniform2fv(location, vec2Arrays[c], 0, vec2Arrays[c].length);
		}
		// vec3 array
		for(int c = 0; c < vec3ArraysIdx; c++) {
			location = program.fetchUniformLocation(vec3ArrayNames[c], false);
			if(location == -1)
				continue;
			program.setUniform3fv(location, vec3Arrays[c], 0, vec3Arrays[c].length);
		}
		// vec4 array
		for(int c = 0; c < vec4ArraysIdx; c++) {
			location = program.fetchUniformLocation(vec4ArrayNames[c], false);
			if(location == -1)
				continue;
			program.setUniform4fv(location, vec4Arrays[c], 0, vec4Arrays[c].length);
		}
		// mat4 array
		for(int c = 0; c < mat4ArraysIdx; c++) {
			location = program.fetchUniformLocation(mat4ArrayNames[c], false);
			if(location == -1)
				continue;
			program.setUniformMatrix4fv(location, mat4Arrays[c], 0, mat4Arrays[c].length);
		}
	}

	public Matrix4 getMatrix4(String name) {
		for(int c = 0; c < mat4Idx; c++) {
			if(mat4Names[c] == name || mat4Names[c].contentEquals(name))
				return mat4[c];
		}
		return null;
	}

	public void setMatrix4(String name, Matrix4 value) {
		if(mat4Idx == mat4.length) {
			Matrix4[] values = new Matrix4[mat4.length + 1];
			String[] names = new String[mat4.length + 1];
			System.arraycopy(mat4, 0, values, 0, mat4.length);
			System.arraycopy(mat4Names, 0, names, 0, mat4.length);
			values[mat4.length] = new Matrix4();
			mat4 = values;
			mat4Names = names;
		}
		mat4[mat4Idx].set(value);
		mat4Names[mat4Idx] = name;
		mat4Idx++;
	}

	public float getFloat(String name, float defaultValue) {
		for(int c = 0; c < floatIdx; c++) {
			if(floatNames[c] == name || floatNames[c].contentEquals(name))
				return floats[c];
		}
		return defaultValue;
	}

	public void setFloat(String name, float value) {
		if(floatIdx == floats.length) {
			float[] values = new float[floats.length + 1];
			String[] names = new String[floats.length + 1];
			System.arraycopy(floats, 0, values, 0, floats.length);
			System.arraycopy(floatNames, 0, names, 0, floats.length);
			floats = values;
			floatNames = names;
		}
		floats[floatIdx] = value;
		floatNames[floatIdx] = name;
		floatIdx++;
	}

	public Vector2 getVector2(String name) {
		for(int c = 0; c < vec2Idx; c++) {
			if(vec2Names[c] == name || vec2Names[c].contentEquals(name))
				return vec2[c];
		}
		return null;
	}

	public void setVector2(String name, Vector2 value) {
		if(vec2Idx == vec2.length) {
			Vector2[] values = new Vector2[vec2.length + 1];
			String[] names = new String[vec2.length + 1];
			System.arraycopy(vec2, 0, values, 0, vec2.length);
			System.arraycopy(vec2Names, 0, names, 0, vec2.length);
			values[vec2.length] = new Vector2();
			vec2 = values;
			vec2Names = names;
		}
		vec2[vec2Idx].set(value);
		vec2Names[vec2Idx] = name;
		vec2Idx++;
	}

	public Vector3 getVector3(String name) {
		for(int c = 0; c < vec3Idx; c++) {
			if(vec3Names[c] == name || vec3Names[c].contentEquals(name))
				return vec3[c];
		}
		return null;
	}

	public void setVector3(String name, Vector3 value) {
		if(vec3Idx == vec3.length) {
			Vector3[] values = new Vector3[vec3.length + 1];
			String[] names = new String[vec3.length + 1];
			System.arraycopy(vec3, 0, values, 0, vec3.length);
			System.arraycopy(vec3Names, 0, names, 0, vec3.length);
			values[vec3.length] = new Vector3();
			vec3 = values;
			vec3Names = names;
		}
		vec3[vec3Idx].set(value);
		vec3Names[vec3Idx] = name;
		vec3Idx++;
	}

	public Color getColor(String name) {
		for(int c = 0; c < colorIdx; c++) {
			if(colorNames[c] == name || colorNames[c].contentEquals(name))
				return colors[c];
		}
		return null;
	}

	public void setColor(String name, Color value) {
		if(colorIdx == colors.length) {
			Color[] values = new Color[colors.length + 1];
			String[] names = new String[colors.length + 1];
			System.arraycopy(colors, 0, values, 0, colors.length);
			System.arraycopy(colorNames, 0, names, 0, colors.length);
			values[colors.length] = new Color();
			colors = values;
			colorNames = names;
		}
		colors[colorIdx].set(value);
		colorNames[colorIdx] = name;
		colorIdx++;
	}

	public float[] getFloatArray(String name) {
		for(int c = 0; c < floatArraysIdx; c++) {
			if(floatArrayNames[c] == name || floatArrayNames[c].contentEquals(name))
				return floatArrays[c];
		}
		return null;
	}

	public void setFloatArray(String name, float[] value) {
		if(floatArraysIdx == floatArrays.length) {
			float[][] values = new float[floatArrays.length + 1][];
			String[] names = new String[floatArrays.length + 1];
			System.arraycopy(floatArrays, 0, values, 0, floatArrays.length);
			System.arraycopy(floatArrayNames, 0, names, 0, floatArrays.length);
			floatArrays = values;
			floatArrayNames = names;
		}
		floatArrays[floatArraysIdx] = value;
		floatArrayNames[floatArraysIdx] = name;
		floatArraysIdx++;
	}

	public float[] getVector2Array(String name) {
		for(int c = 0; c < vec2ArraysIdx; c++) {
			if(vec2ArrayNames[c] == name || vec2ArrayNames[c].contentEquals(name))
				return vec2Arrays[c];
		}
		return null;
	}

	public void setVector2Array(String name, float[] value) {
		if(vec2ArraysIdx == vec2Arrays.length) {
			float[][] values = new float[vec2Arrays.length + 1][];
			String[] names = new String[vec2Arrays.length + 1];
			System.arraycopy(vec2Arrays, 0, values, 0, vec2Arrays.length);
			System.arraycopy(vec2ArrayNames, 0, names, 0, vec2Arrays.length);
			vec2Arrays = values;
			vec2ArrayNames = names;
		}
		vec2Arrays[vec2ArraysIdx] = value;
		vec2ArrayNames[vec2ArraysIdx] = name;
		vec2ArraysIdx++;
	}

	public float[] getVector3Array(String name) {
		for(int c = 0; c < vec3ArraysIdx; c++) {
			if(vec3Names[c] == name || vec3ArrayNames[c].contentEquals(name))
				return vec3Arrays[c];
		}
		return null;
	}

	public void setVector3Array(String name, float[] value) {
		if(vec3ArraysIdx == vec3Arrays.length) {
			float[][] values = new float[vec3Arrays.length + 1][];
			String[] names = new String[vec3Arrays.length + 1];
			System.arraycopy(vec3Arrays, 0, values, 0, vec3Arrays.length);
			System.arraycopy(vec3ArrayNames, 0, names, 0, vec3Arrays.length);
			vec3Arrays = values;
			vec3ArrayNames = names;
		}
		vec3Arrays[vec3ArraysIdx] = value;
		vec3ArrayNames[vec3ArraysIdx] = name;
		vec3ArraysIdx++;
	}

	public float[] getVector4Array(String name) {
		for(int c = 0; c < vec4ArraysIdx; c++) {
			if(vec4ArrayNames[c] == name || vec4ArrayNames[c].contentEquals(name))
				return vec4Arrays[c];
		}
		return null;
	}

	public void setVector4Array(String name, float[] value) {
		if(vec4ArraysIdx == vec4Arrays.length) {
			float[][] values = new float[vec4Arrays.length + 1][];
			String[] names = new String[vec4Arrays.length + 1];
			System.arraycopy(vec4Arrays, 0, values, 0, vec4Arrays.length);
			System.arraycopy(vec4ArrayNames, 0, names, 0, vec4Arrays.length);
			vec4Arrays = values;
			vec4ArrayNames = names;
		}
		vec4Arrays[vec4ArraysIdx] = value;
		vec4ArrayNames[vec4ArraysIdx] = name;
		vec4ArraysIdx++;
	}

	public float[] getMatrix4Array(String name) {
		for(int c = 0; c < mat4ArraysIdx; c++) {
			if(mat4ArrayNames[c] == name || mat4ArrayNames[c].contentEquals(name))
				return mat4Arrays[c];
		}
		return null;
	}

	public void setMatrix4Array(String name, float[] value) {
		if(mat4ArraysIdx == mat4Arrays.length) {
			float[][] values = new float[mat4Arrays.length + 1][];
			String[] names = new String[mat4Arrays.length + 1];
			System.arraycopy(mat4Arrays, 0, values, 0, mat4Arrays.length);
			System.arraycopy(mat4ArrayNames, 0, names, 0, mat4Arrays.length);
			mat4Arrays = values;
			mat4ArrayNames = names;
		}
		mat4Arrays[mat4ArraysIdx] = value;
		mat4ArrayNames[mat4ArraysIdx] = name;
		mat4ArraysIdx++;
	}
	
	public void clear() {
		floatIdx = 0;
		vec2Idx = 0;
		vec3Idx = 0;
		colorIdx = 0;
		mat4Idx = 0;
		floatArraysIdx = 0;
		vec2ArraysIdx = 0;
		vec3ArraysIdx = 0;
		vec4ArraysIdx = 0;
		mat4ArraysIdx = 0;
		material = null;
		bindObject = null;
		camera = null;
		target = 0;
	}

	public abstract void render(Shader shader);
	public abstract void bind(Shader shader);
	public abstract void unbind(Shader shader);
}

package sengine.graphics2d;

import sengine.Pool;
import sengine.Sys;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;

public class Matrices {

	public static class ScissorBox {
		static final BoundingBox scissorBounds = new BoundingBox();
		static final Vector3 vec1 = new Vector3();
		static final Matrix4 mat1 = new Matrix4();
		
		public float x;
		public float y;
		public float width;
		public float height;
        public float screenLength;
		
		public ScissorBox() {
			inf();
		}
		
		public void inf() {
			x = 0f;
			y = 0f;
			width = Float.MAX_VALUE;
			height = Float.MAX_VALUE;
		}
		
		public boolean isInfinite() {
			return width == Float.MAX_VALUE && height == Float.MAX_VALUE;
		}
		
		public boolean isZero() {
			return width <= 0f || height <= 0f;
		}
		
		public boolean contains(float xt, float yt) {
			return Math.abs(xt - x) <= Math.abs(width / 2f) && Math.abs(yt - y) <= Math.abs(height / 2f);
		}
		
		public boolean contains(float regionX, float regionY, float regionWidth, float regionHeight) {
			return contains(regionX - (regionWidth / 2f), regionY + (regionHeight / 2f)) && 
				contains(regionX + (regionWidth / 2f), regionY + (regionHeight / 2f)) &&
				contains(regionX + (regionWidth / 2f), regionY - (regionHeight / 2f)) &&
				contains(regionX - (regionWidth / 2f), regionY - (regionHeight / 2f))
			;
		}

		public boolean overlaps(float regionX, float regionY, float regionWidth, float regionHeight) {
            float widthHalf = width / 2f;
            float heightHalf = height / 2f;
            float regionWidthHalf = regionWidth / 2f;
            float regionHeightHalf = regionHeight / 2f;

			return x - widthHalf < regionX + regionWidthHalf &&         // left r1 is less than right r2
                    x + widthHalf > regionX - regionWidthHalf &&        // right r1 is more than left r2
                    y - heightHalf < regionY + regionHeightHalf &&      // bottom r1 is less than top r2
                    y + heightHalf > regionY - regionHeightHalf;         // top r1 is more than bottom r2
		}

		public boolean contentEquals(ScissorBox scissor) {
			return x == scissor.x && y == scissor.y && width == scissor.width && height == scissor.height;
		}
		
		public void set(ScissorBox scissor) {
			x = scissor.x;
			y = scissor.y;
			width = scissor.width;
			height = scissor.height;
		}
		
		public void set(float x, float y, float width, float height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public void mul(ScissorBox otherScissor) {
			mul(otherScissor.x, otherScissor.y, otherScissor.width, otherScissor.height);
		}
		
		public void mul(float ox, float oy, float owidth, float oheight) {
			float left = x - Math.abs(width / 2f);
			float right = x + Math.abs(width / 2f);
			float top = y + Math.abs(height / 2f);
			float bottom = y - Math.abs(height / 2f);
			
			float otherLeft = ox - Math.abs(owidth / 2f);
			float otherRight = ox + Math.abs(owidth / 2f);
			float otherTop = oy + Math.abs(oheight / 2f);
			float otherBottom = oy - Math.abs(oheight / 2f);
			if(left < otherLeft)
				left = otherLeft; 
			if(right > otherRight)
				right = otherRight;
			if(top > otherTop)
				top = otherTop;
			if(bottom < otherBottom)
				bottom = otherBottom;
			
			set((left + right) / 2f, (top + bottom) / 2f, right - left, top - bottom);
		}
		
		public void set(float x, float y, float z, float width, float height, float depth) {
			width = Math.abs(width) / 2f;
			height = Math.abs(height) / 2f;
			depth = Math.abs(depth) / 2f;
			// Calculate combined matrix
			mat1.set(camera.combined).mul(model);
			// Calculate scissor bounds
			float x0 = x - width, y0 = y - height, z0 = z - depth, x1 = x + width, y1 = y + height, z1 = z + depth;
			scissorBounds.inf();
			scissorBounds.ext(vec1.set(x0, y0, z0).mul(mat1));
			scissorBounds.ext(vec1.set(x0, y0, z1).mul(mat1));
			scissorBounds.ext(vec1.set(x0, y1, z0).mul(mat1));
			scissorBounds.ext(vec1.set(x0, y1, z1).mul(mat1));
			scissorBounds.ext(vec1.set(x1, y0, z0).mul(mat1));
			scissorBounds.ext(vec1.set(x1, y0, z1).mul(mat1));
			scissorBounds.ext(vec1.set(x1, y1, z0).mul(mat1));
			scissorBounds.ext(vec1.set(x1, y1, z1).mul(mat1));
			// Get origin
			scissorBounds.getCenter(vec1);
			this.x = (vec1.x + 1f) / 2f;
			this.y = (vec1.y + 1f) / 2f * camera.viewportHeight;
			// Get dimensions
			scissorBounds.getDimensions(vec1);
			// Set scissor
			this.width = vec1.x / 2f;
			this.height = vec1.y / 2f * camera.viewportHeight;
		}
		
		void apply(GL20 gl, int screenWidth, int screenHeight, float renderLength) {
			int ix = Math.round((x - (width / 2.0f)) * screenWidth);
			int iy = Math.round((y - (height / 2.0f)) / renderLength * screenHeight);
			int iwidth = Math.round(width * screenWidth);
			int iheight = Math.round(height / renderLength * screenHeight);
			if(ix < 0) {
				iwidth += ix;
				ix = 0;
			}
			if(iy < 0) {
				iheight += iy;
				iy = 0;
			}

			if(ix < 0)
				ix = 0;
			else if(ix > screenWidth)
				ix = screenWidth;
			if(iy < 0)
				iy = 0;
			else if(iy > screenHeight)
				iy = screenHeight;
			if(iwidth < 0)
				iwidth = 0;
			else if(iwidth > screenWidth)
				iwidth = screenWidth;
			if(iheight < 0)
				iheight = 0;
			else if(iheight > screenHeight)
				iheight = screenHeight;

			gl.glScissor(ix, iy, iwidth, iheight);
		}
	}
	
	// Scissors
	static final Array<ScissorBox> scissorStack = new Array<ScissorBox>(ScissorBox.class);
	public static final ScissorBox scissor = new ScissorBox();
	
	// Camera matrices
	public static final Matrix4 model = new Matrix4();
	public static Camera camera = null;
	private static final Matrix4 shearMatrix = new Matrix4();
	// Stack
	static final Array<Matrix4> stack = new Array<Matrix4>(Matrix4.class);
	static final Array<Camera> cameraStack = new Array<Camera>(Camera.class);

	// Target
	public static int target = Renderer.TARGET_MATERIAL_SORTED;
    static final IntArray targetStack = new IntArray();


	public static final Pool<ScissorBox> scissorsPool = new Pool<ScissorBox>() {
		@Override
		protected ScissorBox newObject() {
			return new ScissorBox();
		}
	};
	public static final Pool<Vector2> vec2Pool = new Pool<Vector2>() {
		@Override
		protected Vector2 newObject() {
			return new Vector2();
		}
	};
	public static final Pool<Vector3> vec3Pool = new Pool<Vector3>() {
		@Override
		protected Vector3 newObject() {
			return new Vector3();
		}
	};
	public static final Pool<Color> colorPool = new Pool<Color>() {
		@Override
		protected Color newObject() {
			return new Color();
		}
	};
	public static final Pool<Quaternion> quatPool = new Pool<Quaternion>() {
		@Override
		protected Quaternion newObject() {
			return new Quaternion();
		}
	};
	public static final Pool<Matrix3> mat3Pool = new Pool<Matrix3>() {
		@Override
		protected Matrix3 newObject() {
			return new Matrix3();
		}
	};
	public static final Pool<Matrix4> mat4Pool = new Pool<Matrix4>() {
		@Override
		protected Matrix4 newObject() {
			return new Matrix4();
		}
	};

	public static void shear(float sx, float sy) {
        shearMatrix.val[Matrix4.M01] = sx;
        shearMatrix.val[Matrix4.M10] = sy;
        model.mul(shearMatrix);
	}

	public static void billboard(boolean x, boolean y, boolean z) {
		if(x) {
			float xx = model.val[Matrix4.M00];
			float xy = model.val[Matrix4.M01];
			float xz = model.val[Matrix4.M02];
			model.val[Matrix4.M00] = (float) Math.sqrt((xx * xx) + (xy * xy) + (xz * xz));
			model.val[Matrix4.M01] = 0;
			model.val[Matrix4.M02] = 0;
		}
		if(y) {
			float yx = model.val[Matrix4.M10];
			float yy = model.val[Matrix4.M11];
			float yz = model.val[Matrix4.M12];
			model.val[Matrix4.M10] = 0;
			model.val[Matrix4.M11] = (float) Math.sqrt((yx * yx) + (yy * yy) + (yz * yz));
			model.val[Matrix4.M12] = 0;
		}
		if(z) {
			float zx = model.val[Matrix4.M20];
			float zy = model.val[Matrix4.M21];
			float zz = model.val[Matrix4.M22];
			model.val[Matrix4.M20] = 0;
			model.val[Matrix4.M21] = 0;
			model.val[Matrix4.M22] = (float) Math.sqrt((zx * zx) + (zy * zy) + (zz * zz));
		}
	}
	
	public static void clampToBounds(Vector3 vec, BoundingBox bb) {
		vec.x = vec.x < bb.min.x ? bb.min.x : vec.x;
		vec.y = vec.y < bb.min.y ? bb.min.y : vec.y;
		vec.z = vec.z < bb.min.z ? bb.min.z : vec.z;
		vec.x = vec.x > bb.max.x ? bb.max.x : vec.x;
		vec.y = vec.y > bb.max.y ? bb.max.y : vec.y;
		vec.z = vec.z > bb.max.z ? bb.max.z : vec.z;
	}
	
	public static float calculateAngle(float x, float y, float defaultAngle) {
		float angle = (float) Math.toDegrees(Math.atan2(y, x));
		if(Float.isNaN(angle))
			return defaultAngle;
		angle += 90.0f;
		return (angle + 360.0f) % 360.0f;
	}
	
	public static float limitDegrees(float degrees) {
		if(degrees > 0f)
			return degrees % 360f;
		else
			return 360f + (degrees % 360f);
	}
	
	public static void reset() {
		model.idt();
		stack.clear();
		
		cameraStack.clear();
		camera = null;
		
		scissor.inf();
		scissorStack.clear();
		scissorsPool.clear();

        target = Renderer.TARGET_MATERIAL_SORTED;
        targetStack.clear();
		
		vec2Pool.clear();
		vec3Pool.clear();
		colorPool.clear();
		quatPool.clear();
		mat3Pool.clear();
		mat4Pool.clear();
	}
	
	public static void push() {
		// Model matrix
		Matrix4 m = mat4Pool.obtain();
		m.set(model);
		stack.add(m);
		// Scissor
		ScissorBox s = scissorsPool.obtain();
		s.set(scissor);
		scissorStack.add(s);
		// Camera
		cameraStack.add(camera);
        // Target
        targetStack.add(target);
	}
	
	public static void pop() {
		// Model matrix
		Matrix4 m = stack.pop();
		model.set(m);
		mat4Pool.free(m);
		// Scissor
		ScissorBox s = scissorStack.pop();
		scissor.set(s);
		scissorsPool.free(s);
		// Camera
		camera = cameraStack.pop();
        // Target
        target = targetStack.pop();
	}

	private static final ModelView modelAnimatable = new ModelView();
	
	public static Animatable2D getModelMatrixAnimator(float length, Animatable atributes) {
		modelAnimatable.length = length;
		modelAnimatable.attributes = atributes;
		return modelAnimatable;
 	}
	
	private static class ModelView implements Animatable2D {
		float length = 1.0f;
		Animatable attributes = null;
		
		@Override
		public void translate(float x, float y) {
			Matrices.model.translate(x, y, 0.0f);
		}
		
		@Override
		public void rotate(float rotate) {
			Matrices.model.rotate(0, 0, -1, rotate);
		}
		
		@Override
		public void scale(float x, float y) {
			Matrices.model.scale(x, y, 1.0f);
		}

        @Override
        public void shear(float sx, float sy) {
            Matrices.shear(sx, sy);
        }

        @Override
		public void scissor(float x, float y, float width, float height) {
			Matrices.scissor.set(x, y, width, height);
		}
		
		@Override
		public void applyGlobalMatrix() {
			// no special matrix to apply
		}
		
		@Override
		public float getLength() {
			return length;		// default
		}

		@Override
		public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer) {
			if(attributes != null)
				return attributes.getAttribute(attribType, layer);
			else 
				return null;
		}
	}
}

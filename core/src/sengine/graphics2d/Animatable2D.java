package sengine.graphics2d;

public interface Animatable2D extends Animatable {
	// Object metrics
	float getLength();
	// Matrix transformation, affects globally
	void translate(float x, float y);
	void rotate(float rotate);
	void scale(float x, float y);
    void shear(float sx, float sy);
    void scissor(float x, float y, float width, float height);

	void applyGlobalMatrix();
}

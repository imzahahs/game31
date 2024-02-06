package sengine.animation;

import com.badlogic.gdx.graphics.Color;

import sengine.calc.ConstantGraph;
import sengine.calc.Graph;
import sengine.graphics2d.Animatable2D;
import sengine.mass.MassSerializable;
import sengine.materials.ColorAttribute;

public class ColorAnim extends Animation implements MassSerializable {
	
	private final Graph r;
    private final Graph g;
    private final Graph b;
    private final Graph a;
    private final boolean multiply;
    private final int layer;

	public ColorAnim(int rgba) {
		this(rgba, true);
	}

	public ColorAnim(int rgba, boolean multiply) {
		this(1f, rgba, multiply);
	}

    public ColorAnim(float length, int rgba, boolean multiply) {
        this(length, rgba, multiply, 0);
    }

	public ColorAnim(float length, int rgba, boolean multiply, int layer) {
		super(length);

		Color color = new Color();
		color.set(rgba);

		this.r = new ConstantGraph(color.r);
		this.g = new ConstantGraph(color.g);
		this.b = new ConstantGraph(color.b);
		this.a = new ConstantGraph(color.a);
		this.multiply = multiply;
        this.layer = layer;
	}

	public ColorAnim(Color color) {
		this(color, true);
	}

	public ColorAnim(Color color, boolean multiply) {
		this(1f, color, multiply);
	}

	public ColorAnim(float length, Color color, boolean multiply) {
		this(length, color.r, color.g, color.b, color.a, multiply);
	}

	public ColorAnim(float r, float g, float b, float a) {
		this(r, g, b, a, true);
	}

	public ColorAnim(float r, float g, float b, float a, boolean multiply) {
		this(r,g,b,a, multiply, 0);
	}

    public ColorAnim(float r, float g, float b, float a, boolean multiply, int layer) {
        this(1f, r,g,b,a, multiply, layer);
    }

    public ColorAnim(float length, float r, float g, float b, float a, boolean multiply) {
        this(length, r, g, b, a, multiply, 0);
    }

	public ColorAnim(float length, float r, float g, float b, float a, boolean multiply, int layer) {
		super(length);

		this.r = new ConstantGraph(r);
		this.g = new ConstantGraph(g);
		this.b = new ConstantGraph(b);
		this.a = new ConstantGraph(a);
		this.multiply = multiply;
        this.layer = layer;
	}

	public ColorAnim(float length, Graph rgb, Graph a) {
		this(length, rgb, a, true);
	}

    public ColorAnim(float length, Graph rgb, Graph a, boolean multiply) {
        this(length, rgb, a, multiply, 0);
    }

	public ColorAnim(float length, Graph rgb, Graph a, boolean multiply, int layer) {
		super(length);
		
		this.r = rgb;
		this.g = rgb;
		this.b = rgb;
		this.a = a;
		this.multiply = multiply;
        this.layer = layer;
	}

	public ColorAnim(float length, Graph r, Graph g, Graph b, Graph a) {
		this(length, r, g, b, a, true);
	}

    public ColorAnim(float length, Graph r, Graph g, Graph b, Graph a, boolean multiply) {
        this(length, r, g, b, a, multiply, 0);
    }

	@MassConstructor
	public ColorAnim(float length, Graph r, Graph g, Graph b, Graph a, boolean multiply, int layer) {
		super(length);
		
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		this.multiply = multiply;
        this.layer = layer;
	}

	@Override
	public Object[] mass() {
		return new Object[] { length, r, g, b, a, multiply, layer };
	}

	@Override
	public void apply(float renderTime, float progress, Animatable2D animatable) {
		ColorAttribute color = animatable.getAttribute(ColorAttribute.class, layer);
		if(color == null)
			return;		// Color attribute not supported

		if(multiply) {
			if (r != null)
				color.current.r *= r.generate(progress);
			if (g != null)
				color.current.g *= g.generate(progress);
			if (b != null)
				color.current.b *= b.generate(progress);
			if (a != null)
				color.current.a *= a.generate(progress);
		}
		else {
			if (r != null)
				color.current.r = r.generate(progress);
			if (g != null)
				color.current.g = g.generate(progress);
			if (b != null)
				color.current.b = b.generate(progress);
			if (a != null)
				color.current.a = a.generate(progress);
		}
	}
}

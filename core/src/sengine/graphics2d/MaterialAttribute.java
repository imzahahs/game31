package sengine.graphics2d;

public abstract class MaterialAttribute {
	
	
	protected abstract void configure(MaterialConfiguration config);
	protected abstract void copy(MaterialAttribute from);
}

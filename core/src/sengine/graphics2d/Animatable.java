package sengine.graphics2d;

public interface Animatable {
	
	public <T extends MaterialAttribute> T getAttribute(Class<T> attribType, int layer);

}

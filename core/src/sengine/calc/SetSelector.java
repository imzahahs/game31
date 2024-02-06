package sengine.calc;

public abstract class SetSelector<T> {
	
	public final T set[];
	
	public SetSelector(T ... set) {
		if(set.length == 0)
			throw new IllegalArgumentException("Set cannot be empty");
		this.set = set;
	}
	
	public abstract void reset();
	
	public abstract int selectIndex();
	
	public T select() {
		return set[selectIndex()];
	}
	
	public abstract SetSelector<T> instantiate();
}

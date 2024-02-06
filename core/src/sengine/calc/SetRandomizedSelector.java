package sengine.calc;

import sengine.mass.MassSerializable;

public class SetRandomizedSelector<T> extends SetSelector<T> implements MassSerializable {
	public static float iterationThreshold = 0.5f;

	boolean done[];
	boolean selector = false;
	int totalDone = 0;
	
	public static <T> T selectFrom(T ... set) {
		int index = (int) (Math.random() * set.length);
		if(index == set.length)
			index--;
		return set[index];
	}
	
	@MassConstructor
	public SetRandomizedSelector(T ... set) {
		super(set);
		this.done = new boolean[set.length]; 
	}
	@Override
	public Object[] mass() {
		return new Object[] { set };
	}
	
	@Override
	public int selectIndex() { 
		int index;
		// Find a random object from set
		if(((float)totalDone / (float)set.length) > iterationThreshold) {
			// Iterate instead of randomly selecting
			if(Math.random() < 0.5f) {
				for (index = 0; index < set.length; index++) {
					if (done[index] == selector)
						break;
				}
			}
			else {
				for (index = set.length - 1; index >= 0; index--) {
					if (done[index] == selector)
						break;
				}
			}
		}
		else {
			do {
				index = (int)(Math.round(Math.random() * set.length));
				if(index == set.length)
					index = set.length - 1;
			} while(done[index] != selector);
		}
		// Mark this object as done
		done[index] = !selector;
		// Increase total done
		totalDone++;
		// See if all objects have been exhausted, in which case invert selector
		if(totalDone == set.length) {
			selector = !selector;
			totalDone = 0;
		}
		// Return selected response
		return index;
	}

	@Override
	public SetSelector<T> instantiate() {
		return new SetRandomizedSelector<T>(this.set);
	}

	@Override
	public void reset() {
		// Reset selection probability
		selector = false;
		for(int c = 0; c < done.length; c++)
			done[c] = selector;
		totalDone = 0;
	}
}

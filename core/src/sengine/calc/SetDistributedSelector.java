package sengine.calc;

import sengine.mass.MassSerializable;

public class SetDistributedSelector<T> extends SetSelector<T> implements MassSerializable {

	public final float distribution[];
	public final float totalWeight;

	@MassConstructor
	public SetDistributedSelector(T[] set, float distribution[]) {
		super(set);
		if(set.length != distribution.length)
			throw new IllegalArgumentException("Array sizes mismatch");
		this.distribution = distribution;
		// Calculate totalWeight
		float totalWeight = 0.0f;
		for(float c : distribution)
			totalWeight += c;
		this.totalWeight = totalWeight;
	}
	@Override
	public Object[] mass() {
		return new Object[] { set, distribution };
	}

	private int selectIndex(float offset) {
		if(offset < 0.5f) {
			// Must be in the first half of the set
			offset *= totalWeight;
			int c = 0;
			while(c < set.length) {
				if(offset < distribution[c])
					break;
				offset -= distribution[c];
				c++;
			}
			return c;
		}
		// Else must be in the second half of the set
		offset = (1.0f - offset) * totalWeight;
		int c = set.length - 1;
		while(c >= 0) {
			if(offset < distribution[c])
				break;
			offset -= distribution[c];
			c--;
		}
		return c;
	}

	@Override
	public int selectIndex() {
		float offset = (float)Math.random();
		return selectIndex(offset);
	}


	public T selectOffset(float offset) {
        // Normalize
		if(offset > totalWeight)
			offset %= totalWeight;		// wrap
        offset /= totalWeight;
		return set[selectIndex(offset)];
	}


	@Override
	public SetSelector<T> instantiate() {
		return new SetDistributedSelector<T>(set, distribution);
	}

	@Override
	public void reset() {
		// No special handling, selections are always random
	}
}

package sengine;

import com.badlogic.gdx.utils.Array;

public class GarbageCollector {
	static final String TAG = "GarbageCollector";
	
	public interface Collectible {
		boolean performGC(boolean forced);
	}

	private static final Array<Collectible> collectibles = new Array<>(false, 1000, Collectible.class);
	private static int index = 0;

	public static synchronized void performSingleGC(boolean forced) {
		if(collectibles.size == 0)
			return;		// nothing to GC

		if(index >= collectibles.size)
			index = 0;
		Collectible c = collectibles.items[index];
		if(c.performGC(forced)) {
            if(collectibles.items[index] == c)
                collectibles.removeIndex(index);
        }
        else
			index++;
	}
	
	public static synchronized void performGC(boolean forced) {
		if(collectibles.size == 0)
			return;		// nothing to GC
		for(index = 0; index < collectibles.size; index++) {
			Collectible c = collectibles.items[index];
			if(c.performGC(forced)) {
                if(collectibles.items[index] == c)
                    collectibles.removeIndex(index);
				index--;
			}
		}
	}
	
	public static synchronized void add(Collectible c) {
		if(!collectibles.contains(c, true))
			collectibles.add(c);
	}

	public static synchronized void remove(Collectible c) {
		collectibles.removeValue(c, true);
	}
}

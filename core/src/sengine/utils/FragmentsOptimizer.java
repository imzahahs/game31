package sengine.utils;

public class FragmentsOptimizer {
	public static class DataProfile {
		public final float[] composition = new float[256];
		public final int dispersion;
		
		public DataProfile(byte[] data) {
			int dispersion = 0;
			// Profile data
			for(int idx = 0; idx < data.length; idx++) {
				int b = data[idx] + 128;
				if(composition[b] == 0)
					dispersion++;
				composition[b] += (1.0f / (float)data.length);
			}
			this.dispersion = dispersion;
		}
		
		public float compare(DataProfile profile) {
			float correlation = 0.0f;
			
			float cd = 255.0f / (float)dispersion;
			float td = 255.0f / profile.dispersion;

			// Compare bit composition
			for(int idx = 0; idx < composition.length; idx++)
				correlation += Math.min(composition[idx] * cd, profile.composition[idx] * td);
			return correlation;			
		}
	}
	
	static int selectBestCorrelation(boolean[] used, float[] correlation) {
		float best = -1;
		int bestIdx = -1;
		for(int c = 0; c < correlation.length; c++) {
			if(used[c])
				continue;		// already used
			// Check correlation
			if(correlation[c] > best) {
				best = correlation[c];
				bestIdx = c;
			}
		}
		return bestIdx;
	}
	
	public static int[] optimize(byte[][] fragments) {
		int size = fragments.length;
		
		// Profile all blocks first
		DataProfile[] profiles = new DataProfile[size];
		for(int i = 0; i < size; i++)
			profiles[i] = new DataProfile(fragments[i]);
		
		// Prepare correlation map
		float correlation[][] = new float[size][size];
		for(int i = 0; i < size; i++) {
			for(int j = 0; j < size; j++) {
				// Avoid comparing itself
				if(i == j) {
					correlation[i][j] = -1;
					continue;
				}
				// Else compare
				correlation[i][j] = profiles[i].compare(profiles[j]);
			}
		}
		// Encoding map
		int map[] = new int[size];
		boolean used[] = new boolean[size];
		// Start with the lowest dispersion
		int bestDispersion = 1000;
		int bestIdx = -1;
		for(int c = 0; c < size; c++) {
			if(profiles[c].dispersion < bestDispersion) {
				bestDispersion = profiles[c].dispersion;
				bestIdx = c;
			}
		}
		map[0] = bestIdx;
		used[bestIdx] = true;
		for(int c = 1; c < size; c++) {
			// Find next best block
			int idx = selectBestCorrelation(used, correlation[map[c - 1]]);
			map[c] = idx;
			used[idx] = true;
		}
		
		// Return optimized index
		return map;
	}
}
package sengine.utils;

public class Tools {
	
	public static final String[] joinStringArrays(String[][] stringArrays) {
		int total = 0;
		for(int c = 0; c < stringArrays.length; c++)
			total += stringArrays[c].length;
		String[] array = new String[total];
		int idx = 0;
		for(int c = 0; c < stringArrays.length; c++)
			for(int i = 0; i < stringArrays[c].length; i++)
				array[idx++] = stringArrays[c][i];
		return array;
	}

}

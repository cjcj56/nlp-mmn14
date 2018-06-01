package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListPartitioner {
	
	public static List<List<Integer>> partition(int items, int numOfThreads) {
		if(items < numOfThreads) {
			numOfThreads = items;
		}
		int gridSize = Math.floorDiv(items, numOfThreads);
		int remainder = items % numOfThreads;
		List<List<Integer>> partitionedItems = new ArrayList<>(numOfThreads);
		for (int i = 0; i < numOfThreads; ++i) {
			int assignedRemainders = Math.min(remainder, i);
			List<Integer> range = Arrays.asList(i*gridSize + assignedRemainders, i*gridSize + assignedRemainders + gridSize + (i < remainder ? 1 : 0));
			partitionedItems.add(range);
		}

		return partitionedItems;
	}

}

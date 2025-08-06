package ru.practicum.project.util;

import java.util.ArrayList;
import java.util.List;

public class ViewUtils {

	public static <T> List<List<T>> splitToRows(List<T> input, int rowSize) {
		List<List<T>> result = new ArrayList<>();
		for (int i = 0; i < input.size(); i += rowSize) {
			int end = Math.min(i + rowSize, input.size());
			result.add(input.subList(i, end));
		}

		return result;
	}
}

package ru.practicum.project.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import ru.practicum.project.util.ViewUtils;

class ViewUtilsTest {

	@Test
	void splitToRows_ShouldSplitListCorrectly() {
		List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7);
		List<List<Integer>> result = ViewUtils.splitToRows(input, 3);

		assertEquals(3, result.size());
		assertEquals(List.of(1, 2, 3), result.get(0));
		assertEquals(List.of(4, 5, 6), result.get(1));
		assertEquals(List.of(7), result.get(2));
	}

	@Test
	void splitToRows_ShouldReturnEmptyList_WhenInputEmpty() {
		List<List<Integer>> result = ViewUtils.splitToRows(List.of(), 3);
		assertTrue(result.isEmpty());
	}

	@Test
	void splitToRows_ShouldHandleExactDivision() {
		List<Integer> input = List.of(1, 2, 3, 4, 5, 6);
		List<List<Integer>> result = ViewUtils.splitToRows(input, 2);

		assertEquals(3, result.size());
		assertEquals(List.of(1, 2), result.get(0));
		assertEquals(List.of(3, 4), result.get(1));
		assertEquals(List.of(5, 6), result.get(2));
	}

}

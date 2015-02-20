package com.mgeske.tsgamebuilder.testutil;

import java.util.Collection;
import static junit.framework.TestCase.*;

public class Assert {
	public static <T> void assertContains(Collection<T> collection, T item) {
		assertTrue("Item '"+item+"' not found in collection "+collection, collection.contains(item));
	}
	
	public static <T> void assertDoesNotContain(Collection<T> collection, T item) {
		assertFalse("Item '"+item+"' was found in collection "+collection, collection.contains(item));
	}
	
	public static void assertInRange(int num, int lowerBound, int upperBound) {
		String message = num+" is not in the range ("+lowerBound+", "+upperBound+")";
		assertTrue(message, num >= lowerBound);
		assertTrue(message, num <= upperBound);
	}
}

/**
 *
 */
package org.jmist.util;

/**
 * @author bkimmel
 *
 */
public final class MathUtil {

	/**
	 * Returns x if it is within the specified range, or the closest
	 * value within the specified range if x is outside the range.
	 * @param x The value to threshold.
	 * @param min The minimum of the range to threshold to.
	 * @param max The maximum of the range to threshold to.
	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max. 
	 */
	public static double threshold(double x, double min, double max) {
		if (x < min) {
			return min;
		} else if (x > max) {
			return max;
		} else {
			return x;
		}
	}

	/**
	 * Returns x if it is within the specified range, or the closest
	 * value within the specified range if x is outside the range.
	 * @param x The value to threshold.
	 * @param min The minimum of the range to threshold to.
	 * @param max The maximum of the range to threshold to.
	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max. 
	 */
	public static int threshold(int x, int min, int max) {
		if (x < min) {
			return min;
		} else if (x > max) {
			return max;
		} else {
			return x;
		}
	}

	/**
	 * Determines whether two floating point values are close enough
	 * to be considered "equal" (i.e., the difference may be attributed
	 * to rounding errors).
	 * @param x The first value to compare.
	 * @param y The second value to compare.
	 * @param epsilon The minimum difference required for the two values
	 * 		to be considered distinguishable.
	 * @return A value indicating whether the difference between x and y
	 * 		is less than the given threshold.
	 */
	public static boolean equal(double x, double y, double epsilon) {
		return Math.abs(x - y) < epsilon;
	}

	/**
	 * Determines whether two floating point values are close enough
	 * to be considered "equal" (i.e., the difference may be attributed
	 * to rounding errors).
	 * @param x The first value to compare.
	 * @param y The second value to compare.
	 * @return A value indicating whether the difference between x and y
	 * 		is less than MathUtil.EPSILON.
	 * @see #EPSILON
	 */
	public static boolean equal(double x, double y) {
		return Math.abs(x - y) < MathUtil.EPSILON;
	}

	/**
	 * A comparison threshold value to be used when a very high degree
	 * of precision is expected.
	 */
	public static final double TINY_EPSILON		= 1e-12;
	
	/**
	 * A comparison threshold value to be used when a high degree of
	 * precision is expected.
	 */
	public static final double SMALL_EPSILON	= 1e-9;
	
	/**
	 * A comparison threshold value to be used when a normal degree of
	 * precision is expected.
	 */
	public static final double EPSILON			= 1e-6;
	
	/**
	 * A comparison threshold value to be used when a low degree of
	 * precision is expected.
	 */
	public static final double BIG_EPSILON		= 1e-4;

	/**
	 * This class contains only static utility methods and static constants,
	 * and therefore should not be creatable.
	 */
	private MathUtil() {}

}
/**
 *
 */
package ca.eandb.jmist.framework.color;

import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Tuple3;

/**
 * @author Brad
 *
 */
public final class RGB extends Tuple3 {

	/** Serialization version ID. */
	private static final long serialVersionUID = -4621493353796327474L;

	public static final RGB ZERO = new RGB(0.0, 0.0, 0.0);

	/**
	 *
	 * @param r
	 * @param g
	 * @param b
	 */
	public RGB(double r, double g, double b) {
		super(r, g, b);
	}

	public double r() {
		return x;
	}

	public double g() {
		return y;
	}

	public double b() {
		return z;
	}

	public final RGB plus(RGB other) {
		return new RGB(x + other.x, y + other.y, z + other.z);
	}

	public final RGB minus(RGB other) {
		return new RGB(x - other.x, y - other.y, z - other.z);
	}

	public final RGB divide(RGB other) {
		return new RGB(x / other.x, y / other.y, z / other.z);
	}

	public final RGB divide(double c) {
		return new RGB(x / c, y / c, z / c);
	}

	public final RGB times(RGB other) {
		return new RGB(x * other.x, y * other.y, z * other.z);
	}

	public final RGB times(double c) {
		return new RGB(x * c, y * c, z * c);
	}

	public final RGB clamp(double max) {
		return clamp(0.0, max);
	}

	public final RGB clamp(double min, double max) {
		return new RGB(
				MathUtil.threshold(x, min, max),
				MathUtil.threshold(y, min, max),
				MathUtil.threshold(y, min, max));
	}

	public final int toR8G8B8() {
		return
			(MathUtil.threshold((int) Math.floor(256.0 * x), 0, 255) << 16) |
			(MathUtil.threshold((int) Math.floor(256.0 * y), 0, 255) << 8) |
			MathUtil.threshold((int) Math.floor(256.0 * z), 0, 255);
	}

	public CIEXYZ toXYZ() {
		return ColorUtil.convertRGB2XYZ(this);
	}

	public static RGB fromXYZ(double X, double Y, double Z) {
		return ColorUtil.convertXYZ2RGB(X, Y, Z);
	}

	public static RGB fromXYZ(CIEXYZ xyz) {
		return ColorUtil.convertXYZ2RGB(xyz);
	}

}

/**
 *
 */
package ca.eandb.jmist.framework.lens;

import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Lens</code> that projects the world on to the image plane
 * ortographically.
 * @author Brad Kimmel
 */
public final class OrthographicLens extends TransformableLens {

	/**
	 * Creates a new <code>OrthographicLens</code>.
	 * @param width The extent of the image plane along the x-axis.
	 * @param height The extent of the image plane along the y-axis.
	 */
	public OrthographicLens(double width, double height) {
		this.width = width;
		this.height = height;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.packages.TransformableLens#viewRayAt(ca.eandb.jmist.toolkit.Point2)
	 */
	@Override
	protected Ray3 viewRayAt(Point2 p) {

		return new Ray3(
				new Point3(
						(p.x() - 0.5) * this.width,
						(0.5 - p.y()) * this.height,
						0.0
				),
				Vector3.NEGATIVE_K
		);

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.lens.TransformableLens#projectInViewSpace(ca.eandb.jmist.math.Point3)
	 */
	@Override
	protected Point2 projectInViewSpace(Point3 p) {
		if (-p.z() < MathUtil.EPSILON) {
			return null;
		}
		return new Point2((p.x() / width) + 0.5, 0.5 - (p.y() / height));
	}

	/** The extent of the image plane along the x-axis. */
	private final double width;

	/** The extent of the image plane along the y-axis. */
	private final double height;

}

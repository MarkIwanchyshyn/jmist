/**
 *
 */
package ca.eandb.jmist.framework.lens;

import ca.eandb.jmist.math.Interval;
import ca.eandb.jmist.math.Optics;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Sphere;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Lens</code> that simulates an orthogonal projection of a mirrored
 * sphere.
 * @author Brad Kimmel
 */
public final class OmnimaxLens extends SingularApertureLens {

	/**
	 * Serialization version ID.
	 */
	private static final long serialVersionUID = -4366154660419656383L;

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Lens#rayAt(ca.eandb.jmist.math.Point2)
	 */
	public Ray3 rayAt(Point2 p) {

		double		nx		= 2.0 * (p.x() - 0.5);
		double		ny		= 2.0 * (0.5 - p.y());

		Ray3		init	= new Ray3(new Point3(nx, ny, 1.0), Vector3.K);
		Interval	I		= LENS_SPHERE.intersect(init);

		if (I.isEmpty()) {
			return null;
		}

		Vector3		n		= LENS_SPHERE.center().vectorTo(init.pointAt(I.minimum()));
		Vector3		r		= Optics.reflect(init.direction(), n);

		return new Ray3(Point3.ORIGIN, r);

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Lens#project(ca.eandb.jmist.math.Vector3)
	 */
	public Projection project(final Vector3 v) {
		return new Projection() {
			public Point2 pointOnImagePlane() {
				Vector3 dir = v.unit();
				Vector3 half = new Vector3(0.5 * dir.x(), 0.5 * dir.y(), 0.5 * (dir.z() - 1.0)).unit();
				double x = 0.5 * (half.x() + 1.0);
				double y = 0.5 * (1.0 - half.y());
				return new Point2(x, y);
			}

			public Point3 pointOnLens() {
				return Point3.ORIGIN;
			}

			public double importance() {
				return 1.0; // FIXME Light tracing will not work until this is corrected.
			}
		};
	}

	/** The <code>Sphere</code> to bounce the orthogonally generated rays from. */
	private static final Sphere LENS_SPHERE = new Sphere(new Point3(0, 0, 2), 1);

}

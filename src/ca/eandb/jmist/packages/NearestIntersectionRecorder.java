/**
 *
 */
package ca.eandb.jmist.packages;

import ca.eandb.jmist.framework.Geometry;
import ca.eandb.jmist.framework.Intersection;
import ca.eandb.jmist.framework.IntersectionRecorder;
import ca.eandb.jmist.toolkit.Interval;
import ca.eandb.jmist.toolkit.Ray3;
import ca.eandb.jmist.util.MathUtil;

/**
 * An intersection recorder that only keeps the nearest intersection
 * recorded.
 * @author Brad Kimmel
 */
public final class NearestIntersectionRecorder implements IntersectionRecorder {

	/**
	 * Creates a new <code>NearestIntersectionRecorder</code> that records
	 * <code>Intersection</code>s with a non-negligible positive distance.
	 */
	public NearestIntersectionRecorder() {
		this.interval = new Interval(MathUtil.EPSILON, Double.POSITIVE_INFINITY);
	}

	/**
	 * Creates a new <code>NearestIntersectionRecorder</code> that records
	 * <code>Intersection</code>s with a distance greater than that specified.
	 * @param epsilon The minimum distance to accept.
	 */
	public NearestIntersectionRecorder(double epsilon) {
		this.interval = new Interval(epsilon, Double.POSITIVE_INFINITY);
	}

	/**
	 * Creates a new <code>NearestIntersectionRecorder</code> that records
	 * <code>Intersection</code>s within the specified <code>Interval</code>.
	 * @param interval The <code>Interval</code> within which to accept
	 * 		<code>Intersection</code>s.
	 */
	public NearestIntersectionRecorder(Interval interval) {
		this.interval = interval;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.IntersectionRecorder#needAllIntersections()
	 */
	public boolean needAllIntersections() {
		return false;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.IntersectionRecorder#interval()
	 */
	public Interval interval() {
		return this.interval;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.IntersectionRecorder#record(ca.eandb.jmist.framework.Intersection)
	 */
	public void record(Intersection intersection) {
		if (this.interval().contains(intersection.distance())) {
			if (this.nearest == null || intersection.distance() < this.nearest.distance()) {
				this.nearest = intersection;
			}
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.IntersectionRecorder#isEmpty()
	 */
	public boolean isEmpty() {
		return (this.nearest == null);
	}

	/**
	 * Gets the intersection with the smallest ray parameter that has
	 * been recorded.
	 * @return The nearest intersection that has been recorded.
	 */
	public Intersection nearestIntersection() {
		return this.nearest;
	}

	/**
	 * Computes the nearest intersection of a <code>Ray3</code> with a
	 * <code>Geometry</code>.
	 * @param ray The <code>Ray3</code> to intersect with.
	 * @param geometry The <code>Geometry</code> to test for an intersection
	 * 		with.
	 * @return The nearest <code>Intersection</code>, or <code>null</code> if
	 * 		none exists.
	 */
	public static Intersection computeNearestIntersection(Ray3 ray, Geometry geometry) {
		NearestIntersectionRecorder recorder = new NearestIntersectionRecorder();
		geometry.intersect(ray, recorder);
		return recorder.nearestIntersection();
	}

	/** The nearest intersection that has been recorded so far. */
	private Intersection nearest = null;

	/**
	 * The <code>Interval</code> within which to accept
	 * <code>Intersection</code>s.
	 */
	private final Interval interval;

}
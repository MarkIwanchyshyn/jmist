/**
 *
 */
package ca.eandb.jmist.framework.measurement;

import java.util.Arrays;

import ca.eandb.jmist.math.SphericalCoordinates;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>CollectorSphere</code> that it subdivided into arbitrary stacks and slices.
 * @author Brad Kimmel
 */
public final class UncappedLatLongCollectorSphere implements CollectorSphere {

	/** Serialization version ID. */
	private static final long serialVersionUID = -7574177853943171751L;

	/** The polar angles at which to subdivide the sphere. */
	private final double[] stacks;
	
	/** The azimuthal angles at which to subdivide the sphere. */
	private final double[] slices;

	/**
	 * Creates a new <code>UncappedLatLongCollectorSphere</code>.
	 * @param stacks The polar angles at which to subdivide the sphere.  There
	 * 		must be at least two entries and it must be sorted in ascending
	 * 		order.  Each entry must be in the range [0, &pi;].
	 * @param slices The azimuthal angles at which to subdivide the sphere.
	 * 		There must be at least two entries and it must be sorted in
	 * 		ascending order.  The difference between the first and last entries
	 * 		must be at most 2&pi;.
	 * @throws IllegalArgumentException If the constraints on <code>stacks</code>
	 * 		or <code>slices</code> are not met.
	 */
	public UncappedLatLongCollectorSphere(double[] stacks, double[] slices) {
		if (stacks.length < 2) {
			throw new IllegalArgumentException("stacks must have at least two entries");
		}
		for (int i = 1; i < stacks.length; i++) {			
			if (stacks[i - 1] > stacks[i]) {
				throw new IllegalArgumentException("stacks must be ascending");
			}
		}
		if (stacks[0] < 0.0 || stacks[stacks.length - 1] > Math.PI) {
			throw new IllegalArgumentException("stacks entries must fall within [0, pi]");
		}
		if (slices.length < 2) {
			throw new IllegalArgumentException("slices must have at least two entries");
		}
		for (int i = 1; i < slices.length; i++) {			
			if (slices[i - 1] > slices[i]) {
				throw new IllegalArgumentException("slices must be ascending");
			}
		}
		if (slices[slices.length - 1] - slices[0] > 2.0 * Math.PI){
			throw new IllegalArgumentException("slices must not span more than 2*pi radians");
		}
		this.stacks = stacks.clone();
		this.slices = slices.clone();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorCenter(int)
	 */
	public SphericalCoordinates getSensorCenter(int sensor) {

		int stack = sensor / (slices.length - 1);
		int slice = sensor % (slices.length - 1);
		
		double phi = 0.5 * (slices[slice] + slices[slice + 1]);
		double theta = Math.acos(0.5 * (Math.cos(stacks[stack]) + Math.cos(stacks[stack + 1])));

		return SphericalCoordinates.canonical(theta, phi);

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorProjectedSolidAngle(int)
	 */
	public double getSensorProjectedSolidAngle(int sensor) {

		int stack = sensor / (slices.length - 1);
		int slice = sensor % (slices.length - 1);

		return 0.25
				* (slices[slice + 1] - slices[slice])
				* (Math.cos(2.0 * stacks[stack])
						- Math.cos(2.0 * stacks[stack + 1]));

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#getSensorSolidAngle(int)
	 */
	public double getSensorSolidAngle(int sensor) {

		int stack = sensor / (slices.length - 1);
		int slice = sensor % (slices.length - 1);

		return (slices[slice + 1] - slices[slice])
				* (Math.cos(stacks[stack]) - Math.cos(stacks[stack + 1]));

	}

	private int getSensor(SphericalCoordinates v) {
		
		v = v.canonical();

		double theta = v.polar();
		double phi = v.azimuthal();
		
		phi -= 2.0 * Math.PI * Math.floor((phi - slices[0]) / (2.0 * Math.PI));
		
		if (theta < stacks[0] || theta > stacks[stacks.length - 1] || phi > slices[slices.length - 1]) {
			return -1;
		}
		
		int stack = Arrays.binarySearch(stacks, theta);
		if (stack < 0) {
			stack = -(stack + 1);
		}
		stack = Math.max(stack - 1, 0);
		
		int slice = Arrays.binarySearch(slices, phi);
		if (slice < 0) {
			slice = -(slice + 1);
		}
		slice = Math.max(slice - 1, 0);
		
		return stack * (slices.length - 1) + slice;
		
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#record(ca.eandb.jmist.math.Vector3, ca.eandb.jmist.framework.measurement.CollectorSphere.Callback)
	 */
	public void record(Vector3 v, Callback f) {
		record(SphericalCoordinates.fromCartesian(v), f);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#record(ca.eandb.jmist.math.SphericalCoordinates, ca.eandb.jmist.framework.measurement.CollectorSphere.Callback)
	 */
	public void record(SphericalCoordinates v, Callback f) {
		int sensor = getSensor(v);
		if (sensor >= 0) {
			f.record(sensor);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.measurement.CollectorSphere#sensors()
	 */
	public int sensors() {
		return (stacks.length - 1) * (slices.length - 1);
	}

}

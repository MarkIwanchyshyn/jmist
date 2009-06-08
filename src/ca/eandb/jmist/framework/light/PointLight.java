/**
 *
 */
package ca.eandb.jmist.framework.light;

import java.io.Serializable;

import ca.eandb.jmist.framework.Illuminable;
import ca.eandb.jmist.framework.Light;
import ca.eandb.jmist.framework.SurfacePoint;
import ca.eandb.jmist.framework.VisibilityFunction3;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Light</code> that emits from a single point.
 * @author Brad Kimmel
 */
public final class PointLight implements Light, Serializable {

	/**
	 * Creates a new <code>PointLight</code>.
	 * @param location The <code>Point3</code> where the light is to emit from.
	 * @param emission The emission <code>Color</code> of the light.
	 * @param shadows A value indicating whether the light should be affected
	 * 		by shadows.
	 */
	public PointLight(Point3 location, Color emission, boolean shadows) {
		this.location = location;
		this.emission = emission;
		this.shadows = shadows;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Light#illuminate(ca.eandb.jmist.framework.SurfacePoint, ca.eandb.jmist.framework.VisibilityFunction3, ca.eandb.jmist.framework.Illuminable)
	 */
	public void illuminate(SurfacePoint x, VisibilityFunction3 vf, Illuminable target) {

		if (!this.shadows || vf.visibility(x.location(), this.location)) {

			Vector3		from			= x.location().vectorTo(this.location);
			double		dSquared		= from.squaredLength();
			double		attenuation		= 1.0 / (4.0 * Math.PI * dSquared);

			target.illuminate(from.unit(), emission.times(attenuation));

		}

	}

	/** The <code>Point3</code> where the light is to emit from. */
	private final Point3 location;

	/** The emission <code>Color</code> of the light. */
	private final Color emission;

	/** A value indicating whether the light should be affected by shadows. */
	private final boolean shadows;

	/**
	 * Serialization version ID.
	 */
	private static final long serialVersionUID = -5220350307274318220L;

}

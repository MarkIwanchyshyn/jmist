/**
 *
 */
package ca.eandb.jmist.framework.material;

import java.io.Serializable;

import ca.eandb.jmist.framework.IntersectionGeometry;
import ca.eandb.jmist.framework.Painter;
import ca.eandb.jmist.framework.ScatteredRayRecorder;
import ca.eandb.jmist.framework.ScatteredRay;
import ca.eandb.jmist.framework.SurfacePointGeometry;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.ColorModel;
import ca.eandb.jmist.math.RandomUtil;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.SphericalCoordinates;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Material</code> that reflects light equally in all directions in
 * the upper hemisphere.
 * @author Brad Kimmel
 */
public final class LambertianMaterial extends OpaqueMaterial implements
		Serializable {

	/**
	 * Creates a new <code>LambertianMaterial</code> that does not emit light.
	 * @param reflectance The reflectance <code>Painter</code>.
	 */
	public LambertianMaterial(Painter reflectance) {
		this(reflectance, null);
	}

	/**
	 * Creates a new <code>LambertianMaterial</code> that emits light.
	 * @param reflectance The reflectance <code>Painter</code>.
	 * @param emittance The emission <code>Painter</code>.
	 */
	public LambertianMaterial(Painter reflectance, Painter emittance) {
		this.reflectance = reflectance;
		this.emittance = emittance;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AbstractMaterial#isEmissive()
	 */
	@Override
	public boolean isEmissive() {
		return (this.emittance != null);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.material.AbstractMaterial#emission(ca.eandb.jmist.framework.SurfacePointGeometry, ca.eandb.jmist.math.Vector3)
	 */
	@Override
	public Color emission(SurfacePointGeometry x, Vector3 out) {

		if (this.emittance == null || x.getNormal().dot(out) < 0.0) {
			return ColorModel.getInstance().getBlack();
		}

		double ndotv = x.getShadingNormal().dot(out);
		return ndotv > 0.0 ? emittance.getColor(x).times(ndotv) : ColorModel.getInstance().getBlack();

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.material.AbstractMaterial#emit(ca.eandb.jmist.framework.SurfacePointGeometry, ca.eandb.jmist.framework.ScatteredRayRecorder)
	 */
	@Override
	public void emit(SurfacePointGeometry x, ScatteredRayRecorder recorder) {

		if (this.emittance != null) {

			SphericalCoordinates out = RandomUtil.uniformOnUpperHemisphere();
			Ray3 ray = new Ray3(x.getPosition(), out.toCartesian(x.getShadingBasis()));

			if (x.getNormal().dot(ray.direction()) > 0.0) {
				recorder.add(ScatteredRay.diffuse(ray, emittance.getColor(x)));
			}

		}

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.material.AbstractMaterial#scatter(ca.eandb.jmist.framework.IntersectionGeometry, ca.eandb.jmist.framework.ScatteredRayRecorder)
	 */
	@Override
	public void scatter(IntersectionGeometry x, ScatteredRayRecorder recorder) {

		if (this.reflectance != null) {

			SphericalCoordinates out = RandomUtil.diffuse();
			Ray3 ray = new Ray3(x.getPosition(), out.toCartesian(x.getShadingBasis()));

			if (ray.direction().dot(x.getNormal()) > 0.0) {
				recorder.add(ScatteredRay.diffuse(ray, reflectance.getColor(x)));
			}

		}

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.material.AbstractMaterial#scattering(ca.eandb.jmist.framework.IntersectionGeometry, ca.eandb.jmist.math.Vector3)
	 */
	@Override
	public Color scattering(IntersectionGeometry x, Vector3 in) {

		boolean toFront = (x.getNormal().dot(in) > 0.0);

		if (this.reflectance != null && x.isFront() == toFront) {
			return reflectance.getColor(x);
		} else {
			return ColorModel.getInstance().getBlack();
		}

	}

	/** The reflectance <code>Painter</code> of this <code>Material</code>. */
	private final Painter reflectance;

	/** The emittance <code>Painter</code> of this <code>Material</code>. */
	private final Painter emittance;

	/**
	 * Serialization version ID.
	 */
	private static final long serialVersionUID = 485410070543495668L;

}

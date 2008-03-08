/**
 *
 */
package org.jmist.packages.material;

import java.io.Serializable;

import org.jmist.framework.Intersection;
import org.jmist.framework.OpaqueMaterial;
import org.jmist.framework.ScatterRecorder;
import org.jmist.framework.ScatterResult;
import org.jmist.framework.Spectrum;
import org.jmist.framework.SurfacePoint;
import org.jmist.packages.spectrum.ScaledSpectrum;
import org.jmist.toolkit.RandomUtil;
import org.jmist.toolkit.Ray3;
import org.jmist.toolkit.SphericalCoordinates;
import org.jmist.toolkit.Tuple;
import org.jmist.toolkit.Vector3;

/**
 * A <code>Material</code> that reflects light equally in all directions in
 * the upper hemisphere.
 * @author bkimmel
 */
public final class LambertianMaterial extends OpaqueMaterial implements
		Serializable {

	/**
	 * Creates a new <code>LambertianMaterial</code> that does not emit light.
	 * @param reflectance The reflectance <code>Spectrum</code>.
	 */
	public LambertianMaterial(Spectrum reflectance) {
		this(reflectance, null);
	}

	/**
	 * Creates a new <code>LambertianMaterial</code> that emits light.
	 * @param reflectance The reflectance <code>Spectrum</code>.
	 * @param emittance The emission <code>Spectrum</code>.
	 */
	public LambertianMaterial(Spectrum reflectance, Spectrum emittance) {
		this.reflectance = reflectance;
		this.emittance = emittance;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.AbstractMaterial#isEmissive()
	 */
	@Override
	public boolean isEmissive() {
		return (this.emittance != null);
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Material#emission(org.jmist.framework.SurfacePoint, org.jmist.toolkit.Vector3)
	 */
	@Override
	public Spectrum emission(SurfacePoint x, Vector3 out) {

		if (this.emittance == null || x.normal().dot(out) < 0.0) {
			return Spectrum.ZERO;
		}

		double ndotv = x.microfacetNormal().dot(out);
		return ndotv > 0.0 ? new ScaledSpectrum(ndotv, this.emittance) : Spectrum.ZERO;

	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.AbstractMaterial#emit(org.jmist.framework.SurfacePoint, org.jmist.toolkit.Tuple, org.jmist.framework.ScatterRecorder)
	 */
	@Override
	public void emit(SurfacePoint x, Tuple wavelengths, ScatterRecorder recorder) {

		if (this.emittance != null) {

			SphericalCoordinates out = RandomUtil.uniformOnUpperHemisphere();
			Ray3 ray = new Ray3(x.location(), out.toCartesian(x.microfacetBasis()));

			if (x.normal().dot(ray.direction()) > 0.0) {
				double[] radiance = this.emittance.sample(wavelengths, null);
				recorder.record(ScatterResult.diffuse(ray, wavelengths, radiance));
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.AbstractMaterial#scatter(org.jmist.framework.Intersection, org.jmist.toolkit.Tuple, org.jmist.framework.ScatterRecorder)
	 */
	@Override
	public void scatter(Intersection x, Tuple wavelengths, ScatterRecorder recorder) {

		if (this.reflectance != null) {

			SphericalCoordinates out = RandomUtil.diffuse();
			Ray3 ray = new Ray3(x.location(), out.toCartesian(x.microfacetBasis()));

			if (ray.direction().dot(x.normal()) > 0.0) {
				double[] weights = this.reflectance.sample(wavelengths, null);
				recorder.record(ScatterResult.diffuse(ray, wavelengths, weights));
			}

		}

	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.AbstractMaterial#scattering(org.jmist.framework.Intersection, org.jmist.toolkit.Vector3)
	 */
	@Override
	public Spectrum scattering(Intersection x, Vector3 out) {

		boolean toFront = (x.normal().dot(out) > 0.0);

		if (this.reflectance != null && x.front() == toFront) {
			return this.reflectance;
		} else {
			return Spectrum.ZERO;
		}

	}

	/** The reflectance <code>Spectrum</code> of this <code>Material</code>. */
	private final Spectrum reflectance;

	/** The emittance <code>Spectrum</code> of this <code>Material</code>. */
	private final Spectrum emittance;

	/**
	 * Serialization version ID.
	 */
	private static final long serialVersionUID = 485410070543495668L;

}
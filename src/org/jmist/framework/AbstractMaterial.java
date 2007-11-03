/**
 *
 */
package org.jmist.framework;

import org.jmist.toolkit.Tuple;
import org.jmist.toolkit.Vector3;

/**
 * Provides default implementations for a <code>Material</code>.
 * @author bkimmel
 */
public abstract class AbstractMaterial implements Material {

	/* (non-Javadoc)
	 * @see org.jmist.framework.Material#emission(org.jmist.framework.SurfacePoint, org.jmist.toolkit.Vector3)
	 */
	@Override
	public Spectrum emission(SurfacePoint x, Vector3 out) {
		return Spectrum.ZERO;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Material#emit(org.jmist.framework.SurfacePoint, org.jmist.toolkit.Tuple)
	 */
	@Override
	public ScatterRecord emit(SurfacePoint x, Tuple wavelengths) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Material#scatter(org.jmist.framework.Intersection, org.jmist.toolkit.Tuple)
	 */
	@Override
	public ScatterRecord scatter(Intersection x, Tuple wavelengths) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Material#scattering(org.jmist.framework.Intersection, org.jmist.toolkit.Vector3)
	 */
	@Override
	public Spectrum scattering(Intersection x, Vector3 out) {
		return Spectrum.ZERO;
	}

}
/**
 *
 */
package org.jmist.framework;

import org.jmist.toolkit.Vector3;

/**
 * Maps directions in three dimensional space to spectra.
 * @author brad
 */
public interface DirectionalTexture3 {

	/**
	 * Computes the spectrum at the specified direction in the domain.
	 * @param v The <code>Vector3</code> in the domain.
	 * @return The <code>Spectrum</code> at in the direction of <code>v</code>.
	 */
	Spectrum evaluate(Vector3 v);

}

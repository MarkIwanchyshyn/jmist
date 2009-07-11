/**
 *
 */
package ca.eandb.jmist.framework;

import java.io.Serializable;

/**
 * A factory that creates extrapolated spectra from pixel representations.
 * @author Brad Kimmel
 */
public interface PixelSpectrumFactory extends Serializable {

	/**
	 * Creates a <code>Function1</code> extrapolated from the specified pixel.
	 * @param pixel The <code>Pixel</code> to extrapolate.
	 * @return The extrapolated <code>Function1</code>.
	 */
	Function1 createSpectrum(double[] pixel);

}

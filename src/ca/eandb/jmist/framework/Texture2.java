/**
 *
 */
package ca.eandb.jmist.framework;

import java.io.Serializable;

import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.WavelengthPacket;
import ca.eandb.jmist.math.Point2;

/**
 * Maps two dimensional space to colors.
 * @author Brad Kimmel
 */
public interface Texture2 extends Serializable {

	/**
	 * Computes the color at the specified <code>Point2</code> in the
	 * domain.
	 * @param p The <code>Point2</code> in the domain.
	 * @param lambda The <code>WavelengthPacket</code> denoting the wavelengths
	 * 		at which to evaluate the texture.
	 * @return The <code>Color</code> at <code>p</code>.
	 */
	Color evaluate(Point2 p, WavelengthPacket lambda);

}

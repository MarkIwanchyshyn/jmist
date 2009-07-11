/**
 *
 */
package ca.eandb.jmist.framework.shader.pixel;

import ca.eandb.jmist.framework.ImageShader;
import ca.eandb.jmist.framework.PixelShader;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.ColorModel;
import ca.eandb.jmist.math.Box2;

/**
 * A rasterizing pixel shader that shades a random point within the
 * bounds of the pixel.
 * @author Brad Kimmel
 */
public final class RandomPixelShader extends ImageRasterizingPixelShader implements
		PixelShader {

	/**
	 * Serialization version ID.
	 */
	private static final long serialVersionUID = -8237509594777958012L;

	/**
	 * Initializes the source of random numbers and the camera.
	 * @param random The source of random numbers to use.
	 * @param camera The camera to use to shade points on the image
	 * 		plane.
	 * @param model The color model to use for sampling in the wavelength
	 * 		domain.
	 */
	public RandomPixelShader(Random random, ImageShader camera, ColorModel model) {
		super(camera, model);
		this.random = random;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.PixelShader#shadePixel(ca.eandb.jmist.math.Box2)
	 */
	public Color shadePixel(Box2 bounds) {
		return shadeAt(bounds.interpolate(random.next(), random.next()));
	}

	/** The source of random numbers for this pixel shader. */
	private final Random random;

}

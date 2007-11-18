/**
 *
 */
package org.jmist.packages;

import org.jmist.framework.Geometry;
import org.jmist.framework.Intersection;
import org.jmist.framework.Material;
import org.jmist.framework.Observer;
import org.jmist.framework.RayShader;
import org.jmist.framework.Spectrum;
import org.jmist.toolkit.Pixel;
import org.jmist.toolkit.RandomUtil;
import org.jmist.toolkit.Ray3;
import org.jmist.toolkit.Tuple;
import org.jmist.util.MathUtil;

/**
 * @author bkimmel
 *
 */
public final class PathShader implements RayShader {

	/**
	 * Creates a new <code>PathShader</code>.
	 * @param geometry
	 * @param observer
	 */
	public PathShader(Geometry geometry, Observer observer, double meanDepth) {

		assert(meanDepth >= 1.0);

		this.geometry = geometry;
		this.observer = observer;
		this.alpha = 1.0 - (1.0 / meanDepth);

	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.RayShader#shadeRay(org.jmist.toolkit.Ray3, org.jmist.toolkit.Pixel)
	 */
	@Override
	public void shadeRay(Ray3 ray, Pixel pixel) {

		Tuple		wavelengths		= this.observer.sample();
		int			n				= wavelengths.size();
		double[]	importance		= MathUtil.setAll(new double[n], 1.0);
		double[]	sample			= null;
		double[]	result			= new double[n];
		double		opacity			= 0.0;

		while (ray != null) {

			Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, this.geometry);

			if (x == null) {
				break;
			}

			opacity = 1.0;

			Material material = x.material();
			Spectrum emission = material.emission(x, ray.direction().opposite());

			sample = emission.sample(wavelengths, sample);
			MathUtil.modulate(sample, importance);
			MathUtil.add(result, sample);

			if (!RandomUtil.bernoulli(this.alpha)) {
				break;
			}

			ray = material.scatter(x, wavelengths, importance);
			MathUtil.scale(importance, 1.0 / this.alpha);

		}

		/* pixel.set(result, opacity); */

	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.PixelFactory#createPixel()
	 */
	@Override
	public Pixel createPixel() {
		// TODO Auto-generated method stub
		return null;
	}

	private final double alpha;
	private final Geometry geometry;
	private final Observer observer;

}
/**
 *
 */
package org.jmist.packages;

import org.jmist.framework.IIntersection;
import org.jmist.framework.IRayCaster;
import org.jmist.framework.IRayShader;
import org.jmist.toolkit.Interval;
import org.jmist.toolkit.Pixel;
import org.jmist.toolkit.Ray3;

/**
 * @author bkimmel
 *
 */
public final class DistanceRayShader implements IRayShader {

	/**
	 * Initializes the ray caster to use.
	 * @param caster The ray caster to use.
	 */
	public DistanceRayShader(IRayCaster caster) {
		this.caster = caster;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.IRayShader#shadeRay(org.jmist.toolkit.Ray3, org.jmist.toolkit.Pixel)
	 */
	public void shadeRay(Ray3 ray, Pixel pixel) {
		IIntersection intersection = this.caster.castRay(ray, Interval.POSITIVE);

		// TODO finish implementing DistanceRayShader.shadeRay.
		if (intersection != null) {
			//intersection.getRayParameter();
		} else {
			//0.0;
		}
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.IPixelFactory#createPixel()
	 */
	public Pixel createPixel() {
		// TODO Auto-generated method stub
		return null;
	}

	private final IRayCaster caster;

}

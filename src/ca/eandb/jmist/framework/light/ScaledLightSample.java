/*
 * Copyright (c) 2008 Bradley W. Kimmel
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.eandb.jmist.framework.light;

import ca.eandb.jmist.framework.LightSample;
import ca.eandb.jmist.framework.VisibilityFunction3;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.math.Vector3;

/**
 * @author brad
 *
 */
public final class ScaledLightSample implements LightSample {

	private final double factor;

	private final LightSample sample;

	/**
	 * @param factor
	 * @param sample
	 */
	private ScaledLightSample(double factor, LightSample sample) {
		this.factor = factor;
		this.sample = sample;
	}

	/**
	 *
	 * @param factor
	 * @param sample
	 * @return
	 */
	public static ScaledLightSample create(double factor, LightSample sample) {
		if (sample instanceof ScaledLightSample) {
			ScaledLightSample other = (ScaledLightSample) sample;
			return new ScaledLightSample(factor * other.factor, other.sample);
		} else {
			return new ScaledLightSample(factor, sample);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.LightSample#castShadowRay(ca.eandb.jmist.framework.VisibilityFunction3)
	 */
	public boolean castShadowRay(VisibilityFunction3 vf) {
		return sample.castShadowRay(vf);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.LightSample#getDirToLight()
	 */
	public Vector3 getDirToLight() {
		return sample.getDirToLight();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.LightSample#getRadiantIntensity()
	 */
	public Color getRadiantIntensity() {
		return sample.getRadiantIntensity().times(factor);
	}

}

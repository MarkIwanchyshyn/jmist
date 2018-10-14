/**
 * Java Modular Image Synthesis Toolkit (JMIST)
 * Copyright (C) 2018 Bradley W. Kimmel
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
package ca.eandb.jmist.framework.tone;

import ca.eandb.jmist.framework.color.CIEXYZ;
import ca.eandb.jmist.framework.color.CIExyY;

/**
 * A <code>ToneMapperFactory</code> that builds linear tone maps.
 * @author Brad Kimmel
 */
public final class LinearToneMapperFactory implements ToneMapperFactory {

  /** Serialization version ID. */
  private static final long serialVersionUID = -246622078552676822L;

  private static final double DEFAULT_DELTA = 1.0;

  private final double delta;

  public LinearToneMapperFactory(double delta) {
    this.delta = delta;
  }

  public LinearToneMapperFactory() {
    this(DEFAULT_DELTA);
  }

  @Override
  public ToneMapper createToneMapper(Iterable<CIEXYZ> samples) {
    double Yavg = 0.0;
    double Ymax = 0.0;
    int n = 0;
    for (CIEXYZ sample : samples) {
      if (sample != null) {
        double Y = Math.abs(sample.Y());
        if (Y > Ymax) {
          Ymax = Y;
        }
        Yavg += Math.log(delta + Y);
        n++;
      }
    }
    Yavg /= (double) n;
    Yavg = Math.exp(Yavg) - delta;

    double Ymid = 1.03 - 2.0 / (2.0 + Math.log10(Yavg + 1.0));
    CIExyY white = new CIExyY(1.0 / 3.0, 1.0 / 3.0, Yavg / Ymid);
    return new LinearToneMapper(white.toXYZ());
  }

}

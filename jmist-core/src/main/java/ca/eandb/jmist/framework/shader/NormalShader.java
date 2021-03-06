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
package ca.eandb.jmist.framework.shader;

import ca.eandb.jmist.framework.Shader;
import ca.eandb.jmist.framework.ShadingContext;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Shader</code> that assigns a colour based on the surface normal.
 *
 * @author Brad Kimmel
 */
public final class NormalShader implements Shader {

  /** Serialization version ID. */
  private static final long serialVersionUID = -4724073342278275837L;

  @Override
  public Color shade(ShadingContext sc) {
    Vector3 n = sc.getShadingNormal();

    double r = Math.abs(n.x());
    double g = Math.abs(n.y());
    double b = Math.abs(n.z());

    double c = Math.max(r, Math.max(g, b));

    return sc.getColorModel().fromRGB(r / c, g / c, b / c).sample(sc.getWavelengthPacket());
  }

}

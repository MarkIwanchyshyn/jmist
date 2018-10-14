/**
 * Java Modular Image Synthesis Toolkit (JMIST)
 * Copyright (C) 2008-2013 Bradley W. Kimmel
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
package ca.eandb.jmist.framework.material;

import org.apache.commons.math3.util.FastMath;

import ca.eandb.jmist.framework.Painter;
import ca.eandb.jmist.framework.ScatteredRay;
import ca.eandb.jmist.framework.SurfacePoint;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.ColorUtil;
import ca.eandb.jmist.framework.color.Spectrum;
import ca.eandb.jmist.framework.color.WavelengthPacket;
import ca.eandb.jmist.framework.painter.UniformPainter;
import ca.eandb.jmist.framework.random.RandomUtil;
import ca.eandb.jmist.math.Basis3;
import ca.eandb.jmist.math.Optics;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.SphericalCoordinates;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Material</code> based on the modified Phong BRDF.  Implementation as
 * described in:
 *
 * E.P. Lafortune and Y.D. Willems, "Using the Modified Phong Reflectance Model
 * for Physically Based Rendering", Technical Report CW 197, Department of
 * Computing Science, K.U. Leuven.  November 1994.
 *
 * @author Brad Kimmel
 */
public final class ModifiedPhongMaterial extends OpaqueMaterial {

  /** Serialization version ID. */
  private static final long serialVersionUID = 3049341531935254708L;

  /** The <code>Painter</code> to use to assign the diffuse reflectance. */
  private final Painter kdPainter;

  /** The <code>Painter</code> to use to assign the specular reflectance. */
  private final Painter ksPainter;

  /** The sharpness of the specular reflectance lobe. */
  private final double n;

  /**
   * Creates a new <code>ModifiedPhongMaterial</code>.
   * @param kd The <code>Painter</code> to use to assign the diffuse
   *     reflectance.
   * @param ks The <code>Painter</code> to use to assign the specular
   *     reflectance.
   * @param n The sharpness of the specular reflectance lobe.
   */
  public ModifiedPhongMaterial(Painter kd, Painter ks, double n) {
    this.kdPainter = kd;
    this.ksPainter = ks;
    this.n = n;
  }

  /**
   * Creates a new <code>ModifiedPhongMaterial</code>.
   * @param kd The diffuse reflectance <code>Spectrum</code>.
   * @param ks The specular reflectance <code>Spectrum</code>.
   * @param n The sharpness of the specular reflectance lobe.
   */
  public ModifiedPhongMaterial(Spectrum kd, Spectrum ks, double n) {
    this(new UniformPainter(kd), new UniformPainter(ks), n);
  }

  @Override
  public Color bsdf(SurfacePoint x, Vector3 in, Vector3 out,
      WavelengthPacket lambda) {
    if ((in.dot(x.getNormal()) < 0.0) == (out.dot(x.getNormal()) > 0.0)) {
      Color kd = kdPainter.getColor(x, lambda);
      Color d = kd.divide(Math.PI);

      Vector3 r = Optics.reflect(in, x.getNormal());
      double rdoto = r.dot(out);
      if (rdoto > 0.0) {
        Color ks = ksPainter.getColor(x, lambda);
        Color s = ks.times(((n + 2.0) / (2.0 * Math.PI)) * Math.pow(rdoto, n));
        return d.plus(s);
      } else {
        return d;
      }
    } else {
      return lambda.getColorModel().getBlack(lambda);
    }
  }

  @Override
  public double getScatteringPDF(SurfacePoint x, Vector3 in, Vector3 out,
      boolean adjoint, WavelengthPacket lambda) {

    double vdotn = -in.dot(x.getNormal());
    double odotn = out.dot(x.getNormal());
    if ((vdotn > 0.0) != (odotn > 0.0)) {
      return 0.0;
    }

    Color kd = kdPainter.getColor(x, lambda);
    Color ks = ksPainter.getColor(x, lambda);
    double kdY = ColorUtil.getMeanChannelValue(kd);
    double rhod = kdY;

    double ksY = ColorUtil.getMeanChannelValue(ks);
    double rhos = Math.min(1.0, ksY * Math.abs(vdotn) * (n + 2.0) / (n + 1.0));

    Vector3 r = Optics.reflect(in, x.getNormal());
    double rdoto = r.dot(out);

    double pdfd = rhod / Math.PI;
    double pdfs = rdoto > 0.0 ? rhos * (Math.pow(rdoto, n) / Math.abs(odotn)) * (n + 1.0) / (2.0 * Math.PI) : 0.0;

    return pdfd + pdfs;

  }

  @Override
  public ScatteredRay scatter(SurfacePoint x, Vector3 v, boolean adjoint,
      WavelengthPacket lambda, double ru, double rv, double rj) {

    double vdotn = -v.dot(x.getNormal());
//    if (vdotn < 0.0) {
//      return null;
//    }

    Color kd = kdPainter.getColor(x, lambda);
    Color ks = ksPainter.getColor(x, lambda);
    double kdY = ColorUtil.getMeanChannelValue(kd);
    double rhod = kdY;

    double ksY = ColorUtil.getMeanChannelValue(ks);
    double rhos = Math.min(1.0, ksY * Math.abs(vdotn) * (n + 2.0) / (n + 1.0));

    if (rj < rhod) {
      Vector3 out = RandomUtil.diffuse(ru, rv).toCartesian(x.getBasis());
      Ray3 ray = new Ray3(x.getPosition(), out);
      return ScatteredRay.diffuse(ray, kd.divide(rhod), getScatteringPDF(x, v, out, adjoint, lambda));
    } else if (rj < rhod + rhos) {
      Vector3 r = Optics.reflect(v, x.getNormal());
      Vector3 out = new SphericalCoordinates(FastMath.acos(Math.pow(ru, 1.0 / (n + 1.0))), 2.0 * Math.PI * rv).toCartesian(Basis3.fromW(r));
      double ndoto = x.getNormal().dot(out);
      Color weight = ks.times(((n + 2.0) / (n + 1.0)) * Math.abs(ndoto) / rhos);
      //double rdoto = r.dot(out);
      double pdf = getScatteringPDF(x, v, out, adjoint, lambda);//rhod / Math.PI + rhos * (Math.pow(rdoto, n) / ndoto) * (n + 1.0) / (2.0 * Math.PI);
      Ray3 ray = new Ray3(x.getPosition(), out);
      return ScatteredRay.glossy(ray, weight, pdf);
    }
    return null;
  }

}

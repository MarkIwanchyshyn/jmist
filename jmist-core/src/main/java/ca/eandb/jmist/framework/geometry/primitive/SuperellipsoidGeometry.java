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
package ca.eandb.jmist.framework.geometry.primitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.eandb.jmist.framework.IntersectionRecorder;
import ca.eandb.jmist.framework.geometry.PrimitiveGeometry;
import ca.eandb.jmist.math.Basis3;
import ca.eandb.jmist.math.Box3;
import ca.eandb.jmist.math.Interval;
import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Plane3;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Sphere;
import ca.eandb.jmist.math.SphericalCoordinates;
import ca.eandb.jmist.math.Vector3;

/**
 * A superellipsoid <code>SceneElement</code>.
 * @author Brad Kimmel
 */
public final class SuperellipsoidGeometry extends PrimitiveGeometry {

  /** Serialization version ID. */
  private static final long serialVersionUID = -1585163561012430479L;

  private static final Box3 SUPERELLIPSOID_BOUNDING_BOX = new Box3(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0);

  private static final Plane3[] PLANES = {
    Plane3.YZ,
    Plane3.ZX,
    Plane3.XY,
    Plane3.throughOrigin(new Vector3( 1.0,  1.0,  0.0)),
    Plane3.throughOrigin(new Vector3( 1.0,  0.0,  1.0)),
    Plane3.throughOrigin(new Vector3( 0.0,  1.0,  1.0)),
    Plane3.throughOrigin(new Vector3( 1.0, -1.0,  0.0)),
    Plane3.throughOrigin(new Vector3(-1.0,  0.0,  1.0)),
    Plane3.throughOrigin(new Vector3( 0.0,  1.0, -1.0))
  };

  private static final double SUPERELLIPSOID_TOLERANCE = 1e-10;
  private static final int SUPERELLIPSOID_MAX_ITERATIONS = 20;
  private static final int SUPERELLIPSOID_MAX_NEWTON_ITERATIONS = 20;
  private static final double SUPERELLIPSOID_DEPTH_TOLERANCE = MathUtil.BIG_EPSILON;

  private final double e;
  private final double n;

  /**
   * Creates a new <code>SuperellipsoidGeometry</code>.
   * @param e The radial exponent.
   * @param n The vertical exponent.
   */
  public SuperellipsoidGeometry(double e, double n) {
    if (e < 0.0 || n < 0.0) {
      throw new IllegalArgumentException("e and n must be non-negative.");
    }

    this.e = e;
    this.n = n;
  }

  @Override
  public void intersect(Ray3 ray, IntersectionRecorder recorder) {
    List<Double> a = getIntervals(ray);
    List<Double> f = new ArrayList<>(a.size());
    boolean front = true;

    for (int i = 0; i < a.size() - 1; i++) {
      if (i == 0) f.add(evaluate(ray.pointAt(a.get(0))));
      f.add(evaluate(ray.pointAt(a.get(i + 1))));

      if (Math.signum(f.get(i)) != Math.signum(f.get(i + 1))) {
        // interval contains exactly one root, find it
        double t = findRoot(ray, a.get(i), a.get(i + 1));
        if (recorder.needAllIntersections() || t > SUPERELLIPSOID_DEPTH_TOLERANCE) {
          recorder.record(super.newIntersection(ray, t, front));
          if (!recorder.needAllIntersections()) return;
        }
        front = !front;
      } else {
        // interval may contain a dual root or two individual roots
        double[] t = new double[2];

        if (checkRoot(ray, a.get(i), a.get(i + 1), f.get(i), f.get(i + 1), t)) {
          if (recorder.needAllIntersections() || t[0] > SUPERELLIPSOID_DEPTH_TOLERANCE) {
            recorder.record(super.newIntersection(ray, t[0], front));
            if (!recorder.needAllIntersections()) return;
          }
          front = !front;

          if (recorder.needAllIntersections() || t[1] > SUPERELLIPSOID_DEPTH_TOLERANCE) {
            recorder.record(super.newIntersection(ray, t[1], front));
            if (!recorder.needAllIntersections()) return;
          }
          front = !front;
        }
      }
    }
  }

  private static List<Double> getIntervals(Ray3 ray) {
    ArrayList<Double> vt = new ArrayList<>(11);
    Interval I = SUPERELLIPSOID_BOUNDING_BOX.intersect(ray);
    if (I.isEmpty())
      return vt;

    vt.add(I.minimum());
    vt.add(I.maximum());

    for (int i = 0; i < PLANES.length; i++) {
      double t = PLANES[i].intersect(ray);
      if (I.contains(t))
        vt.add(t);
    }

    Collections.sort(vt);
    return vt;
  }

  private double evaluate(Point3 p) {
    return Math.pow(Math.pow(Math.abs(p.x()), 2.0 / e)
        + Math.pow(Math.abs(p.y()), 2.0 / e), e / n)
        + Math.pow(Math.abs(p.z()), 2.0 / n) - 1.0;
  }

  private static final class EvaluateResult {
    public final double value;
    public final double diff;

    public EvaluateResult(double value, double diff) {
      this.value = value;
      this.diff = diff;
    }
  };

  private EvaluateResult evaluate(Ray3 ray, double t) {
    Point3 p = ray.pointAt(t);
    Vector3 d = ray.direction();
    double X2e_1 = Math.pow(Math.abs(p.x()), 2.0 / e - 1.0);
    double Y2e_1 = Math.pow(Math.abs(p.y()), 2.0 / e - 1.0);
    double Z2n_1 = Math.pow(Math.abs(p.z()), 2.0 / n - 1.0);
    double X2e = Math.abs(p.x()) * X2e_1;
    double Y2e = Math.abs(p.y()) * Y2e_1;
    double Z2n = Math.abs(p.z()) * Z2n_1;
    double A_1 = Math.pow(X2e + Y2e, e / n - 1.0);
    double A = (X2e + Y2e) * A_1;

    return new EvaluateResult(
        A + Z2n - 1.0,
        (2.0 / n)
        * (A_1
            * (X2e_1 * d.x() * Math.signum(p.x()) + Y2e_1 * d.y()
                * Math.signum(p.y())) + Z2n_1 * d.z()
            * Math.signum(p.z()))
    );
  }

  private double findRoot(Ray3 ray, double a, double b) {
    Point3 pa = ray.pointAt(a), pb = ray.pointAt(b);
    double va = evaluate(pa), vb = evaluate(pb);

    if (Math.abs(va) < SUPERELLIPSOID_TOLERANCE)
      return a;
    if (Math.abs(vb) < SUPERELLIPSOID_TOLERANCE)
      return b;

    for (int i = 0; i < SUPERELLIPSOID_MAX_ITERATIONS; i++) {
      double  t0, t1, vt0, vt1;
      Point3  pt0, pt1;

      assert(va * vb < 0.0);

      double m = (0.0 - va) / (vb - va);
      assert(MathUtil.inRangeOO(m, 0.0, 1.0));
      t0 = MathUtil.interpolate(a, b, m);
      t1 = MathUtil.interpolate(a, b, 0.5);

      pt0 = ray.pointAt(t0);
      pt1 = ray.pointAt(t1);

      vt0 = evaluate(pt0);
      if (Math.abs(vt0) < SUPERELLIPSOID_TOLERANCE)
        return t0;

      vt1 = evaluate(pt1);
      if (Math.abs(vt1) < SUPERELLIPSOID_TOLERANCE)
        return t1;

      if (va * vt0 > 0.0) {
        if (vb * vt0 > 0.0)
          assert(false);
        else {
          if (va * vt1 > 0.0)  {
            if (m < 0.5) {
              a = t1; pa = pt1; va = vt1;
            } else {
              a = t0; pa = pt0; va = vt0;
            }
          } else {
            a = t0; pa = pt0; va = vt0;
            b = t1; pb = pt1; vb = vt1;
          }
        }
      } else {
        if (va * vt1 > 0.0) {
          a = t1; pa = pt1; va = vt1;
          b = t0; pb = pt0; vb = vt0;
        } else {
          if (m < 0.5) {
            b = t0; pb = pt0; vb = vt0;
          } else {
            b = t1; pb = pt1; vb = vt1;
          }
        }
      }
    }

    return (Math.abs(va) < Math.abs(vb)) ? a : b;
  }

  private boolean checkRoot(Ray3 ray, double a, double b, double va, double vb, double[] t) {
    boolean found_root = false;
    int i;
    double last_vt = Double.POSITIVE_INFINITY;

    t[0] = a + MathUtil.BIG_EPSILON * (b - a);

    for (i = 0; i < SUPERELLIPSOID_MAX_NEWTON_ITERATIONS; i++) {
      EvaluateResult r = evaluate(ray, t[0]);
      if (Double.isNaN(r.value))
        return false;

      if (Math.abs(r.value) < SUPERELLIPSOID_TOLERANCE) {
        found_root = true;
        break;
      }

      if (Math.signum(r.value) != Math.signum(va)) {
        double  m = t[0];
        t[0] = findRoot(ray, a, m);
        t[1] = findRoot(ray, m, b);
        return true;
      }

      if (Math.abs(r.diff) < MathUtil.EPSILON || Math.abs(r.value) > Math.abs(last_vt))
        return false;

      last_vt = r.value;
      t[0] = t[0] - r.value / r.diff;

      if (t[0] < a)
        t[0] = a + MathUtil.TINY_EPSILON * (b - a);
      else if (t[0] > b)
        t[0] = b - MathUtil.TINY_EPSILON * (b - a);
    }

    if (!found_root)
      return false;

    last_vt = Double.POSITIVE_INFINITY;
    t[1] = b + MathUtil.BIG_EPSILON * (a - b);
    found_root = false;

    for (i = 0; i < SUPERELLIPSOID_MAX_NEWTON_ITERATIONS; i++) {
      EvaluateResult r = evaluate(ray, t[1]);

      if (Math.abs(r.value) < SUPERELLIPSOID_TOLERANCE) {
        found_root = true;
        break;
      }

      if (Math.signum(r.value) != Math.signum(vb)) {
        t[1] = findRoot(ray, t[1], b);
        found_root = true;
        break;
      }

      if (Math.abs(r.diff) < MathUtil.EPSILON || Math.abs(r.value) > Math.abs(last_vt))
        break;

      last_vt = r.value;
      t[1] = t[1] - r.value / r.diff;

      if (t[1] < a)
        t[1] = a + MathUtil.TINY_EPSILON * (b - a);
      else if (t[1] > b)
        t[1] = b - MathUtil.TINY_EPSILON * (b - a);
    }

    if (!found_root) {
      t[1] = t[0];
    } else if (t[0] > t[1]) {
      double temp = t[0];
      t[0] = t[1];
      t[1] = temp;
    }

    return true;
  }

  @Override
  protected Basis3 getBasis(GeometryIntersection x) {
    return Basis3.fromW(x.getNormal(), Basis3.Orientation.RIGHT_HANDED);
  }

  @Override
  protected Vector3 getNormal(GeometryIntersection x) {
    Point3 p = x.getPosition();
    double A = Math.pow(Math.pow(Math.abs(p.x()), 2.0 / e) + Math.pow(Math.abs(p.y()), 2.0 / e), e / n - 1.0);
    double X = A * Math.pow(Math.abs(p.x()), 2.0 / e - 1.0);
    double Y = A * Math.pow(Math.abs(p.y()), 2.0 / e - 1.0);
    double Z = Math.pow(Math.abs(p.z()), 2.0 / n - 1.0);

    return new Vector3(
      Math.signum(p.x()) * X,
      Math.signum(p.y()) * Y,
      Math.signum(p.z()) * Z
    ).unit();
  }

  @Override
  protected Point2 getTextureCoordinates(GeometryIntersection x) {
    SphericalCoordinates sc = SphericalCoordinates.fromCartesian(x.getPosition().vectorFromOrigin());
    return new Point2((Math.PI + sc.azimuthal()) / (2.0 * Math.PI), sc.polar() / Math.PI);
  }

  @Override
  public Box3 boundingBox() {
    return SUPERELLIPSOID_BOUNDING_BOX;
  }

  @Override
  public Sphere boundingSphere() {
    return new Sphere(Point3.ORIGIN, Math.sqrt(2.0));
  }

}

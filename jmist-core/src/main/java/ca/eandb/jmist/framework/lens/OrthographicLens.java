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
package ca.eandb.jmist.framework.lens;

import ca.eandb.jmist.framework.ScatteredRay;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.path.EyeNode;
import ca.eandb.jmist.framework.path.EyeTerminalNode;
import ca.eandb.jmist.framework.path.PathInfo;
import ca.eandb.jmist.math.HPoint3;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>Lens</code> that projects the world on to the image plane
 * ortographically.
 * @author Brad Kimmel
 */
public final class OrthographicLens extends AbstractLens {

  /** Serialization version ID. */
  private static final long serialVersionUID = -5086512652648110011L;

  /** The extent of the image plane along the x-axis. */
  private final double width;

  /** The extent of the image plane along the y-axis. */
  private final double height;

  /** The area of the image plane (in meters squared). */
  private final double area;

  /**
   * Creates a new <code>OrthographicLens</code>.
   * @param width The extent of the image plane along the x-axis.
   * @param height The extent of the image plane along the y-axis.
   */
  public OrthographicLens(double width, double height) {
    this.width = width;
    this.height = height;
    this.area = width * height;
  }

//  /* (non-Javadoc)
//   * @see ca.eandb.jmist.framework.Lens#rayAt(ca.eandb.jmist.math.Point2)
//   */
//  public Ray3 rayAt(Point2 p) {
//    return new Ray3(
//        new Point3(
//            (p.x() - 0.5) * this.width,
//            (0.5 - p.y()) * this.height,
//            0.0
//        ),
//        Vector3.NEGATIVE_K
//    );
//  }
//
//  /* (non-Javadoc)
//   * @see ca.eandb.jmist.framework.Lens#areaOfAperture()
//   */
//  public double areaOfAperture() {
//    return area;
//  }
//
//  /* (non-Javadoc)
//   * @see ca.eandb.jmist.framework.Lens#project(ca.eandb.jmist.math.Point3)
//   */
//  public Projection project(final Point3 p) {
//    if (-p.z() < MathUtil.EPSILON) {
//      return null;
//    }
//    return new Projection() {
//      public Point2 pointOnImagePlane() {
//        return new Point2((p.x() / width) + 0.5, 0.5 - (p.y() / height));
//      }
//
//      public Point3 pointOnLens() {
//        return new Point3(p.x(), p.y(), 0.0);
//      }
//
//      public double importance() {
//        return 1.0; // FIXME Light tracing will not work until this is corrected.
//      }
//    };
//  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.Lens#sample(ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.path.PathInfo, double, double, double)
   */
  @Override
  public EyeNode sample(Point2 p, PathInfo pathInfo, double ru, double rv,
      double rj) {
    return new Node(p, pathInfo, ru, rv, rj);
  }

  /**
   * An <code>EyeNode</code> generated by a <code>OrthographicLens</code>.
   */
  private final class Node extends EyeTerminalNode {

    /** Projected point on the image plane. */
    private final Point2 pointOnImagePlane;

    /**
     * Creates a <code>Node</code>.
     * @param pointOnImagePlane The <code>Point2</code> on the image plane.
     * @param pathInfo The <code>PathInfo</code> describing the context for
     *     this node.
     */
    public Node(Point2 pointOnImagePlane, PathInfo pathInfo, double ru, double rv, double rj) {
      super(pathInfo, ru, rv, rj);
      this.pointOnImagePlane = pointOnImagePlane;
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.EyeNode#sample(ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.Random)
     */
    public ScatteredRay sample(double ru, double rv, double rj) {
      Point2 p = pointOnImagePlane;
      Ray3 ray = new Ray3(
          new Point3(
              (p.x() - 0.5) * width,
              (0.5 - p.y()) * height,
              0.0),
          Vector3.NEGATIVE_K);
      Color color = getWhite();
      return ScatteredRay.specular(ray, color, 1.0);
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#scatterTo(ca.eandb.jmist.framework.path.PathNode)
     */
    public Color scatter(Vector3 v) {
      return getBlack();
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.EyeNode#project(ca.eandb.jmist.math.HPoint3)
     */
    public Point2 project(HPoint3 x) {
      if (!x.isPoint()) {
        return null;
      }
      Point3 p = x.toPoint3();
      return new Point2(0.5 + p.x() / width, 0.5 - p.y() / height);
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#getCosine(ca.eandb.jmist.math.Vector3)
     */
    public double getCosine(Vector3 v) {
      return -v.z() / v.length();
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#getPosition()
     */
    public HPoint3 getPosition() {
      Point2 p = pointOnImagePlane;
      return new Point3(
          (p.x() - 0.5) * width,
          (0.5 - p.y()) * height,
          0.0);
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#getPDF()
     */
    public double getPDF() {
      return 1.0 / area;
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#isSpecular()
     */
    public boolean isSpecular() {
      return false;
    }

    /* (non-Javadoc)
     * @see ca.eandb.jmist.framework.path.PathNode#getPDF(ca.eandb.jmist.math.Vector3)
     */
    public double getPDF(Vector3 v) {
      return 0.0;
    }

  }

}

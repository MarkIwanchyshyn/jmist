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
package ca.eandb.jmist.framework.geometry;

import ca.eandb.jmist.framework.Intersection;
import ca.eandb.jmist.framework.IntersectionRecorder;
import ca.eandb.jmist.framework.NearestIntersectionRecorder;
import ca.eandb.jmist.framework.ShadingContext;
import ca.eandb.jmist.framework.SurfacePoint;
import ca.eandb.jmist.math.Box3;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Sphere;

/**
 * A <code>SceneElement</code> composed of a single primitive.
 * @author Brad Kimmel
 */
public abstract class PrimitiveGeometry extends AbstractGeometry {

  /** Serialization version ID. */
  private static final long serialVersionUID = 5445535598470809422L;

  /**
   * Ensures that the specified primitive index is valid for this
   * <code>PrimitiveGeometry</code>.  Since there is only one primitive, it
   * must be zero.
   * @param index The value to check.
   * @exception IndexOutOfBoundsException if <code>index</code> is out of
   *     range, i.e., <code>index != 0</code>.
   */
  private void validate(int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException();
    }
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#generateRandomSurfacePoint(int, ca.eandb.jmist.framework.ShadingContext)
   */
  @Override
  public final void generateRandomSurfacePoint(int index, ShadingContext context, double ru, double rv, double rj) {
    validate(index);
    generateRandomSurfacePoint(context, ru, rv, rj);
  }

  /**
   * Generates a random surface point uniformly distributed on the surface of
   * this <code>PrimitiveGeometry</code> (optional operation).
   * @param context The <code>ShadingContext</code> in which to set the
   *     generated surface point.
   * @param ru The first random variable (must be in [0, 1]).
   * @param rv The second random variable (must be in [0, 1]).
   * @param rj The third random variable (must be in [0, 1]).
   */
  public void generateRandomSurfacePoint(ShadingContext context, double ru, double rv, double rj) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#generateImportanceSampledSurfacePoint(int, ca.eandb.jmist.framework.SurfacePoint, ca.eandb.jmist.framework.ShadingContext)
   */
  @Override
  public final double generateImportanceSampledSurfacePoint(int index,
      SurfacePoint x, ShadingContext context, double ru, double rv, double rj) {
    validate(index);
    return generateImportanceSampledSurfacePoint(0, x, context, ru, rv, rj);
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.SceneElement#getBoundingBox(int)
   */
  public final Box3 getBoundingBox(int index) {
    validate(index);
    return boundingBox();
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.SceneElement#getBoundingSphere(int)
   */
  public final Sphere getBoundingSphere(int index) {
    validate(index);
    return boundingSphere();
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.SceneElement#getNumPrimitives()
   */
  public final int getNumPrimitives() {
    return 1;
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.SceneElement#getSurfaceArea(int)
   */
  @Override
  public final double getSurfaceArea(int index) {
    validate(index);
    return getSurfaceArea();
  }

  /**
   * Computes the surface area of this <code>PrimitiveGeometry</code>
   * (optional operation).
   * @return The surface area of this <code>PrimitiveGeometry</code>.
   */
  public double getSurfaceArea() {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.SceneElement#intersect(int, ca.eandb.jmist.math.Ray3, ca.eandb.jmist.framework.IntersectionRecorder)
   */
  public final void intersect(int index, Ray3 ray, IntersectionRecorder recorder) {
    validate(index);
    intersect(ray, recorder);
  }

  /**
   * Computes the intersections of the specified ray with this
   * <code>PrimitiveGeometry</code>.
   * @param ray The <code>Ray3</code> to intersect with.
   * @param recorder The <code>IntersectionRecorder</code> to add the
   *     computed intersections to.
   */
  public abstract void intersect(Ray3 ray, IntersectionRecorder recorder);

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#intersects(int, ca.eandb.jmist.math.Ray3)
   */
  @Override
  public final boolean visibility(int index, Ray3 ray) {
    validate(index);
    return visibility(ray);
  }

  /**
   * Determines if the specified ray intersects with this
   * <code>PrimitiveGeometry</code>.
   * @param ray The <code>Ray3</code> to intersect with.
   * @return A value indicating if the ray intersects this
   *     <code>PrimitiveGeometry</code>.
   */
  public boolean visibility(Ray3 ray) {
    Intersection x = NearestIntersectionRecorder
        .computeNearestIntersection(ray, this);
    return (x == null);
  }

  /* (non-Javadoc)
   * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#intersects(int, ca.eandb.jmist.math.Box3)
   */
  @Override
  public final boolean intersects(int index, Box3 box) {
    validate(index);
    return intersects(box);
  }

  /**
   * Determines if the specified box intersects with the surface of this
   * <code>PrimitiveGeometry</code>.
   * @param box The <code>Box3</code> to check for intersection with.
   * @return A value indicating if the box intersects the surface of this
   *     <code>PrimitiveGeometry</code>.
   */
  public boolean intersects(Box3 box) {
    return box.intersects(boundingBox());
  }

  protected final GeometryIntersection newIntersection(Ray3 ray,
      double distance, boolean front) {

    return super.newIntersection(ray, distance, front, 0);

  }

  protected final GeometryIntersection newSurfacePoint(Point3 p, boolean front) {

    return super.newSurfacePoint(p, front, 0);

  }

  protected final GeometryIntersection newSurfacePoint(Point3 p) {

    return super.newSurfacePoint(p, true, 0);

  }

}

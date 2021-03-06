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
package ca.eandb.jmist.framework.accel;

import ca.eandb.jmist.framework.IntersectionRecorder;
import ca.eandb.jmist.framework.NearestIntersectionRecorder;
import ca.eandb.jmist.framework.SceneElement;
import ca.eandb.jmist.framework.Visitor;
import ca.eandb.jmist.framework.scene.SceneElementDecorator;
import ca.eandb.jmist.math.Interval;
import ca.eandb.jmist.math.Ray3;

/**
 * A decorator <code>SceneElement</code> that applies a bounding box hierarchy
 * to the decorated <code>SceneElement</code> to accelerate ray-intersection
 * tests.
 *
 * @author Brad Kimmel
 */
public final class BBHSceneElement extends SceneElementDecorator {

  /** Serialization version ID. */
  private static final long serialVersionUID = -3334720789877135936L;

  /**
   * The <code>BoundingBoxHierarchy3</code> to use for accelerating
   * ray-intersection tests.
   */
  private transient BoundingBoxHierarchy3 bbh = null;

  /**
   * @param inner The <code>SceneElement</code> to apply the bounding box
   *     hierarchy to.
   */
  public BBHSceneElement(SceneElement inner) {
    super(inner);
  }

  /** Called to build the BBH on demand. */
  private void ensureReady() {
    if (bbh == null) {
      build();
    }
  }

  /** Builds the BBH. */
  private synchronized void build() {
    if (bbh != null) { // double check inside synchronized method
      return;
    }

    bbh = new BoundingBoxHierarchy3();
    for (int i = 0, n = getNumPrimitives(); i < n; i++) {
      bbh.addItem(i, getBoundingBox(i));
    }
  }

  /**
   * The <code>Visitor</code> used by the BBH.
   * @author Brad Kimmel
   */
  private final class BBHVisitor implements Visitor {

    /** The <code>Ray3</code> to perform intersection tests with. */
    private final Ray3 ray;

    /** The <code>IntersectionRecorder</code> to record intersections to. */
    private final IntersectionRecorder recorder;

    /**
     * Creates a new <code>BBHVisitor</code>.
     * @param ray The <code>Ray3</code> to perform intersection tests with.
     * @param recorder The <code>IntersectionRecorder</code> to record
     *     intersections to.
     */
    public BBHVisitor(Ray3 ray, IntersectionRecorder recorder) {
      this.ray = ray;
      this.recorder = recorder;
    }

    @Override
    public boolean visit(Object object) {
      int index = (Integer) object;
      intersect(index, ray, recorder);
      return true;
    }

  }

  @Override
  public void intersect(Ray3 ray, IntersectionRecorder recorder) {
    ensureReady();
    bbh.intersect(ray, recorder.interval(), new BBHVisitor(ray, recorder));
  }

  @Override
  public boolean visibility(Ray3 ray) {
    NearestIntersectionRecorder recorder = new NearestIntersectionRecorder(new Interval(0.0, ray.limit()));
    intersect(ray, recorder);
    return recorder.isEmpty();
  }

}

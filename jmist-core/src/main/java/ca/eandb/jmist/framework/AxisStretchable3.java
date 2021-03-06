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
package ca.eandb.jmist.framework;

/**
 * A three dimensional object that can be stretched along the coordinate axes.
 * @author Brad Kimmel
 */
public interface AxisStretchable3 extends Scalable {

  /**
   * Stretches the object along the x-axis.
   * Equivalent to {@code this.stretch(cx, 1.0, 1.0);}
   * @param cx The factor by which to stretch the object along the x-axis.
   * @see #stretch(double, double, double)
   */
  void stretchX(double cx);

  /**
   * Stretches the object along the y-axis.
   * Equivalent to {@code this.stretch(1.0, cy, 1.0);}
   * @param cy The factor by which to stretch the object along the y-axis.
   * @see #stretch(double, double, double)
   */
  void stretchY(double cy);

  /**
   * Stretches the object along the z-axis.
   * Equivalent to {@code this.stretch(1.0, 1.0, cz);}
   * @param cz The factor by which to stretch the object along the z-axis.
   * @see #stretch(double, double, double)
   */
  void stretchZ(double cz);

  /**
   * Stretches an object along each axis independently.
   * @param cx The factor by which to scale the object along the x-axis.
   * @param cy The factor by which to scale the object along the y-axis.
   * @param cz The factor by which to scale the object along the z-axis.
   */
  void stretch(double cx, double cy, double cz);

}

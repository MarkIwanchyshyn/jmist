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
package ca.eandb.jmist.framework.display.visualizer;

import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.RGB;

public final class AutomaticLinearChannelVisualizer extends
    NonVolatileColorVisualizer {

  /** Serialization version ID. */
  private static final long serialVersionUID = 6203765621561793103L;

  private final int channel;

  private final boolean allowMinimumToFloat;

  private ColorVisualizer inner;

  public AutomaticLinearChannelVisualizer(int channel, boolean allowMinimumToFloat) {
    this.channel = channel;
    this.allowMinimumToFloat = allowMinimumToFloat;
    this.inner = new LinearChannelVisualizer(channel);
  }

  @Override
  public boolean analyze(Iterable<Color> samples) {
    double minimum = Double.POSITIVE_INFINITY;
    double maximum = Double.NEGATIVE_INFINITY;
    for (Color color : samples) {
      double value = color.getValue(channel);
      if (value < minimum) {
        minimum = value;
      }
      if (value > maximum) {
        maximum = value;
      }
    }
    inner = allowMinimumToFloat ?
        new LinearChannelVisualizer(channel, minimum, maximum) :
        new LinearChannelVisualizer(channel, maximum);
    return true;
  }

  @Override
  public RGB visualize(Color color) {
    return inner.visualize(color);
  }

}

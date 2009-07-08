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

package ca.eandb.jmist.framework.random;

import ca.eandb.jmist.framework.Random;

/**
 * Adapts a <code>java.util.Random</code>.
 * @author Brad Kimmel
 */
public final class RandomAdapter implements Random {

	/** The wrapped <code>java.util.Random</code>. */
	private final java.util.Random rnd;

	/**
	 * Creates a new <code>RandomAdapter</code>.
	 * @param rnd The <code>java.util.Random</code> to adapt.
	 */
	public RandomAdapter(java.util.Random rnd) {
		this.rnd = rnd;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Random#createCompatibleRandom()
	 */
	@Override
	public Random createCompatibleRandom() {
		return new RandomAdapter(rnd);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Random#next()
	 */
	@Override
	public double next() {
		return rnd.nextDouble();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Random#reset()
	 */
	@Override
	public void reset() {
		/* nothing to do. */
	}

}
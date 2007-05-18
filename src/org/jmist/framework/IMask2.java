/**
 *
 */
package org.jmist.framework;

import org.jmist.toolkit.*;

/**
 * @author bkimmel
 *
 */
public interface IMask2 {

	/**
	 * Evaluates the mask at the specified point.
	 * @param p The point at which to evaluate the mask.
	 * @return The opacity (dimensionless) at the
	 * 		specified point.  The return value shall
	 * 		fall between 0 and 1 inclusive.
	 */
	double opacity(Point2 p);

}

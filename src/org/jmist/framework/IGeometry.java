/**
 *
 */
package org.jmist.framework;

import org.jmist.toolkit.*;

/**
 * @author bkimmel
 *
 */
public interface IGeometry extends IBounded3, IVisibilityFunction3 {

	IIntersection intersect(Ray3 ray, Interval I);

	ISurfacePoint generateRandomSurfacePoint();

}
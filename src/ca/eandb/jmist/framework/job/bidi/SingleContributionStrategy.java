/**
 *
 */
package ca.eandb.jmist.framework.job.bidi;

import ca.eandb.jmist.framework.Lens;
import ca.eandb.jmist.framework.Light;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.framework.path.PathInfo;
import ca.eandb.jmist.framework.path.PathNode;
import ca.eandb.jmist.framework.path.PathUtil;
import ca.eandb.jmist.math.Point2;

/**
 * @author Brad
 *
 */
public final class SingleContributionStrategy implements
		BidiPathStrategy {

	/** Serialization version ID. */
	private static final long serialVersionUID = 7678023287170417943L;


	private final int lightDepth;

	private final int eyeDepth;

	public SingleContributionStrategy(int lightDepth, int eyeDepth) {
		this.lightDepth = lightDepth;
		this.eyeDepth = eyeDepth;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.path.BidiPathStrategy#getWeight(ca.eandb.jmist.framework.path.PathNode, ca.eandb.jmist.framework.path.PathNode)
	 */
	public double getWeight(PathNode lightNode, PathNode eyeNode) {
		int s = lightNode != null ? lightNode.getDepth() + 1 : 0;
		int t = eyeNode != null ? eyeNode.getDepth() + 1 : 0;
		return (s == lightDepth && t == eyeDepth) ? 1.0 : 0.0;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.path.BidiPathStrategy#traceEyePath(ca.eandb.jmist.framework.Lens, ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.path.PathInfo, ca.eandb.jmist.framework.Random)
	 */
	public PathNode traceEyePath(Lens lens, Point2 p, PathInfo pathInfo,
			Random rnd) {
		if (eyeDepth > 0) {
			PathNode head = lens.sample(p, pathInfo, rnd.next(), rnd.next(), rnd.next());
			return PathUtil.expand(head, eyeDepth - 1, rnd);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.path.BidiPathStrategy#traceLightPath(ca.eandb.jmist.framework.Light, ca.eandb.jmist.framework.path.PathInfo, ca.eandb.jmist.framework.Random)
	 */
	public PathNode traceLightPath(Light light, PathInfo pathInfo, Random rnd) {
		if (lightDepth > 0) {
			PathNode head = light.sample(pathInfo, rnd.next(), rnd.next(), rnd.next());
			return PathUtil.expand(head, lightDepth - 1, rnd);
		}
		return null;
	}

}
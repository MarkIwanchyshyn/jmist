/**
 *
 */
package ca.eandb.jmist.framework.path;

/**
 * @author Brad
 *
 */
public abstract class LightTerminalNode extends AbstractTerminalNode implements
		LightNode {

	/**
	 * @param pathInfo
	 */
	public LightTerminalNode(PathInfo pathInfo, double ru, double rv, double rj) {
		super(pathInfo, ru, rv, rj);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.path.PathNode#isOnLightPath()
	 */
	public final boolean isOnLightPath() {
		return true;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.path.PathNode#reverse(ca.eandb.jmist.framework.path.PathNode, ca.eandb.jmist.framework.path.PathNode)
	 */
	public PathNode reverse(PathNode newParent, PathNode grandChild) {
		return null;
	}

}
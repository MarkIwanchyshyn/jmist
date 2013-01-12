/**
 * 
 */
package ca.eandb.jmist.framework;

import java.io.Serializable;

/**
 * Represents something that can be animated.
 * @author Brad Kimmel
 */
public interface Animator extends Serializable {

	void setTime(double time);
	
	public static final Animator STATIC = new Animator() {
		private static final long serialVersionUID = 7446813488725915900L;
		public final void setTime(double time) {
			/* nothing to do. */
		}
	};
	
}

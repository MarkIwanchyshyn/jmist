/**
 *
 */
package ca.eandb.jmist.framework.material.biospec;

import ca.eandb.jmist.framework.ScatteredRay;
import ca.eandb.jmist.framework.SurfacePoint;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.ColorUtil;
import ca.eandb.jmist.framework.color.Spectrum;
import ca.eandb.jmist.framework.color.WavelengthPacket;
import ca.eandb.jmist.framework.material.OpaqueMaterial;
import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Vector3;

/**
 * @author brad
 *
 */
public final class AbsorbingMaterial extends OpaqueMaterial {

	/** Serialization version ID. */
	private static final long serialVersionUID = 5282310548843499966L;

	/** The absorption coefficient of the medium (in m<sup>-1</sup>). */
	private final Spectrum absorptionCoefficient;

	/** The thickness of the medium (in meters). */
	private final double thickness;

	/**
	 * Creates a new <code>AbsorbingSurfaceScatterer</code>.
	 * @param absorptionCoefficient The absorption coefficient of the medium
	 * 		(in m<sup>-1</sup>).
	 * @param thickness The thickness of the medium (in meters).
	 */
	public AbsorbingMaterial(Spectrum absorptionCoefficient,
			double thickness) {
		this.absorptionCoefficient = absorptionCoefficient;
		this.thickness = thickness;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.material.AbstractMaterial#scatter(ca.eandb.jmist.framework.SurfacePoint, ca.eandb.jmist.math.Vector3, boolean, ca.eandb.jmist.framework.color.WavelengthPacket, double, double, double)
	 */
	@Override
	public ScatteredRay scatter(SurfacePoint x, Vector3 v, boolean adjoint,
			WavelengthPacket lambda, double ru, double rv, double rj) {
		
		Color col = absorptionCoefficient.sample(lambda);
//		double abs = ColorUtil.getMeanChannelValue(col);
//		
//		if (abs > MathUtil.EPSILON) {
//			double p = -Math.log(1.0 - ru) * Math.abs(x.getNormal().dot(v)) / abs;
//			
//			col = col.times(-thickness).exp();
//			col = col.divide(ColorUtil.getMeanChannelValue(col));
//			
//			if (p > thickness) {
//				return ScatteredRay.transmitSpecular(new Ray3(x.getPosition(), v), col, 1.0);
//			}
//		}
//		
//		return null;
		
		col = col.times(-thickness / Math.abs(x.getNormal().dot(v))).exp();
		return ScatteredRay.transmitSpecular(new Ray3(x.getPosition(), v), col, 1.0);		
		
	}
	
	

}
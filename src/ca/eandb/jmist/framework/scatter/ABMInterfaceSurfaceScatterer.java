/**
 *
 */
package ca.eandb.jmist.framework.scatter;

import ca.eandb.jmist.framework.Function1;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.framework.SurfacePointGeometry;
import ca.eandb.jmist.framework.function.ConstantFunction1;
import ca.eandb.jmist.framework.random.RandomUtil;
import ca.eandb.jmist.math.Basis3;
import ca.eandb.jmist.math.Optics;
import ca.eandb.jmist.math.SphericalCoordinates;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>SurfaceScatterer</code> that represents an interface between two
 * layers in the ABM-U or ABM-B implementation.
 * @see ABMSurfaceScatterer
 * @author Brad Kimmel
 */
public final class ABMInterfaceSurfaceScatterer implements SurfaceScatterer {

	/** The refractive index of the medium above the interface. */
	private final Function1 riBelow;

	/** The refractive index of the medium below the interface. */
	private final Function1 riAbove;

	/**
	 * The perturbation exponent used for a ray reflected from the top side of
	 * the interface.
	 */
	private final double n11;

	/**
	 * The perturbation exponent used for a ray transmitted from the top side
	 * to the bottom side of the interface.
	 */
	private final double n12;

	/**
	 * The perturbation exponent used for a ray transmitted from the bottom
	 * side to the top side of the interface.
	 */
	private final double n21;

	/**
	 * The perturbation exponent used for a ray reflected from the bottom side
	 * of the interface.
	 */
	private final double n22;

	/**
	 * Creates a new <code>ABMInterfaceSurfaceScatterer</code>.
	 * @param riBelow The refractive index of the medium above the interface.
	 * @param riAbove The refractive index of the medium below the interface.
	 * @param n11 The perturbation exponent used for a ray reflected from the
	 * 		top side of the interface.
	 * @param n12 The perturbation exponent used for a ray transmitted from the
	 * 		top side to the bottom side of the interface.
	 * @param n21 The perturbation exponent used for a ray transmitted from the
	 * 		bottom side to the top side of the interface.
	 * @param n22 The perturbation exponent used for a ray reflected from the
	 * 		bottom side of the interface.
	 */
	public ABMInterfaceSurfaceScatterer(Function1 riBelow, Function1 riAbove, double n11, double n12, double n21, double n22) {
		this.riBelow = riBelow;
		this.riAbove = riAbove;
		this.n11 = n11;
		this.n12 = n12;
		this.n21 = n21;
		this.n22 = n22;

//		try {
//			FileOutputStream file = new FileOutputStream("/Users/brad/interface.csv", true);
//			PrintStream out = new PrintStream(new CompositeOutputStream().addChild(System.out).addChild(file));
//
//			Vector3 N = Vector3.K;
//			for (int angle = 0; angle < 90; angle++) {
//				double rad = Math.toRadians(angle);
//				Vector3 v = new Vector3(Math.sin(rad), 0.0, -Math.cos(rad));
//				for (int lambda = 400; lambda <= 700; lambda += 5) {
//					double n1 = riAbove.evaluate(1e-9 * (double) lambda);
//					double n2 = riBelow.evaluate(1e-9 * (double) lambda);
//					double R = Optics.reflectance(v, n1, n2, N);
//					if (lambda > 400) {
//						out.print(',');
//					}
//					out.print(R);
//				}
//				out.println();
//			}
//			for (int angle = 0; angle < 90; angle++) {
//				double rad = Math.toRadians(angle);
//				Vector3 v = new Vector3(Math.sin(rad), 0.0, Math.cos(rad));
//				for (int lambda = 400; lambda <= 700; lambda += 5) {
//					double n1 = riAbove.evaluate(1e-9 * (double) lambda);
//					double n2 = riBelow.evaluate(1e-9 * (double) lambda);
//					double R = Optics.reflectance(v, n1, n2, N);
//					if (lambda > 400) {
//						out.print(',');
//					}
//					out.print(R);
//				}
//				out.println();
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Creates a new <code>ABMInterfaceSurfaceScatterer</code>.
	 * @param riBelow The refractive index of the medium above the interface.
	 * @param riAbove The refractive index of the medium below the interface.
	 * @param n11 The perturbation exponent used for a ray reflected from the
	 * 		top side of the interface.
	 * @param n12 The perturbation exponent used for a ray transmitted from the
	 * 		top side to the bottom side of the interface.
	 * @param n21 The perturbation exponent used for a ray transmitted from the
	 * 		bottom side to the top side of the interface.
	 * @param n22 The perturbation exponent used for a ray reflected from the
	 * 		bottom side of the interface.
	 */
	public ABMInterfaceSurfaceScatterer(double riBelow, double riAbove, double n11, double n12, double n21, double n22) {
		this(new ConstantFunction1(riBelow), new ConstantFunction1(riAbove), n11, n12, n21, n22);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.scatter.SurfaceScatterer#scatter(ca.eandb.jmist.framework.SurfacePointGeometry, ca.eandb.jmist.math.Vector3, boolean, ca.eandb.jmist.framework.color.WavelengthPacket, ca.eandb.jmist.framework.Random)
	 */
	public Vector3 scatter(SurfacePointGeometry x, Vector3 v, boolean adjoint,
			double lambda, Random rnd) {

		double n1 = riAbove.evaluate(lambda);
		double n2 = riBelow.evaluate(lambda);
		Vector3 N = x.getNormal();
		double R = Optics.reflectance(v, n1, n2, N);

		boolean fromSide = (v.dot(N) < 0.0);
		boolean toSide;
		Vector3 w;
		double specularity;

		if (RandomUtil.bernoulli(R, rnd)) {
			toSide = fromSide;
			specularity = fromSide ? n11 : n22;
			w = Optics.reflect(v, N);
		} else {
			toSide = !fromSide;
			specularity = fromSide ? n12 : n21;
			w = Optics.refract(v, n1, n2, N);
		}

		if (!Double.isInfinite(specularity)) {
			Basis3 basis = Basis3.fromW(w);
			do {
				SphericalCoordinates perturb = new SphericalCoordinates(
						Math.acos(Math.pow(1.0 - rnd.next(), 1.0 / (specularity + 1.0))),
						2.0 * Math.PI * rnd.next());
				w = perturb.toCartesian(basis);
			} while ((w.dot(N) > 0.0) != toSide);
		}

		return w;
	}

}

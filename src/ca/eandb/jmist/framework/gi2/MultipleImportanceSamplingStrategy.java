/**
 *
 */
package ca.eandb.jmist.framework.gi2;

import ca.eandb.jmist.framework.Function1;
import ca.eandb.jmist.framework.Lens;
import ca.eandb.jmist.framework.Light;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Vector3;

/**
 * @author Brad
 *
 */
public final class MultipleImportanceSamplingStrategy implements BidiPathStrategy {

	private final int maxLightDepth;

	private final int maxEyeDepth;

	private final Function1 heuristic;

	/**
	 * @param maxLightDepth
	 * @param maxEyeDepth
	 * @param heuristic
	 */
	public MultipleImportanceSamplingStrategy(int maxLightDepth, int maxEyeDepth, Function1 heuristic) {
		this.maxLightDepth = maxLightDepth;
		this.maxEyeDepth = maxEyeDepth;
		this.heuristic = heuristic;
	}

	public static final MultipleImportanceSamplingStrategy useBalanceHeuristic(
			int maxLightDepth, int maxEyeDepth) {
		return new MultipleImportanceSamplingStrategy(maxLightDepth,
				maxEyeDepth, null);
	}

	public static final MultipleImportanceSamplingStrategy usePowerHeuristic(
			int maxLightDepth, int maxEyeDepth) {
		return new MultipleImportanceSamplingStrategy(maxLightDepth,
				maxEyeDepth, new Function1() {
					private static final long serialVersionUID = -343284523840769794L;
					public double evaluate(double x) {
						return x * x;
					}
				});
	}

	public static final MultipleImportanceSamplingStrategy usePowerHeuristic(
			final double exp, int maxLightDepth, int maxEyeDepth) {
		return new MultipleImportanceSamplingStrategy(maxLightDepth,
				maxEyeDepth, new Function1() {
					private static final long serialVersionUID = -3002611588758156695L;
					public double evaluate(double x) {
						return Math.pow(x, exp);
					}
				});
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#getWeight(ca.eandb.jmist.framework.gi2.PathNode, ca.eandb.jmist.framework.gi2.PathNode)
	 */
	public double getWeight(PathNode lightNode, PathNode eyeNode) {
		int s = getNodeDepth(lightNode);
		int t = getNodeDepth(eyeNode);
		int k = s + t - 1;

		if (k < 0) {
			return 0.0;
		}

		double gle = lightNode == null || eyeNode == null ? 1.0
				: PathUtil.getGeometricFactor(lightNode, eyeNode);
		double[] pdf = new double[k + 2];

		// TODO Populate pdf
		pdf[s] = 1.0;

		// Shorter eye path, longer light path
		if (eyeNode != null) {
			if (s < maxLightDepth) {
				if (lightNode != null) {
					Vector3 v = PathUtil.getDirection(lightNode, eyeNode);
					double ratio = (lightNode.getPDF(v) * gle)
							/ (eyeNode.getPDF() * eyeNode.getGeometricFactor());
					pdf[s + 1] = pdf[s] * ratio;
				} else if (eyeNode instanceof ScatteringNode) {
					ScatteringNode scatNode = (ScatteringNode) eyeNode;
					double ratio = scatNode.getSourcePDF()
							/ (eyeNode.getPDF() * eyeNode.getGeometricFactor());
					pdf[s + 1] = pdf[s] * ratio;
				}
			}

			PathNode zjp2 = lightNode;
			PathNode zjp1 = eyeNode; // z_{j+1}, j == k - i
			for (int i = s + 1; i <= k; i++) {
				if (i + 1 > maxLightDepth) {
					break;
				}

				PathNode zj = zjp1.getParent();
				double rpdf = 0.0;
				if (zjp2 != null) {
					Vector3 v = PathUtil.getDirection(zjp2, zjp1);
					rpdf = zjp1.getReversePDF(v);
				} else if (zjp1 instanceof ScatteringNode) {
					ScatteringNode scatNode = (ScatteringNode) zjp1;
					Vector3 v = PathUtil.getDirection(zjp1, zj);
					rpdf = scatNode.getSourcePDF(v);
				}
				double ratio = (rpdf * zjp1.getGeometricFactor())
						/ (zj.getPDF() * zj.getGeometricFactor());

				pdf[i + 1] = pdf[i] * ratio;

				zjp2 = zjp1;
				zjp1 = zj;
			}

			PathNode zj = eyeNode;
			for (int i = s; i <= k; i++) {
				if (zj.isSpecular()) {
					pdf[i + 1] = 0.0;
				}
				zj = zj.getParent();
			}
		}

		// Shorter light path, longer eye path
		if (lightNode != null) {
			if (t < maxEyeDepth) {
				if (eyeNode != null) {
					Vector3 v = PathUtil.getDirection(eyeNode, lightNode);
					double ratio = (eyeNode.getPDF(v) * gle)
							/ (lightNode.getPDF() * lightNode.getGeometricFactor());
					pdf[s - 1] = pdf[s] * ratio;
				} else {
					/* Here the ratio has in the numerator the probability density
					 * of selecting the node at the end of the light path (i.e.,
					 * lightNode) as the starting point for an eye path.  Since we
					 * do not currently support intersections with the aperture,
					 * this probability is zero.  Therefore pdf[s - 1] == 0.0, so
					 * we have nothing to do here.
					 */
				}
			}

			PathNode yip1 = eyeNode;
			PathNode yi = lightNode;
			for (int i = s - 1; i > 0; i--) {
				if (k + 2 - i > maxEyeDepth) {
					break;
				}

				PathNode yim1 = yi.getParent();
				double rpdf = yip1 != null
						? yi.getReversePDF(PathUtil.getDirection(yip1, yi))
						: 0.0;
				double ratio = (rpdf * yi.getGeometricFactor())
						/ (yim1.getPDF() * yim1.getGeometricFactor());

				pdf[i - 1] = pdf[i] * ratio;

				yip1 = yi;
				yi = yim1;
			}

			PathNode yim1 = lightNode;
			for (int i = s; i > 0; i--) {
				if (yim1.isSpecular()) {
					pdf[i - 1] = 0.0;
				}
				yim1 = yim1.getParent();
			}
		}

		if (heuristic != null) {
			for (int i = 0; i < pdf.length; i++) {
				pdf[i] = heuristic.evaluate(pdf[i]);
			}
		}
		double total = MathUtil.sum(pdf);

		return pdf[s] / total;
	}

	private final int getNodeDepth(PathNode node) {
		return node != null ? node.getDepth() + 1 : 0;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#traceEyePath(ca.eandb.jmist.framework.Lens, ca.eandb.jmist.math.Point2, ca.eandb.jmist.framework.gi2.PathInfo, ca.eandb.jmist.framework.Random)
	 */
	public PathNode traceEyePath(Lens lens, Point2 p, PathInfo pathInfo,
			Random rnd) {
		if (maxEyeDepth > 0) {
			PathNode head = lens.sample(p, pathInfo, rnd);
			return PathUtil.expand(head, maxEyeDepth - 1, rnd);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.gi2.BidiPathStrategy#traceLightPath(ca.eandb.jmist.framework.Light, ca.eandb.jmist.framework.gi2.PathInfo, ca.eandb.jmist.framework.Random)
	 */
	public PathNode traceLightPath(Light light, PathInfo pathInfo, Random rnd) {
		if (maxLightDepth > 0) {
			PathNode head = light.sample(pathInfo, rnd);
			return PathUtil.expand(head, maxLightDepth - 1, rnd);
		}
		return null;
	}

}

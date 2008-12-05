/**
 *
 */
package ca.eandb.jmist.packages.shader.ray;

import ca.eandb.jmist.framework.Geometry;
import ca.eandb.jmist.framework.Illuminable;
import ca.eandb.jmist.framework.Intersection;
import ca.eandb.jmist.framework.Light;
import ca.eandb.jmist.framework.Material;
import ca.eandb.jmist.framework.Observer;
import ca.eandb.jmist.framework.RayShader;
import ca.eandb.jmist.framework.ScatterResult;
import ca.eandb.jmist.framework.SpectralEstimator;
import ca.eandb.jmist.framework.Spectrum;
import ca.eandb.jmist.packages.NearestIntersectionRecorder;
import ca.eandb.jmist.packages.RandomScatterRecorder;
import ca.eandb.jmist.toolkit.RandomUtil;
import ca.eandb.jmist.toolkit.Ray3;
import ca.eandb.jmist.toolkit.Tuple;
import ca.eandb.jmist.toolkit.Vector3;
import ca.eandb.jmist.util.ArrayUtil;
import ca.eandb.jmist.util.MathUtil;

/**
 * @author bkimmel
 *
 */
public final class PathShader implements RayShader {

	/**
	 * Creates a new <code>PathShader</code>.
	 * @param geometry
	 * @param light
	 * @param observer
	 */
	public PathShader(Geometry geometry, Light light, Observer observer) {
		this.geometry = geometry;
		this.light = light;
		this.observer = observer;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.RayShader#shadeRay(ca.eandb.jmist.toolkit.Ray3, double[])
	 */
	public double[] shadeRay(Ray3 ray, double[] pixel) {
		return this.observer.acquire(new PathEstimator(ray), pixel);
	}

	private final class PathEstimator implements SpectralEstimator {

		public PathEstimator(Ray3 ray) {
			this.ray = ray;
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.SpectralEstimator#sample(ca.eandb.jmist.toolkit.Tuple, double[])
		 */
		public double[] sample(Tuple wavelengths, double[] responses) {
			return PathShader.this.sample(this.ray, wavelengths, responses);
		}

		private final Ray3 ray;

	}

	private double[] sample(Ray3 ray, Tuple wavelengths, double[] responses) {

		responses = ArrayUtil.initialize(responses, wavelengths.size());

		if (ray != null) {
			double[] importance = ArrayUtil.setAll(new double[wavelengths.size()], 1.0);
			responses = this.sample(ray, wavelengths, new RandomScatterRecorder(), null, importance, responses, 0);
		}

		return responses;

	}

	private double[] sample(Ray3 ray, Tuple wavelengths,
			RandomScatterRecorder scattering, double[] sample,
			double[] importance, double[] responses, int depth) {

		Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, this.geometry);

		if (x == null) {
			return responses;
		}

		Material material = x.material();
		
		if (depth == 0) {
			Spectrum emission = material.emission(x, ray.direction().opposite());
	
			sample = emission.sample(wavelengths, sample);
			sample = MathUtil.modulate(sample, importance);
			responses = MathUtil.add(responses, sample);
		}
		
		if (this.light != null && material != null) {
			this.light.illuminate(x, this.geometry, new IlluminationTarget(x,
					wavelengths, importance, responses));
		}

		scattering.reset(RandomUtil.categorical(importance));
		material.scatter(x, wavelengths, scattering);

		ScatterResult sr = scattering.getScatterResult();

		if (sr == null) {
			return responses;
		}

		if (importance.length > 1 && sr.dispersed()) {

			int index = sr.dispersionIndex();

			double[] dispersedImportance = MathUtil.scale(sr.weights(),
					importance[index]);
			double[] dispersedResponses = this
					.sample(sr.scatteredRay(), sr.wavelengths(), scattering,
							null, dispersedImportance, null, depth + 1);

			assert(dispersedResponses.length == 1);
			responses[index] += dispersedResponses[0];

			return responses;

		} else {

			MathUtil.modulate(importance, sr.weights());
			return this.sample(sr.scatteredRay(), sr.wavelengths(), scattering,
					sample, importance, responses, depth + 1);

		}

	}

	private final class IlluminationTarget implements Illuminable {

		/**
		 * @param x
		 * @param wavelengths
		 * @param importance
		 * @param responses
		 */
		public IlluminationTarget(Intersection x, Tuple wavelengths, double[] importance, double[] responses) {
			this.x = x;
			this.wavelengths = wavelengths;
			this.importance = importance;
			this.responses = responses;
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.Illuminable#illuminate(ca.eandb.jmist.toolkit.Vector3, ca.eandb.jmist.framework.Spectrum)
		 */
		public void illuminate(Vector3 from, Spectrum radiance) {

			Material	material = x.material();
			Vector3		n = x.microfacetNormal();
			double		ndotv = n.dot(from);

			Spectrum	scattering = material.scattering(x, from);

			if (scattering != null) {

				sample = radiance.sample(wavelengths, sample);
				scattering.modulate(wavelengths, sample);
				MathUtil.modulate(sample, importance);
				MathUtil.scale(sample, Math.abs(ndotv));
				MathUtil.add(responses, sample);

			}

		}

		private final Intersection x;
		private final Tuple wavelengths;
		private final double[] importance;
		private final double[] responses;
		private double[] sample;

	}

	private final Geometry geometry;
	private final Light light;
	private final Observer observer;

}

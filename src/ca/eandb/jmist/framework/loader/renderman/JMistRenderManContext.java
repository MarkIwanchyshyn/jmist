/**
 * 
 */
package ca.eandb.jmist.framework.loader.renderman;

import static ca.eandb.jmist.framework.loader.renderman.RtErrorSeverity.ERROR;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorSeverity.SEVERE;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorSeverity.WARNING;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.BADHANDLE;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.BADTOKEN;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.ILLSTATE;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.NESTING;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.RANGE;
import static ca.eandb.jmist.framework.loader.renderman.RtErrorType.UNIMPLEMENT;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import ca.eandb.jmist.framework.geometry.MeshGeometry;
import ca.eandb.jmist.framework.geometry.mesh.PolygonMesh;
import ca.eandb.jmist.framework.geometry.primitive.PartialConeGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialCylinderGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialDiscGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialHyperboloidGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialParaboloidGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialSphereGeometry;
import ca.eandb.jmist.framework.geometry.primitive.PartialTorusGeometry;
import ca.eandb.jmist.framework.geometry.primitive.SuperellipsoidGeometry;
import ca.eandb.jmist.framework.scene.MergeSceneElement;
import ca.eandb.jmist.math.Box2;
import ca.eandb.jmist.math.Interval;
import ca.eandb.jmist.math.MathUtil;
import ca.eandb.jmist.math.Matrix4;
import ca.eandb.jmist.math.Plane3;
import ca.eandb.jmist.math.Point2;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Vector3;

/**
 * @author Brad
 *
 */
public final class JMistRenderManContext implements RenderManContext {

	private Stack<Attributes> attributes;

	private Stack<RtMatrix> transformation;
	
	private Options globalOptions;
	
	private Options options;
	
	private int currentFrame;
	
	private RtErrorHandler errorHandler;
	
	private MergeSceneElement geometry;
	
	
	private Map<RtToken, RtMatrix> coordinateSystems;
	
	private final class RetainedObject implements RtObjectHandle {
		public final MergeSceneElement geometry;
		public RetainedObject(MergeSceneElement geometry) {
			this.geometry = geometry;
		}
	}
	
	
	private void saveAttributes() {
		transformBegin();
	}
	
	private void restoreAttributes() {
		transformEnd();
	}
	
	private RtMatrix getCurrentTransformation() {
		return transformation.peek();
	}
	
	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#declare(java.lang.String, java.lang.String)
	 */
	@Override
	public RtToken declare(String name, String declaration) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#frameBegin(int)
	 */
	@Override
	public void frameBegin(int frame) {
		if (currentFrame >= 0) {
			errorHandler.apply(NESTING, SEVERE, "Frames may not be nested");
		} else {
			saveAttributes();
			options = options.clone();
			currentFrame = frame;
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#frameEnd()
	 */
	@Override
	public void frameEnd() {
		if (currentFrame < 0) {
			errorHandler.apply(ILLSTATE, SEVERE, "There is no active frame");
		} else {
			currentFrame = -1;
			options = globalOptions;
			restoreAttributes();
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#worldBegin()
	 */
	@Override
	public void worldBegin() {
		coordinateSystems.put(RI_CAMERA, transformation.peek());
		transformation.push(RtMatrix.IDENTITY);
		saveAttributes();
		geometry = new MergeSceneElement();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#worldEnd()
	 */
	@Override
	public void worldEnd() {
		geometry = null;
		restoreAttributes();
		transformation.pop();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#format(int, int, double)
	 */
	@Override
	public void format(int xresolution, int yresolution, double pixelaspectratio) {
		options.setHorizontalResolution(xresolution);
		options.setVerticalResolution(yresolution);
		options.setPixelAspectRatio(pixelaspectratio);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#frameAspectRatio(double)
	 */
	@Override
	public void frameAspectRatio(double frameaspectratio) {
		options.setFrameAspectRatio(frameaspectratio);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#screenWindow(double, double, double, double)
	 */
	@Override
	public void screenWindow(double left, double right, double bottom,
			double top) {
		options.setScreenWindow(new Box2(left, top, right, bottom));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#cropWindow(double, double, double, double)
	 */
	@Override
	public void cropWindow(double xmin, double xmax, double ymin, double ymax) {
		options.setCropWindow(new Box2(xmin, ymin, xmax, ymax));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#projection(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void projection(RtToken name, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");		
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#clipping(double, double)
	 */
	@Override
	public void clipping(double near, double far) {
		options.setClippingInterval(new Interval(near, far));
	}
	
	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#clippingPlane(double, double, double, double, double, double)
	 */
	@Override
	public void clippingPlane(double x, double y, double z, double nx, double ny, double nz) {
		Plane3 plane = Plane3.throughPoint(new Point3(x, y, z), new Vector3(nx, ny, nz));
		options.getClippingPlanes().add(plane);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#depthOfField(double, double, double)
	 */
	@Override
	public void depthOfField(double fstop, double focallength,
			double focaldistance) {
		options.setfStop(fstop);
		options.setFocalLength(focallength);
		options.setFocalDistance(focaldistance);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#shutter(double, double)
	 */
	@Override
	public void shutter(double min, double max) {
		options.setShutter(new Interval(min, max));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#pixelVariance(double)
	 */
	@Override
	public void pixelVariance(double variation) {
		options.setPixelVariance(variation);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#pixelSamples(double, double)
	 */
	@Override
	public void pixelSamples(double xsamples, double ysamples) {
		options.setHorizontalSamplingRate(xsamples);
		options.setVerticalSamplingRate(ysamples);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#pixelFilter(ca.eandb.jmist.framework.loader.renderman.RtFilterFunc, double, double)
	 */
	@Override
	public void pixelFilter(RtFilterFunc filterfunc, double xwidth,
			double ywidth) {
		options.setPixelFilter(filterfunc);
		options.setHorizontalFilterWidth(xwidth);
		options.setVerticalFilterWidth(ywidth);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#exposure(double, double)
	 */
	@Override
	public void exposure(double gain, double gamma) {
		options.setExposureGain(gain);
		options.setExposureGamma(gamma);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#imager(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void imager(RtToken name, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#quantize(ca.eandb.jmist.framework.loader.renderman.RtToken, int, int, int, double)
	 */
	@Override
	public void quantize(RtToken type, int one, int min, int max,
			double ditheramplitude) {
		if (type == RI_RGBA) {
			options.setColorQuantizerOne(one);
			options.setColorQuantizerMinimum(min);
			options.setColorQuantizerMaximum(max);
			options.setColorQuantizerDitherAmplitude(ditheramplitude);
		} else if (type == RI_Z) {
			options.setDepthQuantizerOne(one);
			options.setDepthQuantizerMinimum(min);
			options.setDepthQuantizerMaximum(max);
			options.setDepthQuantizerDitherAmplitude(ditheramplitude);
		} else {
			errorHandler.apply(BADTOKEN, WARNING, "`type' must be RGBA or Z");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#display(java.lang.String, ca.eandb.jmist.framework.loader.renderman.RtToken, ca.eandb.jmist.framework.loader.renderman.RtToken, java.lang.Object[])
	 */
	@Override
	public void display(String name, RtToken type, RtToken mode,
			Object... parmaterlist) {
		options.setDisplayName(name);
		options.setDisplayType(type);
		options.setDisplayMode(mode);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#hider(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void hider(RtToken type, Map<RtToken, Object> params) {
		options.setHider(type);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#colorSamples(int, double[], double[])
	 */
	@Override
	public void colorSamples(int n, double[] nRGB, double[] RGBn) {
		options.setColorSamples(n);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#relativeDetail(double)
	 */
	@Override
	public void relativeDetail(double relativedetail) {
		options.setRelativeDetail(relativedetail);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#option(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void option(RtToken name, Map<RtToken, Object> params) {
		for (Entry<RtToken, Object> entry : params.entrySet()) {
			options.set(name, entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#attributeBegin()
	 */
	@Override
	public void attributeBegin() {
		saveAttributes();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#attributeEnd()
	 */
	@Override
	public void attributeEnd() {
		restoreAttributes();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#color(ca.eandb.jmist.framework.loader.renderman.RtColor)
	 */
	@Override
	public void color(RtColor color) {
		attributes.peek().setColor(color);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#opacity(ca.eandb.jmist.framework.loader.renderman.RtColor)
	 */
	@Override
	public void opacity(RtColor color) {
		attributes.peek().setOpacity(color);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#textureCoordinates(double, double, double, double, double, double, double, double)
	 */
	@Override
	public void textureCoordinates(double s1, double t1, double s2, double t2,
			double s3, double t3, double s4, double t4) {
		attributes.peek().setTextureCoordinates(
				new Point2(s1, t1),
				new Point2(s2, t2),
				new Point2(s3, t3),
				new Point2(s4, t4));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#lightSource(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public RtLightHandle lightSource(RtToken shadername,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#areaLightSource(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public RtLightHandle areaLightSource(RtToken shadername,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#illuminate(ca.eandb.jmist.framework.loader.renderman.RtLightHandle, boolean)
	 */
	@Override
	public void illuminate(RtLightHandle light, boolean onoff) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#surface(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void surface(RtToken shadername, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#displacement(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void displacement(RtToken shadername, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#atmosphere(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void atmosphere(RtToken shadername, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#interior(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void interior(RtToken shadername, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#exterior(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void exterior(RtToken shadername, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#shadingRate(double)
	 */
	@Override
	public void shadingRate(double size) {
		attributes.peek().setEffectiveShadingRate(size);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#shadingInterpolation(ca.eandb.jmist.framework.loader.renderman.RtToken)
	 */
	@Override
	public void shadingInterpolation(RtToken type) {
		attributes.peek().setShadingInterpolation(type);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#matte(boolean)
	 */
	@Override
	public void matte(boolean onoff) {
		attributes.peek().setMatteSurfaceFlag(onoff);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#bound(ca.eandb.jmist.framework.loader.renderman.RtBound)
	 */
	@Override
	public void bound(RtBound bound) {
		attributes.peek().setBound(bound);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#detail(ca.eandb.jmist.framework.loader.renderman.RtBound)
	 */
	@Override
	public void detail(RtBound bound) {
		attributes.peek().setDetail(bound);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#detailRange(double, double, double, double)
	 */
	@Override
	public void detailRange(double minvisible, double lowertransition,
			double uppertransition, double maxvisible) {
		attributes.peek().setDetailRange(minvisible, lowertransition, uppertransition, maxvisible);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#geometricApproximation(ca.eandb.jmist.framework.loader.renderman.RtToken, double)
	 */
	@Override
	public void geometricApproximation(RtToken type, double value) {
		attributes.peek().setGeometricApproximation(type, value);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#orientation(ca.eandb.jmist.framework.loader.renderman.RtToken)
	 */
	@Override
	public void orientation(RtToken orientation) {
		if (orientation != RI_INSIDE && orientation != RI_OUTSIDE && orientation != RI_LH && orientation != RI_RH) {
			errorHandler.apply(BADTOKEN, ERROR, "Orientation must be one of (INSIDE, OUTSIDE, LH, RH)");
		} else {
			attributes.peek().setOrientation(orientation);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#reverseOrientation()
	 */
	@Override
	public void reverseOrientation() {
		RtToken orientation = attributes.peek().getOrientation();
		if (orientation == RI_INSIDE) {
			orientation = RI_OUTSIDE;
		} else if (orientation == RI_OUTSIDE) {
			orientation = RI_INSIDE;
		} else if (orientation == RI_LH) {
			orientation = RI_RH;
		} else if (orientation == RI_RH) {
			orientation = RI_LH;
		}
		attributes.peek().setOrientation(orientation);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#sides(int)
	 */
	@Override
	public void sides(int sides) {
		if (sides != 1 && sides != 2) {
			errorHandler.apply(RANGE, ERROR, "`sides' must be 1 or 2");
		} else {
			attributes.peek().setNumberOfSides(sides);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#identity()
	 */
	@Override
	public void identity() {
		 transform(RtMatrix.IDENTITY);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#transform(ca.eandb.jmist.framework.loader.renderman.RtMatrix)
	 */
	@Override
	public void transform(RtMatrix transform) {
		this.transformation.set(0, transform);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#concatTransform(ca.eandb.jmist.framework.loader.renderman.RtMatrix)
	 */
	@Override
	public void concatTransform(RtMatrix transform) {
		transform(transformation.peek().concatenate(transform));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#perspective(double)
	 */
	@Override
	public void perspective(double fov) {
		concatTransform(RtMatrix.perspective(fov));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#translate(double, double, double)
	 */
	@Override
	public void translate(double dx, double dy, double dz) {
		concatTransform(RtMatrix.translate(dx, dy, dz));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#scale(double, double, double)
	 */
	@Override
	public void scale(double sx, double sy, double sz) {
		concatTransform(RtMatrix.scale(sx, sy, sz));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#skew(double, double, double, double, double, double, double)
	 */
	@Override
	public void skew(double angle, double dx1, double dy1, double dz1,
			double dx2, double dy2, double dz2) {
		concatTransform(RtMatrix.skew(angle, dx1, dy1, dz1, dx2, dy2, dz2));
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#coordinateSystem(ca.eandb.jmist.framework.loader.renderman.RtToken)
	 */
	@Override
	public void coordinateSystem(RtToken name) {
		coordinateSystems.put(name, getCurrentTransformation());
	}
	
	private RtMatrix getTransform(RtToken name) {
		/* TODO missing NDC token */
		if (name == RI_RASTER) {
			
		} else if (name == RI_SCREEN) {
			
		} else if (name == RI_CAMERA) {
			
		} else if (name == RI_WORLD) {
			
		} else if (name == RI_OBJECT) {
			
		} else {
			RtMatrix matrix = coordinateSystems.get(name);
			if (matrix == null) {
				errorHandler.apply(BADTOKEN, ERROR, "Undefined named coordinate system");
			} else {
				return matrix;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#coordSysTransform(ca.eandb.jmist.framework.loader.renderman.RtToken)
	 */
	@Override
	public void coordSysTransform(RtToken name) {
		RtMatrix matrix = getTransform(name);
		if (matrix != null) {
			transform(matrix);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#transformPoints(ca.eandb.jmist.framework.loader.renderman.RtToken, ca.eandb.jmist.framework.loader.renderman.RtToken, int, ca.eandb.jmist.framework.loader.renderman.RtPoint[])
	 */
	@Override
	public RtPoint[] transformPoints(RtToken fromspace, RtToken tospace,
			RtPoint[] points) {
		RtMatrix fromTransform = getTransform(fromspace);
		RtMatrix toTransform = getTransform(tospace);
		
		if (fromTransform == null || toTransform == null) {
			return null;
		}
		
		Matrix4 from = fromTransform.toMatrix();
		Matrix4 to = toTransform.toMatrix();
		if (MathUtil.isZero(to.determinant())) {
			return null;
		} else {
			Matrix4 T = from.divide(to);
			for (int i = 0; i < points.length; i++) {
				points[i] = new RtPoint(T.times(points[i].toPoint().toVector4()).project());
			}
			return points;
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#transformBegin()
	 */
	@Override
	public void transformBegin() {
		transformation.push(transformation.peek());
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#transformEnd()
	 */
	@Override
	public void transformEnd() {
		transformation.pop();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#attribute(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void attribute(RtToken name, Map<RtToken, Object> params) {
		for (Entry<RtToken, Object> entry : params.entrySet()) {
			attributes.peek().set(name, entry.getKey(), entry.getValue());
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#polygon(int, java.util.Map)
	 */
	@Override
	public void polygon(int nvertices, Map<RtToken, Object> params) {
		if (geometry != null) {
			PolygonMesh mesh = new PolygonMesh();
			double[] p = (double[]) params.get(RI_P);
			for (int i = 0; i < nvertices; i += 3) {
				mesh.addVertex(new Point3(p[i + 0], p[i + 1], p[i + 2]));
			}
			geometry.addChild(new MeshGeometry(mesh));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#generalPolygon(int, int[], java.util.Map)
	 */
	@Override
	public void generalPolygon(int nloops, int[] nvertices,
			Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#pointsPolygons(int, int[], int[], java.util.Map)
	 */
	@Override
	public void pointsPolygons(int npolys, int[] nvertices, int[] vertices,
			Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#pointsGeneralPolygons(int, int[], int[], int[], java.util.Map)
	 */
	@Override
	public void pointsGeneralPolygons(int npolys, int[] nloops,
			int[] nvertices, int[] vertices, Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#basis(ca.eandb.jmist.framework.loader.renderman.RtBasis, int, ca.eandb.jmist.framework.loader.renderman.RtBasis, int)
	 */
	@Override
	public void basis(RtBasis ubasis, int ustep, RtBasis vbasis, int vstep) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#patch(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void patch(RtToken type, Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#patchMesh(ca.eandb.jmist.framework.loader.renderman.RtToken, int, ca.eandb.jmist.framework.loader.renderman.RtToken, int, ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void patchMesh(RtToken type, int nu, RtToken uwrap, int nv,
			RtToken vwrap, Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#nuPatch(int, int, double[], double, double, int, int, double[], double, double)
	 */
	@Override
	public void nuPatch(int nu, int uorder, double[] uknot, double umin,
			double umax, int nv, int vorder, double[] vknot, double vmin,
			double vmax) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#trimCurve(int, int[], int[], double[], double, double, int[], double[], double[], double[])
	 */
	@Override
	public void trimCurve(int nloops, int[] ncurves, int[] order,
			double[] knot, double min, double max, int[] n, double[] u,
			double[] v, double[] w) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#subdivisionMesh(ca.eandb.jmist.framework.loader.renderman.RtToken, int, int[], int[], int, ca.eandb.jmist.framework.loader.renderman.RtToken[], int[], int[], double[], java.util.Map)
	 */
	@Override
	public void subdivisionMesh(RtToken scheme, int nfaces, int[] nvertices,
			int[] vertices, int ntags, RtToken[] tags, int[] nargs,
			int[] intargs, double[] doubleargs, Map<RtToken, Object> params) {
		if (geometry != null) {
			errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#sphere(double, double, double, double, java.util.Map)
	 */
	@Override
	public void sphere(double radius, double zmin, double zmax,
			double thetamax, Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialSphereGeometry(radius, zmin, zmax, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#cone(double, double, double, java.util.Map)
	 */
	@Override
	public void cone(double height, double radius, double thetamax,
			Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialConeGeometry(height, radius, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#cylinder(double, double, double, double, java.util.Map)
	 */
	@Override
	public void cylinder(double radius, double zmin, double zmax,
			double thetamax, Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialCylinderGeometry(radius, zmin, zmax, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#hyperboloid(ca.eandb.jmist.framework.loader.renderman.RtPoint, ca.eandb.jmist.framework.loader.renderman.RtPoint, double, java.util.Map)
	 */
	@Override
	public void hyperboloid(RtPoint point1, RtPoint point2, double thetamax,
			Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialHyperboloidGeometry(point1.toPoint(), point2.toPoint(), thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#paraboloid(double, double, double, double, java.util.Map)
	 */
	@Override
	public void paraboloid(double rmax, double zmin, double zmax,
			double thetamax, Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialParaboloidGeometry(rmax, zmin, zmax, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#disk(double, double, double, java.util.Map)
	 */
	@Override
	public void disk(double height, double radius, double thetamax,
			Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialDiscGeometry(height, radius, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#torus(double, double, double, double, double, java.util.Map)
	 */
	@Override
	public void torus(double majorradius, double minorradius, double phimin,
			double phimax, double thetamax, Map<RtToken, Object> params) {
		if (geometry != null) {
			geometry.addChild(new PartialTorusGeometry(majorradius, minorradius, phimin, phimax, thetamax));
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#points(int, java.util.Map)
	 */
	@Override
	public void points(int npoints, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#curves(ca.eandb.jmist.framework.loader.renderman.RtToken, int, int[], ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void curves(RtToken type, int ncurves, int[] nvertices,
			RtToken wrap, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#blobby(int, int, int[], int, double[], int, java.lang.String[], java.util.Map)
	 */
	@Override
	public void blobby(int nleaf, int ncode, int[] code, int ndoubles,
			double[] doubles, int nstrings, String[] strings,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#procedural(java.lang.Object, ca.eandb.jmist.framework.loader.renderman.RtBound, ca.eandb.jmist.framework.loader.renderman.RtProcSubdivFunc)
	 */
	@Override
	public void procedural(Object data, RtBound bound,
			RtProcSubdivFunc subdividefunc) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#procRunProgram(java.lang.Object, double)
	 */
	@Override
	public void procRunProgram(Object data, double detail) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#procDynamicLoad(java.lang.Object, double)
	 */
	@Override
	public void procDynamicLoad(Object data, double detail) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#geometry(ca.eandb.jmist.framework.loader.renderman.RtToken, java.util.Map)
	 */
	@Override
	public void geometry(RtToken type, Map<RtToken, Object> params) {
		if (geometry != null) {
			if (type == RI_EXT_SUPERELLIPSOID) {
				double[] exp = (double[]) params.get(RI_EXT_EXPONENTS);
				for (double e : exp) {
					if (e <= 0.0) {
						errorHandler.apply(RANGE, ERROR, "Superellipsoid exponents must be positive");
						return;
					}
				}
				switch (exp.length) {
				case 1:
					geometry.addChild(new SuperellipsoidGeometry(exp[0], exp[0]));
					break;
				case 2:
					geometry.addChild(new SuperellipsoidGeometry(exp[0], exp[1]));
					break;					
				default:
					errorHandler.apply(RtErrorType.CONSISTENCY, ERROR, "Superellipsoid requires 1 or 2 exponents");
				}
			} else {
				errorHandler.apply(BADTOKEN, WARNING, "Unrecognized geometry type");
			}
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#solidBegin(ca.eandb.jmist.framework.loader.renderman.RtToken)
	 */
	@Override
	public void solidBegin(RtToken operation) {
		saveAttributes();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#solidEnd()
	 */
	@Override
	public void solidEnd() {
		restoreAttributes();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#objectBegin()
	 */
	@Override
	public RtObjectHandle objectBegin() {
		saveAttributes();
		geometry = new MergeSceneElement();
		return new RetainedObject(geometry);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#objectEnd()
	 */
	@Override
	public void objectEnd() {
		geometry = null;
		restoreAttributes();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#objectInstance(ca.eandb.jmist.framework.loader.renderman.RtObjectHandle)
	 */
	@Override
	public void objectInstance(RtObjectHandle handle) {
		if (geometry != null) {
			try {
				RetainedObject obj = (RetainedObject) handle;
				geometry.addChild(obj.geometry);
			} catch (ClassCastException e) {
				errorHandler.apply(BADHANDLE, ERROR, "Invalid object handle");
			}
		} else {
			errorHandler.apply(ILLSTATE, ERROR, "Current mode does not permit geometry");
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#motionBegin(int, double[])
	 */
	@Override
	public void motionBegin(int n, double... t) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#motionEnd()
	 */
	@Override
	public void motionEnd() {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#makeTexture(java.lang.String, java.lang.String, ca.eandb.jmist.framework.loader.renderman.RtToken, ca.eandb.jmist.framework.loader.renderman.RtToken, ca.eandb.jmist.framework.loader.renderman.RtFilterFunc, double, double, java.util.Map)
	 */
	@Override
	public void makeTexture(String picturename, String texturename,
			RtToken swrap, RtToken twrap, RtFilterFunc filterfunc,
			double swidth, double twidth, Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#makeLatLongEnvironment(java.lang.String, java.lang.String, ca.eandb.jmist.framework.loader.renderman.RtFilterFunc, double, double, java.util.Map)
	 */
	@Override
	public void makeLatLongEnvironment(String picturename, String texturename,
			RtFilterFunc filterfunc, double swidth, double twidth,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#makeCubeFaceEnvironment(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, double, ca.eandb.jmist.framework.loader.renderman.RtFilterFunc, double, double, java.util.Map)
	 */
	@Override
	public void makeCubeFaceEnvironment(String px, String nx, String py,
			String ny, String pz, String nz, String texturename, double fov,
			RtFilterFunc filterfunc, double swidth, double twidth,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#makeShadow(java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	public void makeShadow(String picturename, String texturename,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#errorHandler(ca.eandb.jmist.framework.loader.renderman.RtErrorHandler)
	 */
	@Override
	public void errorHandler(RtErrorHandler handler) {
		this.errorHandler = handler;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#archiveRecord(ca.eandb.jmist.framework.loader.renderman.RtToken, java.lang.String, java.lang.String[])
	 */
	@Override
	public void archiveRecord(RtToken type, String format, String... arg) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.loader.renderman.RenderManContext#readArchive(ca.eandb.jmist.framework.loader.renderman.RtToken, ca.eandb.jmist.framework.loader.renderman.RtArchiveCallback, java.util.Map)
	 */
	@Override
	public void readArchive(RtToken name, RtArchiveCallback callback,
			Map<RtToken, Object> params) {
		errorHandler.apply(UNIMPLEMENT, WARNING, "Not yet implemented");
	}

}

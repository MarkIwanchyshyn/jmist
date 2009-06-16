/**
 *
 */
package ca.eandb.jmist.framework.geometry;

import java.util.ArrayList;
import java.util.List;

import ca.eandb.jmist.framework.AffineTransformable3;
import ca.eandb.jmist.framework.Geometry;
import ca.eandb.jmist.framework.IntersectionGeometry;
import ca.eandb.jmist.framework.IntersectionGeometryDecorator;
import ca.eandb.jmist.framework.IntersectionRecorderDecorator;
import ca.eandb.jmist.framework.IntersectionRecorder;
import ca.eandb.jmist.framework.InvertibleAffineTransformation3;
import ca.eandb.jmist.math.AffineMatrix3;
import ca.eandb.jmist.math.Basis3;
import ca.eandb.jmist.math.Box3;
import ca.eandb.jmist.math.LinearMatrix3;
import ca.eandb.jmist.math.Point3;
import ca.eandb.jmist.math.Ray3;
import ca.eandb.jmist.math.Sphere;
import ca.eandb.jmist.math.Vector3;

/**
 * A <code>CompositeGeometry</code> that may be transformed.
 * @author Brad Kimmel
 */
public final class TransformableGeometry extends CompositeGeometry implements
		AffineTransformable3 {

	/**
	 * Creates a new <code>TransformableGeometry</code>.
	 */
	public TransformableGeometry() {
		/* do nothing */
	}

	/**
	 * Creates a new <code>TransformableGeometry</code>.
	 * @param geometry The <code>Geometry</code> to make transformable.
	 */
	public TransformableGeometry(Geometry geometry) {
		this.addChild(geometry);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Geometry#intersect(ca.eandb.jmist.toolkit.Ray3, ca.eandb.jmist.framework.IntersectionRecorder)
	 */
	public void intersect(Ray3 ray, IntersectionRecorder recorder) {

		ray			= this.model.applyInverse(ray);
		recorder	= new TransformedIntersectionRecorder(recorder);

		for (Geometry geometry : this.children()) {
			geometry.intersect(ray, recorder);
		}

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Geometry#isClosed()
	 */
	@Override
	public boolean isClosed() {
		for (Geometry geometry : this.children()) {
			if (!geometry.isClosed()) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Bounded3#boundingBox()
	 */
	@Override
	public Box3 boundingBox() {

		List<Point3> corners = new ArrayList<Point3>(8 * this.children().size());

		for (Geometry geometry : this.children()) {
			Box3 childBoundingBox = geometry.boundingBox();
			for (int i = 0; i < 8; i++) {
				corners.add(this.model.apply(childBoundingBox.corner(i)));
			}
		}

		return Box3.smallestContainingPoints(corners);

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Bounded3#boundingSphere()
	 */
	@Override
	public Sphere boundingSphere() {

		List<Point3> corners = new ArrayList<Point3>(8 * this.children().size());

		for (Geometry geometry : this.children()) {
			Box3 childBoundingBox = geometry.boundingBox();
			for (int i = 0; i < 8; i++) {
				corners.add(this.model.apply(childBoundingBox.corner(i)));
			}
		}

		return Sphere.smallestContaining(corners);

	}

	/**
	 * An <code>IntersectionRecorder</code> that transforms the recorded
	 * intersections according to the transformation for this
	 * <code>TransformableGeometry</code>.
	 * @author Brad Kimmel
	 */
	private final class TransformedIntersectionRecorder extends
			IntersectionRecorderDecorator {

		/**
		 * Creates a new <code>TransformedIntersectionRecorder</code>.
		 * @param inner The <code>IntersectionRecorder</code> to record
		 * 		intersections to.
		 */
		public TransformedIntersectionRecorder(IntersectionRecorder inner) {
			super(inner);
		}

		/*
		 *
		 */
		@Override
		public void record(IntersectionGeometry intersection) {
			this.inner.record(new TransformedIntersection(intersection));
		}

	}

	/**
	 * An <code>IntersectionGeometry</code> that has been transformed according to the
	 * transformation applied to this <code>TransformableGeometry</code>.
	 * @author Brad Kimmel
	 */
	private final class TransformedIntersection extends IntersectionGeometryDecorator {

		/**
		 * Creates a new <code>TransformedIntersection</code>.
		 * @param local The <code>IntersectionGeometry</code> in local coordinate
		 * 		space.
		 */
		public TransformedIntersection(IntersectionGeometry local) {
			super(local);
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#incident()
		 */
		@Override
		public Vector3 getIncident() {
			return model.apply(this.inner.getIncident());
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#basis()
		 */
		@Override
		public Basis3 getBasis() {
			Basis3 localBasis = this.inner.getBasis();
			return Basis3.fromUV(
					model.apply(localBasis.u()),
					model.apply(localBasis.v()),
					localBasis.orientation()
			);
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#location()
		 */
		@Override
		public Point3 getPosition() {
			return model.apply(this.inner.getPosition());
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#shadingBasis()
		 */
		@Override
		public Basis3 getShadingBasis() {
			Basis3 localShadingBasis = this.inner.getShadingBasis();
			return Basis3.fromUV(
					model.apply(localShadingBasis.u()),
					model.apply(localShadingBasis.v()),
					localShadingBasis.orientation()
			);
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#shadingNormal()
		 */
		@Override
		public Vector3 getShadingNormal() {
			// TODO implement this directly for better performance.
			return this.getShadingBasis().w();
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#normal()
		 */
		@Override
		public Vector3 getNormal() {
			// TODO implement this directly for better performance.
			return this.getBasis().w();
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jmist.framework.IntersectionGeometryDecorator#tangent()
		 */
		@Override
		public Vector3 getTangent() {
			return model.apply(this.inner.getTangent());
		}

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AffineTransformable3#transform(ca.eandb.jmist.toolkit.AffineMatrix3)
	 */
	public void transform(AffineMatrix3 T) {
		this.model.transform(T);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.LinearTransformable3#transform(ca.eandb.jmist.toolkit.LinearMatrix3)
	 */
	public void transform(LinearMatrix3 T) {
		this.model.transform(T);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Rotatable3#rotate(ca.eandb.jmist.toolkit.Vector3, double)
	 */
	public void rotate(Vector3 axis, double angle) {
		this.model.rotate(axis, angle);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Rotatable3#rotateX(double)
	 */
	public void rotateX(double angle) {
		this.model.rotateX(angle);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Rotatable3#rotateY(double)
	 */
	public void rotateY(double angle) {
		this.model.rotateY(angle);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Rotatable3#rotateZ(double)
	 */
	public void rotateZ(double angle) {
		this.model.rotateZ(angle);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Translatable3#translate(ca.eandb.jmist.toolkit.Vector3)
	 */
	public void translate(Vector3 v) {
		this.model.translate(v);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Scalable#scale(double)
	 */
	public void scale(double c) {
		this.model.scale(c);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.Stretchable3#stretch(ca.eandb.jmist.toolkit.Vector3, double)
	 */
	public void stretch(Vector3 axis, double c) {
		this.model.stretch(axis, c);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AxisStretchable3#stretch(double, double, double)
	 */
	public void stretch(double cx, double cy, double cz) {
		this.model.stretch(cx, cy, cz);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AxisStretchable3#stretchX(double)
	 */
	public void stretchX(double cx) {
		this.model.stretchX(cx);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AxisStretchable3#stretchY(double)
	 */
	public void stretchY(double cy) {
		this.model.stretchY(cy);
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jmist.framework.AxisStretchable3#stretchZ(double)
	 */
	public void stretchZ(double cz) {
		this.model.stretchZ(cz);
	}

	/** The transformation to apply to this <code>Geometry</code>. */
	private final InvertibleAffineTransformation3 model = new InvertibleAffineTransformation3();

}

/**
 *
 */
package org.jmist.packages;

import org.jmist.framework.AbstractGeometry;
import org.jmist.framework.Geometry;
import org.jmist.framework.Intersection;
import org.jmist.framework.IntersectionDecorator;
import org.jmist.framework.IntersectionRecorderDecorator;
import org.jmist.framework.IntersectionRecorder;
import org.jmist.toolkit.Basis3;
import org.jmist.toolkit.Box3;
import org.jmist.toolkit.Ray3;
import org.jmist.toolkit.Sphere;
import org.jmist.toolkit.Vector3;

/**
 * A <code>Geometry</code> decorator that flips another <code>Geometry</code>
 * inside out.
 * @author bkimmel
 */
public final class InsideOutGeometry extends AbstractGeometry {

	/**
	 * Creates a new <code>InsideOutGeometry</code>.
	 * @param inner The <code>Geometry</code> to turn inside out.
	 */
	public InsideOutGeometry(Geometry inner) {
		this.inner = inner;
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Geometry#intersect(org.jmist.toolkit.Ray3, org.jmist.framework.IntersectionRecorder)
	 */
	@Override
	public void intersect(Ray3 ray, IntersectionRecorder recorder) {
		recorder = new InsideOutIntersectionRecorder(recorder);
		this.inner.intersect(ray, recorder);
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Geometry#isClosed()
	 */
	@Override
	public boolean isClosed() {
		return this.inner.isClosed();
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Bounded3#boundingBox()
	 */
	@Override
	public Box3 boundingBox() {
		return this.inner.boundingBox();
	}

	/* (non-Javadoc)
	 * @see org.jmist.framework.Bounded3#boundingSphere()
	 */
	@Override
	public Sphere boundingSphere() {
		return this.inner.boundingSphere();
	}

	/**
	 * An <code>IntersectionRecorder</code> decorator that flips the normals,
	 * bases, tangents and the {@link Intersection#front()} property of all
	 * <code>Intersection</code>s recorded to it.
	 * @author bkimmel
	 */
	private static final class InsideOutIntersectionRecorder extends
			IntersectionRecorderDecorator {

		/**
		 * Creates a new <code>InsideOutIntersectionRecorder</code>.
		 * @param inner The <code>IntersectionRecorder</code> to decorate.
		 */
		public InsideOutIntersectionRecorder(IntersectionRecorder inner) {
			super(inner);
		}

		/* (non-Javadoc)
		 * @see org.jmist.framework.IntersectionRecorderDecorator#record(org.jmist.framework.Intersection)
		 */
		@Override
		public void record(Intersection intersection) {
			this.inner.record(new InsideOutIntersection(intersection));
		}

		/**
		 * An <code>Intersection</code> decorator that flips the normals,
		 * bases, tangents, and the {@link Intersection#front()} properties of
		 * the decorated <code>Intersection</code>.
		 * @author bkimmel
		 */
		private static final class InsideOutIntersection extends IntersectionDecorator {

			/**
			 * Creates a new <code>InsideOutIntersection</code>.
			 * @param inner The decorated <code>Intersection</code>.
			 */
			public InsideOutIntersection(Intersection inner) {
				super(inner);
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#front()
			 */
			@Override
			public boolean front() {
				return !this.inner.front();
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#basis()
			 */
			@Override
			public Basis3 basis() {
				return this.inner.basis().opposite();
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#microfacetBasis()
			 */
			@Override
			public Basis3 microfacetBasis() {
				return this.inner.microfacetBasis().opposite();
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#microfacetNormal()
			 */
			@Override
			public Vector3 microfacetNormal() {
				return this.inner.microfacetNormal().opposite();
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#normal()
			 */
			@Override
			public Vector3 normal() {
				return this.inner.normal().opposite();
			}

			/* (non-Javadoc)
			 * @see org.jmist.framework.IntersectionDecorator#tangent()
			 */
			@Override
			public Vector3 tangent() {
				return this.inner.tangent().opposite();
			}

		}

	}

	/** The decorated <code>Geometry</code>. */
	private final Geometry inner;

}

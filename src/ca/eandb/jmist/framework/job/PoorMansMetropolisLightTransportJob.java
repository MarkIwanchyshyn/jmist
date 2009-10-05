/*
 * Copyright (c) 2008 Bradley W. Kimmel
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.eandb.jmist.framework.job;

import java.util.ArrayList;
import java.util.List;

import ca.eandb.jdcp.job.AbstractParallelizableJob;
import ca.eandb.jdcp.job.TaskWorker;
import ca.eandb.jmist.framework.Display;
import ca.eandb.jmist.framework.Lens;
import ca.eandb.jmist.framework.Light;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.framework.Raster;
import ca.eandb.jmist.framework.RasterUtil;
import ca.eandb.jmist.framework.Scene;
import ca.eandb.jmist.framework.color.Color;
import ca.eandb.jmist.framework.color.ColorModel;
import ca.eandb.jmist.framework.color.ColorUtil;
import ca.eandb.jmist.framework.job.bidi.BidiPathStrategy;
import ca.eandb.jmist.framework.job.bidi.PathMeasure;
import ca.eandb.jmist.framework.path.EyeNode;
import ca.eandb.jmist.framework.path.LightNode;
import ca.eandb.jmist.framework.path.Path;
import ca.eandb.jmist.framework.path.PathInfo;
import ca.eandb.jmist.framework.path.PathNode;
import ca.eandb.jmist.framework.path.ScatteringNode;
import ca.eandb.jmist.framework.random.CategoricalRandom;
import ca.eandb.jmist.framework.random.RandomUtil;
import ca.eandb.jmist.framework.random.RepeatableRandom;
import ca.eandb.jmist.math.Box2;
import ca.eandb.jmist.math.Point2;
import ca.eandb.util.UnexpectedException;
import ca.eandb.util.progress.ProgressMonitor;

/**
 * @author brad
 *
 */
public final class PoorMansMetropolisLightTransportJob extends AbstractParallelizableJob {

	/** Serialization version ID. */
	private static final long serialVersionUID = -6940841062797196504L;

	private final Scene scene;

	private final ColorModel colorModel;

	private final Random random;

	private transient Raster raster;

	private final Display display;

	private final BidiPathStrategy strategy;

	private final PathMeasure measure;

	private final int tasks;

	private final int width;

	private final int height;

	private final int mutations;
	
	private final int minMutationsPerTask;
	
	private final int extraMutations;
	
	private final int initialMutations;
	
	private final boolean displayPartialResults;

	private transient int tasksProvided = 0;

	private transient int tasksSubmitted = 0;

	private transient int mutationsSubmitted = 0;

	public PoorMansMetropolisLightTransportJob(Scene scene, Display display,
			int width, int height, ColorModel colorModel, Random random,
			BidiPathStrategy strategy, PathMeasure measure, int mutations,
			int initialMutations,
			int tasks, boolean displayPartialResults) {
		this.scene = scene;
		this.display = display;
		this.colorModel = colorModel;
		this.random = random;
		this.tasks = tasks;
		this.width = width;
		this.height = height;
		this.strategy = strategy;
		this.measure = measure;
		this.mutations = mutations;
		this.initialMutations = initialMutations;
		this.minMutationsPerTask = mutations / tasks;
		this.extraMutations = mutations - minMutationsPerTask * tasks;
		this.displayPartialResults = displayPartialResults;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.ParallelizableJob#getNextTask()
	 */
	public synchronized Object getNextTask() throws Exception {
		if (tasksProvided < tasks) {
			return tasksProvided++ < extraMutations ? minMutationsPerTask + 1
					: minMutationsPerTask;
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.ParallelizableJob#isComplete()
	 */
	public boolean isComplete() throws Exception {
		return tasksSubmitted == tasks;
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.ParallelizableJob#submitTaskResults(java.lang.Object, java.lang.Object, ca.eandb.util.progress.ProgressMonitor)
	 */
	public synchronized void submitTaskResults(Object task, Object results,
			ProgressMonitor monitor) throws Exception {

		int taskMutations = (Integer) task;
		Raster taskRaster = (Raster) results;

		monitor.notifyStatusChanged("Accumulating partial results...");

		mutationsSubmitted += taskMutations;
		if (displayPartialResults) {
			double alpha = (double) taskMutations / (double) mutationsSubmitted;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					raster.setPixel(x, y, raster.getPixel(x, y).times(
							1.0 - alpha).plus(
							taskRaster.getPixel(x, y).times(alpha)));
				}
			}
			display.setPixels(0, 0, raster);
		} else {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					raster.setPixel(x, y, raster.getPixel(x, y).plus(taskRaster.getPixel(x, y)));
				}
			}
		}

		monitor.notifyProgress(++tasksSubmitted, tasks);
		if (tasksSubmitted == tasks) {
			monitor.notifyStatusChanged("Ready to write results");
		} else {
			monitor.notifyStatusChanged("Waiting for partial results");
		}

	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.AbstractParallelizableJob#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		raster = colorModel.createRaster(width, height);
		if (displayPartialResults) {
			display.initialize(width, height, colorModel);
		}
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.AbstractParallelizableJob#finish()
	 */
	@Override
	public void finish() throws Exception {
		if (!displayPartialResults) {
			double mutationsPerPixel = (double) mutations
					/ (double) (width * height);
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					raster.setPixel(x, y, raster.getPixel(x, y).divide(mutationsPerPixel));
				}
			}
			display.initialize(width, height, colorModel);
			display.setPixels(0, 0, raster);
		}
		display.finish();
	}

	/* (non-Javadoc)
	 * @see ca.eandb.jdcp.job.ParallelizableJob#worker()
	 */
	public TaskWorker worker() throws Exception {
		return new Worker();
	}
	
	private static class Contribution {
		public final Point2 pos;
		public final Color score;
		public Contribution(Point2 pos, Color score) {
			this.pos = pos;
			this.score = score;
		}
	}
	
	private static final class LCG {
		private final java.util.Random rnd = new java.util.Random();
		private long seed;
		private long a;
		private long c;
		private int pos;
		private final int length;
		private final long mask;
		
		public LCG(int length) {
			this.length = length;
			this.pos = length;
			this.mask = (long) (Integer.highestOneBit(length) << 1) - 1;
		}
		public int next() {
			if (pos++ >= length) {
				seed = (rnd.nextLong() ^ 0x5DEECE66DL) & ((1L << 48) - 1);
				a = (rnd.nextLong() ^ 0x5DEECE66DL) & ((1L << 48) - 1);
				c = (rnd.nextLong() ^ 0x5DEECE66DL) & ((1L << 48) - 1);
				a = (a ^ (a & 0x3L)) | 0x1L;
				c = (c ^ (c & 0x1L)) | 0x1L;
				pos = 0;
			}
			int x;
			do {
				seed = (seed * a + c) & ((1L << 48) - 1);
				x = (int) (seed & mask);
			} while (x >= length);
			return x;
		}
	};

	private final class Worker implements TaskWorker {

		/** Serialization version ID. */
		private static final long serialVersionUID = -7848301189373426210L;
		
		private transient ThreadLocal<LCG> lcg = new ThreadLocal<LCG>() {
			protected LCG initialValue() {
				return new LCG(width * height);
			}
		};

		private transient ThreadLocal<Raster> raster = new ThreadLocal<Raster>() {
			protected Raster initialValue() {
				return colorModel.createRaster(width, height);
			}
		};
		
		private transient ThreadLocal<RepeatableRandom> seqX = new ThreadLocal<RepeatableRandom>() {
			public RepeatableRandom initialValue() {
				return new RepeatableRandom(
						PoorMansMetropolisLightTransportJob.this.random);
			}
		};
		
		private transient ThreadLocal<RepeatableRandom> seqY = new ThreadLocal<RepeatableRandom>() {
			public RepeatableRandom initialValue() {
				return new RepeatableRandom(
						PoorMansMetropolisLightTransportJob.this.random);
			}
		};
		
		private transient ThreadLocal<CategoricalRandom> mutationType = new ThreadLocal<CategoricalRandom>() {
			final double[] weights = new double[]{ 40, 0, 60 };
			public CategoricalRandom initialValue() {
				return new CategoricalRandom(weights);
			}
		};
		
		private Path generateNewPath() {
			RepeatableRandom seq = (RepeatableRandom) seqX.get().createCompatibleRandom();
			seqY.set(seq);
			
//			int index			= lcg.get().next();
//			int x				= index % width;
//			int y				= index / width;
//
//			double y0			= (double) y / height;
//			double y1			= (double) (y + 1) / height;
//
//			double x0			= (double) x / width;
//			double x1			= (double) (x + 1) / width;
//
//			Box2 bounds			= new Box2(x0, y0, x1, y1);

			//Point2 p			= RandomUtil.uniform(bounds, seq);
			Point2 p			= RandomUtil.canonical2(seq);
			seq.mark();
			
			Color sample		= colorModel.sample(seq);
			seq.mark();
			
			PathInfo pi			= new PathInfo(scene, sample.getWavelengthPacket());
			Lens lens			= scene.getLens();
			PathNode eyeTail	= strategy.traceEyePath(lens, p,
										pi, seq);
			seq.mark();
			
			Light light			= scene.getLight();
			PathNode lightTail	= strategy.traceLightPath(
										light, pi, seq);
			seq.mark();

			return new Path(lightTail, eyeTail);
		}
		
		private Path mutateImagePoint(Path path, double width) {
			RepeatableRandom seq = seqX.get().cloneSequence();
			seqY.set(seq);
			seq.reset();
			
			seq.mutate(width);
			Point2 p			= RandomUtil.canonical2(seq);
			seq.mark();
			
			Color sample		= colorModel.sample(seq);
			seq.mark();
			
			PathInfo pi			= new PathInfo(scene, sample.getWavelengthPacket());
			Lens lens			= scene.getLens();
			PathNode eyeTail	= strategy.traceEyePath(lens, p,
										pi, seq);
			seq.mark();
			
			PathNode lightTail	= path.getLightTail();
			seq.mark();

			return new Path(lightTail, eyeTail);
		}
		
		private Path mutateAll(Path path, double width) {
			RepeatableRandom seq = seqX.get().cloneSequence();
			seqY.set(seq);
			seq.reset();
			
			seq.mutate(width);
			Point2 p			= RandomUtil.canonical2(seq);
			seq.mark();
			
			seq.mutate(width);
			Color sample		= colorModel.sample(seq);
			seq.mark();
			
			seq.mutate(width);
			PathInfo pi			= new PathInfo(scene, sample.getWavelengthPacket());
			Lens lens			= scene.getLens();
			PathNode eyeTail	= strategy.traceEyePath(lens, p,
										pi, seq);
			seq.mark();

			seq.mutate(width);
			Light light			= scene.getLight();
			PathNode lightTail	= strategy.traceLightPath(
										light, pi, seq);
			seq.mark();

			return new Path(lightTail, eyeTail);
		}
		
		private Path mutate(Path path) {
			switch (mutationType.get().next(random)) {
			case 0: return generateNewPath();
			case 1: return mutateImagePoint(path, 0.22);
			case 2: return mutateAll(path, 0.22);
			default:
				throw new UnexpectedException();
			}
		}
		
		public double evaluate(List<Contribution> contrib) {
			double f = 0.0;
			for (Contribution c : contrib) {
				f += c.score.luminance();
			}
			return f;
		}
		
		public void record(List<Contribution> contrib, double weight) {
			for (Contribution c : contrib) {
				RasterUtil.addPixel(raster.get(), c.pos, c.score.times(weight));
			}
		}

		/* (non-Javadoc)
		 * @see ca.eandb.jdcp.job.TaskWorker#performTask(java.lang.Object, ca.eandb.util.progress.ProgressMonitor)
		 */
		public Object performTask(Object task, ProgressMonitor monitor) {

			int		mutations			= (Integer) task;
			int		numPixels			= width * height;
			double	mutationsPerPixel	= (double) mutations / (double) numPixels;
			double	lightImageWeight	= 1.0 / mutationsPerPixel;
			Path	x					= null;
			double	fy;
			double	fx = 0.0;
			List<Contribution> cx		= new ArrayList<Contribution>();
			List<Contribution> cy		= new ArrayList<Contribution>();
			boolean accept;
			
			raster.get().clear();
			
			mutations += initialMutations;
			for (int i = 0; i < mutations; i++) {
				if (!monitor.notifyProgress(i, mutations))
					return null;

				Path y = (x != null) ? mutate(x) : generateNewPath();

				Color score			= join(y.getLightTail(), y.getEyeTail(),
											lightImageWeight, cy);
				if (score != null) {
					Point2 p = y.getPointOnImagePlane();
					cy.add(new Contribution(p, score));
				}

				fy = evaluate(cy);
				
				if (fy >= fx) {
					// always accept
					accept = true;
					
					if (i > initialMutations && fy > 0.0) {
						// add contributions from y
						record(cy, 1.0 / fy);
					}
					
				} else { // fy < fx
					double a = fy / fx;
					accept = RandomUtil.bernoulli(a, random);
					
					if (i > initialMutations) {
						// add weighted contributions from x and y
						if (fy > 0.0) {
							record(cy, a / fy);
						}
						record(cx, (1.0 - a) / fx);
//						if (accept) {
//							record(cy, 1.0 / fy);
//						} else {
//							record(cx, 1.0 / fx);
//						}
					}					
				}
				
				if (accept) {
					seqX.set(seqY.get());
					x = y;
					fx = fy;
					List<Contribution> temp = cx;
					cx = cy;
					cy = temp;
					cy.clear();					
				}				
			}

			monitor.notifyProgress(numPixels, numPixels);
			monitor.notifyComplete();

			return raster.get();

		}

//
//		private void joinLightPathToEye(EyeNode eye, PathNode tail, double weight) {
//			PathNode node = tail;
//			while (node != null) {
//				double w = strategy.getWeight(node, eye);
//				if (!MathUtil.isZero(w)) {
//					Color c = PathUtil.join(eye, node);
//					if (c != null) {
//						Point2 p = eye.project(node.getPosition());
//						if (p != null) {
//							if (node instanceof LightNode) {
//								write(p, c.times(w));
//							} else {
//								write(p, c.times(weight * w));
//							}
//						}
//					}
//				}
//				node = node.getParent();
//			}
//		}
//
//		private Color joinInnerToInner(PathNode eyeNode, PathNode lightNode) {
//			assert(eyeNode.getDepth() > 0 && lightNode.getDepth() > 0);
//			double w = strategy.getWeight(lightNode, eyeNode);
//			if (!MathUtil.isZero(w)) {
//				Color c = PathUtil.join(eyeNode, lightNode);
//				return ColorUtil.mul(c, w);
//			} else {
//				return null;
//			}
//		}
//
//		private Color joinInnerToLight(ScatteringNode eyeNode, LightNode light) {
//			return joinInnerToInner(eyeNode, light);
//		}

		private Color join(PathNode lightTail, PathNode eyeTail,
				double lightImageWeight, List<Contribution> contrib) {
			Color score = null;

			PathNode lightNode = lightTail;
			while (true) {

				PathNode eyeNode = eyeTail;
				while (true) {

					Color c = joinAt(lightNode, eyeNode, lightImageWeight,
							contrib);
					score = ColorUtil.add(score, c);

					if (eyeNode == null) {
						break;
					}
					eyeNode = eyeNode.getParent();
				} // eye path loop

				if (lightNode == null) {
					break;
				}
				lightNode = lightNode.getParent();
			} // light path loop

			return score;
		}

		private Color joinAt(PathNode lightNode, PathNode eyeNode,
				double lightImageWeight, List<Contribution> contrib) {
			int l = lightNode != null ? lightNode.getDepth() : -1;
			int e = eyeNode != null ? eyeNode.getDepth() : -1;

			if (e == 0 && l == 0) {
				return joinLightToEye((LightNode) lightNode, (EyeNode) eyeNode,
						lightImageWeight, contrib);
			} else if (e <= 0 && l <= 0) {
				return null;
			} else if (e < 0) {
				return lightPathOnCamera((ScatteringNode) lightNode,
						lightImageWeight);
			} else if (l < 0) {
				return eyePathOnLight((ScatteringNode) eyeNode);
			} else if (e == 0) {
				return joinInnerToEye(lightNode, (EyeNode) eyeNode,
						lightImageWeight, contrib);
			} else if (l == 0) {
				return joinLightToInner((LightNode) lightNode, eyeNode);
			} else {
				return joinInnerToInner((ScatteringNode) lightNode, eyeNode);
			}
		}

		private Color joinInnerToInner(PathNode lightNode, PathNode eyeNode) {
			double w = strategy.getWeight(lightNode, eyeNode);
			if (w > 0.0) {//MathUtil.EPSILON) {
				Color c = measure.evaluate(lightNode, eyeNode);
				return ColorUtil.mul(c, w);
			} else {
				return null;
			}
		}

		private Color joinLightToInner(LightNode lightNode, PathNode eyeNode) {
			// TODO Ignore given light node and select a better light node
			// for the the node on the eye path we want to illuminate.
			return joinInnerToInner(lightNode, eyeNode);
		}

		private Color joinInnerToEye(PathNode lightNode, EyeNode eyeNode,
				double weight, List<Contribution> contrib) {
			double w = strategy.getWeight(lightNode, eyeNode);
			if (w > 0.0) {//MathUtil.EPSILON) {
				Point2 p = eyeNode.project(lightNode.getPosition());
				if (p != null) {
					Color c = measure.evaluate(lightNode, eyeNode);
					c = ColorUtil.mul(c, weight * w);
					if (c != null) {
						contrib.add(new Contribution(p, c));
					}
				}
			}
			return null;
		}

		private Color eyePathOnLight(ScatteringNode eyeNode) {
			if (!eyeNode.isOnLightSource()) {
				return null;
			}

			double w = strategy.getWeight(null, eyeNode);
			if (w > 0.0) {// MathUtil.EPSILON) {
				Color c = measure.evaluate(null, eyeNode);
				return ColorUtil.mul(c, w);
			} else {
				return null;
			}
		}

		private Color lightPathOnCamera(ScatteringNode lightNode,
				double weight) {
			// This cannot happen because the aperture is not part of the scene
			return null;
		}

		private Color joinLightToEye(LightNode lightNode, EyeNode eyeNode,
				double weight, List<Contribution> contrib) {
			return joinInnerToEye(lightNode, eyeNode, weight, contrib);
		}



	}

}

package org.jmist.toolkit;

import java.awt.image.DataBuffer;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipOutputStream;

import javax.imageio.spi.IIORegistry;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.JDialog;

import org.jdcp.job.DummyParallelizableJob;
import org.jdcp.job.ParallelizableJob;
import org.jdcp.job.ParallelizableJobRunner;
import org.jmist.framework.ConstantSpectrum;
import org.jmist.framework.Geometry;
import org.jmist.framework.ImageShader;
import org.jmist.framework.Intersection;
import org.jmist.framework.Light;
import org.jmist.framework.Material;
import org.jmist.framework.Observer;
import org.jmist.framework.PixelShader;
import org.jmist.framework.ProbabilityDensityFunction;
import org.jmist.framework.RayShader;
import org.jmist.framework.Spectrum;
import org.jmist.framework.measurement.CollectorSphere;
import org.jmist.packages.BasisSpectrumFactory;
import org.jmist.packages.NearestIntersectionRecorder;
import org.jmist.packages.PiecewiseLinearProbabilityDensityFunction;
import org.jmist.packages.WavefrontObjectReader;
import org.jmist.packages.geometry.BoundingBoxHierarchyGeometry;
import org.jmist.packages.geometry.CompositeGeometry;
import org.jmist.packages.geometry.SubtractionGeometry;
import org.jmist.packages.geometry.TransformableGeometry;
import org.jmist.packages.geometry.primitive.CylinderGeometry;
import org.jmist.packages.geometry.primitive.PolygonGeometry;
import org.jmist.packages.geometry.primitive.SphereGeometry;
import org.jmist.packages.job.PhotometerJob;
import org.jmist.packages.job.RasterJob;
import org.jmist.packages.lens.PinholeLens;
import org.jmist.packages.lens.TransformableLens;
import org.jmist.packages.light.CompositeLight;
import org.jmist.packages.light.PointLight;
import org.jmist.packages.light.SimpleCompositeLight;
import org.jmist.packages.material.LambertianMaterial;
import org.jmist.packages.material.MirrorMaterial;
import org.jmist.packages.measurement.EqualSolidAnglesCollectorSphere;
import org.jmist.packages.observer.FixedObserver;
import org.jmist.packages.random.NRooksRandom;
import org.jmist.packages.random.ThreadLocalRandom;
import org.jmist.packages.shader.image.CameraImageShader;
import org.jmist.packages.shader.pixel.AveragingPixelShader;
import org.jmist.packages.shader.pixel.RandomPixelShader;
import org.jmist.packages.shader.ray.PathShader;
import org.jmist.packages.spectrum.BlackbodySpectrum;
import org.jmist.packages.spectrum.PiecewiseLinearSpectrum;
import org.jmist.packages.spectrum.ScaledSpectrum;
import org.jmist.packages.spectrum.SumSpectrum;
import org.jmist.toolkit.Grid3.Cell;
import org.jmist.toolkit.matlab.MatlabImageWriterSpi;
import org.jmist.toolkit.matlab.MatlabWriter;
import org.jmist.util.ArrayUtil;
import org.selfip.bkimmel.io.FileUtil;
import org.selfip.bkimmel.jobs.Job;
import org.selfip.bkimmel.progress.CompositeProgressMonitor;
import org.selfip.bkimmel.progress.ConsoleProgressMonitor;
import org.selfip.bkimmel.progress.DummyProgressMonitor;
import org.selfip.bkimmel.progress.ProgressDialog;
import org.selfip.bkimmel.progress.ProgressMonitor;
import org.selfip.bkimmel.progress.ProgressPanel;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		testLinearMatrix3Identity();
//		testReflect();
//		testWeakReference();
//		testObserver();
//		testTuple();
//		testGrid();
//		testRayCircleIntersection();
//		testSwap();
//		testPermutations();
//		testNRooks();
//		testPolynomial();
//		testRoots();
		//testShade();

		//testJobMasterServer();
		//testJobMasterServiceClient();
		//testProgressTree();

		//testParallelizableJobAsJob();

		//testZip();
		//testMath();
		//testLambertianMaterial();
		//testLens();
		//testPolynomial2();
		//testCsg();
		//testSpectrum();
		//testTransformableGeometry();

		//testRender();
		//testPLPDF();

		//testMatlabWriter();
		//testGZip();
		//testInflate();
		//testRange();
		//testMatrix();
		//testParallelJob();

		//testProgressPanel();
		//testScripting();
		testPinholeCamera();
	}

	private static void testPinholeCamera() {
		double aspect = 297.0/293.0;
		double vfov = Math.toRadians(97.0);
		TransformableLens lens = PinholeLens.fromVfovAndAspect(vfov, aspect);
		lens.rotateX(Math.toRadians(-21.0));
		lens.translate(Vector3.K.times(1.5));
		File file = new File("C:/camera.csv");
		try {
			PrintStream out = new PrintStream(new FileOutputStream(file));

			for (int i = 0; i < 297; i++) {
				for (int j = 0; j < 243; j++) {
					Point2 p = new Point2((double) i / 296.0, (double) j / 242.0);
					Ray3 ray = lens.rayAt(p);
					Vector3 v = ray.direction();

					out.print(v.x());
					out.print(',');
					out.print(v.y());
					out.print(',');
					out.print(v.z());
					out.println();
				}
			}

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void testScripting() {
		  ScriptEngineManager mgr = new ScriptEngineManager();
		  List<ScriptEngineFactory> factories =
		      mgr.getEngineFactories();
		  for (ScriptEngineFactory factory: factories) {
		    System.out.println("ScriptEngineFactory Info");
		    String engName = factory.getEngineName();
		    String engVersion = factory.getEngineVersion();
		    String langName = factory.getLanguageName();
		    String langVersion = factory.getLanguageVersion();
		    System.out.printf("\tScript Engine: %s (%s)\n",
		        engName, engVersion);
		    List<String> engNames = factory.getNames();
		    for(String name: engNames) {
		      System.out.printf("\tEngine Alias: %s\n", name);
		    }
		    System.out.printf("\tLanguage: %s (%s)\n",
		        langName, langVersion);
		  }

	}

	@SuppressWarnings("unused")
	private static void testProgressPanel() {

		JDialog dialog = new JDialog();
		ProgressPanel panel = new ProgressPanel();
		dialog.add(panel);
		dialog.setBounds(100, 100, 640, 480);

		dialog.setVisible(true);

		for (int i = 0; i < 10; i++) {

			ProgressMonitor child = panel.createChildProgressMonitor("Test " + new Integer(i + 1).toString());

			for (int j = 0; j < 10; j++) {

				ProgressMonitor grandChild = child.createChildProgressMonitor("Test " + new Integer(i + 1).toString() + " " + new Integer(j + 1).toString());

				for (int k = 0; k < 10; k++) {
					grandChild.notifyProgress(k, 10);
					child.notifyProgress(j * 10 + k, 100);
					panel.notifyProgress(i * 100 + j * 10 + k, 1000);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				grandChild.notifyProgress(10, 10);
				grandChild.notifyComplete();

			}

			child.notifyProgress(100, 100);
			child.notifyComplete();

		}

		panel.notifyProgress(1000, 1000);
		panel.notifyComplete();

		dialog.setVisible(false);
		System.exit(0);

	}

	@SuppressWarnings("unused")
	private static void testMatrix() {

		Matrix A = Matrix.rowMajor(5, 3, new double[]{
				1, 0.5, 2,
				0, 5, 3,
				-1, -6, 3,
				3, 2, 1,
				9, 1, -8
		});

		System.out.println("A");
		printMatrix(A, System.out);

		System.out.println("A.diagonal()");
		printMatrix(A.diagonal(), System.out);

		System.out.println("A.diagonal().transpose()");
		printMatrix(A.diagonal().transpose(), System.out);

		System.out.println("A.row(1)");
		printMatrix(A.row(1), System.out);

		System.out.println("A.column(0)");
		printMatrix(A.column(0), System.out);

		System.out.println("A.column(2).transpose()");
		printMatrix(A.column(2).transpose(), System.out);

		System.out.println("A.transpose()");
		printMatrix(A.transpose(), System.out);

		System.out.println("A.slice(1, 1, 3, 2)");
		printMatrix(A.slice(1, 1, 3, 2), System.out);

		System.out.println("A.slice(1, 1, 3, 2).diagonal()");
		printMatrix(A.slice(1, 1, 3, 2).diagonal(), System.out);

		System.out.println("A.slice(1, 1, 3, 2).transpose()");
		printMatrix(A.slice(1, 1, 3, 2).transpose(), System.out);

		System.out.println("Matrix.ones(3, 2)");
		printMatrix(Matrix.ones(3, 2), System.out);

		System.out.println("Matrix.zeros(2, 3)");
		printMatrix(Matrix.zeros(2, 3), System.out);

		System.out.println("Matrix.constant(3, 4, 5)");
		printMatrix(Matrix.constant(3, 4, 5), System.out);

	}

	private static void printMatrix(Matrix matrix, PrintStream out) {
		for (int r = 0; r < matrix.rows(); r++) {
			for (int c = 0; c < matrix.columns(); c++) {
				if (c > 0) out.print(' ');
				double value = matrix.at(r, c);
				if (value >= 0.0) {
					out.print(' ');
				}
				out.print(matrix.at(r, c));
			}
			out.println();
		}
	}

	@SuppressWarnings("unused")
	private static void testRange() {

		for (int x : ArrayUtil.range(1, 10)) {
			System.out.printf("Test %d.\n", x);
		}

		for (int bottlesOfBeer : ArrayUtil.range(99, 1)) {

			System.out.printf("%d bottles of beer on the wall,\n", bottlesOfBeer);
			System.out.printf("%d bottles of beer.\n", bottlesOfBeer);
			System.out.println("Take one down, pass it around,");
			System.out.printf("%d bottles of beer on the wall.\n", bottlesOfBeer - 1);

		}

	}

	@SuppressWarnings("unused")
	private static void testInflate() {

		Inflater inf = new Inflater();

		inf.setInput(new byte[]{ (byte) 0x78, (byte) 0x9c, (byte) 0xe3, (byte) 0x63, (byte) 0x60, (byte) 0x60, (byte) 0xf0, (byte) 0x00, (byte) 0x62, (byte) 0x36, (byte) 0x20, (byte) 0xe6, (byte) 0x00, (byte) 0x62, (byte) 0x16, (byte) 0x06, (byte) 0x08, (byte) 0x60, (byte) 0x85, (byte) 0xf2, (byte) 0x19, (byte) 0x81, (byte) 0x98, (byte) 0x1b, (byte) 0x4a, (byte) 0x83, (byte) 0xd4, (byte) 0x24, (byte) 0x96, (byte) 0x96, (byte) 0x64, (byte) 0xe4, (byte) 0x17, (byte) 0x31, (byte) 0x30, (byte) 0x08, (byte) 0x40, (byte) 0xc5, (byte) 0x9d, (byte) 0x8a, (byte) 0x12, (byte) 0x53, (byte) 0x14, (byte) 0xbc, (byte) 0x33, (byte) 0x73, (byte) 0x73, (byte) 0x53, (byte) 0x73, (byte) 0xc0, (byte) 0xfa, (byte) 0x00, (byte) 0x9d, (byte) 0x68, (byte) 0x07, (byte) 0x2f });
		byte[] buffer = new byte[1000];
		int len = 0;

		try {
			len = inf.inflate(buffer);
			inf.end();
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
		FileOutputStream file = new FileOutputStream("C:/test.out");
		file.write(buffer, 0, len);
		file.flush();
		file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void testGZip() {

		try {

			OutputStream file = new FileOutputStream("C:/test.txt.gz");
			GZIPOutputStream gzip = new GZIPOutputStream(file);
			PrintStream out = new PrintStream(gzip);

			out.println("This is a test.");

			out.flush();
			out.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void testMatlabWriter() {

		try {
			OutputStream file = new FileOutputStream("C:/test.mat");
			MatlabWriter writer = new MatlabWriter(file);
			//writer.write(new double[]{1.1, 2.2, 3.3});
			writer.write("author", "Brad Kimmel");
			writer.write("Z", new Complex[]{ new Complex(1,2) }, new int[]{ 1, 1 });
			writer.write("Y", new int[]{ 1,2,3,4,5,6 }, new int[]{ 6,5,4,3,2,1 }, new int[]{6, 1});
			writer.write("X", new double[]{ 1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8 }, new int[]{ 2, 4 });
			writer.flush();
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static void testPLPDF() {
		ProbabilityDensityFunction pdf = new PiecewiseLinearProbabilityDensityFunction(
				new double[]{ 0, 33, 66, 100 },
				new double[]{ 0, 1, 1, 0 }
		);

		double[] samples = pdf.sample(new double[100000]);
		Arrays.sort(samples);

		int i = 0;
		int c;
		for (int x = 5; x < 101; x += 5) {
			c = 0;
			while (i < samples.length && samples[i] < (double) x) {
				c++; i++;
			}
			System.out.println(c);
		}

	}

	@SuppressWarnings("unused")
	private static void testRender() {

		try {

			Spectrum emission = new ScaledSpectrum(1, new BlackbodySpectrum(5500)); // new ConstantSpectrum(1e3);
			Spectrum reflectance = new ConstantSpectrum(1.0);

			Material matte = new LambertianMaterial(reflectance, null);
			Material reflective = new MirrorMaterial(new PiecewiseLinearSpectrum(new double[]{
					350e-9, 400e-9, 450e-9, 500e-9, 550e-9, 600e-9, 650e-9, 700e-9, 750e-9, 800e-9, 850e-9
			}, new double[] {
					0.1, 0.12, 0.15, 0.2, 0.3, 0.5, 0.75, 0.8, 0.9, 0.92, 0.93
					//0.0, 0.0, 0.0, 0, 0, 0, 0, 0.8, 0.9, 0.92, 0.93
			}));
			Material emissive = new LambertianMaterial(null, emission);

			TransformableLens lens = PinholeLens.fromHfovAndAspect(0.785398, 1.0);
			//TransformableGeometry object = new TransformableGeometry(new CylinderGeometry(new Point3(0, -1, 0), 0.25, 2, matte));
			//TransformableGeometry object = new TransformableGeometry(new TorusGeometry(1.0, 0.25, matte));
			//TransformableGeometry object = new TransformableGeometry(new DiscGeometry(Point3.ORIGIN, Vector3.J, 1.0, true, matte));
			//TransformableGeometry object = new TransformableGeometry(new SuperellipsoidGeometry(1.5, 1.5, matte));
			//TransformableGeometry object = new TransformableGeometry(new SphereGeometry(Point3.ORIGIN, 1, matte));
			//TransformableGeometry object = new TransformableGeometry(new BoxGeometry(new Box3(-0.25, -0.25, -0.25, 0.25, 0.25, 0.25), matte));
			//TransformableGeometry mirror = new TransformableGeometry(new RectangleGeometry(new Point3(0, -0.5, 0), Basis3.fromW(Vector3.J, Basis3.Orientation.RIGHT_HANDED), 10, 10, true, reflective));
//			TransformableGeometry object = new TransformableGeometry(new PolyhedronGeometry(
//					new Point3[]{
//							new Point3(-0.5, 0, -0.5),
//							new Point3( 0.5, 0, -0.5),
//							new Point3( 0.5, 0,  0.5),
//							new Point3(-0.5, 0,  0.5),
//							new Point3( 0.0, 1,  0.0)
//					},
//					new int[][]{
//							new int[]{ 0, 1, 2, 3 },
//							new int[]{ 1, 0, 4 },
//							new int[]{ 2, 1, 4 },
//							new int[]{ 3, 2, 4 },
//							new int[]{ 0, 3, 4 }
//					}
//					, matte));
			//TransformableGeometry object = new TransformableGeometry(new HeightFieldGeometry(new Box2(-1, -1, 1, 1), createHeightField(), matte));
			//TransformableGeometry object = new TransformableGeometry(createSphereSnowflake(matte, 10));
			TransformableGeometry object = new TransformableGeometry(new PolygonGeometry(
					new Point3[]{
							new Point3( 2,  2,  0),
							new Point3(-2,  2,  0),
							new Point3(-2, -2,  0),
							new Point3( 2, -2,  0),
							new Point3( 1,  1,  0),
							new Point3(-1,  1,  0),
							new Point3(-1, -1,  0),
							new Point3( 1, -1,  0)
					},
					new int[][]{
							new int[]{ 0, 1, 2, 3 },
							new int[]{ 7, 6, 5, 4 }
					},
					matte
			));
			CylinderGeometry emitter = new CylinderGeometry(new Point3(0, -10, 0), 10, 20, emissive);
			//Light light = new PointLight(new Point3(0, 0, 4), emission, false);
			CompositeLight light = new SimpleCompositeLight();
			CompositeGeometry geometry = new TransformableGeometry()
					.addChild(object);	/* inner cylinder */
					//.addChild(mirror);
					//.addChild(new InsideOutGeometry(emitter));

			object.rotateX(Math.toRadians(25));
			object.rotateY(Math.toRadians(25));
			object.rotateZ(Math.toRadians(15));
			//lens.translate(new Vector3(0, 0, 5));

			/*
			Model model = EmissionTestModel.getInstance();
			geometry = model.getGeometry();
			lens = (TransformableLens) model.getLens();
			light = model.getLight();
			*/
			//lens = new OmnimaxLens();
			//lens.rotateY(-Math.PI / 2.0);
			lens.rotateX(-Math.atan2(2.75, 1.5));
			lens.translate(new Vector3(-0.5, 2.75, 1.5));

			geometry = new BoundingBoxHierarchyGeometry();
			WavefrontObjectReader reader = new WavefrontObjectReader();
			reader.addMaterial("diffuse50SG", new LambertianMaterial(new ConstantSpectrum(0.5)));
			reader.addMaterial("diffuseLuminaire1SG", new LambertianMaterial(null, Spectrum.ONE));
			reader.read(new FileInputStream("C:\\Documents and Settings\\Erin\\My Documents\\Brad\\jmist\\secondary.obj"), geometry, light, 0.01);

			//Observer observer = new StandardObserver(StandardObserver.Type.CIE_2_DEGREE, 3);
			//Observer observer = new FixedObserver(ArrayUtil.range(400e-9, 700e-9, 31));
			Observer observer = new FixedObserver(550e-9);

			RayShader shader = new PathShader(geometry, light, observer);//new VisibilityRayShader(geometry, 255, 0);//
			ImageShader camera = new CameraImageShader(lens, shader);
			PixelShader pixelShader = new AveragingPixelShader(1000, new RandomPixelShader(new ThreadLocalRandom(new NRooksRandom(1000, 2)), camera));
			//ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ), false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
			SampleModel sm = new PixelInterleavedSampleModel(DataBuffer.TYPE_DOUBLE, 256, 256, 1, 256*1, ArrayUtil.range(0, 0));

			//FileChannel ch = new RandomAccessFile("/tmp/render.tmp", "rw").getChannel();
			//ByteBuffer buf = ch.map(FileChannel.MapMode.READ_WRITE, 0, 256 * 256 * 1 * 8);
			ByteBuffer buf = ByteBuffer.wrap(new byte[256 * 256 * 1 * 8]);
			//ch.close();
			DoubleBuffer dbuf = buf.asDoubleBuffer();
			DataBuffer db = new DoubleDataBufferAdapter(dbuf);
			WritableRaster raster = Raster.createWritableRaster(sm, db, null);
			//BufferedImage image = new BufferedImage(cm, raster, false, null);
			//BufferedImage image = new BufferedImage(500, 500, BufferedImage.);
			IIORegistry.getDefaultInstance().registerServiceProvider(new MatlabImageWriterSpi());
			ParallelizableJob job = new RasterJob(pixelShader, raster, "mat", 10, 10);
			JDialog dialog = new JDialog();
			ProgressPanel monitor = new ProgressPanel();
			dialog.add(monitor);
			dialog.setVisible(true);
			//ProgressMonitor monitor = DummyProgressMonitor.getInstance();

			File base = new File("C:\\Documents and Settings\\Erin\\My Documents\\Brad\\jmist");
			UUID id = UUID.randomUUID();
			File dir = new File(base, id.toString());

			Job runner = new ParallelizableJobRunner(job, dir, Runtime.getRuntime().availableProcessors());
			runner.go(monitor);
			//job.go(monitor);

			File zipFile = new File(base, id.toString() + ".zip");
			FileUtil.zip(zipFile, dir);

//			dialog.setVisible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static Geometry createSphereSnowflake(Material material, int levels) {
		CompositeGeometry geometry = new BoundingBoxHierarchyGeometry(); // FIXME
		createSphereSnowflake(geometry, Sphere.UNIT, material, Basis3.STANDARD, levels, true);
		return geometry;
	}

	private static void createSphereSnowflake(CompositeGeometry geometry, Sphere center, Material material, Basis3 basis, int levels, boolean first) {

		if (levels > 0) {

			Geometry sphere = new SphereGeometry(center, material);
			geometry.addChild(sphere);

			double alpha = 0.5;
			double radius = alpha * center.radius();
			for (int i = 0; i < 3; i++) {

				SphericalCoordinates sc = new SphericalCoordinates(2.0 * Math.PI / 3.0, ((double) (i - 1)) * 2.0 * Math.PI / 3.0, radius + center.radius()).canonical();
				SphericalCoordinates u = new SphericalCoordinates(Math.PI / 2.0, 0.0);
				Vector3 orientation = sc.toCartesian(basis).opposite();

				radius *= 0.8;

				Sphere childSphere = new Sphere(center.center().minus(orientation), radius);
				Basis3 childBasis = Basis3.fromWU(orientation, u.toCartesian(basis));
				createSphereSnowflake(geometry, childSphere, material, childBasis, levels - 1, false);

			}

			if (first) {
				SphericalCoordinates sc = new SphericalCoordinates(0.0, 0.0, radius + center.radius());
				SphericalCoordinates u = new SphericalCoordinates(Math.PI / 2.0, 0.0);
				Vector3 orientation = sc.toCartesian(basis).opposite();

				radius *= 0.8;

				Sphere childSphere = new Sphere(center.center().minus(orientation), radius);
				Basis3 childBasis = Basis3.fromWU(orientation, u.toCartesian(basis));
				createSphereSnowflake(geometry, childSphere, material, childBasis, levels - 1, false);

			}

		}

	}

	@SuppressWarnings("unused")
	private static Matrix createHeightField() {
		double[] height = new double[1001 * 1001];
		for (int x = 0, i = 0; x <= 1000; x++) {
			double dx = 4.0 * Math.PI * (double) (x - 500) / 500.0;
			for (int z = 0; z <= 1000; z++, i++) {
				double dz = 4.0 * Math.PI * (double) (z - 500) / 500.0;
				//height[i] = 0.1 * Math.cos(Math.sqrt(dx * dx + dz * dz));
				//height[i] = 0.1 * Math.cos(Math.sqrt(dx * dx + dz * dz)) / (1.0 + 0.1 * (dx * dx + dz * dz));
				//height[i] = -0.001 * (dx * dx + dz * dz) + 0.1 * Math.sin(dx) * Math.sin(dz);
				dx = (double) (x - 500) / 500.0;
				dz = (double) (z - 500) / 500.0;
				//double rx = Math.round(dx);
				//double rz = Math.round(dz);
				double d = Math.sqrt(dx * dx + (Math.abs(dz) - 0.25) * (Math.abs(dz) - 0.25));
				double det1 = 0.25 * 0.25 - d * d;
				double det2 = 0.025 * 0.025 - d * d;
				double h1 = det1 > 0.0 ? Math.sqrt(det1) : 0.0;
				double h2 = det2 > 0.0 ? Math.sqrt(det2) : 0.0;
				height[i] = h1 + h2;
			}
		}
		return Matrix.columnMajor(1001, 1001, height);
	}

	@SuppressWarnings("unused")
	private static void testTransformableGeometry() {

		TransformableLens lens = PinholeLens.fromHfovAndAspect(Math.PI / 3, 1.0);
		TransformableGeometry geometry = new TransformableGeometry(
				new CylinderGeometry(
						new Point3(0, -1, 0),
						0.25,
						2,
						null
				)
		);

		//lens.rotateX(Math.toRadians(40.0));
		//lens.rotateY(Math.toRadians(25.0));
		lens.translate(Vector3.K);
		geometry.translate(Vector3.NEGATIVE_K);
		geometry.translate(new Vector3(0.2, 0, 0));

		Ray3 ray = lens.rayAt(new Point2(0.5, 0.5));
		printRay3(ray);
		System.out.println();

		Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, geometry);

		if (x != null) {
			printPoint3(x.location());
			System.out.println();
			printVector3(x.normal());
		} else {
			System.out.print("No intersection");
		}
		System.out.println();

	}

	@SuppressWarnings("unused")
	private static void testSpectrum() {

		BasisSpectrumFactory factory = new BasisSpectrumFactory()
			.addBasisSpectrum(new BlackbodySpectrum(5500))
			.addBasisSpectrum(new BlackbodySpectrum(7000))
			.addBasisSpectrum(new SumSpectrum()
				.addChild(new BlackbodySpectrum(4500))
				.addChild(new BlackbodySpectrum(6500))
			)
			.addBasisSpectrum(Spectrum.ONE);

		Spectrum spectrum = factory.create(0.5, 0.75, 0.2);

		double value = spectrum.sample(550e-9);

	}

	@SuppressWarnings("unused")
	private static void testPolynomial2() {

		Polynomial f = new Polynomial(1, -2, 3);
		System.out.printf("f(t)=%s\nf(3)=%f\n", f.toString("t"), f.at(3));

	}

	private static void printPoint3(Point3 p) {
		System.out.printf("[Point3: %f %f %f]", p.x(), p.y(), p.z());
	}

	private static void printVector3(Vector3 v) {
		System.out.printf("[Vector3: %f %f %f]", v.x(), v.y(), v.z());
	}

	private static void printRay3(Ray3 ray) {
		System.out.print("[Ray3: ");
		printPoint3(ray.origin());
		System.out.print(" ");
		printVector3(ray.direction());
		System.out.print("]");
	}

	@SuppressWarnings("unused")
	private static void testCsg() {

		TransformableLens lens = PinholeLens.fromHfovAndAspect(Math.PI / 3, 1.0);

		//lens.rotateX(Math.toRadians(40.0));
		//lens.rotateY(Math.toRadians(25.0));
		lens.translate(Vector3.K);

		Ray3 ray = lens.rayAt(new Point2(0.5, 0.5));
		printRay3(ray);
		System.out.println();

		Geometry geometry = new SubtractionGeometry()
			.addChild(new CylinderGeometry(new Point3(0, -1, -0.1), 0.5, 2, null))
			.addChild(new CylinderGeometry(new Point3(0, -1, 0.1), 0.5, 2, null))
			.addChild(new CylinderGeometry(new Point3(0, -1, 0.6), 0.5, 2, null));

		Light light = new PointLight(new Point3(1, 1, 1), new ConstantSpectrum(1e3), true);

		Intersection x = NearestIntersectionRecorder.computeNearestIntersection(ray, geometry);

		//light.illuminate(x, geometry);

		//ScatterResult scatter = x.material().scatter(x, new Tuple(550));


		//Shader shader = new DirectIlluminationShader(geometry, light);



		if (x != null) {
			printPoint3(x.location());
			System.out.println();
			printVector3(x.normal());
		} else {
			System.out.print("No intersection");
		}
		System.out.println();

	}

	@SuppressWarnings("unused")
	private static void testLens() {

		TransformableLens lens = PinholeLens.fromHfovAndAspect(Math.PI / 3, 1.0);

		lens.rotateX(Math.toRadians(40.0));
		lens.rotateY(Math.toRadians(25.0));
		lens.translate(Vector3.K);

		Ray3 ray = lens.rayAt(new Point2(0.5, 0.5));
		printRay3(ray);
		System.out.println();

		NearestIntersectionRecorder recorder = new NearestIntersectionRecorder();

		Geometry geometry = new CylinderGeometry(new Point3(0, -1, 0), 0.25, 2, null);

		geometry.intersect(ray, recorder);

		if (!recorder.isEmpty()) {
			printPoint3(recorder.nearestIntersection().location());
			System.out.println();
			printVector3(recorder.nearestIntersection().normal());
		} else {
			System.out.print("No intersection");
		}
		System.out.println();

	}

	@SuppressWarnings("unused")
	private static void testMath() {

		System.out.println(Math.acos(-1.0));

	}

	@SuppressWarnings("unused")
	private static void testLambertianMaterial() {
//		public PhotometerJob(Material specimen,
//				SphericalCoordinates[] incidentAngles, double[] wavelengths,
//				long samplesPerMeasurement, long samplesPerTask, CollectorSphere prototype) {
//
		ParallelizableJob job = getMeasurementJob();


//		JDialog dialog = new JDialog();
//		ProgressPanel progressTree = new ProgressPanel("Working...");
//		dialog.add(progressTree);
//
//		dialog.setVisible(true);

		UUID id = UUID.randomUUID();
		File base = new File("/home/bwkimmel");
		File dir = new File(base, id.toString());
		dir.mkdir();
		Job runner = new ParallelizableJobRunner(job, dir, 2);

		try {
			runner.go(DummyProgressMonitor.getInstance());
			if (job.isComplete()) {
				File zipFile = new File(base, id.toString() + ".zip");
				FileUtil.zip(zipFile, dir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	private static ParallelizableJob getMeasurementJob() {
		Spectrum reflectance = new ConstantSpectrum(0.75);
		Material specimen = new LambertianMaterial(reflectance);
		CollectorSphere collector = new EqualSolidAnglesCollectorSphere(30, 30, true, false);
		SphericalCoordinates[] incidentAngles = { SphericalCoordinates.NORMAL };
		double[] wavelengths = { 5e-7 /* 500nm */ };

		ParallelizableJob job = new PhotometerJob(specimen, incidentAngles, wavelengths, 100000000, 1000000, collector);
		return job;
	}

	@SuppressWarnings("unused")
	private static void testZip() {

		try {
			FileOutputStream fos = new FileOutputStream("C:/test2.zip");
			ZipOutputStream zip = new ZipOutputStream(fos);
//			zip.putNextEntry(new ZipEntry("test/blah.dat"));
//			zip.write(new byte[5]);
//			zip.putNextEntry(new ZipEntry("blah.dat2"));
//			zip.write(new byte[5]);
//			zip.closeEntry();

			zip.finish();

			zip.close();

		} catch (Exception e) {
			e.printStackTrace();
		}


	}

	@SuppressWarnings("unused")
	private static void testParallelizableJobAsJob() {

		Job job = new DummyParallelizableJob(100, 500, 800);
		try {
			job.go(
					new CompositeProgressMonitor()
						.addProgressMonitor(new ProgressDialog())
						.addProgressMonitor(new ProgressDialog())
						.addProgressMonitor(new ConsoleProgressMonitor())
			);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static void testProgressTree() {

		JDialog dialog = new JDialog();
		ProgressPanel progressTree = new ProgressPanel("Working...");
		dialog.add(progressTree);

		ProgressMonitor child = progressTree.createChildProgressMonitor("Test");

		child.notifyStatusChanged("Booya");
		child.notifyIndeterminantProgress();

		dialog.setVisible(true);

	}

//	@SuppressWarnings("unused")
//	private static void testJobMasterServiceClient() {
//
//		String host = "localhost";
//		JDialog dialog = new JDialog();
//		ProgressPanel monitor = new ProgressPanel();
//		ParallelizableJob job = getMeasurementJob(); //new DummyParallelizableJob(100, 5000, 10000);
//		Executor threadPool = Executors.newFixedThreadPool(2, new BackgroundThreadFactory());
//		Job submitJob = new ServiceSubmitJob(job, 0, host);
//		Job workerJob = new ThreadServiceWorkerJob(host, 10000, 2, threadPool);
//
//		dialog.add(monitor);
//		dialog.setBounds(0, 0, 400, 300);
//		dialog.setVisible(true);
//
//		submitJob.go(monitor);
//		//workerJob.go(monitor);
//
//	}

	@SuppressWarnings("unused")
	private static void testParallelJob() {

		String host = "localhost";
		ParallelizableJob job = getMeasurementJob(); //new DummyParallelizableJob(100, 5000, 10000);
		File dir = new File("/home/bwkimmel", UUID.randomUUID().toString());
		dir.mkdir();
		Job runner = new ParallelizableJobRunner(job, dir, 8);
		try {
			runner.go(new ConsoleProgressMonitor());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unused")
	private static void testShade() {

//		Lens lens = new FisheyeLens();
//		RayShader rayShader = new DirectionalTestRayShader();
//		ImageShader imageShader = new CameraImageShader(lens, rayShader);
//		PixelShader pixelShader = new SimplePixelShader(imageShader);
//		ImageRasterWriter rasterWriter = new ImageRasterWriter(2048, 2048);
//		Job job = new RasterJob(pixelShader, rasterWriter);
//		ProgressMonitor monitor = new ProgressDialog();
//
//		job.go(monitor);
//
//		try {
//			rasterWriter.save("C:\\test.jpg", "jpg");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testRoots() {
		Polynomial[] roots = new Polynomial[4];
		Polynomial[] poly = new Polynomial[4];
		int i;
		java.util.Random rnd = new java.util.Random();

		System.out.print("All roots:");
		for (i = 0; i < roots.length; i++) {
			roots[i] = new Polynomial(-rnd.nextDouble(), 1.0);
			System.out.print(" ");
			System.out.print(-roots[i].coefficient(0));
			poly[i] = i > 0 ? poly[i - 1].times(roots[i]) : roots[i];
		}
		System.out.println();

		for (i = 0; i < poly.length; i++) {
			System.out.println(poly[i]);
			System.out.print("roots:");
			for (double root : poly[i].roots()) {
				System.out.print(" ");
				System.out.print(root);
			}
			System.out.println();
		}
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testPolynomial() {
		Polynomial p = new Polynomial(3, 2, 1);
		Polynomial q = new Polynomial(-4, 1, -1);

		Polynomial r = p.times(q);
		System.out.printf("(%s)(%s) = (%s)\n", p.toString(), q.toString(), r.toString());

		r = p.plus(q);
		System.out.printf("(%s) + (%s) = (%s)", p.toString(), q.toString(), r.toString());

		System.out.println("------------------------");
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testNRooks() {
		NRooksRandom rand = new NRooksRandom(10, 4);
		int i;
		int j;
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 4; j++) {
				if (j > 0) {
					System.out.print(", ");
				}
				System.out.printf("%f", rand.next());
			}
			System.out.println();
		}

		System.out.println("------------------------");
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testPermutations() {
		Permutation Px = Permutation.random(8);
		Permutation Py = Permutation.random(8);

		int board[][] = new int[8][8];
		int i,j;

		for (i = 0; i < 8; i++) {
			board[Px.at(i)][Py.at(i)] = i + 1;
		}

		for (i = 0; i < 8; i++) {
			for (j = 0; j < 8; j++) {
				if (board[j][i] > 0) {
					System.out.printf("[%d]", board[j][i]);
				} else {
					System.out.print("[ ]");
				}
			}
			System.out.println();
		}

		System.out.println("------------------------");
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testSwap() {
		double[] arr = new double[2];

		arr[0] = 1.0;
		arr[1] = 2.0;

		System.out.println("arr=[" + arr[0] + ", " + arr[1] + "]");
		swapFirstTwoElements(arr);
		System.out.println("arr=[" + arr[0] + ", " + arr[1] + "]");

		System.out.println("------------------------");
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	private static void testRayCircleIntersection() {
		boolean passed = true;
		java.util.Random rnd = new java.util.Random();

		for (int i = 0; i < 1000; i++) {
			Point2		center = new Point2(rnd.nextDouble() * 100.0 - 50.0, rnd.nextDouble() * 100.0 - 50.0);
			double		radius = rnd.nextDouble() * 10.0;
			Circle		circle = new Circle(center, radius);
			Point2		origin = new Point2(rnd.nextDouble() * 100.0 - 50.0, rnd.nextDouble() * 100.0 - 50.0);
			double		theta = rnd.nextDouble() * 2.0 * Math.PI;
			double		r = rnd.nextDouble() * radius;
			Vector2		centerToTarget = new Vector2(r * Math.cos(theta), r * Math.sin(theta));
			Point2		target = center.plus(centerToTarget);
			Vector2		direction = target.vectorFrom(origin).unit();
			Ray2		ray2 = new Ray2(origin, direction);
			Interval	J = circle.intersect(ray2);
			Point2		p0 = ray2.pointAt(J.minimum());
			Point2		p1 = ray2.pointAt(J.maximum());

			boolean		ok0 = circle.nearBoundary(p0) || circle.contains(p0);
			boolean		ok1 = circle.nearBoundary(p1);

			if (!ok0 || !ok1) {
				System.out.println("Error, ray-circle intersection point not on boundary of circle.");
				passed = false;
				break;
			}
		}

		if (passed) {
			System.out.println("ray-circle intersection test passed.");
		}

	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testGrid() {
		Grid3 grid = new Grid3(new Box3(0.0, 0.0, 0.0, 1.0, 1.0, 1.0), 4, 4, 4);
		Ray3 ray = new Ray3(new Point3(-1.0, -1.5, -1.0), new Vector3(1.4, 2.1, 1.3).unit());

		Grid3.Visitor visitor = new Grid3.Visitor() {

			/* (non-Javadoc)
			 * @see org.jmist.toolkit.Grid3.Visitor#visit(org.jmist.toolkit.Ray3, org.jmist.toolkit.Interval, org.jmist.toolkit.Grid3.Cell)
			 */
			public boolean visit(Ray3 ray, Interval I, Cell cell) {

				System.out.println("I=[" + I.minimum() + ", " + I.maximum() + "]");
				System.out.println("cell=[" + cell.getX() + " " + cell.getY() + " " + cell.getZ() + "]");

				Point3		p0 = ray.pointAt(I.minimum());
				Point3		p1 = ray.pointAt(I.maximum());
				Vector3		N0 = cell.getBoundingBox().normalAt(p0);
				Vector3		N1 = cell.getBoundingBox().normalAt(p1);

				System.out.println("p0=(" + p0.x() + ", " + p0.y() + ", " + p0.z() + "); N0=(" + N0.x() + ", " + N0.y() + ", " + N0.z() + ")");
				System.out.println("p1=(" + p1.x() + ", " + p1.y() + ", " + p1.z() + "); N1=(" + N1.x() + ", " + N1.y() + ", " + N1.z() + ")");

				System.out.println("-----------------------------");

				return true;

			}

		};

		grid.intersect(ray, Interval.POSITIVE, visitor);
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testTuple() {
		double[] array = new double[]{ 1.0, 2.0, 3.0 };
		Tuple zzz = new Tuple(array);
		array[1] = -2.0;

		System.out.println("-------");
		System.out.println(zzz.filter(new Tuple.Filter() { public boolean apply(double value) { return value < 2.5; } }));
		System.out.println(zzz.map(new Tuple.Function() {

			public double apply(double value) {
				return value * value;
			}} ));
		System.out.println(zzz.reduce(Tuple.PRODUCT_OPERATOR));

		System.out.println(zzz.reverse());
		System.out.println(zzz.append(zzz.reverse()));
		System.out.println(zzz.append(4.0));
		System.out.println(zzz.append(new double[]{4.0, 5.0}));
		System.out.println(zzz.permute(new int[]{2,1,2,1,0}));
		System.out.println(zzz.right(2));
		System.out.println(zzz.left(2));
		System.out.println(zzz.append(zzz.reverse()).slice(2, 5));
		System.out.println(Tuple.zeros(5));
		System.out.println(Tuple.ones(7));
		System.out.println(zzz.combine(Tuple.ones(3), Tuple.SUM_OPERATOR));
		System.out.println("-------");
		System.out.print("zzz[1]=");
		System.out.println(zzz.at(1));
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testReflect() {
		Vector3 I = new Vector3(1.0, 1.0, 1.0).unit();
		Vector3 R = Optics.reflect(I, Vector3.J);
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testWeakReference() {
		Object x = new Integer(5);
		Reference<Object> ref1 = new WeakReference<Object>(x);
		Reference<Object> ref2 = new WeakReference<Object>(x);

		System.out.println(ref1.get().equals(ref2.get()));
	}

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static void testLinearMatrix3Identity() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				System.out.print(" " + LinearMatrix3.IDENTITY.at(i, j));
			}
			System.out.println();
		}
	}

	public static void swapFirstTwoElements(double[] arr) {
		double temp = arr[0];
		arr[0] = arr[1];
		arr[1] = temp;
	}

}

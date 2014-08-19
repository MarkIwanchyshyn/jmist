/* Copyright (c) 2014 Bradley W. Kimmel
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
package ca.eandb.jmist.proto.factory;

import ca.eandb.jdcp.job.ParallelizableJob;
import ca.eandb.jmist.framework.Display;
import ca.eandb.jmist.framework.ImageShader;
import ca.eandb.jmist.framework.PixelShader;
import ca.eandb.jmist.framework.Random;
import ca.eandb.jmist.framework.RayShader;
import ca.eandb.jmist.framework.Scene;
import ca.eandb.jmist.framework.color.ColorModel;
import ca.eandb.jmist.framework.job.RasterJob;
import ca.eandb.jmist.framework.random.NRooksRandom;
import ca.eandb.jmist.framework.random.ThreadLocalRandom;
import ca.eandb.jmist.framework.shader.image.CameraImageShader;
import ca.eandb.jmist.framework.shader.pixel.RandomPixelShader;
import ca.eandb.jmist.framework.shader.ray.SceneRayShader;
import ca.eandb.jmist.proto.RenderProtos;

/**
 *
 */
public final class ProtoRenderFactory {

  private final ProtoSceneFactory sceneFactory;

  private final ColorModel colorModel;

  public ProtoRenderFactory(ProtoSceneFactory sceneFactory,
                            ColorModel colorModel) {
    this.sceneFactory = sceneFactory;
    this.colorModel = colorModel;
  }

  public ParallelizableJob createRenderJob(RenderProtos.RenderJob jobIn,
                                           Display display) {
    Scene scene = sceneFactory.createScene(jobIn.getScene());
    RayShader rayShader = new SceneRayShader(scene);
    ImageShader imageShader = new CameraImageShader(scene.getLens(), rayShader);
    Random pixelRandom = new ThreadLocalRandom(
        new NRooksRandom(jobIn.getSamplesPerPixel(), 2));
    PixelShader pixelShader = new RandomPixelShader(pixelRandom, imageShader,
                                                    colorModel);
    RenderProtos.Size imageSize = jobIn.getImageSize();
    int rows = Math.max(1, imageSize.getX() / jobIn.getTileSize().getX());
    int cols = Math.max(1, imageSize.getY() / jobIn.getTileSize().getY());
    return new RasterJob(colorModel, pixelShader, display,
                         imageSize.getX(), imageSize.getY(),
                         rows, cols);
  }

}

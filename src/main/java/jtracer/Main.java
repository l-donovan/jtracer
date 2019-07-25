package jtracer;

import jtracer.tracelib.*;
import jtracer.tracelib.basetypes.*;
import jtracer.tracelib.helper.Vec;
import jtracer.tracelib.primitives.Plane;
import jtracer.tracelib.primitives.Sphere;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static jtracer.tracelib.Core.STD_COLORS;
import static jtracer.tracelib.Core.STD_MATERIALS;

public class Main {
    private static Scene scene = new Scene()
        .withBackgroundColor(STD_COLORS.get("celestial-blue"))
        .withMaterials(STD_MATERIALS)
        .withObjects(new SceneObject[]{
            new Sphere(new Vec(-1.0, 1.0, 7.0), 2.0, "glass"),
            new Sphere(new Vec(-0.75, -1.0, 12.0), 2.0, "mirror"),
            new Sphere(new Vec(3.0, 0.5, 6.0), 1.5, "mirror"),
            new Sphere(new Vec(2.5, 0.0, 4.0), 1.0, "glass"),
            new Plane(new Vec(0, -5, 0), new Vec(1, -5, 0), new Vec(1, -5, 1), "floor")
        }).withLights(new SceneLight[]{
            new SceneLight(new Vec(-20,  70, -20), 0.5),
            new SceneLight(new Vec(30,  50,  12), 1.0),
            new SceneLight(new Vec(10, -30,  12), 0.4)
        });

    private static Camera camera = new Camera()
        .withFOV(90.0)
        .withRes(new Vec(1920, 1080))
        .withPos(new Vec(0, 0, 0))
        .withRot(new Vec(0, 0, 0.25));

    private static Options options = new Options()
        .withBias(0.00001)
        .withMaxDepth(16)
        .withProcCount(6);

    public static void main(String[] args) {
        Vec[][] screen = Core.renderScene(scene, camera, options);
        BufferedImage img = Core.pixelsToImage(screen);
        try {
            File f = new File("test.bmp");
            ImageIO.write(img, "bmp", f);
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package jtracer.tracelib;

import jtracer.tracelib.basetypes.*;
import jtracer.tracelib.helper.PreloadedQueue;
import jtracer.tracelib.helper.RenderThread;
import jtracer.tracelib.helper.SlottedList;
import jtracer.tracelib.helper.Vec;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static jtracer.tracelib.basetypes.Intersection.NO_INTERSECTION;
import static jtracer.tracelib.helper.MathUtils.clamp;
import static jtracer.tracelib.helper.MathUtils.sgn;

public class Core {
    public static double minPositive(double ...args) {
        OptionalDouble d = Arrays.stream(args).filter(arg -> arg >= 0).min();
        return d.isPresent() ? d.getAsDouble() : 0;
    }

    public static Vec rot(Vec v, double cos_rx, double cos_ry, double cos_rz, double sin_rx, double sin_ry, double sin_rz) {
        double c1 = cos_rx * v.at(2) - sin_rx * v.at(1);
        double c2 = cos_rx * v.at(1) + sin_rx * v.at(2);
        double c3 = cos_ry * v.at(0) - sin_ry * c1;

        return new Vec(
            cos_rz * c3 + sin_rz * c2,
            cos_rz * c2 - sin_rz * c3,
            cos_ry * c1 + sin_ry * v.at(0)
        );
    }

    private static Intersection checkIntersection(Vec origin, Vec v, SceneObject[] objects) {
        Intersection closest = NO_INTERSECTION;

        for (SceneObject object : objects) {
            Intersection i = object.intersection(origin, v);
            if (i.didIntersect() && (!closest.didIntersect() || i.getDistance() < closest.getDistance())) {
                closest = i;
            }
        }

        return closest;
    }

    private static Vec reflect(Vec d, Vec n) {
        return d.sub(n.mul(d.dot(n)).mul(2.0));
    }

    private static Vec refract(Vec d, Vec n, double ior) {
        double cosi = clamp(d.dot(n), -1.0, 1.0);
        double etai = 1.0;
        double etat = ior;

        if (cosi < 0.0) {
            cosi = -cosi;
        } else {
            double t = etai;
            etai = etat;
            etat = t;
            n = n.neg();
        }

        double eta = etai / etat;
        double k = 1.0 - Math.pow(eta, 2.0) * (1.0 - Math.pow(cosi, 2.0));

        if (k < 0) {
            return new Vec(0, 0, 0);
        } else {
            return d.mul(eta).add(n.mul(eta * cosi - Math.sqrt(k)));
        }
    }

    private static double fresnel(Vec d, Vec n, double ior) {
        double cosi = clamp(d.dot(n), -1, 1);
        double etai = 1.0;
        double etat = ior;

        if (cosi > 0) {
            double t = etai;
            etai = etat;
            etat = t;
        }

        double sint = etai / etat * Math.sqrt(Math.max(1 - Math.pow(cosi, 2.0), 0.0));

        if (sint >= 1) {
            return 1.0;
        } else {
            double cost = Math.sqrt(Math.max(1 - Math.pow(sint, 2.0), 0.0));
            cosi = Math.abs(cosi);
            double rs = (etat * cosi - etai * cost) / (etat * cosi + etai * cost);
            double rp = (etai * cosi - etat * cost) / (etai * cosi + etat * cost);
            return (Math.pow(rs, 2.0) + Math.pow(rp, 2.0)) / 2.0;
        }
    }

    public static Vec castRay(Vec pos, Vec d, Scene scene, Options options) {
        return castRay(pos, d, scene, options, 0);
    }

    private static Vec castRay(Vec pos, Vec d, Scene scene, Options options, int depth) {
        Vec hitColor = scene.backgroundColor;

        if (depth > options.maxDepth) {
            return hitColor;
        }

        Intersection i = checkIntersection(pos, d, scene.objects);
        Vec hitPos = i.intersectionPosition();
        SceneObject hitObj = i.intersectionObject();

        if (i.didIntersect()) {
            Material material = scene.materials.get(hitObj.getMaterialName());

            Vec n = hitObj.normal(pos, hitPos);

            if (material.reflective) {
                double kr = fresnel(d, n, material.ior);
                Vec reflectionDir = reflect(d, n).norm();

                if (material.refractive) {
                    Vec reflectionOrigin = hitPos.add(n.mul(options.bias).mul(sgn(reflectionDir.dot(n))));
                    Vec reflectionColor = castRay(reflectionOrigin, reflectionDir, scene, options, depth + 1);
                    Vec refractionDir = refract(d, n, material.ior).norm();
                    Vec refractionOrigin = hitPos.add(n.mul(options.bias).mul(sgn(refractionDir.dot(n))));
                    Vec refractionColor = castRay(refractionOrigin, refractionDir, scene, options, depth + 1);
                    hitColor = reflectionColor.mul(kr).add(refractionColor.mul(1.0 - kr));
                } else {
                    Vec reflectionOrigin = hitPos.sub(n.mul(options.bias).mul(sgn(reflectionDir.dot(n))));
                    Vec reflectionColor = castRay(reflectionOrigin, reflectionDir, scene, options, depth + 1);
                    hitColor = reflectionColor.mul(kr);
                }
            } else {
                double lightAmt = 0.0;
                Vec specularColor = new Vec(0.0, 0.0, 0.0);
                Vec shadowOrigin = hitPos.sub(n.mul(options.bias).mul(sgn(d.dot(n))));

                for (SceneLight light : scene.lights) {
                    Vec vec = light.pos.sub(shadowOrigin);
                    double ld2 = vec.mag2();
                    vec = vec.norm();
                    double ldn = Math.max(vec.dot(n), 0);
                    Intersection j = checkIntersection(shadowOrigin, vec, scene.objects);
                    if (!j.didIntersect() || Math.pow(j.getDistance(), 2) >= ld2) {
                        lightAmt += light.intensity * ldn;
                    }
                    Vec reflectionDir = reflect(vec.neg(), n);
                    specularColor = specularColor.add(
                        Math.pow(Math.max(-reflectionDir.dot(n), 0), material.specularExp) * light.intensity
                    );
                }

                hitColor = material.diffuseColor.mul(lightAmt * material.diffuseCoeff)
                    .add(specularColor.mul(material.specularCoeff));
            }
        }

        return hitColor;
    }

    public static Vec[][] renderScene(Scene scene, Camera camera, Options options) {
        System.out.println("Rendering scene...");
        long u = System.currentTimeMillis();
        camera.updateCachedValues();

        int lineCount = (int) camera.res.at(1);
        final PreloadedQueue<Integer> inQueue = new PreloadedQueue<>();
        final SlottedList<Vec[]> outList = new SlottedList<>(lineCount);

        for (int i = 0; i < lineCount; i++) {
            inQueue.push(i);
        }

        for (int i = 0; i < options.procCount; i++) {
            RenderThread process = new RenderThread(scene, camera, options, inQueue, outList);
            process.start();
        }

        try {
            outList.waitUntilFull();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        Vec[][] screen = new Vec[(int) camera.res.at(1)][(int) camera.res.at(0)];
        for (int i = 0; i < lineCount; i++) {
            screen[i] = outList.get(i);
        }

        long v = System.currentTimeMillis();
        System.out.println(String.format("\nScene took %f seconds to render", (v - u) / 1000.0));

        return screen;
    }

    public static BufferedImage pixelsToImage(Vec[][] pixels) {
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                out.setRGB(x, y, new Color(
                    (int) (pixels[y][x].at(0) * 0xff),
                    (int) (pixels[y][x].at(1) * 0xff),
                    (int) (pixels[y][x].at(2) * 0xff)).getRGB());
            }
        }
        return out;
    }

    public static Map<String, Material> STD_MATERIALS = Map.ofEntries(
        Map.entry("glass", new Material()
            .withReflection(true)
            .withRefraction(true)
            .withSpecularExp(25)
            .withDiffuseColor(new Vec(0.2, 0.2, 0.2))
            .withDiffuseCoeff(0.8)
            .withSpecularCoeff(0.2)
            .withIndexOfRefraction(1.52)),
        Map.entry("glossy", new Material()
            .withReflection(false)
            .withRefraction(false)
            .withSpecularExp(25)
            .withDiffuseColor(new Vec(0.8, 0.7, 0.2))
            .withDiffuseCoeff(0.8)
            .withSpecularCoeff(0.2)
            .withIndexOfRefraction(1.0)),
        Map.entry("rubber", new Material()
            .withReflection(false)
            .withRefraction(false)
            .withSpecularExp(25)
            .withDiffuseColor(new Vec(0.2, 0.2, 0.2))
            .withDiffuseCoeff(0.8)
            .withSpecularCoeff(0.2)
            .withIndexOfRefraction(1.3)),
        Map.entry("floor", new Material()
            .withReflection(false)
            .withRefraction(false)
            .withSpecularExp(25)
            .withDiffuseColor(new Vec(0.2, 0.2, 0.2))
            .withDiffuseCoeff(0.8)
            .withSpecularCoeff(0.2)
            .withIndexOfRefraction(1.3)),
        Map.entry("mirror", new Material()
            .withReflection(true)
            .withRefraction(false)
            .withSpecularExp(25)
            .withDiffuseColor(new Vec(0.5, 0.5, 0.5))
            .withDiffuseCoeff(1.0)
            .withSpecularCoeff(0.2)
            .withIndexOfRefraction(0.01))
    );

    public static Map<String, Vec> STD_COLORS = Map.ofEntries(
        Map.entry("black",          new Vec(0.0, 0.0, 0.0)),
        Map.entry("white",          new Vec(1.0, 1.0, 1.0)),
        Map.entry("red",            new Vec(1.0, 0.0, 0.0)),
        Map.entry("green",          new Vec(0.0, 1.0, 0.0)),
        Map.entry("blue",           new Vec(0.0, 0.0, 1.0)),
        Map.entry("yellow",         new Vec(1.0, 1.0, 0.0)),
        Map.entry("cyan",           new Vec(0.0, 1.0, 1.0)),
        Map.entry("magenta",        new Vec(1.0, 0.0, 1.0)),
        Map.entry("grey",           new Vec(0.5, 0.5, 0.5)),
        Map.entry("navy",           new Vec(0.0, 0.0, 0.5)),
        Map.entry("olive",          new Vec(0.5, 0.5, 0.0)),
        Map.entry("maroon",         new Vec(0.5, 0.0, 0.0)),
        Map.entry("teal",           new Vec(0.0, 0.5, 0.5)),
        Map.entry("purple",         new Vec(0.5, 0.0, 0.5)),
        Map.entry("rose",           new Vec(1.0, 0.0, 0.5)),
        Map.entry("azure",          new Vec(0.0, 0.5, 1.0)),
        Map.entry("lime",           new Vec(0.749019, 1.0, 0.0)),
        Map.entry("gold",           new Vec(1.0, 0.843138, 0.0)),
        Map.entry("brown",          new Vec(0.164706, 0.164706, 0.647059)),
        Map.entry("orange",         new Vec(1.0, 0.647059, 0.0)),
        Map.entry("indigo",         new Vec(0.294118, 0.0, 0.509804)),
        Map.entry("pink",           new Vec(1.0, 0.752941, 0.796078)),
        Map.entry("cherry",         new Vec(0.870588, 0.113725, 0.388235)),
        Map.entry("silver",         new Vec(0.752941, 0.752941, 0.752941)),
        Map.entry("violet",         new Vec(0.541176, 0.168627, 0.886275)),
        Map.entry("apricot",        new Vec(0.984314, 0.807843, 0.694118)),
        Map.entry("chartreuse",     new Vec(0.5, 1.0, 0.0)),
        Map.entry("orange-red",     new Vec(1.0, 0.270588, 0.0)),
        Map.entry("blueberry",      new Vec(0.309804, 0.52549, 0.968627)),
        Map.entry("raspberry",      new Vec(0.890196, 0.043137, 0.360784)),
        Map.entry("turquoise",      new Vec(0.25098, 0.878431, 0.815686)),
        Map.entry("amethyst",       new Vec(0.6, 0.4, 0.8)),
        Map.entry("celestial-blue", new Vec(0.286275, 0.592157, 0.815686))
    );
}

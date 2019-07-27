package jtracer.tracelib.primitives;

import jtracer.tracelib.Core;
import jtracer.tracelib.basetypes.Intersection;
import jtracer.tracelib.basetypes.SceneObject;
import jtracer.tracelib.helper.Vec;

public class Sphere implements SceneObject {
    private Vec pos;
    private double radius2;
    private String materialName;

    public Sphere(Vec pos, double radius, String materialName) {
        this.pos = pos;
        this.radius2 = Math.pow(radius, 2.0);
        this.materialName = materialName;
    }

    @Override
    public Intersection intersection(Vec p, Vec d) {
        Vec m = p.sub(this.pos);
        double b = m.dot(d);
        double c = m.mag2() - this.radius2;

        if (c > 0 && b > 0) {
            return Intersection.NO_INTERSECTION;
        }

        double discr = Math.pow(b, 2) - c;

        if (discr < 0) {
            return Intersection.NO_INTERSECTION;
        }

        double t = Core.minPositive(-b - Math.sqrt(discr), -b + Math.sqrt(discr));
        Vec q = p.add(d.mul(t));

        return new Intersection(true, t, q, this);
    }

    @Override
    public Vec normal(Vec p, Vec q) {
        return q.sub(this.pos).norm();
    }

    @Override
    public String getMaterialName() {
        return this.materialName;
    }
}

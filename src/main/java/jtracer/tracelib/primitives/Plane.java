package jtracer.tracelib.primitives;

import jtracer.tracelib.basetypes.Intersection;
import jtracer.tracelib.basetypes.SceneObject;
import jtracer.tracelib.helper.Vec;

public class Plane implements SceneObject {
    private Vec v0, v1, v2;
    private String materialName;

    public Plane(Vec v0, Vec v1, Vec v2, String materialName) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.materialName = materialName;
    }

    @Override
    public Intersection intersection(Vec p, Vec d) {
        Vec n = this.v1.sub(this.v0).cross(this.v2.sub(this.v0));

        double nDotDir = n.dot(d);

        if (Math.abs(nDotDir) < 0.0001) {
            return Intersection.NO_INTERSECTION;
        }

        double s = n.dot(this.v0);
        double t = (n.dot(p) + s) / nDotDir;

        if (t < 0) {
            return Intersection.NO_INTERSECTION;
        }

        Vec q = p.add(d.mul(t));

        return new Intersection(true, t, q, this);
    }

    @Override
    public Vec normal(Vec p, Vec q) {
        Vec n = this.v1.sub(this.v0).cross(this.v2.sub(this.v0));
        double d = n.dot(this.v0);
        double r = n.dot(p) + d;

        if (r < 0) {
            n = n.neg();
        }

        return n.norm();
    }

    @Override
    public String getMaterialName() {
        return this.materialName;
    }
}

package jtracer.tracelib.primitives;

import jtracer.tracelib.basetypes.Intersection;
import jtracer.tracelib.basetypes.SceneObject;
import jtracer.tracelib.helper.Vec;

import static jtracer.tracelib.basetypes.Intersection.getPlanarNormal;

public class Triangle implements SceneObject {
    private Vec v0, v1, v2;
    private String materialName;

    public Triangle(Vec v0, Vec v1, Vec v2, String materialName) {
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

        if (n.dot(this.v1.sub(this.v0).cross(q.sub(this.v0))) < 0 ||
            n.dot(this.v2.sub(this.v1).cross(q.sub(this.v1))) < 0 ||
            n.dot(this.v0.sub(this.v2).cross(q.sub(this.v2))) < 0) {
            return Intersection.NO_INTERSECTION;
        }

        return new Intersection(true, t, q, this);
    }

    @Override
    public Vec normal(Vec p, Vec q) {
        return getPlanarNormal(p, this.v0, this.v0, this.v2);
    }

    @Override
    public String getMaterialName() {
        return this.materialName;
    }
}

package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public class Intersection {
    private boolean intersected;
    private double distance;
    private Vec position;
    private SceneObject object;

    public static final Intersection NO_INTERSECTION = new Intersection(false, 0, null, null);

    public Intersection(boolean intersected, double dist, Vec pos, SceneObject object) {
        this.intersected = intersected;
        this.distance = dist;
        this.position = pos;
        this.object = object;
    }

    public boolean didIntersect() {
        return this.intersected;
    }

    public double getDistance() {
        return this.distance;
    }

    public Vec intersectionPosition() {
        return this.position;
    }

    public SceneObject intersectionObject() {
        return this.object;
    }

    public static Vec getPlanarNormal(Vec p, Vec v0, Vec v1, Vec v2) {
        Vec n = v1.sub(v0).cross(v2.sub(v0));
        double s = n.dot(v0);
        double r = n.dot(p) + s;

        if (r >= 0) {
            n = n.neg();
        }

        return n.norm();
    }
}

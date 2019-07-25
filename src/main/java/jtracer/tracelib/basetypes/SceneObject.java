package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public interface SceneObject {
    Intersection intersection(Vec p, Vec d);
    Vec normal(Vec p, Vec q);
    String getMaterialName();
}

package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public class SceneLight {
    public double intensity;
    public Vec pos;

    public SceneLight(Vec pos, double i) {
        this.pos = pos;
        this.intensity = i;
    }
}

package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public class SceneLight {
    public double intensity;
    public Vec pos, hue;

    public SceneLight(Vec pos, double i, Vec hue) {
        this.pos = pos;
        this.intensity = i;
        this.hue = hue;
    }
}

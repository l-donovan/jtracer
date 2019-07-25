package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public class Camera {
    public double fov, aspectRatio, scale, cos_rx, cos_ry, cos_rz, sin_rx, sin_ry, sin_rz;
    public Vec res, pos, rot;

    public Camera() {}

    public Camera withFOV(double fov) {
        this.fov = fov;
        return this;
    }

    public Camera withRes(Vec res) {
        this.res = res;
        return this;
    }

    public Camera withPos(Vec pos) {
        this.pos = pos;
        return this;
    }

    public Camera withRot(Vec rot) {
        this.rot = rot;
        return this;
    }

    public void updateCachedValues() {
        this.cos_rx = Math.cos(this.rot.at(0));
        this.cos_ry = Math.cos(this.rot.at(1));
        this.cos_rz = Math.cos(this.rot.at(2));
        this.sin_rx = Math.sin(this.rot.at(0));
        this.sin_ry = Math.sin(this.rot.at(1));
        this.sin_rz = Math.sin(this.rot.at(2));

        this.aspectRatio = this.res.at(0) / this.res.at(1);
        this.scale = Math.tan(Math.toRadians(this.fov / 2.0));
    }
}

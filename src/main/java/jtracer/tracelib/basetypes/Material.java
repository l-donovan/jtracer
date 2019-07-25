package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

public class Material {
    public boolean reflective, refractive;
    public double ior, specularExp, specularCoeff, diffuseCoeff;
    public Vec diffuseColor;

    public Material withReflection(boolean reflection) {
        this.reflective = reflection;
        return this;
    }

    public Material withRefraction(boolean refraction) {
        this.refractive = refraction;
        return this;
    }

    public Material withSpecularExp(double specularExp) {
        this.specularExp = specularExp;
        return this;
    }

    public Material withDiffuseColor(Vec diffuseColor) {
        this.diffuseColor = diffuseColor;
        return this;
    }

    public Material withDiffuseCoeff(double diffuseCoeff) {
        this.diffuseCoeff = diffuseCoeff;
        return this;
    }

    public Material withSpecularCoeff(double specularCoeff) {
        this.specularCoeff = specularCoeff;
        return this;
    }

    public Material withIndexOfRefraction(double ior) {
        this.ior = ior;
        return this;
    }
}

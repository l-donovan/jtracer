package jtracer.tracelib.helper;

import java.util.Arrays;

public class Vec {
    private double[] elem;
    private int nElem;

    public Vec(double... args) {
        this.elem = args;
        this.nElem = args.length;
    }

    public double at(int index) {
        return this.elem[index];
    }

    public double dot(Vec v) {
        double out = 0;
        for (int i = 0; i < this.nElem; i++) {
            out += this.elem[i] * v.elem[i];
        }
        return out;
    }

    public Vec cross(Vec v) {
        return new Vec(
            this.elem[1] * v.elem[2] - this.elem[2] * v.elem[1],
            this.elem[2] * v.elem[0] - this.elem[0] * v.elem[2],
            this.elem[0] * v.elem[1] - this.elem[1] * v.elem[0]
        );
    }

    public double mag2() {
        return Arrays.stream(this.elem).map(num -> Math.pow(num, 2.0)).sum();
    }

    public double mag() {
        return Math.sqrt(this.mag2());
    }

    public Vec norm() {
        double m = this.mag();

        if (m == 0.0) {
            return new Vec(Arrays.stream(this.elem).map(num -> 0).toArray());
        } else {
            return new Vec(Arrays.stream(this.elem).map(num -> num / m).toArray());
        }
    }

    public Vec add(double n) {
        return new Vec(Arrays.stream(this.elem).map(num -> num + n).toArray());
    }

    public Vec add(Vec v) {
        double[] newElem = new double[this.nElem];

        for (int i = 0; i < this.nElem; i++) {
            newElem[i] = this.elem[i] + v.elem[i];
        }

        return new Vec(newElem);
    }

    public Vec sub(double n) {
        return new Vec(Arrays.stream(this.elem).map(num -> num - n).toArray());
    }

    public Vec sub(Vec v) {
        double[] newElem = new double[this.nElem];

        for (int i = 0; i < this.nElem; i++) {
            newElem[i] = this.elem[i] - v.elem[i];
        }

        return new Vec(newElem);
    }

    public Vec mul(double n) {
        return new Vec(Arrays.stream(this.elem).map(num -> num * n).toArray());
    }

    public Vec neg() {
        return new Vec(Arrays.stream(this.elem).map(num -> -num).toArray());
    }
}

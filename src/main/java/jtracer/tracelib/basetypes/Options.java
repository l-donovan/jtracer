package jtracer.tracelib.basetypes;

public class Options {
    public int maxDepth, procCount;
    public double bias;

    public Options withMaxDepth(int depth) {
        this.maxDepth = depth;
        return this;
    }

    public Options withProcCount(int procCount) {
        this.procCount = procCount;
        return this;
    }

    public Options withBias(double bias) {
        this.bias = bias;
        return this;
    }
}

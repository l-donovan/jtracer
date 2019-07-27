package jtracer.tracelib.helper;

public class MathUtils {
    public static double clamp(double num, double lower, double upper) {
        return Math.max(Math.min(num, upper), lower);
    }

    public static double sgn(double num) {
        return Math.copySign(1, num);
    }
}

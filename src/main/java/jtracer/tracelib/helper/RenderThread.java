package jtracer.tracelib.helper;

import jtracer.tracelib.Core;
import jtracer.tracelib.basetypes.Camera;
import jtracer.tracelib.basetypes.Options;
import jtracer.tracelib.basetypes.Scene;

import java.util.EmptyStackException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderThread implements Runnable {
    private Scene scene;
    private Camera camera;
    private Options options;
    private final PreloadedQueue<Integer> inQueue;
    private final SlottedList<Vec[]> outList;

    private final AtomicBoolean running = new AtomicBoolean(true);

    public RenderThread(Scene scene, Camera camera, Options options, PreloadedQueue<Integer> inQueue, SlottedList<Vec[]> outList) {
        this.scene = scene;
        this.camera = camera;
        this.options = options;
        this.inQueue = inQueue;
        this.outList = outList;
    }

    public void start() {
        Thread worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        this.running.set(false);
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            try {
                int y = inQueue.pop();
                Vec[] row = new Vec[(int) camera.res.at(0)];
                for (int x = 0; x < camera.res.at(0); x++) {
                    Vec vec = Core.rot(new Vec(
                            (2 * (x + 0.5) / camera.res.at(0) - 1) * camera.aspectRatio * camera.scale,
                            (1 - 2 * (y + 0.5) / camera.res.at(1)) * camera.scale,
                            1.0
                    ), camera.cos_rx, camera.cos_ry, camera.cos_rz, camera.sin_rx, camera.sin_ry, camera.sin_rz).norm();

                    row[x] = Core.castRay(camera.pos, vec, scene, options);
                }
                System.out.print(String.format("%1$4s ", y));
                outList.set(y, row);
            } catch (EmptyStackException | TimeoutException e) {
                running.set(false);
            }
        }
    }
}

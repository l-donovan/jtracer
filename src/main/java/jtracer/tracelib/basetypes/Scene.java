package jtracer.tracelib.basetypes;

import jtracer.tracelib.helper.Vec;

import java.util.Map;

public class Scene {
    public Vec backgroundColor;
    public SceneObject[] objects;
    public Map<String, Material> materials;
    public SceneLight[] lights;

    public Scene() {}

    public Scene withBackgroundColor(Vec color) {
        this.backgroundColor = color;
        return this;
    }

    public Scene withMaterials(Map<String, Material> materials) {
        this.materials = materials;
        return this;
    }

    public Scene withObjects(SceneObject[] objects) {
        this.objects = objects;
        return this;
    }

    public Scene withLights(SceneLight[] lights) {
        this.lights = lights;
        return this;
    }
}

package com.restonic4.engine.scene;

import com.restonic4.engine.math.Transform;

public class MeshInstance {
    private final Mesh mesh;
    private final Transform transform;

    public MeshInstance(Mesh mesh, Transform transform) {
        this.mesh = mesh;
        this.transform = transform;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Transform getTransform() {
        return transform;
    }
}

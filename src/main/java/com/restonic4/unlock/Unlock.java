package com.restonic4.unlock;

import com.restonic4.engine.math.Time;
import com.restonic4.engine.rendering.Renderer;
import com.restonic4.engine.rendering.Window;
import com.restonic4.engine.math.Transform;
import com.restonic4.engine.scene.Camera;
import com.restonic4.engine.scene.Mesh;
import com.restonic4.engine.scene.MeshInstance;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Unlock {
    public static void main(String[] args) {
        Window window = Window.create();

        List<Mesh> meshes = Mesh.load("Cube.glb");
        Mesh mesh = meshes.get(0);
        Camera camera = new Camera();
        Renderer renderer = new Renderer();

        List<MeshInstance> meshInstances = new ArrayList<>();
        Transform transform = new Transform();
        transform.setPosition(0, 0, -10);
        transform.setRotationXYZ(0, 0, 0);
        meshInstances.add(new MeshInstance(mesh, transform));

        Time time = new Time();

        while (!window.shouldClose()) {
            time.update();

            double delta = time.delta();

            transform.rotateXYZ((float) (0.1f * delta), (float) (0.1f * delta), (float) (0.1f * delta));
            renderWindow(window, renderer, camera, mesh, meshInstances);
        }

        mesh.dispose();
        window.dispose();
    }

    private static void renderWindow(Window window, Renderer renderer, Camera camera, Mesh mesh, List<MeshInstance> meshInstances) {
        window.makeContextCurrent();

        glfwPollEvents();

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        renderer.render(camera, window, mesh, meshInstances);

        glfwSwapBuffers(window.getHandle());
    }
}

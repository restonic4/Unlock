package com.restonic4.unlock;

import com.restonic4.bloom.backends.glfw.GlfwWindow;
import com.restonic4.bloom.backends.vulkan.VulkanTest;
import com.restonic4.bloom.core.graphics.GraphicsApi;
import com.restonic4.bloom.core.math.Time;
import com.restonic4.bloom.core.rendering.Renderer;
import com.restonic4.bloom.core.window.Window;
import com.restonic4.bloom.core.math.Transform;
import com.restonic4.bloom.core.scene.Camera;
import com.restonic4.bloom.core.scene.Mesh;
import com.restonic4.bloom.core.scene.MeshInstance;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Unlock {
    public static final GraphicsApi API = GraphicsApi.VULKAN;

    public static void main(String[] args) {
        VulkanTest.init();
    }

    private static void init() {
        Window window = new GlfwWindow(500, 500);

        List<Mesh> meshes = Mesh.load("Cube.glb");
        Mesh mesh = meshes.get(0);
        Camera camera = new Camera();
        Renderer renderer = new Renderer();

        List<MeshInstance> meshInstances = new ArrayList<>();

        meshInstances.add(createCube(mesh, 0, 0, -10));
        meshInstances.add(createCube(mesh, 5, 0, -10));
        meshInstances.add(createCube(mesh, -5, 0, -10));
        meshInstances.add(createCube(mesh, 0, 5, -10));

        Time time = new Time();
        float speed = 1;

        while (!window.shouldClose()) {
            time.update();

            double delta = time.delta();

            for (int i = 0; i < meshInstances.size(); i++) {
                MeshInstance instance = meshInstances.get(i);
                Transform transform = instance.getTransform();

                transform.rotateXYZ((float) (speed * delta), (float) (speed * delta), (float) (speed * delta));
            }

            renderWindow(window, renderer, camera, mesh, meshInstances);
        }

        mesh.dispose();
        window.dispose();
        glfwTerminate();
    }

    private static MeshInstance createCube(Mesh mesh, int x, int y, int z) {
        Transform transform = new Transform();
        transform.setPosition(x, y, z);

        return new MeshInstance(mesh, transform);
    }

    private static void renderWindow(Window window, Renderer renderer, Camera camera, Mesh mesh, List<MeshInstance> meshInstances) {
        if (!(window instanceof GlfwWindow glfwWindow)) return;

        window.makeContextCurrent();

        glfwPollEvents();

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        renderer.render(camera, glfwWindow, mesh, meshInstances);

        glfwSwapBuffers(glfwWindow.getHandle());
    }
}

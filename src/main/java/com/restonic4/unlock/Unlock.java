package com.restonic4.unlock;

import com.restonic4.bloom.events.EventResult;
import com.restonic4.engine.Window;
import com.restonic4.engine.api.events.WindowEvents;
import com.restonic4.engine.scene.Mesh;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Unlock {
    public static void main(String[] args) {
        Window window = Window.create();
        Window window2 = Window.create();

        List<Mesh> meshes = Mesh.load("Cube.glb");
        System.out.println(meshes);

        WindowEvents.RESIZED.register((window1, oldX, oldY, newX, newY) -> {
            System.out.println("WOW");
            System.out.println(oldX + " -> " + newX);
            System.out.println(oldY + " -> " + newY);
            return EventResult.CANCELED;
        });

        while (!window.shouldClose() && !window2.shouldClose()) {
            renderWindow(window);
            renderWindow(window2);
        }

        window.dispose();
        window2.dispose();
    }

    private static void renderWindow(Window window) {
        window.makeContextCurrent();

        glfwPollEvents();

        glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT);

        glfwSwapBuffers(window.getHandle());
    }
}

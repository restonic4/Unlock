package com.restonic4.unlock;

import com.restonic4.engine.Window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Unlock {
    public static void main(String[] args) {
        System.out.println("hi");

        Window window = Window.create();

        while (!glfwWindowShouldClose(window.getHandle())) {
            glfwPollEvents();

            glClearColor(0.1f, 0.1f, 0.15f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);

            glfwSwapBuffers(window.getHandle());
        }
    }
}

package com.restonic4.engine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

public class Window implements Disposable {
    private final long handle;
    private int width, height;

    private GLFWWindowSizeCallback sizeCallback;
    private GLFWWindowPosCallback posCallback;
    private GLFWWindowFocusCallback focusCallback;
    private GLFWWindowIconifyCallback iconifyCallback;
    private GLFWWindowCloseCallback closeCallback;
    private GLFWWindowMaximizeCallback maximizeCallback;
    private GLFWWindowRefreshCallback refreshCallback;

    private Window(long handle, int width, int height) {
        this.handle = handle;
        this.width = width;
        this.height = height;
    }

    public static Window create() {
        // Print GLFW errors to stderr
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        // OpenGL version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // macOS
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Settings
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Creation
        long windowHandle = glfwCreateWindow(500, 500, "LWJGL3 Window", MemoryUtil.NULL, MemoryUtil.NULL);
        if (windowHandle == MemoryUtil.NULL) throw new IllegalStateException("Failed to create GLFW window");

        Window window = new Window(windowHandle, 500, 500);
        window.centerWindow();

        glfwMakeContextCurrent(windowHandle);
        glfwSwapInterval(1); // vsynq for now
        GL.createCapabilities();

        window.installEvents();

        return window;
    }

    private void installEvents() {
        sizeCallback = glfwSetWindowSizeCallback(this.handle, (window, width, height) -> {
            System.out.println("Window resized: " + width + ", " + height);
            this.width = width;
            this.height = height;
        });

        posCallback = glfwSetWindowPosCallback(this.handle, (window, x, y) -> {
            System.out.println("Window moved: " + x + ", " + y);
        });

        focusCallback = glfwSetWindowFocusCallback(this.handle, (window, focused) -> {
            if (focused) {
                System.out.println("Window focused");
            } else {
                System.out.println("Window unfocused");
            }
        });

        iconifyCallback = glfwSetWindowIconifyCallback(this.handle, (window, iconified) -> {
            if (iconified) {
                System.out.println("Window minimalized");
            } else {
                System.out.println("Window restored");
            }
        });

        closeCallback = glfwSetWindowCloseCallback(this.handle, window -> {
            System.out.println("Window close attempt");
        });

        maximizeCallback = glfwSetWindowMaximizeCallback(this.handle, (window, maximized) -> {
            if (maximized) {
                System.out.println("Window maximized");
            } else {
                System.out.println("Window unmaximized");
            }
        });

        refreshCallback = glfwSetWindowRefreshCallback(this.handle, window -> {
            System.out.println("Window needs to be refreshed");
        });
    }

    // Wayland crash
    public void centerWindow() {
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode != null) {
            int x = (videoMode.width() - getWidth()) / 2;
            int y = (videoMode.height() - getHeight()) / 2;
            glfwSetWindowPos(this.handle, x, y);
        }
    }

    public long getHandle() { return handle; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    @Override
    public void dispose() {
        if (sizeCallback != null) sizeCallback.free();
        if (posCallback != null) posCallback.free();
        if (focusCallback != null) focusCallback.free();
        if (iconifyCallback != null) iconifyCallback.free();
        if (closeCallback != null) closeCallback.free();
        if (maximizeCallback != null) maximizeCallback.free();
        if (refreshCallback != null) refreshCallback.free();

        glfwDestroyWindow(this.handle);
        glfwTerminate();

        try (GLFWErrorCallback errorCallback = GLFW.glfwSetErrorCallback(null)) {
            if (errorCallback != null) errorCallback.free();
        }
    }
}

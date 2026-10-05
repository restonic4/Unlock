package com.restonic4.engine;

import com.restonic4.bloom.events.EventResult;
import com.restonic4.engine.api.events.WindowEvents;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.GLFW.*;

public class Window extends RenderTarget implements Disposable {
    private final long handle;
    private int x, y;
    private int width, height;

    private boolean shouldClose;

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
        this.shouldClose = false;
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

        glfwShowWindow(windowHandle);

        return window;
    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(handle);
    }

    private void installEvents() {
        sizeCallback = glfwSetWindowSizeCallback(this.handle, (window, width, height) -> {
            EventResult eventResult = WindowEvents.RESIZED.invoker().onEvent(this, this.width, this.height, width, height);
            if (eventResult == EventResult.CANCELED) {
                setSize(this.width, this.height);
                return;
            }

            this.width = width;
            this.height = height;
        });

        posCallback = glfwSetWindowPosCallback(this.handle, (window, x, y) -> {
            EventResult eventResult = WindowEvents.MOVED.invoker().onEvent(this, this.x, this.y, x, y);
            if (eventResult == EventResult.CANCELED) {
                setPosition(this.x, this.y);
                return;
            }

            this.x = x;
            this.y = y;
        });

        focusCallback = glfwSetWindowFocusCallback(this.handle, (window, focused) -> {
            if (focused) {
                WindowEvents.FOCUSED.invoker().onEvent(this);
            } else {
                EventResult eventResult = WindowEvents.UNFOCUSED.invoker().onEvent(this);
                if (eventResult == EventResult.CANCELED) setFocus();
            }
        });

        iconifyCallback = glfwSetWindowIconifyCallback(this.handle, (window, iconified) -> {
            if (iconified) {
                WindowEvents.ICONIFIED.invoker().onEvent(this);
            } else {
                WindowEvents.RESTORED.invoker().onEvent(this);
            }
        });

        closeCallback = glfwSetWindowCloseCallback(this.handle, window -> {
            EventResult eventResult = WindowEvents.CLOSE_ATTEMPT.invoker().onEvent(this);
            if (eventResult != EventResult.CANCELED) this.shouldClose = true;
        });

        maximizeCallback = glfwSetWindowMaximizeCallback(this.handle, (window, maximized) -> {
            if (maximized) {
                WindowEvents.MAXIMIZED.invoker().onEvent(this);
            } else {
                WindowEvents.UNMAXIMIZED.invoker().onEvent(this);
            }
        });

        refreshCallback = glfwSetWindowRefreshCallback(this.handle, window -> WindowEvents.REFRESH_REQUIRED.invoker().onEvent(this));
    }

    public void centerWindow() {
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (videoMode != null) {
            int x = (videoMode.width() - getWidth()) / 2;
            int y = (videoMode.height() - getHeight()) / 2;
            setPosition(x, y);
        }
    }

    @Deprecated(forRemoval = true) // TODO: We cant on Wayland, so we should not rely on this.
    public void setPosition(int x, int y) {
        if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) return;
        glfwSetWindowPos(this.handle, x, y);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(this.handle, width, height);
    }

    @Deprecated(forRemoval = true) // TODO: We cant on Wayland, so we should not rely on this.
    public void setFocus() {
        if (glfwGetPlatform() == GLFW_PLATFORM_WAYLAND) return;
        glfwFocusWindow(this.handle);
    }

    public long getHandle() { return handle; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean shouldClose() { return shouldClose; }

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

        GLFWErrorCallback errorCallback = GLFW.glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
        }
    }
}

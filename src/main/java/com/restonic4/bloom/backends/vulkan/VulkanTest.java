package com.restonic4.bloom.backends.vulkan;

import com.restonic4.bloom.backends.glfw.GlfwWindow;
import com.restonic4.bloom.core.platform.Version;
import org.lwjgl.system.Configuration;

import static org.lwjgl.glfw.GLFW.*;

public class VulkanTest {
    private static final boolean ENABLE_VALIDATION_LAYERS = Configuration.DEBUG.get(true);

    public static void init() {
        GlfwWindow window = new GlfwWindow(500, 500);

        VulkanInstance vkInstance = VulkanInstance.create(
                "Bloom", new Version(0, 0, 0),
                "Unlock", new Version(0, 0, 0),
                ENABLE_VALIDATION_LAYERS
        );

        VulkanPhysicalDevice device = VulkanPhysicalDevice.pickBestDevice(vkInstance.getInstance());
        System.out.println("Device type: " + device.getType());
        System.out.println("Device score: " + device.getScore());

        while (!window.shouldClose()) {
            glfwPollEvents();
        }

        window.dispose();
        vkInstance.destroy();
        glfwTerminate();
    }
}

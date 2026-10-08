package com.restonic4.unlock;

import com.restonic4.bloom.backends.glfw.GlfwWindow;
import com.restonic4.bloom.backends.vulkan.device.VulkanLogicalDevice;
import com.restonic4.bloom.backends.vulkan.device.VulkanPhysicalDevice;
import com.restonic4.bloom.backends.vulkan.instance.VulkanInstance;
import com.restonic4.bloom.backends.vulkan.surface.VulkanSurface;
import com.restonic4.bloom.backends.vulkan.swapchain.VulkanSwapChain;
import com.restonic4.bloom.backends.vulkan.swapchain.VulkanSwapChainImageViews;
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

        VulkanSurface surface = VulkanSurface.create(vkInstance.getInstance(), window.getHandle());
        VulkanPhysicalDevice physicalDevice = VulkanPhysicalDevice.pickBestDevice(vkInstance.getInstance(), surface.getHandle());
        VulkanLogicalDevice logicalDevice = VulkanLogicalDevice.create(physicalDevice, surface.getHandle());

        VulkanSwapChain swapChain = VulkanSwapChain.create(physicalDevice, logicalDevice, surface.getHandle(), window.getHandle());
        VulkanSwapChainImageViews imageViews = VulkanSwapChainImageViews.create(logicalDevice, swapChain);

        while (!window.shouldClose()) {
            glfwPollEvents();
        }

        imageViews.dispose();
        swapChain.dispose();
        logicalDevice.dispose();
        surface.dispose();
        vkInstance.dispose();

        window.dispose();
        glfwTerminate();
    }
}

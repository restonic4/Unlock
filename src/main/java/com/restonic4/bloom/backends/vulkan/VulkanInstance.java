package com.restonic4.bloom.backends.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.vulkan.VK10.VK_API_VERSION_1_0;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateInstance;
import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

public class VulkanInstance {
    private VkInstance instance;

    public void create(String engineName, int engineVersion, String appName, int appVersion) {
        VK.create();

        // TODO: Use VK_MAKE_VERSION(MAJOR, MINOR, PATCH) to pass from string to int

        if (!glfwVulkanSupported()) throw new IllegalStateException("Vulkan is not supported");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationName(stack.UTF8(appName))
                    .applicationVersion(appVersion)
                    .pEngineName(stack.UTF8(engineName))
                    .engineVersion(engineVersion)
                    .apiVersion(VK_API_VERSION_1_0);


            PointerBuffer requiredExtensions = glfwGetRequiredInstanceExtensions();
            if (requiredExtensions == null) throw new IllegalStateException("Failed to get GLFW Vulkan extensions");

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                    .sType$Default()
                    .pApplicationInfo(appInfo)
                    .ppEnabledExtensionNames(requiredExtensions);

            PointerBuffer pInstance = stack.mallocPointer(1);

            int result = vkCreateInstance(createInfo, null, pInstance);
            if (result != VK_SUCCESS) throw new IllegalStateException("Failed to create Vulkan instance: " + result);

            instance = new VkInstance(pInstance.get(0), createInfo);
        }
    }

    public void destroy() {
        if (instance != null) {
            vkDestroyInstance(instance, null);
            instance = null;
        }

        VK.destroy();
    }

    public VkInstance getInstance() {
        return instance;
    }
}

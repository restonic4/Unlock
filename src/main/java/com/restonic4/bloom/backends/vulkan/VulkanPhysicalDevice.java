package com.restonic4.bloom.backends.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanPhysicalDevice {
    private final VkPhysicalDevice handle;
    private final ScoreData scoreData;

    private VulkanPhysicalDevice(VkPhysicalDevice handle, ScoreData scoreData) {
        this.handle = handle;
        this.scoreData = scoreData;
    }

    public static VulkanPhysicalDevice pickBestDevice(VkInstance instance) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer deviceCount = stack.ints(0);
            vkEnumeratePhysicalDevices(instance, deviceCount, null);

            if (deviceCount.get(0) == 0) throw new RuntimeException("Failed to find GPUs with Vulkan support");

            PointerBuffer physicalDevices = stack.mallocPointer(deviceCount.get(0));

            vkEnumeratePhysicalDevices(instance, deviceCount, physicalDevices);

            VulkanPhysicalDevice bestDevice = null;
            ScoreData bestScore = null;

            for (int i = 0; i < physicalDevices.capacity(); i++) {
                VkPhysicalDevice device = new VkPhysicalDevice(physicalDevices.get(i), instance);

                ScoreData scoreData = rateDeviceSuitability(device);
                if (scoreData == null) continue;

                if (bestScore == null || scoreData.score() > bestScore.score()) {
                    bestScore = scoreData;
                    bestDevice = new VulkanPhysicalDevice(device, scoreData);
                }
            }

            if (bestDevice == null) throw new RuntimeException("Failed to find a suitable GPU");
            return bestDevice;
        }
    }

    // TODO: Improve scoring
    private static ScoreData rateDeviceSuitability(VkPhysicalDevice device) {
        int score = 0;
        Type type;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);

            VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);

            vkGetPhysicalDeviceProperties(device, properties);

            vkGetPhysicalDeviceFeatures(device, features);

            // Hard requirement: must have graphics queue
            QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(device);
            if (!indices.isComplete()) {
                return null;
            }

            // Hard requirement: geometry shaders
            if (!features.geometryShader()) {
                return null;
            }

            // Prefer discrete GPUs
            if (properties.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                score += 1000;
                type = Type.DISCRETE;
            } else {
                type = Type.INTEGRATED;
            }

            // Prefer GPUs with greater texture dimensions
            score += properties.limits().maxImageDimension2D();

            return new ScoreData(score, type);
        }
    }

    public VkPhysicalDevice getHandle() { return handle; }
    public int getScore() { return scoreData.score(); }
    public Type getType() { return scoreData.type(); }

    public enum Type { DISCRETE, INTEGRATED, UNKNOWN }
    public record ScoreData(int score, Type type) {}
}

package com.restonic4.bloom.backends.vulkan;

import com.restonic4.bloom.core.Disposable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK10.*;

public class VulkanLogicalDevice implements Disposable {
    private final VkDevice handle;
    private final VkQueue graphicsQueue;

    private VulkanLogicalDevice(VkDevice handle, VkQueue graphicsQueue) {
        this.handle = handle;
        this.graphicsQueue = graphicsQueue;
    }

    public static VulkanLogicalDevice create(VulkanPhysicalDevice physicalDevice) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(physicalDevice.getHandle());
            if (!indices.isComplete()) throw new IllegalStateException("Physical device has no graphics queue");

            // Queue creation

            VkDeviceQueueCreateInfo.Buffer queueCreateInfo = VkDeviceQueueCreateInfo.calloc(1, stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(indices.graphics())
                    .pQueuePriorities(stack.floats(1.0f));

            // Device features

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

            // Logical device

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .pQueueCreateInfos(queueCreateInfo)
                    .pEnabledFeatures(deviceFeatures);

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

            int result = vkCreateDevice(physicalDevice.getHandle(), createInfo, null, pDevice);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create logical device: " + result);
            }

            VkDevice device = new VkDevice(pDevice.get(0), physicalDevice.getHandle(), createInfo);

            // Retrieve graphics queue

            PointerBuffer pGraphicsQueue = stack.pointers(VK_NULL_HANDLE);

            vkGetDeviceQueue(device, indices.graphics(), 0, pGraphicsQueue);

            VkQueue graphicsQueue = new VkQueue(pGraphicsQueue.get(0), device);

            return new VulkanLogicalDevice(device, graphicsQueue);
        }
    }

    @Override
    public void dispose() {
        vkDestroyDevice(handle, null);
    }
}

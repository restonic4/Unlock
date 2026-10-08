package com.restonic4.bloom.backends.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

public record VulkanShaderStage(VulkanShaderModule module, int stage, String entryPoint) {
    public VulkanShaderStage(VulkanShaderModule module, int stage) {
        this(module, stage, "main");
    }

    public VkPipelineShaderStageCreateInfo createInfo(MemoryStack stack) {
        return VkPipelineShaderStageCreateInfo.calloc(stack)
                .sType$Default()
                .stage(stage)
                .module(module.getHandle())
                .pName(stack.UTF8(entryPoint));
    }
}
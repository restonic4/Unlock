package com.restonic4.bloom.core.rendering;

import com.restonic4.bloom.backends.opengl.OpenGlShaderProgram;
import com.restonic4.bloom.core.scene.Camera;
import com.restonic4.bloom.core.scene.Mesh;
import com.restonic4.bloom.core.scene.MeshInstance;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

public class Renderer {
    private OpenGlShaderProgram shader;

    public Renderer() {
        this.shader = OpenGlShaderProgram.fromResources("core.vsh", "core.fsh");;
    }

    // TODO: reuse float buffer
    public void render(Camera mainCamera, RenderTarget renderTarget, Mesh mesh, List<MeshInstance> instances) {
        shader.bind();

        float aspectRatio = renderTarget.getAspectRatio();
        Matrix4fc projectionMatrix = mainCamera.getProjectionMatrix(aspectRatio);
        Matrix4fc viewMatrix = mainCamera.getViewMatrix();

        shader.set("uProjection", projectionMatrix);
        shader.set("uView", viewMatrix);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer instanceBuffer = stack.mallocFloat(instances.size() * 16);

            for (int i = 0; i < instances.size(); i++) {
                instances.get(i).getTransform().getModelMatrix().get(i * 16, instanceBuffer);
            }

            instanceBuffer.limit(instances.size() * 16);
            instanceBuffer.position(0);

            mesh.uploadInstances(instanceBuffer);
        }

        mesh.bind();

        glDrawElementsInstanced(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0, instances.size());

        mesh.unbind();
        OpenGlShaderProgram.unbind();
    }
}

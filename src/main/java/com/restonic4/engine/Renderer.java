package com.restonic4.engine;

import com.restonic4.engine.scene.Camera;
import com.restonic4.engine.scene.Mesh;
import com.restonic4.engine.scene.MeshInstance;
import org.joml.Matrix4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING;
import static org.lwjgl.opengl.GL30.GL_VERTEX_ARRAY_BINDING;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

public class Renderer {
    private Shader shader;

    public Renderer() {
        this.shader = Shader.fromResources("core.vsh", "core.fsh");;
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

            for (MeshInstance instance : instances) {
                instance.getTransform().getModelMatrix().get(instanceBuffer);
            }

            instanceBuffer.flip();

            mesh.uploadInstances(instanceBuffer);
        }

        mesh.bind();

        //glDrawArrays(GL_TRIANGLES, 0, 3);

        /*glDrawElements(
                GL_TRIANGLES,
                mesh.getIndexCount(),
                GL_UNSIGNED_INT,
                0
        );*/
        glDrawElementsInstanced(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0, instances.size());

        mesh.unbind();
        Shader.unbind();
    }
}

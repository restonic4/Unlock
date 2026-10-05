package com.restonic4.engine.scene;

import com.restonic4.engine.Disposable;
import com.restonic4.engine.Resource;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class Mesh implements Disposable {
    float[] vertices;
    float[] normals;
    float[] tangents;
    float[] uvs;

    int[] indices;

    private static final int FLOATS_PER_VERTEX = 3 + 3 + 3 + 2; // P + N + T + UV
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;

    private final int vao;

    private final int vertexVbo;
    private final int indexVbo;
    private final int instanceVbo;

    private final int indexCount;

    public Mesh(float[] vertices, float[] normals, float[] tangents, float[] uvs, int[] indices) {
        this.vertices = vertices;
        this.normals = normals;
        this.tangents = tangents;
        this.uvs = uvs;
        this.indices = indices;

        this.indexCount = indices.length;

        if (vertices.length / 3 != normals.length / 3 || vertices.length / 3 != tangents.length / 3 || vertices.length / 3 != uvs.length / 2) {
            throw new IllegalArgumentException("Vertex attribute arrays have different sizes");
        }

        int vertexCount = vertices.length / 3;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vertexVbo = createVertexVbo(vertexCount, vertices, normals, tangents, uvs);
        indexVbo = createIndexVbo(indices);
        instanceVbo = createInstanceVbo();

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static int createVertexVbo(int vertexCount, float[] vertices, float[] normals, float[] tangents, float[] uvs) {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexCount * FLOATS_PER_VERTEX);

        for (int i = 0; i < vertexCount; i++) {
            // Position
            vertexBuffer.put(vertices[i * 3]);
            vertexBuffer.put(vertices[i * 3 + 1]);
            vertexBuffer.put(vertices[i * 3 + 2]);

            // Normal
            vertexBuffer.put(normals[i * 3]);
            vertexBuffer.put(normals[i * 3 + 1]);
            vertexBuffer.put(normals[i * 3 + 2]);

            // Tangent
            vertexBuffer.put(tangents[i * 3]);
            vertexBuffer.put(tangents[i * 3 + 1]);
            vertexBuffer.put(tangents[i * 3 + 2]);

            // UV
            vertexBuffer.put(uvs[i * 2]);
            vertexBuffer.put(uvs[i * 2 + 1]);
        }

        vertexBuffer.flip();

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, STRIDE, 0);

        // normal
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, STRIDE, 3L * Float.BYTES);

        // tangent
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, STRIDE, 6L * Float.BYTES);

        // uv
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 2, GL_FLOAT, false, STRIDE, 9L * Float.BYTES);

        return vbo;
    }

    public static int createIndexVbo(int[] indices) {
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices).flip();

        int vbo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        return vbo;
    }

    public static int createInstanceVbo() {
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        glBufferData(GL_ARRAY_BUFFER, 16L * Float.BYTES, GL_STREAM_DRAW);

        // mat4 occupies 4 attribute locations: 4,5,6,7, OpenGL supports at max 4 floats, we need to split it.
        for (int i = 0; i < 4; i++) {
            int location = 4 + i;

            glEnableVertexAttribArray(location);
            glVertexAttribPointer(location, 4, GL_FLOAT, false, 16 * Float.BYTES, i * 4L * Float.BYTES);
            glVertexAttribDivisor(location, 1); // Change the attribute each instance, not per vertex
        }

        return vbo;
    }

    public void bind() {
        glBindVertexArray(vao);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void uploadInstances(FloatBuffer data) {
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, data, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void dispose() {
        glDeleteBuffers(vertexVbo);
        glDeleteBuffers(indexVbo);
        glDeleteBuffers(instanceVbo);
        glDeleteVertexArrays(vao);
    }

    public static List<Mesh> load(String resourcePath) {
        ByteBuffer data = Resource.loadBuffer(resourcePath);

        try {
            String extension = getExtension(resourcePath);

            AIScene scene = Assimp.aiImportFileFromMemory(data, getImportFlags(), extension);
            if (scene == null) throw new RuntimeException( "Failed to load model: " + resourcePath + "\nAssimp: " + Assimp.aiGetErrorString() );

            try {
                return convertScene(scene);
            } finally {
                Assimp.aiReleaseImport(scene);
            }
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    private static int getImportFlags() {
        return Assimp.aiProcess_Triangulate
                | Assimp.aiProcess_GenSmoothNormals
                | Assimp.aiProcess_CalcTangentSpace
                | Assimp.aiProcess_JoinIdenticalVertices
                | Assimp.aiProcess_ImproveCacheLocality;
    }

    private static String getExtension(String path) {
        int dot = path.lastIndexOf('.');
        if (dot == -1 || dot == path.length() - 1) return null;
        return path.substring(dot + 1);
    }

    private static List<Mesh> convertScene(AIScene scene) {
        List<Mesh> meshes = new ArrayList<>(scene.mNumMeshes());
        PointerBuffer meshPointers = scene.mMeshes();

        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh aiMesh = AIMesh.create(meshPointers.get(i));
            meshes.add(convertMesh(aiMesh));
        }

        return meshes;
    }

    private static Mesh convertMesh(AIMesh aiMesh) {
        int vertexCount = aiMesh.mNumVertices();

        float[] vertices = new float[vertexCount * 3];
        float[] normals = new float[vertexCount * 3];
        float[] tangents = new float[vertexCount * 3];
        float[] uvs = new float[vertexCount * 2];

        AIVector3D.Buffer aiVertices = aiMesh.mVertices();
        AIVector3D.Buffer aiNormals = aiMesh.mNormals();
        AIVector3D.Buffer aiTangents = aiMesh.mTangents();
        AIVector3D.Buffer aiUVs = aiMesh.mTextureCoords(0);

        for (int i = 0; i < vertexCount; i++) {
            AIVector3D vertex = aiVertices.get(i);

            vertices[i * 3] = vertex.x();
            vertices[i * 3 + 1] = vertex.y();
            vertices[i * 3 + 2] = vertex.z();

            if (aiNormals != null) {
                AIVector3D normal = aiNormals.get(i);

                normals[i * 3] = normal.x();
                normals[i * 3 + 1] = normal.y();
                normals[i * 3 + 2] = normal.z();
            }

            if (aiTangents != null) {
                AIVector3D tangent = aiTangents.get(i);

                tangents[i * 3] = tangent.x();
                tangents[i * 3 + 1] = tangent.y();
                tangents[i * 3 + 2] = tangent.z();
            }

            if (aiUVs != null) {
                AIVector3D uv = aiUVs.get(i);

                uvs[i * 2] = uv.x();
                uvs[i * 2 + 1] = uv.y();
            }
        }

        int indexCount = aiMesh.mNumFaces() * 3;
        int[] indices = new int[indexCount];

        AIFace.Buffer faces = aiMesh.mFaces();

        int index = 0;

        for (int i = 0; i < aiMesh.mNumFaces(); i++) {
            AIFace face = faces.get(i);

            for (int j = 0; j < face.mNumIndices(); j++) {
                indices[index++] = face.mIndices().get(j);
            }
        }

        return new Mesh(vertices, normals, tangents, uvs, indices);
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getVertexCount() {
        return vertices.length / 3;
    }

    public int getIndexCount() {
        return indexCount;
    }
}

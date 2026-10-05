package com.restonic4.engine.rendering;

import com.restonic4.engine.Disposable;
import com.restonic4.engine.Resource;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader implements Disposable {
    private final int program;
    private final Map<String, Integer> uniformLocations = new HashMap<>();

    private Shader(String vertexSource, String fragmentSource) {
        int vertexShader = compile(GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = compile(GL_FRAGMENT_SHADER, fragmentSource);

        program = glCreateProgram();

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        glLinkProgram(program);

        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(program);

            glDeleteShader(vertexShader);
            glDeleteShader(fragmentShader);
            glDeleteProgram(program);

            throw new RuntimeException("Failed to link shader:\n" + log);
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public static Shader fromResources(String vertexPath, String fragmentPath) {
        return new Shader(Resource.loadString(vertexPath), Resource.loadString(fragmentPath));
    }

    public static Shader fromStrings(String vertexSource, String fragmentSource) {
        return new Shader(vertexSource, fragmentSource);
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);

        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);

            glDeleteShader(shader);

            String typeName = switch (type) {
                case GL_VERTEX_SHADER -> "vertex";
                case GL_FRAGMENT_SHADER -> "fragment";
                default -> "unknown";
            };

            throw new RuntimeException("Failed to compile " + typeName + " shader:\n" + log);
        }

        return shader;
    }

    public void bind() { glUseProgram(program); }
    public static void unbind() { glUseProgram(0); }

    public int getId() { return program; }

    // Uniforms

    public int getUniformLocation(String name) {
        return uniformLocations.computeIfAbsent(name, n -> glGetUniformLocation(program, n));
    }

    public void set(String name, int value) {
        int location = getUniformLocation(name);
        if (location != -1) glUniform1i(location, value);
    }

    public void set(String name, float value) {
        int location = getUniformLocation(name);
        if (location != -1) glUniform1f(location, value);
    }

    public void set(String name, boolean value) {
        set(name, value ? 1 : 0);
    }

    public void set(String name, float x, float y) {
        int location = getUniformLocation(name);
        if (location != -1) glUniform2f(location, x, y);
    }

    public void set(String name, float x, float y, float z) {
        int location = getUniformLocation(name);
        if (location != -1) glUniform3f(location, x, y, z);
    }

    public void set(String name, float x, float y, float z, float w) {
        int location = getUniformLocation(name);
        if (location != -1) glUniform4f(location, x, y, z, w);
    }

    public void set(String name, Vector2fc value) {
        set(name, value.x(), value.y());
    }

    public void set(String name, Vector3fc value) {
        set(name, value.x(), value.y(), value.z());
    }

    public void set(String name, Vector4fc value) {
        set(name, value.x(), value.y(), value.z(), value.w());
    }

    // TODO: reuse float buffer
    public void set(String name, Matrix4fc value) {
        int location = getUniformLocation(name);
        if (location == -1) return;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = value.get(stack.mallocFloat(16));
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    @Override
    public void dispose() {
        glDeleteProgram(program);
    }
}

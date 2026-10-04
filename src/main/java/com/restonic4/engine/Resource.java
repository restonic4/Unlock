package com.restonic4.engine;

import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class Resource {
    public static ByteBuffer load(String path) {
        String resourcePath = normalize(path);

        try (InputStream input = Resource.class .getResourceAsStream(resourcePath)) {
            if (input == null) throw new RuntimeException( "Resource not found: " + resourcePath );

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            byte[] data = output.toByteArray();

            ByteBuffer result = MemoryUtil.memAlloc(data.length);
            result.put(data).flip();

            return result;
        } catch (IOException e) {
            throw new RuntimeException( "Failed to read resource: " + resourcePath, e );
        }
    }

    private static String normalize(String path) {
        return path.startsWith("/") ? path : "/" + path;
    }
}

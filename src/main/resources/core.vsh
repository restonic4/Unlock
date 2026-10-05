#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 4) in mat4 aModel;

uniform mat4 uProjection;
uniform mat4 uView;

void main() {
    gl_Position = uProjection * uView * aModel * vec4(aPosition, 1.0);
}
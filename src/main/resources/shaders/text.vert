#version 150 core

in vec2 position;
in vec3 textColor;
in vec2 texCoordinate;

out vec2 textureCoord;
out vec3 outTextColor;

uniform mat4 view;
uniform mat4 projection;

void main() {
    outTextColor = textColor;
    textureCoord = texCoordinate;
    mat4 mvp = projection * view;
    gl_Position = mvp * vec4(position, 0.0, 1.0);
}
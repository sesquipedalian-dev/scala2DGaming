#version 150 core

in vec3 position;
in vec3 color;
in vec2 texCoord;

out vec3 vertexColor;
out vec2 textureCoord;

uniform mat4 view;
uniform mat4 projection;

void main() {
    vertexColor = color;
    textureCoord = texCoord; // pass through to fragment shader
    mat4 mvp = projection * view;
    gl_Position = mvp * vec4(position, 1.0);
}
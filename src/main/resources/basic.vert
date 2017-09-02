#version 150 core

in vec3 position;
in vec2 texCoord;

out vec2 textureCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    textureCoord = texCoord; // pass through to fragment shader
    mat4 mvp = projection * view * model;
    gl_Position = mvp * vec4(position, 1.0);
}
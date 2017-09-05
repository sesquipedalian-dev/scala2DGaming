#version 150 core

in vec2 position;
in vec2 texCoord;
in float texIndex;

out vec3 textureCoord;

uniform mat4 projection;

void main() {
    textureCoord = vec3(texCoord, texIndex);
    mat4 mvp = projection;
    gl_Position = mvp * vec4(position, 0.0, 1.0);
}
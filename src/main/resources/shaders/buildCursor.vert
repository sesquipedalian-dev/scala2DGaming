#version 150 core

in vec2 pos;

uniform mat4 projection;

void main() {
    gl_Position = projection * vec4(pos, 0.0, 1.0);
}

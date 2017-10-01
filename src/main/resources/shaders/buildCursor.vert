#version 150 core

in vec2 pos;

uniform mat4 projection;
uniform vec3 geoColor;

out vec3 vertColor;

void main() {
    vertColor = geoColor;
    gl_Position = projection * vec4(pos, 0.0, 1.0);
}

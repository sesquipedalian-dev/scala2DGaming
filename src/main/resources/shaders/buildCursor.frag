#version 150 core

in vec3 vertColor;
out vec4 outColor;

void main() {
	outColor = vec4(vertColor.rgb, .33);
}

#version 150 core

uniform vec3 geoColor;
out vec4 outColor;

void main() {
	outColor = vec4(geoColor.rgb, .33);
}

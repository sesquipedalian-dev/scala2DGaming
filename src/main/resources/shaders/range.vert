#version 150 core

in vec2 pos;
in vec4 color;
in float minAngle;
in float maxAngle;
in float length;

out vec4 geoColor;
out float geoMinAngle;
out float geoMaxAngle;
out float geoLength;

void main() {
    geoColor = color;
	gl_Position = vec4(pos, 0.0, 1.0);
	geoLength = length;
	geoMinAngle = minAngle;
	geoMaxAngle = maxAngle;
}

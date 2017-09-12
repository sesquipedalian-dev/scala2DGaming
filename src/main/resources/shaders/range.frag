#version 150 core

in vec4 fragColor;

out vec4 outColor;

void main() {
    outColor = vec4(fragColor.rgb, 1.0);
//	outColor = fragColor;
//	outColor = vec4(0.0, 0.0, 0.0, 1.0);
}

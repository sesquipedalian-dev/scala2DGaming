#version 150 core

in vec3 outTextColor;
in vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    fragColor = vec4(1, 1, 1, textureColor.r) * vec4(outTextColor, 1.0);
}
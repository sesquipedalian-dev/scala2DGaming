#version 150 core

in vec3 textureCoord;

out vec4 fragColor;

uniform sampler2DArray texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    fragColor = textureColor;
}
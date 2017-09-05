#version 150 core

in vec3 outTextColor;
in vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    fragColor = vec4(
        outTextColor.r * textureColor.r,
        outTextColor.g * textureColor.r,
        outTextColor.b * textureColor.r,
        textureColor.r
    );
}
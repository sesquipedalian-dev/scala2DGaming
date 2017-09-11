#version 150 core

in vec3 outTextColor;
in vec2 textureCoord;

out vec4 fragColor;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    float alpha = 0;
    if(textureColor.r > 0) {
        alpha = textureColor.r;
    } else {
        alpha = .5; // allow text to partially obscure stuff behind it - it will contrast better
    }
//    float alpha = textureColor.r;
    fragColor = vec4(
        outTextColor.r * textureColor.r,
        outTextColor.g * textureColor.r,
        outTextColor.b * textureColor.r,
        alpha
    );
}
#version 150 core

in vec3 textureCoord;

out vec4 fragColor;

uniform sampler2DArray texImage;
uniform float forceAlpha;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    if(textureColor.rgb == vec3(1.0, 1.0, 1.0)) { // any white pixels go transparent
        fragColor = vec4(0.0, 0.0, 0.0, 0.0);
    } else {
        fragColor = vec4(textureColor.rgb, textureColor.a * forceAlpha);
    }

//    fragColor = vec4(textureCoord.x, textureCoord.y, 0, 1.0);
}
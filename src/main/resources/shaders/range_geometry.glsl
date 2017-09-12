#version 150 core

layout(points) in;
//layout(triangle_strip) out;
layout(line_strip, max_vertices = 2) out;

in vec4[] geoColor;
out vec4 fragColor;

in float[] geoMinAngle;
in float[] geoMaxAngle;
in float[] geoLength;

uniform mat4 projection;

void main() {
    fragColor = geoColor[0];

    vec4 centerPoint = gl_in[0].gl_Position;

//    gl_Position = projection * (centerPoint + vec4(-geoLength[0], 0.0, 0.0, 0.0));
//    EmitVertex();
//
//    gl_Position = projection * (centerPoint + vec4(geoLength[0], 0.0, 0.0, 0.0));
//    EmitVertex();
//    EndPrimitive();

    gl_Position = vec4(-.5, 0, 0, 1);
    EmitVertex();

    gl_Position = vec4(.5, 0, 0, 1);
    EmitVertex();

    EndPrimitive();
}

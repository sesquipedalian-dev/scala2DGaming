#version 150 core
#define M_PI 3.1415926535897932384626433832795
#define PORTION_OF_CIRCLE_BETWEEN_VERTS 0.09817477042468103870195760572748 // 64 triangles for a circle
#define MAX_VERTICES 100 // M_PI * 2 / angle_between_verts

layout(points) in;
layout(triangle_strip, max_vertices = MAX_VERTICES) out;

in vec4[] geoColor;
out vec4 fragColor;

in float[] geoMinAngle;
in float[] geoMaxAngle;
in float[] geoLength;

uniform mat4 projection;

void main() {
    fragColor = geoColor[0];

    vec4 centerPoint = gl_in[0].gl_Position + vec4(512, 0, 0, 0);
    vec4 lastOffset = centerPoint + (geoLength[0] * vec4(cos(geoMinAngle[0]), sin(geoMinAngle[0]), 0.0, 0.0));
    vec4 maxAngleOffset = centerPoint + (geoLength[0] * vec4(cos(geoMaxAngle[0]), sin(geoMaxAngle[0]), 0.0, 0.0));

    for(
        float angle = geoMinAngle[0];
        angle <= geoMaxAngle[0];
        angle = angle + PORTION_OF_CIRCLE_BETWEEN_VERTS
    ) {
        gl_Position = projection * centerPoint;
        EmitVertex();

        gl_Position = projection * lastOffset;
        EmitVertex();

        lastOffset = centerPoint + (geoLength[0] * vec4(cos(angle), sin(angle), 0.0, 0.0));
        gl_Position = projection * lastOffset;
        EmitVertex();

        EndPrimitive();
    }
}

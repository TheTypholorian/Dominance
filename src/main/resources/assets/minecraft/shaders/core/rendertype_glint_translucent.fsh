#version 150

#moj_import <dominance:glint.glsl>
#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform mat4 ProjMat;
uniform float RenderTime;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in float camDistance;
in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor = glintEnchant(RenderTime, camDistance) * ColorModulator * linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
}
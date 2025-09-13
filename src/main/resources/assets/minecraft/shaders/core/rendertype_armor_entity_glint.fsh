#version 150

#moj_import <dominance:glint.glsl>
#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform mat4 ProjMat;
uniform float GameTime;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float GlintAlpha;

in float vertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
    vec4 min = vec4(0.17, 0.07, 0.3, 1);
    vec4 max = vec4(0.64, 0.32, 1, 1);
    fragColor = glintEnchant(min, max, GameTime * 1000, fragDistance(ProjMat), texCoord0) * ColorModulator;
}

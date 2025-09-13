#include "dominance:glint"

uniform sampler2D DiffuseSampler0;
uniform float GameTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

in vec2 FragTexCoord;

out vec4 fragColor;

void main() {
    vec4 min = vec4(1, 0.41, 1, 1);
    vec4 max = vec4(1, 0.76, 0.4, 1);
    fragColor = glintCube(min, max, GameTime * 1000, fragDistance(ProjMat), FragTexCoord);
}
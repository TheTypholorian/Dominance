uniform sampler2D DiffuseSampler0;
uniform float GameTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

out vec4 fragColor;

float linearDepth(float depth, mat4 proj) {
    return proj[3][2] / (depth * 2 - 1 + proj[2][2]);
}

void main() {
    vec4 min = vec4(1, 0.41, 1, 1);
    vec4 max = vec4(1, 0.76, 0.4, 1);
    fragColor = ((max - min) * (sin(GameTime * 3000) / 2 + 0.5) + min) * sin(linearDepth(gl_FragCoord.z, ProjMat) + GameTime * 3000) / 4 + 1.25;
}
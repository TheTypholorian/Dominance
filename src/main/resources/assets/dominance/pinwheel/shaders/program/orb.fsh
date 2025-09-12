uniform sampler2D DiffuseSampler0;
uniform float GameTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

in vec2 FragTexCoord;

out vec4 fragColor;

void main() {
    vec4 min = vec4(1, 0.41, 1, 1);
    vec4 max = vec4(1, 0.76, 0.4, 1);
    float shine = sin(GameTime * 5000) * sin(ProjMat[3][2] / (gl_FragCoord.z * 2 - 1 + ProjMat[2][2]) * 2) / 2 * sqrt(clamp(distance(FragTexCoord, vec2(0.5, 0.5)) / 0.7, 0, 1)) / 2;
    fragColor = ((max - min) * (sin(GameTime * 1500) / 2 + 0.5 + clamp(shine, -0.5, 0)) + min) * (1 + clamp(shine, 0, 0.5));
}
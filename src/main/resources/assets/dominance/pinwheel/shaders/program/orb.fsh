uniform sampler2D DiffuseSampler0;
uniform float GameTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

in vec2 FragTexCoord;

out vec4 fragColor;

void main() {
    vec4 min = vec4(1, 0.41, 1, 1);
    vec4 max = vec4(1, 0.76, 0.4, 1);
    float depth = ProjMat[3][2] / (gl_FragCoord.z * 2 - 1 + ProjMat[2][2]);
    float blend = sin(GameTime * 3000) / 2 + sin(depth) / 4 + 0.5;
    vec2 tc = FragTexCoord - 0.5;
    float dist = clamp((tc.x * tc.x + tc.y * tc.y) * 2, 0, 1);
    fragColor = (max - min) * blend + min;
    fragColor *= clamp(sin(depth * 3 + GameTime * 3000) / 4 + 1.25 + sin(dist) / 3, 1, 1.75 - blend / 4);
}
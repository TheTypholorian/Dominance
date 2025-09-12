uniform sampler2D DiffuseSampler0;
uniform float GameTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

out vec4 fragColor;

void main() {
    vec4 min = vec4(1, 0.41, 1, 1);
    vec4 max = vec4(1, 0.76, 0.4, 1);
    float depth = ProjMat[3][2] / (gl_FragCoord.z * 2 - 1 + ProjMat[2][2]);
    fragColor = (max - min) * (sin(depth / 2 + GameTime * 3000) / 2 + 0.5) + min;
    fragColor *= sin(depth * 3 + GameTime * 3000) / 4 + 1.25;
}
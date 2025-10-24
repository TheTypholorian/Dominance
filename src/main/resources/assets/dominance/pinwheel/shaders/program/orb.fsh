uniform sampler2D DiffuseSampler0;
uniform float RenderTime;
uniform vec2 ScreenSize;
uniform mat4 ProjMat;

in vec2 FragTexCoord;

out vec4 fragColor;

void main() {
    vec4 min1 = vec4(1, 0.41, 1, 1); // purple
    vec4 min2 = vec4(1, 0.3, 0.7, 1); // red
    vec4 max1 = vec4(1, 0.76, 0.4, 1); // orange
    vec4 max2 = vec4(1, 0.9, 0.5, 1); // yellow

    vec2 tcDelta = abs(FragTexCoord - vec2(0.5));
    float angle = abs(tcDelta.x - tcDelta.y); // 0 on corners, 1 on edges

    float time = RenderTime + (length(tcDelta) + angle / 3) / 3;

    float minF = mod(time / 5, 2);

    if (minF > 1) {
        minF = 2 - minF;
    }

    float maxF = mod((time + 0.5) / 3, 2);

    if (maxF > 1) {
        maxF = 2 - maxF;
    }

    float lf = mod(time, 2);

    if (lf > 1) {
        lf = 2 - lf;
    }

    vec4 minL = (min1 - min2) * minF + min2;
    vec4 maxL = (max1 - max2) * maxF + max2;

    fragColor = (maxL - minL) * lf + minL;
}
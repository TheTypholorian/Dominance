uniform sampler2D DiffuseSampler0;
uniform sampler2D DiffuseDepthSampler;

uniform float uTime = 0.5;

out vec4 fragColor;

void main() {
    fragColor = vec4(mod(uTime, 1), 0, 0, 1);
    //fragColor = vec4(1, 0.5, 0.25, 1);
}
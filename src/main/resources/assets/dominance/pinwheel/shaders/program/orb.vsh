#version 150

in vec3 Position;
in vec2 TexCoord;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 CameraPos;

out vec2 FragTexCoord;

void main() {
    vec4 pos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * pos;
    FragTexCoord = TexCoord;
}

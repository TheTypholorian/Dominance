#version 150

in vec3 Position;
in vec2 TexCoord;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 FragTexCoord;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    FragTexCoord = TexCoord;
}

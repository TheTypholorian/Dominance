float fragDistance(mat4 _ProjMat) {
    return _ProjMat[3][2] / (gl_FragCoord.z * 2 - 1 + _ProjMat[2][2]);
}

vec4 glintEnchant(vec4 _min, vec4 _max, float _GameTime, float _dist, vec2 _FragTexCoord) {
    float cScale = sqrt(clamp(distance(_FragTexCoord, vec2(0.5, 0.5)) / 0.7, 0, 1));
    float shine = sin(_GameTime * 5) * sin(_dist * 8) * cScale / 4;
    return ((_max - _min) * (sin(_GameTime * 2 + cScale * 2) / 2 + 0.5 + clamp(shine, -0.5, 0.0)) + _min) * (1 + clamp(shine, 0.0, 0.5));
}

vec4 glintCube(vec4 _min, vec4 _max, float _GameTime, float _dist, vec2 _FragTexCoord) {
    float cScale = sqrt(clamp(distance(_FragTexCoord, vec2(0.5, 0.5)) / 0.7, 0, 1));
    float shine = sin(_GameTime * 5) * sin(_dist * 2) * cScale / 4;
    return ((_max - _min) * (sin(_GameTime * 1.5 + cScale * 2) / 2 + 0.5 + clamp(shine, -0.5, 0.0)) + _min) * (1 + clamp(shine, 0.0, 0.5));
}
varying vec2 uv;
uniform sampler2D m_screen;
vec4 HDR(vec4 L) {
    L = L * 3.0;
    L.r = L.r < 1.413 ? pow(L.r * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.r);
    L.g = L.g < 1.413 ? pow(L.g * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.g);
    L.b = L.b < 1.413 ? pow(L.b * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.b);
    L.a = L.a < 1.413 ? pow(L.a * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.a);
    return L;
}

void main(void){

gl_FragColor = vec4(1.0);


}

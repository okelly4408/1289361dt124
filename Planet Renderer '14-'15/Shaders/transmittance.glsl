
#ifdef VERTEX

void main() {
}

#endif

#ifdef GEOMETRY
layout(points) in;
layout(triangle_strip, max_vertices = 4) out;
out vec2 uv;
void main(){
    gl_Position = vec4(-1.0, -1.0, 0.0, 1.0);
    uv = vec2(0.0,0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
    uv = vec2(1.0,0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
    uv = vec2(0.0,1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
    uv = vec2(1.0,1.0);
    gl_Layer = 0;
    EmitVertex();
    
    EndPrimitive();
}

#endif

#ifdef FRAGMENT
out vec4 fragColor;
in vec2 uv;
float opticalDepth(float H, float r, float mu) {
    float result = 0.0;
    float dx = limit(r, mu) / float(TRANSMITTANCE_INTEGRAL_SAMPLES);
    float xi = 0.0;
    float yi = exp(-(r - Rg) / H);
    for (int i = 1; i <= TRANSMITTANCE_INTEGRAL_SAMPLES; ++i) {
        float xj = float(i) * dx;
        float yj = exp(-(sqrt(r * r + xj * xj + 2.0 * xj * r * mu) - Rg) / H);
        result += (yi + yj) / 2.0 * dx;
        xi = xj;
        yi = yj;
    }
    return mu < -sqrt(1.0 - (Rg / r) * (Rg / r)) ? 1e9 : result;
}

void main() {
    float r, muS;
    getTransmittanceRMu(r, muS);
    vec3 depth = betaR * opticalDepth(HR, r, muS) + betaMEx * opticalDepth(HM, r, muS);
  fragColor = vec4(exp(-depth), 0.0); // Eq (5)
}

#endif
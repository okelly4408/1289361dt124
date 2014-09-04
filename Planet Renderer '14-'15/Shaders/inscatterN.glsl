
// computes higher order scattering (line 9 in algorithm 4.1)

uniform float r;
uniform vec4 dhdH;
uniform int layer;

uniform sampler3D deltaJSampler;

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
    uv = vec2(0.0, 0.0);
    gl_Layer = layer;
    EmitVertex();
    
    gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
    uv = vec2(1.0, 0.0);
    gl_Layer = layer;
    EmitVertex();
    
    gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
    uv = vec2(0.0, 1.0);
    gl_Layer = layer;
    EmitVertex();
    
    gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
    uv = vec2(1.0, 1.0);
    gl_Layer = layer;
    EmitVertex();
    
    EndPrimitive();
}


#endif

#ifdef FRAGMENT

out vec4 fragColor;

vec3 integrand(float r, float mu, float muS, float nu, float t) {
    float ri = sqrt(r * r + t * t + 2.0 * r * mu * t);
    float mui = (r * mu + t) / ri;
    float muSi = (nu * t + muS * r) / ri;
    return texture4D(deltaJSampler, ri, mui, muSi, nu).rgb * transmittance(r, mu, t);
}

vec3 inscatter(float r, float mu, float muS, float nu) {
    vec3 raymie = vec3(0.0);
    float dx = limit(r, mu) / float(INSCATTER_INTEGRAL_SAMPLES);
    float xi = 0.0;
    vec3 raymiei = integrand(r, mu, muS, nu, 0.0);
    for (int i = 1; i <= INSCATTER_INTEGRAL_SAMPLES; ++i) {
        float xj = float(i) * dx;
        vec3 raymiej = integrand(r, mu, muS, nu, xj);
        raymie += (raymiei + raymiej) / 2.0 * dx;
        xi = xj;
        raymiei = raymiej;
    }
    return raymie;
}

void main() {
    float mu, muS, nu;
    getMuMuSNu(r, dhdH, mu, muS, nu);
    fragColor.rgb = inscatter(r, mu, muS, nu);
    fragColor.a = 1.0;
}

#endif


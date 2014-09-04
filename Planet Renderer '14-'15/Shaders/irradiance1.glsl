
#ifdef VERTEX

void main() {
}

#endif

#ifdef GEOMETRY
layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

void main(){
    gl_Position = vec4(-1.0, -1.0, 0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    EndPrimitive();
}

#endif

#ifdef FRAGMENT
out vec4 fragColor;
void main() {
    float r, muS;
    getIrradianceRMuS(r, muS);
    fragColor = vec4(transmittance(r, muS) * max(muS, 0.0), 0.0);
}

#endif
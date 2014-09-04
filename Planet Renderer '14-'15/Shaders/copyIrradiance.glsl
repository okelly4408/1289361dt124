


uniform float k; // k=0 for line 4, k=1 for line 10
uniform sampler2D deltaESampler;

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
    vec2 uv = gl_FragCoord.xy / vec2(64, 16);
    fragColor = k * texture(deltaESampler, uv); // k=0 for line 4, k=1 for line 10
}

#endif

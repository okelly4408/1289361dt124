

uniform float r;
uniform vec4 dhdH;
uniform int layer;

uniform sampler3D deltaSRSampler;
uniform sampler3D deltaSMSampler;

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
void main() {
  
    vec3 uvw = vec3(gl_FragCoord.xy, float(layer) + 0.5) / vec3(ivec3( RES_MU_S * RES_NU, RES_MU, RES_R));
    vec4 ray = texture(deltaSRSampler, uvw);
    vec4 mie = texture(deltaSMSampler, uvw);
    fragColor = vec4(ray.rgb, mie.r); // store only red component of single Mie scattering (cf. "Angular precision")
}

#endif

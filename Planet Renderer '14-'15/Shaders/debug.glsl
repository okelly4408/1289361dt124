
uniform sampler2D texture2;
uniform sampler3D texture3;
uniform float depth;
#ifdef VERTEX
void main(){
}
#endif

#ifdef GEOMETRY

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;
out vec2 uv;
out vec4 pos;
void main(){
    gl_Position = vec4(-1.0, -1.0, 0.0, 1.0);
    pos = gl_Position;
    uv = vec2(0.0, 0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
     pos = gl_Position;
    uv = vec2(1.0, 0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
     pos = gl_Position;
    uv = vec2(0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
     pos = gl_Position;
    uv = vec2(1.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    EndPrimitive();
}


#endif


#ifdef FRAGMENT

in vec2 uv;
in vec4 pos;
out vec4 fragColor;

void main(){
//   fragColor.rgb = texture(texture3, vec3(uv, depth)).rgb;
//    fragColor.rgb = texture(texture2, uv).rgb;
fragColor.rgb = pos.xyz * 0.5 + 0.5;

    fragColor.a = 1.0;
}


#endif
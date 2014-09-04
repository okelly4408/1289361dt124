
#ifdef VERTEX
void main(){
}
#endif

#ifdef GEOMETRY

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;
out vec2 uv;
void main(){
    gl_Position = vec4(-1.0, -1.0, 0.0, 1.0);
    uv = vec2(0.0, 0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
    uv = vec2(1.0, 0.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
    uv = vec2(0.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
    uv = vec2(1.0, 1.0);
    gl_Layer = 0;
    EmitVertex();
    
    EndPrimitive();
}


#endif




#ifdef FRAGMENT
out vec4 fragData[ 1 ];
in vec2 uv;

uniform sampler2D permSampler2d;
uniform sampler2D permGradSampler;

vec3 fade(vec3 t)
{
	return (t * t * t * (t * (t * 6.0 - 15.0) + 10.0)); 
}
vec4 perm2d(vec2 p)
{
	return texture(permSampler2d, p).rgba;
}
float gradperm(float x, vec3 p)
{
	return dot(vec3(texture(permGradSampler, vec2(x,0.0)).xyz), p);
}

float inoise(in vec3 p)
{
	vec3 P = mod(floor(p), 256.0);	
  	p -= floor(p);                      
	vec3 f = fade(p);                 

	P = P / 256.0;
	const float one = 1.0 / 256.0;
	  
	vec4 AA = perm2d(P.yx) + P.z;
 
  	return mix( mix( mix( gradperm(AA.x, p ),  
                             gradperm(AA.z, p + vec3(-1.0, 0.0, 0.0) ), f.x),
                       mix( gradperm(AA.y, p + vec3(0.0, -1.0, 0.0) ),
                             gradperm(AA.w, p + vec3(-1.0, -1.0, 0.0) ), f.x), f.y),
                             
                 mix( mix( gradperm(AA.x+one, p + vec3(0.0, 0.0, -1.0) ),
                             gradperm(AA.z+one, p + vec3(-1.0, 0.0, -1.0) ), f.x),
                       mix( gradperm(AA.y+one, p + vec3(0.0, -1.0, -1.0) ),
                             gradperm(AA.w+one, p + vec3(-1.0, -1.0, -1.0) ), f.x), f.y), f.z);
}

//**RIDGED MULTIFRACTAL**//
float RMF(in vec3 v)  {

		 float result, frequency, signal, weight;
		 float H = 0.75;
		 float lacunarity = 2.13413;
		 int octaves = 12;
		 float offset = 1.0;
		 float gain = 1.0;
         int i;
         frequency = 1.0;
      
         signal = inoise(v);
         
         if (signal < 0.0) signal = -signal;
             
         signal = offset - signal;
         
         signal *=signal;
        
         result = signal;
         weight = 1.0;

         for (i = 1; i < octaves; i++) {   
             v*=lacunarity;         
             weight = signal * gain;

             if ( weight > 1.0 ) weight = 1.0;
                 
             if ( weight < 0.0 ) weight = 0.0;
                 
            signal = (inoise(v));

             if ( signal < 0.0 ) signal = -signal;
                 
             signal = offset - signal;
             signal *= signal;
             signal *= weight;
             result += signal * pow(frequency, -H);
             frequency *= lacunarity;
         }       
         return (result - 1.0)/2.0;        
     }
     
float fbm(in vec3 p, in int octaves){
float f = 0.0;
float scale = 0.0;
float frequency = 0.50;
float lacunarity = 2.0134134;
for(int i = 0; i<octaves; i++){
float cell = inoise(p);
f+= (frequency * (cell) );
p = p*lacunarity;
scale += frequency;
frequency /= 2.0;
}
 return (f/scale);
}
     float getTerraced(float val, float n, float power)
{
float dVal = val * n;
float f = fract(dVal);
float i = floor(dVal);

return (i + pow(f, power)) / n;
}


void main(void){

vec3 p = vec3(uv.x, 0.0, uv.y);
float h = getTerraced(RMF((p))+0.5, 1.3, 1.9);
//float h = RMF(p) + 0.5;
fragData[0] = vec4(h);

//}

}

#endif


uniform sampler2D m_permutationTexture;
uniform sampler2D m_gradientTexture;
uniform sampler2D m_altpermutationTexture;
uniform sampler2D m_altgradientTexture;
uniform sampler2D m_cellRandTex;
uniform float m_Off;
uniform float m_Gain;
uniform float m_H;
uniform float m_Amp;
uniform float m_lac;
uniform float m_num_terraces;
uniform float m_terrace_slope;
uniform float m_cont_upper;
uniform float m_cont_lower;
uniform float m_mount_upper;
uniform float m_mount_lower;
uniform float m_cont_frequency;
uniform float m_hmf_off;
uniform float m_hmf_lac;
uniform float m_hmf_h;
uniform float m_fbm_lac;
uniform float m_fbm_pers;
uniform float m_rim_width;
uniform float m_depth;
uniform float m_radius;
uniform float m_slope;
uniform float m_octaves;
uniform float m_crater_frequency;
varying vec3 varPos;
uniform float m_denom1;
uniform float m_denom2;
uniform float m_Size;
uniform float m_worldSize;
uniform vec3 m_faceOffset;
uniform vec3 m_meshOffset;
uniform int m_t;
varying vec3 spherePos;
uniform bool m_mars;

//quintic interpolation for noise
vec3 fade(vec3 t)
{
	return (t * t * t * (t * (t * 6.0 - 15.0) + 10.0)); 
}

//samples a texture of random values
vec4 perm2d(vec2 p)
{
	return texture2D(m_permutationTexture, p);
}

//samples a texture of gradients 
float gradperm(float x, vec3 p)
{
	return dot(vec3(texture2D(m_gradientTexture, vec2(x,0.0)).xyz), p);
}

//samples a texture of random values
vec4 perm2dALT(vec2 p)
{
	return texture2D(m_altpermutationTexture, p);
}

//samples a texture of gradients 
float gradpermALT(float x, vec3 p)
{
	return dot(vec3(texture2D(m_altgradientTexture, vec2(x,0.0)).xyz), p);
}

// 3D Perlin (gradient) noise type 0 = standard textures, 1 = alternate textures
float inoise(in vec3 p, int type)
{
	vec3 P = mod(floor(p), 256.0);	
  	p -= floor(p);                      
	vec3 f = fade(p);                 

	P = P / 256.0;
	vec4 AA;
	const float one = 1.0 / 256.0;
	if(type == 0){
	 AA = perm2d(P.xy) + P.z;
	}else if(type == 1){
	 AA = perm2dALT(P.xy) + P.z;
	}
 if(type == 0){
  	return ( (mix( mix( mix( gradperm(AA.x, p ),  
                             gradperm(AA.z, p + vec3(-1.0, 0.0, 0.0) ), f.x),
                       mix( gradperm(AA.y, p + vec3(0.0, -1.0, 0.0) ),
                             gradperm(AA.w, p + vec3(-1.0, -1.0, 0.0) ), f.x), f.y),
                             
                 mix( mix( gradperm(AA.x+one, p + vec3(0.0, 0.0, -1.0) ),
                             gradperm(AA.z+one, p + vec3(-1.0, 0.0, -1.0) ), f.x),
                       mix( gradperm(AA.y+one, p + vec3(0.0, -1.0, -1.0) ),
                             gradperm(AA.w+one, p + vec3(-1.0, -1.0, -1.0) ), f.x), f.y), f.z)));
}else if(type == 1){
                           return ( (mix( mix( mix( gradpermALT(AA.x, p ),  
                             gradpermALT(AA.z, p + vec3(-1.0, 0.0, 0.0) ), f.x),
                       mix( gradpermALT(AA.y, p + vec3(0.0, -1.0, 0.0) ),
                             gradpermALT(AA.w, p + vec3(-1.0, -1.0, 0.0) ), f.x), f.y),
                             
                 mix( mix( gradpermALT(AA.x+one, p + vec3(0.0, 0.0, -1.0) ),
                             gradpermALT(AA.z+one, p + vec3(-1.0, 0.0, -1.0) ), f.x),
                       mix( gradpermALT(AA.y+one, p + vec3(0.0, -1.0, -1.0) ),
                             gradpermALT(AA.w+one, p + vec3(-1.0, -1.0, -1.0) ), f.x), f.y), f.z))) ;  
                             }else{
                             return 0.0;
                             }
                             
}
vec3 grad(float x)
{
return vec3(texture2D(m_gradientTexture, vec2(x,0.0)).xyz);
}
//returns derivative of noise with respect to x,y,z
vec4 dnoise(in vec3 p)
{
	vec3 P = mod(floor(p), 256.0);	
  	p -= floor(p);                      
	vec3 f = fade(p);                 

	P = P / 256.0;
	const float one = 1.0 / 256.0;
	  
	vec4 AA = perm2d(P.xy) + P.z;
	
//get gradients
	vec3 g000 = grad(AA.x);
	vec3 g100 = grad(AA.z);
	vec3 g010 = grad(AA.y);
	vec3 g110 = grad(AA.w);
	
	vec3 g001 = grad(AA.x + one);
	vec3 g101 = grad(AA.z + one);
	vec3 g011 = grad(AA.y + one);
	vec3 g111 = grad(AA.w + one);

 	float dot000 = dot(g000, p);
	float dot100 = dot(g100, p + vec3(-1.0,0.0,0.0));
	float dot010 = dot(g010, p + vec3(0.0, -1.0, 0.0));
	float dot110 = dot(g110, p + vec3(-1.0, -1.0, 0.0));

	float dot001 = dot(g001, p + vec3(0.0, 0.0, -1.0));
	float dot101 = dot(g101, p + vec3(-1.0, 0.0, -1.0));
	float dot011 = dot(g011, p + vec3(0.0, -1.0, -1.0));
	float dot111 = dot(g111, p + vec3(-1.0, -1.0, -1.0));
	
//calculate perlin noise
                          
float n = dot000 
  + f.x*(dot100 - dot000)
  + f.y*(dot010 - dot000)
  + f.z*(dot001 - dot000)
  + (f.x*f.y)*(dot110 - dot010 - dot100 + dot000)
  + (f.x*f.z)*(dot101 - dot001 - dot100 + dot000)
  + (f.y*f.z)*(dot011 - dot001 - dot010 + dot000)
  + (f.x*f.y*f.z)*(dot111 - dot011 - dot101 + dot001 - dot110 + dot010 + dot100 - dot000);
                            
//derivative of hermite polynomial
	vec3 df = 30.0 * p * p * (p - 1.0) * (p - 1.0);
	
//calculate partial derivatives
	float dnx = g000.x
   + df.x * (dot100 - dot000)
   + f.x * (g100.x - g000.x)
   + f.y * (g010.x - g000.x)
   + f.z * (g001.x - g000.x)
   + (df.x * f.y)*(dot110 - dot010 - dot100 + dot000)
   + (f.x * f.y) * (g110.x - g010.x - g100.x + g000.x)
   + (df.x * f.z)*(dot101 - dot001 - dot100 + dot000)
   + (f.x * f.z) * (g101.x - g001.x - g100.x - g000.x)
   + (f.y * f.z) * (g011.x - g001.x - g010.x + g000.x)
   + (df.x * f.y * f.z)*(dot111 - dot011 - dot101 + dot001 - dot110 + dot010 + dot100 - dot000)
   + (f.x * f.y * f.z)*(g111.x - g011.x - g101.x + g001.x - g110.x + g010.x + g100.x - g000.x);
   
   float dny = g000.y
   + (f.x)*   (g100.y - g000.y)
   + (df.y)*  (dot010 - dot000)
   + (f.y)*   (g010.y - g000.y)
   + (f.z)*   (g001.y - g000.y)
   + (f.x * df.y)* (dot110 - dot010 - dot100 + dot000)
   + (f.x * f.y)*  (g110.y - g010.y - g100.y + g000.y)
   + (f.x * f.z)*  (g101.y - g001.y - g100.y + g000.y)
   + (df.y * f.z)* (dot011 - dot001 - dot010 + dot000)
   + (f.y * f.z)* (g011.y - g001.y - g010.y + g000.y)
   + (f.x * df.y * f.z)* (dot111 - dot011 - dot101 + dot001 - dot110 + dot010 + dot100 - dot000)
   + (f.x * f.y * f.z)* (g111.y - g011.y - g101.y + g001.y - g110.y + g010.y + g100.y - g000.y);
   
   float dnz = g000.z
   + (f.x)*   (g100.z - g000.z)
   + (f.y)*   (g010.z - g000.z)
   + (df.z)*  (dot001 - dot000)
   + (f.z)*   (g001.z - g000.z)
   + (f.x * f.y)*  (g110.z - g010.z - g100.z + g000.z)
   + (f.x * df.z)* (dot101 - dot001 - dot100 + dot000)
   + (f.x * f.z)*  (g101.z - g001.z - g100.z + g000.z)
   + (f.y * df.z)* (dot011 - dot001 - dot010 + dot000)
   + (f.y * f.z)*  (g011.z - g001.z - g010.z + g000.z)
   + (f.x * f.y * df.z)* (dot111 - dot011 - dot101 + dot001 - dot110 + dot010 + dot100 - dot000)
   + (f.x * f.y * f.z)* (g111.z - g011.z - g101.z + g001.z - g110.z + g010.z + g100.z - g000.z);

	return vec4(n, dnx, dny, dnz);
} 
//Voronoi or Worley noise
//f.x = F1
//f.y - f.x = F2

vec4 gpuGetCell3D(const in int x, const in int y, const in int z)
{
	float u = float(x + y * 31) / 256.0;
	float v = float(z - x * 3) / 256.0;
	return(texture2D(m_cellRandTex, vec2(u, v)));
}

vec2 gpuCellNoise3D(const in vec3 xyz)
{
	int xi = int(floor(xyz.x));
	int yi = int(floor(xyz.y));
	int zi = int(floor(xyz.z));

	float xf = xyz.x - float(xi);
	float yf = xyz.y - float(yi);
	float zf = xyz.z - float(zi);

	float dist1 = 9999999.0;
	float dist2 = 9999999.0;
	vec3 cell;

	for (int z = -1; z <= 1; z++)
	{
		for (int y = -1; y <= 1; y++)
		{
			for (int x = -1; x <= 1; x++)
			{
				cell = gpuGetCell3D(xi + x, yi + y, zi + z).xyz;
				cell.x += (float(x) - xf);
				cell.y += (float(y) - yf);
				cell.z += (float(z) - zf);
				float dist = dot(cell, cell);
				if (dist < dist1)
				{
					dist2 = dist1;
					dist1 = dist;
				}
				else if (dist < dist2)
				{
					dist2 = dist;
				}
			}
		}
	}
	return vec2(sqrt(dist1), sqrt(dist2));
}

float f2mf1(vec3 p){
vec2 c = gpuCellNoise3D(p);

return c.y - c.x;
}

//classic ridged multifractal noise

float RMF(in vec3 v, int type)  {

		 float result, frequency, signal, weight;
		 float H = m_H;
		 float lacunarity = m_lac;
		 int octaves = 23;
		 float offset = m_Off;
		 float gain = m_Gain;
         int i;
         frequency = 1.0;
      
         signal = inoise(v, type);
         
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
                 
            signal = (inoise(v, type));

             if ( signal < 0.0 ) signal = -signal;
                 
             signal = offset - signal;
             signal *= signal;
             signal *= weight;
             result += signal * pow(frequency, -H);
             frequency *= lacunarity;
         }       
         return (result - 1.0)/2.0;        
     }
     
     
     
//fractional Brownian motion
float fbm(in vec3 v, in int octaves, int type, float frequency){
		  float persistence = m_fbm_pers;
          float lacunarity = m_fbm_lac;
          float sum = 0.0;
          float noise = 0.0;
          float pers = 1.0;
          v *= frequency;
 
          for (int i = 0; i<octaves; i++)
          {
             noise = inoise(v,type);
             sum += noise * pers;
             v *= lacunarity;
             pers *= persistence;
        }
        return  sum;
}


//hyrbid multifractal noise
float HMF( vec3 point, int type, int octaves)
{
float H = m_hmf_h;
float lacunarity = m_hmf_lac;
float offset = m_hmf_off;
      float frequency, result, signal, weight; 
      int   i;
      frequency = 1.0;      
      result = ( inoise( point, type ) + offset ) * pow(frequency, -H);
      frequency *= lacunarity;
      weight = result;
      point *= lacunarity;
      for (i=1; i<octaves; i++) {
            if ( weight > 1.0 )  weight = 1.0;
            signal = ( inoise( point, type ) + offset ) * pow(frequency, -H);
            frequency *= lacunarity;
            result += weight * signal;
            weight *= signal;

            point *= lacunarity;
            
      } 

      return( result - 1.0)/2.0;

} 

//variant of ridged multifractal noise
float rnoise(vec3 p, int type)
{
float n = 1.0 - abs(inoise(p, type) * 2.0);
return n*n - 0.5;
}

float rmf3D(vec3 p, int octaves, float frequency, float lacunarity, float gain, int type)
{
float sum = 0.0;
float amp = 1.0;
for(int i = 0; i < octaves; i++)
{
  float n = rnoise(p * frequency, type);
  sum += n * amp;
  frequency *= lacunarity;
  amp *= gain;
}
return sum;
}

//maps a point on a cube to a point on a sphere

vec3 spherize(in vec3 v){
float x = (v.x * (sqrt(1.0-((v.y*v.y)/m_denom1)-((v.z*v.z)/m_denom1)+(((v.y*v.y)*(v.z*v.z))/m_denom2))));
float y = (v.y * (sqrt(1.0-((v.x*v.x)/m_denom1)-((v.z*v.z)/m_denom1)+(((v.x*v.x)*(v.z*v.z))/m_denom2))));
float z = (v.z * (sqrt(1.0-((v.y*v.y)/m_denom1)-((v.x*v.x)/m_denom1)+(((v.y*v.y)*(v.x*v.x))/m_denom2))));
return vec3(x,y,z);
}
	   
//creates "terraced" cliffs on mountains
	   
float getTerraced(float val, float n, float power)
{
float dVal = val * n;
float f = fract(dVal);
float i = floor(dVal);

return (i + pow(f, power)) / n;
}

float simpleFBM(in vec3 p, int octaves){
	float f = 0.0;
	float scale = 0.0;
	float frequency = 0.5;
	float lacunarity = 2.0134134;
	
	for(int i = 0; i<octaves; i++){
		float cell = inoise(p,0);
		f+= frequency * (cell);
		p = p*lacunarity;
		scale += frequency;
		frequency /= 2.0;
	}
    	return f/scale;
}

float fractalCells(in vec3 p, int octaves){
	float f = 0.0;
	float scale = 0.0;
	float frequency = 0.5;
	float lacunarity = 2.0134134;
	
	for(int i = 0; i<octaves; i++){
		vec2 cell = gpuCellNoise3D(p);
		f+= frequency * (cell.x);
		p = p*lacunarity;
		scale += frequency;
		frequency /= 2.0;
	}
    	return f/scale;
}

float craterNoise3D(in vec3 p, in float radius, in float slope, in float rimWidth, in float depth, in float frequency, in int octaves){

	float fractal = simpleFBM(p * frequency * 2.0, octaves) * 0.17;
	float cell = gpuCellNoise3D((p * frequency) + fractal ).x;
	float r = radius + fractal;
	float crater = smoothstep(slope, r, cell);
	  	  crater = mix(depth, crater, crater);
	float rim = 1.0 - smoothstep(r, r + rimWidth, cell);
	      crater = rim - (1.0 - crater);
return crater * 0.175;
}
float jordanTurbulence(in vec3 p)
{
float lacunarity = 2.0;
float gain1 = 0.8,  gain = 0.5,
                        warp0 = 0.4,  warp = 0.35,
                        damp0 = 1.0,  damp = 0.8,
                        damp_scale = 1.0;

    vec4 n = dnoise(p);
    vec4 n2 = n * n.x;
    float sum = n2.x;
    vec3 dsum_warp = warp0*n2.yzw;
    vec3 dsum_damp = damp0*n2.yzw;

    float amp = gain1;
    float freq = lacunarity;
    float damped_amp = amp * gain;

    for(int i=1; i < 21; i++)
    {
        n = dnoise(p * freq + dsum_warp.xyz);
        n2 = n * n.x;
        sum += damped_amp * n2.x;
        dsum_warp += warp * n2.yzw;
        dsum_damp += damp * n2.yzw;
        freq *= lacunarity;
        amp *= gain;
        damped_amp = amp * (1.0-damp_scale/(1.0+dot(dsum_damp,dsum_damp)));
    }
    return sum;
}
//simulates erosion by warping ridges along gradients...uses derivative of perlin noise
float swissTurbulence(in vec3 p){
float lacunarity = 2.0;
float gain = 0.5;
float warp = 0.15;
int octaves = 19;
     float sum = 0.0;
     float freq = 1.0, amp = 1.0;
vec3 dsum = vec3(0.0,0.0,0.0);
for(int i=0; i < octaves; i++)
     {
vec4 n = dnoise( (p + warp * dsum) * freq);
sum += amp * (1.0 - abs(n.x));
dsum += amp * n.yzw * -n.x;
freq *= lacunarity;
amp *= gain * clamp(sum,0.0,1.0);

    }
    return sum;
}

void main(void){
vec3 varPos1 = varPos;
//scale the interpolated values to match vertices of the node
varPos1 *= ((m_Size + 1.0) / 2.0);
vec3 spherePos;

//the vertex position of the sphere is calculated

	if(m_t == 1) //top or bottom cube face
{
vec3 v1 = vec3(varPos1.x, 0.0, varPos1.y);
vec3 v2 = v1 + m_faceOffset;
vec3 v3 = v2 + m_meshOffset;

spherePos = spherize(v3);
}
	else if(m_t == 2) //front or back face
{
vec3 v1 = vec3(varPos1.x, varPos1.y, 0.0);
vec3 v2 = v1 + m_faceOffset;
vec3 v3 = v2 + m_meshOffset;

spherePos = spherize(v3);
}
	else if(m_t == 3) //right or left face
{
vec3 v1 = vec3(0.0, varPos1.x, varPos1.y);
vec3 v2 = v1 + m_faceOffset;
vec3 v3 = v2 + m_meshOffset;

spherePos = spherize(v3);
}

//the position is used as input for the noise 
vec3 p = spherePos / m_worldSize;
float c = fbm(p,10,0,m_cont_frequency);
float m = fbm(p,5,1,0.7);

float rOver4 = (m_worldSize)/4.0;
float climateType;
float climateOffset = c * m_worldSize;
float b = ( abs(spherePos.y) + climateOffset)/m_worldSize;

//good desert terrain: HMF(p  + (0.5 * height), 1)
//default plain lands = HMF( p * 10.0, 1) * 0.45 + (height * 0.1) 
float height = pow(2.0, rmf3D(p, 23, 23.0, 1.75, 0.6,0));
float dunes = (HMF(p * 42.0, 1,20)*0.4);
height = getTerraced(height, 4.0, 1.95);
float mountains = RMF(p * 42.0,1) + 0.5;
mountains = getTerraced(mountains, m_num_terraces, m_terrace_slope + (simpleFBM(p * 60.0, 4)) );
//compute interpolant for land and sea
float blendCoast = smoothstep(m_cont_lower, m_cont_upper, c);
//com
float desertBlend = smoothstep(0.45, 0.70, b);
float standardBlend = smoothstep(m_mount_lower, m_mount_upper, m );
float non_mountainous = (HMF( p * 33.0, 1,11) * 0.375 + (height * 0.15));
float standardLands = mix(pow(non_mountainous,1.35), mountains, standardBlend);
float desertLands = mix(standardLands, HMF(p * 0.85  + (0.5 * height), 1,16) * 0.75, smoothstep(0.5, 0.75, m));
float land = mix(desertLands, standardLands, desertBlend);
land = mix(land, mix(dunes, mountains, standardBlend), smoothstep(0.99, 1.3, b));
float terrain = 0.0;
if(m_mars){
//same land sans water and with craters distributed evenly throughout the world
terrain = land;
float crater = craterNoise3D(p, m_radius, m_slope, m_rim_width, m_depth, m_crater_frequency, int(m_octaves));
terrain += crater;
//eliminates areas that would be marked for water...
terrain = mix(abs(simpleFBM(p * 50.0, 12) * .125), terrain, smoothstep(0.0, 0.1, terrain) );
} else{
terrain = mix(0.0, land, blendCoast);
}
float h = terrain;
h = max(0.0, h);
//float b = (abs(spherePos.y) + climateOffset)/abs(spherePos.y);

gl_FragData[0] = vec4(h,standardBlend,b,1.0);

}

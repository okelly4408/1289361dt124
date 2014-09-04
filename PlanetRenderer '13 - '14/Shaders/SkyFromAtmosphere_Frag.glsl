uniform vec3 m_v3LightPos;
uniform float m_fg;
uniform float m_fg2;
varying vec4 c0, c1;
varying vec3 v3Direction;
uniform float m_fExposure;
uniform float m_gamma;

vec4 HDR(vec4 L) {
    L = L * m_fExposure;
    L.r = L.r < 1.413 ? pow(L.r * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.r);
    L.g = L.g < 1.413 ? pow(L.g * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.g);
    L.b = L.b < 1.413 ? pow(L.b * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.b);
    L.a = L.a < 1.413 ? pow(L.a * 0.38317, 1.0 / 2.2) : 1.0 - exp(-L.a);
    return L;
}

void main (void)
{
	float fCos = dot(m_v3LightPos, v3Direction) / length(v3Direction);
	float fRayleighPhase = 0.75 * (2.0 + 0.5 * fCos*fCos);
	float fMiePhase = 1.5 * ((1.0 - m_fg2) / (2.0 + m_fg2)) * (1.0 + fCos*fCos) / pow(1.0 + m_fg2 - 2.0*m_fg*fCos, 1.5);
	gl_FragColor =  pow(HDR(( fRayleighPhase * c0 + fMiePhase * c1)), vec4(m_gamma));


}


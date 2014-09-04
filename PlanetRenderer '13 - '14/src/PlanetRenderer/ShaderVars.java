package PlanetRenderer;

import java.util.HashMap;
import java.util.Map;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

public class ShaderVars {
	
	static Map<String, Float> scattering_floats;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("Kr", 0.0055f);
		valuesByName.put("Km", 0.0010f);
		valuesByName.put("sun_brightness", 20f);
		valuesByName.put("g", -0.95f);
		valuesByName.put("scale_depth", 0.25f);
		
		scattering_floats = (valuesByName);
	}
	
	static Map<String, Vector3f> scattering_vec;
	static{
		Map<String, Vector3f> valuesByName = new HashMap<String, Vector3f>();
		valuesByName.put("wavelength", new Vector3f(0.710f, 0.570f, 0.475f));
		scattering_vec = valuesByName;
	}
	
	 private static int nSamples;           // Number of sample rays to use in integral equation
	    private static float Kr;               // Rayleigh scattering constant
	    private static float KrESun, Kr4PI;    // Kr * ESun, Kr * 4 * PI
	    private static float Km;               // Mie scattering constant
	    private static float KmESun, Km4PI;    // Km * ESun, Km * 4 * PI
	    private static float ESun;             // Sun brightness constant
	    private static float G;                // The Mie phase asymmetry factor
	    private static float innerRadius;      // Ground radius (outer radius is always 1.025 * innerRadius)
	    private static float scale;            // )1 / (outerRadius - innerRadius)
	    private static float scaleDepth;       // The scale depth (i.e. the altitude at which the atmosphere's average density is found)
	    private static float scaleOverScaleDepth; // scale / scaleDepth

	    private static float ATMscale;
	    private static float ATMscaleOverScaleDepth;
	    
	    private static Vector3f wavelength;
	    private static Vector3f invWavelength4; // 1 / pow(wavelength, 4) for the red, green, and blue channels
	    private static float exposure;
	

public static void initialize(float radius){
	innerRadius = radius;
	AtmosphereCalcs();
}
	
private static void AtmosphereCalcs() {
    nSamples = 25;
    Kr = scattering_floats.get("Kr");
    Km = scattering_floats.get("Km");
    ESun = scattering_floats.get("sun_brightness");
    exposure = 2f;
    
    // earth: 0.650f, 0.570f, 0.475f
    // martian: 0.709f, 0.776f, 0.795f
    wavelength = scattering_vec.get("wavelength");

    G = scattering_floats.get("g");            
    invWavelength4 = new Vector3f();
    scaleDepth = scattering_floats.get("scale_depth");        
    updateCalculations();
}

/**
 * Call this method after changing parameter values
 */
public static void updateCalculations() {
    ATMscale = 1.0f / ((innerRadius * 1.025f) - innerRadius);
    ATMscaleOverScaleDepth = ATMscale / scaleDepth;
    
    scale = 1.0f / ((innerRadius * 1.025f) - innerRadius);
    scaleOverScaleDepth = scale / scaleDepth;
    
    KrESun = Kr * ESun;
    KmESun = Km * ESun; 
    Kr4PI = Kr * 4.0f * FastMath.PI;
    Km4PI = Km * 4.0f * FastMath.PI;

    invWavelength4.x = 1.0f / FastMath.pow(wavelength.x, 4.0f);
    invWavelength4.y = 1.0f / FastMath.pow(wavelength.y, 4.0f);
    invWavelength4.z = 1.0f / FastMath.pow(wavelength.z, 4.0f);

}


public static float getRadius() { return innerRadius; }
public static int getnSamples() { return nSamples; }
public static float getfSamples() { return (float)nSamples; }
public static float getKr() { return Kr; }
public static float getKrESun() { return KrESun; }
public static float getKr4PI() { return Kr4PI; }
public static float getKm() { return Km; }
public static float getKmESun() { return KmESun; }
public static float getKm4PI() { return Km4PI; }
public static float getESun() { return ESun; }
public static float getG() { return G; }
public static float getInnerRadius() { return innerRadius; }
public static float getOuterRadius() { return (innerRadius) * 1.025f; }   
public static float getScale() { return scale; }
public static float getATMScale() { return ATMscale; }
public static float getScaleDepth() { return scaleDepth; }
public static float getScaleOverScaleDepth() { return scaleOverScaleDepth; }
public static float getATMScaleOverScaleDepth() { return ATMscaleOverScaleDepth; }
public static Vector3f getWavelength() { return wavelength; }
public static Vector3f getInvWavelength4() { return invWavelength4; }
public static float getExposure() { return exposure; }

}

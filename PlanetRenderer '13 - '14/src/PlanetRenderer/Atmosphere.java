package PlanetRenderer;

import java.util.HashMap;
import java.util.Map;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;

public class Atmosphere implements AppState{
static DirectionalLight sun;
private float radius;
private static Material skyFromSpace, groundFromSpace;
private static Material skyFromAtmosphere;
private Geometry atmosphere, clouds;
private Application mainClass;
private boolean initialized;
private static boolean inAtmosphere = false;

static Map<String, Float> sky_exposures_and_gammas;
static{
	Map<String, Float> valuesByName = new HashMap<String, Float>();

	valuesByName.put("afs_exposure", 3.0f);
	valuesByName.put("afs_gamma", 2.2f);
	
	valuesByName.put("afa_exposure", 3.0f);
	valuesByName.put("afa_gamma", 1.2f);
	sky_exposures_and_gammas = (valuesByName);
}

	public Atmosphere(DirectionalLight sun, float radius){
		Atmosphere.sun = sun;
		this.radius = radius;
	}

	
	
	public void makeSphere(){
		Mesh sp = new Sphere(256,256,ShaderVars.getOuterRadius());
		atmosphere = new Geometry("Mesh", sp);
		atmosphere.setMaterial(skyFromSpace);
		Mesh cl = new Sphere(256,256,radius * 1.01f);
		clouds = new Geometry("Mesh", cl);
		clouds.setMaterial(groundFromSpace);

	}
	
	public void update(float tpf){
		float camHeight = mainClass.getCamera().getLocation().length();
		Vector3f camLocation = mainClass.getCamera().getLocation();
		 if(camHeight > ShaderVars.getOuterRadius()){
			 	inAtmosphere = false;
	           skyFromSpace.setVector3("v3CameraPos", camLocation);
	           skyFromSpace.setFloat("fCameraHeight", camHeight);
	           skyFromSpace.setFloat("fCameraHeight2", (camHeight * camHeight));
	           skyFromSpace.setVector3("v3LightPos", sun.getDirection().normalize());
	           skyFromSpace.setFloat("fExposure", sky_exposures_and_gammas.get("afs_exposure"));
	   		   skyFromSpace.setFloat("gamma", sky_exposures_and_gammas.get("afs_gamma"));
	           atmosphere.setMaterial(skyFromSpace);
	           }else{
	        	  inAtmosphere = true;
	        	  skyFromAtmosphere.setVector3("v3CameraPos", camLocation);
	              skyFromAtmosphere.setFloat("fCameraHeight", camHeight);
	              skyFromAtmosphere.setFloat("fCameraHeight2", (camHeight * camHeight));
	              skyFromAtmosphere.setVector3("v3LightPos", sun.getDirection().normalize());
	              skyFromAtmosphere.setFloat("fExposure", sky_exposures_and_gammas.get("afa_exposure"));
	      		  skyFromAtmosphere.setFloat("gamma", sky_exposures_and_gammas.get("afa_gamma"));
	              atmosphere.setMaterial(skyFromAtmosphere);
	           }
		
	}
		
	public void initialize(AppStateManager stateManager, Application app) {
		this.mainClass = app;
		initialized = true;
		ShaderVars.initialize(radius);
		skyFromSpace = new Material(mainClass.getAssetManager(), "SkyFromSpace.j3md");
		loadShaderParams(skyFromSpace);
		skyFromSpace.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Front);
		skyFromAtmosphere = new Material(mainClass.getAssetManager(), "SkyFromAtmosphere.j3md");
		loadShaderParams(skyFromAtmosphere);
		skyFromAtmosphere.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Front);
		makeSphere();
		update(0);

		Node rootNode = (Node)app.getViewPort().getScenes().get(0);
		rootNode.attachChild(atmosphere);	
		
		
	}
public static void loadShaderParams(Material mat){
	if(inAtmosphere){
		mat.setFloat("fExposure", sky_exposures_and_gammas.get("afs_exposure"));
		mat.setFloat("gamma", sky_exposures_and_gammas.get("afs_gamma"));
	}else{
		mat.setFloat("fExposure", sky_exposures_and_gammas.get("afa_exposure"));
		mat.setFloat("gamma", sky_exposures_and_gammas.get("afa_gamma"));
	}
	mat.setVector3("v3LightPos", sun.getDirection().normalize());
	mat.setVector3("v3InvWavelength", ShaderVars.getInvWavelength4());
	mat.setFloat("fKrESun", ShaderVars.getKrESun());
	mat.setFloat("fKmESun", ShaderVars.getKmESun());
	mat.setFloat("fOuterRadius", ShaderVars.getOuterRadius());
	mat.setFloat("fInnerRadius", ShaderVars.getInnerRadius());
	mat.setFloat("fOuterRadius2", ShaderVars.getOuterRadius() * ShaderVars.getOuterRadius());
	mat.setFloat("fInnerRadius2", ShaderVars.getInnerRadius() * ShaderVars.getInnerRadius());
	mat.setFloat("fKr4PI", ShaderVars.getKr4PI());
	mat.setFloat("fKm4PI", ShaderVars.getKm4PI());
	mat.setFloat("fScale", ShaderVars.getATMScale());
	mat.setFloat("fScaleDepth", ShaderVars.getScaleDepth());
	mat.setFloat("fScaleOverScaleDepth", ShaderVars.getATMScaleOverScaleDepth());
	mat.setFloat("fSamples", ShaderVars.getfSamples());
	mat.setInt("nSamples", ShaderVars.getnSamples());
	mat.setFloat("fg", ShaderVars.getG());
	mat.setFloat("fg2", ShaderVars.getG() * ShaderVars.getG());
	mat.setFloat("fExposure", ShaderVars.getExposure());
	
	
}

public static void recompute(){
	loadShaderParams(skyFromSpace);
	loadShaderParams(skyFromAtmosphere);
}

	public boolean isEnabled() {
		return true;
	}

	public boolean isInitialized() {
		return initialized;
	}

	
	
	public void postRender() {
		
	}

	public void render(RenderManager r) {
		
	}

	public void setEnabled(boolean active) {
		
	}

	public void stateAttached(AppStateManager stateManager) {
	}

	public void stateDetached(AppStateManager arg0) {
		atmosphere.removeFromParent();
	}	
	public void cleanup() {
		
	}


}

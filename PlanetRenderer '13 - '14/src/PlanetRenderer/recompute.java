package PlanetRenderer;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.RendererException;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture2D;

public class ReCompute  implements AppState,ActionListener   {
	static float radius;
	static AssetManager assetManager;
	static RenderManager renderManager;
	static Camera c1;
	private Application mainClass;
	static int itexSize = (int)MainClass.ftexSize;
public ReCompute(float radius, AssetManager assetManager, RenderManager renderManager){
	ReCompute.radius = radius;
	ReCompute.assetManager = assetManager;
	ReCompute.renderManager = renderManager;
	c1 = new Camera(itexSize, itexSize);
	c1.setLocation(new Vector3f (0,0,1));
	c1.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);
}
		
public void onAction(String key, boolean keyPressed, float fps) {
	if(key.equals("Recompute") && !keyPressed){
		pConsole.console.output("creating new world...", pConsole.yellow);
		recompute();
		pConsole.console.output("new world created with seed "+HeightMap.seed, pConsole.green);
	}
	 if(key.equals("Reload") && !keyPressed){
		pConsole.console.output("recompiling shaders...", pConsole.yellow);
		reloadShaders();
		pConsole.console.output("recompiled shaders successfully", pConsole.green);
	}
}
public void setUpKeys(){
	mainClass.getInputManager().addMapping("Recompute", new KeyTrigger(KeyInput.KEY_R));
	mainClass.getInputManager().addMapping("Reload", new KeyTrigger(KeyInput.KEY_G));
	mainClass.getInputManager().addListener(this, 
			 "Reload", "Recompute");
}

public static void recompute(){
	HeightMap.initialize(radius);
	for(int i = 0; i<QuadMesh.list.size(); i++){
	int t = 0;
	QuadMesh q = QuadMesh.list.get(i);
	HeightMap hm = new HeightMap();
	ViewPort vp = renderManager.createPreView("Viewport", c1);
	if(q.face.equals(QuadMesh.faces.TOP.toString()) || q.face.equals(QuadMesh.faces.BOTTOM.toString()))
		t = 1;
		
	if(q.face.equals(QuadMesh.faces.FRONT.toString()) || q.face.equals(QuadMesh.faces.BACK.toString()))
		t = 2;
		
	if(q.face.equals(QuadMesh.faces.RIGHT.toString()) || q.face.equals(QuadMesh.faces.LEFT.toString()))
		t = 3;
		
	Texture2D hm1 = hm.getHeightMap1(t, assetManager, vp, itexSize, q);
	q.Heightmap.getImage().dispose();
	q.Heightmap = hm1;
	LOD_Control.frame_count  = 0;
	}
	System.gc();
}

public static void recompute(long seed){
	HeightMap.initialize(radius, seed);
	for(int i = 0; i<QuadMesh.list.size(); i++){
	int t = 0;
	QuadMesh q = QuadMesh.list.get(i);
	HeightMap hm = new HeightMap();
	ViewPort vp = renderManager.createPreView("Viewport", c1);
	if(q.face.equals(QuadMesh.faces.TOP.toString()) || q.face.equals(QuadMesh.faces.BOTTOM.toString()))
		t = 1;
		
	if(q.face.equals(QuadMesh.faces.FRONT.toString()) || q.face.equals(QuadMesh.faces.BACK.toString()))
		t = 2;
		
	if(q.face.equals(QuadMesh.faces.RIGHT.toString()) || q.face.equals(QuadMesh.faces.LEFT.toString()))
		t = 3;
		
	Texture2D hm1 = hm.getHeightMap1(t, assetManager, vp, itexSize, q);
	q.Heightmap.getImage().dispose();
	q.Heightmap = hm1;
	LOD_Control.frame_count  = 0;
	}
	
	System.gc();
}

public static void recomputeGroundScattering(){
	for(int i = 0; i<QuadMesh.list.size(); i++){
		loadShaderParams(QuadMesh.list.get(i).mesh.getMaterial());
	}
}

private static void loadShaderParams(Material mat){
 	mat.setVector3("v3LightPos", MainClass.sun.getDirection().normalize());
    mat.setVector3("v3InvWavelength", ShaderVars.getInvWavelength4());
    mat.setFloat("fKrESun", ShaderVars.getKrESun());
    mat.setFloat("fKmESun", ShaderVars.getKmESun());
    mat.setFloat("fOuterRadius", ShaderVars.getOuterRadius());
    mat.setFloat("fOuterRadius2", ShaderVars.getOuterRadius() * ShaderVars.getOuterRadius());
    mat.setFloat("fInnerRadius", radius);
    mat.setFloat("fInnerRadius2", ShaderVars.getInnerRadius() * ShaderVars.getInnerRadius());
    mat.setFloat("fKr4PI", ShaderVars.getKr4PI());
    mat.setFloat("fKm4PI", ShaderVars.getKm4PI());
    mat.setFloat("fScale", ShaderVars.getScale());
    mat.setFloat("fScaleDepth", ShaderVars.getScaleDepth());
    mat.setFloat("fScaleOverScaleDepth", ShaderVars.getScaleOverScaleDepth());
    mat.setFloat("fSamples", ShaderVars.getfSamples());
    mat.setInt("nSamples", ShaderVars.getnSamples());
    mat.setFloat("fExposure", ShaderVars.getExposure());
    mat.setBoolean("debugColor", LOD_Control.debugColor);
    mat.setBoolean("hmView", LOD_Control.hmView);
    mat.setBoolean("mars", LOD_Control.mars);
}

private static void loadShaderParams(Material mat, QuadMesh q){
	loadCamera(mat);
 	mat.setVector3("v3LightPos", MainClass.sun.getDirection().normalize());
    mat.setVector3("v3InvWavelength", ShaderVars.getInvWavelength4());
    mat.setFloat("fKrESun", ShaderVars.getKrESun());
    mat.setFloat("fKmESun", ShaderVars.getKmESun());
    mat.setFloat("fOuterRadius", ShaderVars.getOuterRadius());
    mat.setFloat("fOuterRadius2", ShaderVars.getOuterRadius() * ShaderVars.getOuterRadius());
    mat.setFloat("fInnerRadius", radius);
    mat.setFloat("fInnerRadius2", ShaderVars.getInnerRadius() * ShaderVars.getInnerRadius());
    mat.setFloat("fKr4PI", ShaderVars.getKr4PI());
    mat.setFloat("fKm4PI", ShaderVars.getKm4PI());
    mat.setFloat("fScale", ShaderVars.getScale());
    mat.setFloat("fScaleDepth", ShaderVars.getScaleDepth());
    mat.setFloat("fScaleOverScaleDepth", ShaderVars.getScaleOverScaleDepth());
    mat.setFloat("fSamples", ShaderVars.getfSamples());
    mat.setInt("nSamples", ShaderVars.getnSamples());
    mat.setFloat("fExposure", ShaderVars.getExposure());
    mat.setBoolean("debugColor", LOD_Control.debugColor);
    mat.setBoolean("hmView", LOD_Control.hmView);
    mat.setBoolean("mars", LOD_Control.mars);
    mat.setTexture("HeightMap", q.Heightmap);
    mat.setFloat("intensity", q.intensity);
	mat.setVector3("Color", q.color);
	mat.setMatrix4("cubeMatrix", q.cubeMatrix);
	mat.setVector3("meshOffset", q.meshOffset);
	mat.setFloat("scale", q.width/(radius * 2)); 
	mat.setFloat("size", q.width);
	mat.getAdditionalRenderState().setWireframe(MainClass.useWireframe);
	if(mat.getMaterialDef().getName().equals("GroundFromSpace")){
	mat.setFloat("gamma", LOD_Control.ground_exposures_and_gammas.get("gfs_gamma"));
	mat.setFloat("fExposure", LOD_Control.ground_exposures_and_gammas.get("gfs_exposure"));
	}else{
	mat.setFloat("gamma", LOD_Control.ground_exposures_and_gammas.get("gfa_gamma"));
	mat.setFloat("fExposure", LOD_Control.ground_exposures_and_gammas.get("gfa_exposure"));
	}
	mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
	

}
public static void reloadShaders(){
	for(int i = 0; i<QuadMesh.list.size(); i++){
		QuadMesh q = QuadMesh.list.get(i);
		Material old = q.mesh.getMaterial();
		Material m = reloadMaterial(old,q);
	    q.mesh.setMaterial(m);
	}
}
public static void loadCamera(Material m){
	m.setFloat("fCameraHeight", MainClass.getInstance().getCamera().getLocation().length());
	m.setFloat("fCameraHeight2", MainClass.getInstance().getCamera().getLocation().lengthSquared());
	m.setVector3("v3CameraPos", MainClass.getInstance().getCamera().getLocation());
	m.setVector3("v3CamDir", MainClass.getInstance().getCamera().getDirection());
}


private static Material reloadMaterial(Material mat, QuadMesh q) {
   ((DesktopAssetManager) assetManager).clearCache();
    Material dummy = new Material(mat.getMaterialDef());
    loadShaderParams(dummy, q);
    Geometry dummyGeom = new Geometry("dummyGeom",new Box(1f, 1f, 1f));
    dummyGeom.setMaterial(dummy);
    try {
        renderManager.preloadScene(dummyGeom);
    } catch (RendererException e) {
        System.err.println(e.getMessage());
        return null;
    }
    return dummy;
}


	@Override
	public void cleanup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(AppStateManager arg0, Application app) {
			this.mainClass = app;
			setUpKeys();
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void postRender() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(RenderManager arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEnabled(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateAttached(AppStateManager arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateDetached(AppStateManager arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(float arg0) {
		// TODO Auto-generated method stub
		
	}

}

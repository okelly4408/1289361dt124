package PlanetRenderer;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.Camera.FrustumIntersect;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;
import com.jme3.util.BufferUtils;
import com.jme3.util.Screenshots;


public class LOD_Control implements AppState {
private boolean initialized;

private float ftexSize = MainClass.ftexSize;
private int itexSize = (int)ftexSize;
static DirectionalLight sun;
static boolean debugColor = false, hmView = false, lodOn = true;
private Geometry surface;
private float min_size = 8f;
private Application mainClass;
private boolean started = false;
private Material groundFromSpace, groundFromSky;
private AssetManager assetManager;
private RenderManager renderManager;
private Node rootNode;
private Future<QuadMesh> future = null;
private Camera c1;
private QuadMesh p, qReturned;
private float radius;
private String path = "GroundFromSpace.j3md";
static boolean mars = false;
static Map<String, Float> ground_exposures_and_gammas;
static{
	Map<String, Float> valuesByName = new HashMap<String, Float>();

	valuesByName.put("gfs_exposure", 4.0f);
	valuesByName.put("gfs_gamma", 1.5f);
	
	valuesByName.put("gfa_exposure", 3.0f);
	valuesByName.put("gfa_gamma", 1.5f);
	ground_exposures_and_gammas = (valuesByName);
}

	/** manages the level of detail as well as creating the mesh and materials for the ShaderVars surface
	 * @param ShaderVars must be called to supply LOD with initial quads
	 */

	public LOD_Control(float radius, DirectionalLight sun){
		LOD_Control.sun = sun;
		this.radius = radius;
		c1 = new Camera(itexSize, itexSize);
		c1.setLocation(new Vector3f (0,0,1));
		c1.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);

	}
	static int frame_count = 0;

	public void update(float tpf) {
		
		if(future != null){
    		if(future.isDone()){
    			try {
    				if(future.get() != null){
					qReturned = (QuadMesh) future.get();
    				}
					if(qReturned != null && qReturned.hasChildren){
	    	    	    rootNode.attachChild(qReturned.children.get(0).mesh);
					    rootNode.attachChild(qReturned.children.get(1).mesh);
					    rootNode.attachChild(qReturned.children.get(2).mesh);
					    rootNode.attachChild(qReturned.children.get(3).mesh); 
					/*	SimpleBatchNode batch = new SimpleBatchNode();
						batch.attachChild(qReturned.children.get(0).mesh);
						batch.attachChild(qReturned.children.get(1).mesh);
						batch.attachChild(qReturned.children.get(2).mesh);
						batch.attachChild(qReturned.children.get(3).mesh); */
						

					    qReturned.children.get(0).isBeingSplit = false;
					    qReturned.children.get(1).isBeingSplit = false;
					    qReturned.children.get(2).isBeingSplit = false;
					    qReturned.children.get(3).isBeingSplit = false;
					    
					    QuadMesh.list.add(qReturned.children.get(0));
					    QuadMesh.list.add(qReturned.children.get(1));
					    QuadMesh.list.add(qReturned.children.get(2));
					    QuadMesh.list.add(qReturned.children.get(3));
					    
					    qReturned.isBeingSplit = false;
					    p.isBeingSplit = false;
					    rootNode.detachChild(qReturned.mesh); 
					    qReturned = null;
					    future = null;
					    started = false;
					    frame_count = 1;
					    
	    	    }
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	}
		
		 Vector3f camPos = mainClass.getCamera().getLocation();
		
		float camHeight = camPos.length();
		
		if(camHeight > ShaderVars.getOuterRadius()){	
			path = "GroundFromSpace.j3md";
		}else{
			path = "GroundFromAtmosphere.j3md";
			} 
		
			 for(int i = 0; i<QuadMesh.list.size(); i++){
				 
				 if(mainClass.getRenderManager().getPreViews().size() > i && frame_count > 1){
					 
						ViewPort vp = mainClass.getRenderManager().getPreViews().get(i);
	//					getCenterOffset(vp.getOutputFrameBuffer());
						vp.clearScenes();	
						mainClass.getRenderManager().removePreView(vp);
					}  
				
				 float iOverD = (float) 1E8;
			    	QuadMesh q = QuadMesh.list.get(i);  
			    	float distance = q.center.distance(mainClass.getCamera().getLocation()); // this gets the distance from the center of the quad and the camera
			    	iOverD = distance/q.arcLengthOverSize;
				
		    	if(!q.isBeingSplit){
			 		if(camHeight > ShaderVars.getOuterRadius() && q.inAtmosphere){
			 			
			 			q.inAtmosphere = false;
			 			Material m = makeMaterial(path);
			 			loadShaderParams(m);
			 			q.mesh.setMaterial(m);
			 		      q.mesh.getMaterial().setFloat("intensity", q.intensity);
					    	q.mesh.getMaterial().setVector3("Color", q.color);
					    	q.mesh.getMaterial().setMatrix4("cubeMatrix", q.cubeMatrix);
					    	q.mesh.getMaterial().setVector3("meshOffset", q.meshOffset);
					    	q.mesh.getMaterial().setFloat("scale", q.width/(radius * 2)); 
			 		}else if(camHeight < ShaderVars.getOuterRadius() && !q.inAtmosphere){
			 			q.inAtmosphere = true;
			 			Material m = makeMaterial(path);
			 			loadShaderParams(m);
			 			q.mesh.setMaterial(m);
			 		      q.mesh.getMaterial().setFloat("intensity", q.intensity);
					    	q.mesh.getMaterial().setVector3("Color", q.color);
					    	q.mesh.getMaterial().setMatrix4("cubeMatrix", q.cubeMatrix);
					    	q.mesh.getMaterial().setVector3("meshOffset", q.meshOffset);
					    	q.mesh.getMaterial().setFloat("scale", q.width/(radius * 2)); 
			 		}
			 		if(path.equals("GroundFromAtmosphere.j3md")){
			 			q.mesh.getMaterial().setFloat("fExposure", ground_exposures_and_gammas.get("gfa_exposure"));
			 			q.mesh.getMaterial().setFloat("gamma", ground_exposures_and_gammas.get("gfa_gamma"));
			 		}else{
			 			q.mesh.getMaterial().setFloat("fExposure", ground_exposures_and_gammas.get("gfs_exposure"));
			 			q.mesh.getMaterial().setFloat("gamma", ground_exposures_and_gammas.get("gfs_gamma"));
			 		}
			 		if(q.mesh.getMaterial() != null && q.Heightmap != null){
			 		
			     	q.mesh.getMaterial().setVector3("v3CameraPos", camPos);
			    	q.mesh.getMaterial().setVector3("v3CamDir", mainClass.getCamera().getDirection());
			    	q.mesh.getMaterial().setFloat("fCameraHeight", camHeight);
			    	q.mesh.getMaterial().setFloat("fCameraHeight2", camHeight * camHeight);
			    	q.mesh.getMaterial().setVector3("v3LightPos", sun.getDirection());
			    	q.mesh.getMaterial().setTexture("HeightMap", q.Heightmap);
			    	q.mesh.getMaterial().setBoolean("debugColor", debugColor);
			    	q.mesh.getMaterial().setBoolean("hmView", hmView);
			    	q.mesh.getMaterial().setBoolean("mars", mars);
			    	q.mesh.getMaterial().getAdditionalRenderState().setWireframe(MainClass.useWireframe); 

			 		}
		    	}
		    	
				
			    if(lodOn){
			    	
			    	if(MainClass.checkFrustum){
			    		if(!inFrustum(q, 0) && q.inFrustum){
			    			q.inFrustum = false;
			    			detachChildren(q);
			    		}else if(inFrustum(q, 0) && !q.inFrustum){
			    			q.inFrustum = true;
			    			attachChildren(q);	
			    		}
			    	} 
		    if(MainClass.lodView >= iOverD 
		    		&& !q.hasChildren 
		    		&& q.width > (min_size)
		    		&& mainClass.getRenderManager().getPreViews().size() < 4
		    		&& !started
		    		&& inFrustum(q)){

		    	try{
	    	        if(qReturned == null && future == null){
	    	        	q.isBeingSplit = true;
	    	        	started = true;
	    	        	p = q;
	    	        	p.isBeingSplit = true;
	    	            future = MainClass.executor.submit(getQuad);
	    	        }
	    	        
	    	    } 
	    	    catch(Exception e){ 
	    	    }
	    	   
		    	
			    }  else if(MainClass.lodView < iOverD 
			    		&& q.hasChildren 
			    		&& q.parent !=null 
			    		&& !q.children.get(0).hasChildren
			    		&& !q.children.get(1).hasChildren
			    		&& !q.children.get(2).hasChildren
			    		&& !q.children.get(3).hasChildren
			    		&& future == null
			    		&& !q.isBeingSplit){
			    	
			    	
			QuadMesh q1 = q.children.get(0);
			QuadMesh q2 = q.children.get(1);
			QuadMesh q3 = q.children.get(2);
			QuadMesh q4 = q.children.get(3);
			  q1.Heightmap.getImage().dispose();
			  q2.Heightmap.getImage().dispose();
			  q3.Heightmap.getImage().dispose();
			  q4.Heightmap.getImage().dispose();  
			   
		            QuadMesh.list.remove(q1);
		            QuadMesh.list.remove(q2);
		            QuadMesh.list.remove(q3);
		            QuadMesh.list.remove(q4);

			    	rootNode.detachChild(q1.mesh);
			    	rootNode.detachChild(q2.mesh);
			    	rootNode.detachChild(q3.mesh);
			    	rootNode.detachChild(q4.mesh);
			    	

			    	q.children.clear();
			    	q.hasChildren = false;
			    	rootNode.attachChild(q.mesh);
			    	
			    	
			    	
			    } 
				 } 
		    } frame_count++;
		    
		
	} 
		
	
	private void loadShaderParams(Material mat){
		if(path.equals("GroundFromAtmosphere.j3md")){
 			mat.setFloat("fExposure", ground_exposures_and_gammas.get("gfa_exposure"));
 			mat.setFloat("gamma", ground_exposures_and_gammas.get("gfa_gamma"));
 		}else{
 			mat.setFloat("fExposure", ground_exposures_and_gammas.get("gfs_exposure"));
 			mat.setFloat("gamma", ground_exposures_and_gammas.get("gfs_gamma"));
 		}
		 	mat.setVector3("v3LightPos", sun.getDirection());
		 	mat.setVector3("v3CamDir", mainClass.getCamera().getDirection());
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
		    mat.setBoolean("debugColor", debugColor);
		    mat.setBoolean("hmView", hmView);
		    mat.setBoolean("mars", mars);
		
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app) {
		
		initialized = true;
		this.mainClass = app;
		this.assetManager = mainClass.getAssetManager();
		this.renderManager = mainClass.getRenderManager();
		this.rootNode = (Node)app.getViewPort().getScenes().get(0);
		@SuppressWarnings("unused")
		Planet planet = new Planet(radius, assetManager, renderManager, ftexSize);
		ShaderVars.initialize(radius);
		loadShaderParams(QuadMesh.list.get(0).mesh.getMaterial());
		loadShaderParams(QuadMesh.list.get(1).mesh.getMaterial());
		loadShaderParams(QuadMesh.list.get(2).mesh.getMaterial());
		loadShaderParams(QuadMesh.list.get(3).mesh.getMaterial());
		loadShaderParams(QuadMesh.list.get(4).mesh.getMaterial());
		loadShaderParams(QuadMesh.list.get(5).mesh.getMaterial()); 
		
		rootNode.attachChild(QuadMesh.list.get(0).mesh);
		rootNode.attachChild(QuadMesh.list.get(1).mesh);
		rootNode.attachChild(QuadMesh.list.get(2).mesh);
		rootNode.attachChild(QuadMesh.list.get(3).mesh);
		rootNode.attachChild(QuadMesh.list.get(4).mesh);
		rootNode.attachChild(QuadMesh.list.get(5).mesh); 

		groundFromSpace = new Material(mainClass.getAssetManager(), "GroundFromSpace.j3md");
		loadShaderParams(groundFromSpace);
		groundFromSky = new Material(mainClass.getAssetManager(), "GroundFromAtmosphere.j3md");
		groundFromSky.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		loadShaderParams(groundFromSky);
	}
	
	

//util methods
	
public boolean inFrustum(QuadMesh q){
	if(MainClass.checkFrustum){
	FrustumIntersect s = mainClass.getCamera().contains(q.sphere);
	
	if(s.equals(FrustumIntersect.Inside) || s.equals(FrustumIntersect.Intersects)){
		return true;
	}else{
		return false;
		}
	}else{ return true; }
}

public boolean inFrustum(QuadMesh q, int i){	
	
	//FrustumIntersect s = MainClass.getInstance().getCamera().contains(q.sphere);
FrustumIntersect s = mainClass.getCamera().contains(q.sphere);
	if(s.equals(FrustumIntersect.Inside) || s.equals(FrustumIntersect.Intersects) ){
		
		return true;
	}else{
		q.color = new Vector3f(1,0,0);
		q.mesh.getMaterial().setVector3("Color", q.color);
		return false;
		}

	}


private Material makeMaterial(String p){
	Material mat = new Material(assetManager, p);
	loadShaderParams(mat);
	mat.getAdditionalRenderState().setWireframe(MainClass.useWireframe);
	mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
	
	return mat;
}




private Callable<QuadMesh> getQuad = new Callable<QuadMesh>(){

public QuadMesh call() throws Exception { 

boolean inAtmosphere;
Vector3f camPos = mainClass.getCamera().getLocation();
float camHeight = camPos.length();
	if(camHeight > ShaderVars.getOuterRadius()){
		inAtmosphere = false;
	}else{
		inAtmosphere = true;
	}
	Matrix4f mat = new Matrix4f();

	int t = 0;
	if(p.face.equals(QuadMesh.faces.TOP.toString()) || p.face.equals(QuadMesh.faces.BOTTOM.toString())){
		t = 1;
		mat.m00 = 1;
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m03 = p.faceIndex.x;
		
		mat.m10 = 0;
		mat.m11 = 0;
		mat.m12 = 0;
		mat.m13 = p.faceIndex.y;
		
		mat.m20 = 0;
		mat.m21 = 0;
		mat.m22 = 1;
		mat.m23 = p.faceIndex.z;
		
		mat.m30 = 0;
		mat.m31 = 0;
		mat.m32 = 0;
		mat.m33 = 0;
		
		}
	if(p.face.equals(QuadMesh.faces.FRONT.toString()) || p.face.equals(QuadMesh.faces.BACK.toString())){
		t = 2;
		mat.m00 = 1;
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m03 = p.faceIndex.x;
		
		mat.m10 = 0;
		mat.m11 = 0;
		mat.m12 = 1;
		mat.m13 = p.faceIndex.y;
		
		mat.m20 = 0;
		mat.m21 = 0;
		mat.m22 = 0;
		mat.m23 = p.faceIndex.z;
		
		mat.m30 = 0;
		mat.m31 = 0;
		mat.m32 = 0;
		mat.m33 = 0;
		}
	if(p.face.equals(QuadMesh.faces.RIGHT.toString()) || p.face.equals(QuadMesh.faces.LEFT.toString())){
		t = 3;
		mat.m00 = 0;
		mat.m01 = 0;
		mat.m02 = 0;
		mat.m03 = p.faceIndex.x;
		
		mat.m10 = 1;
		mat.m11 = 0;
		mat.m12 = 0;
		mat.m13 = p.faceIndex.y;
		
		mat.m20 = 0;
		mat.m21 = 0;
		mat.m22 = 1;
		mat.m23 = p.faceIndex.z;
		
		mat.m30 = 0;
		mat.m31 = 0;
		mat.m32 = 0;
		mat.m33 = 0;
		
		}
	
    	QuadMesh.split(p,p.index1,p.index2);

    	QuadMesh q1 = p.children.get(0);
    	QuadMesh q2 = p.children.get(1);
    	QuadMesh q3 = p.children.get(2);
    	QuadMesh q4 = p.children.get(3);
    	float scale = (p.width / 2f)/(radius * 2);
    	q1.inAtmosphere = inAtmosphere;
    	q2.inAtmosphere = inAtmosphere;
    	q3.inAtmosphere = inAtmosphere;
    	q4.inAtmosphere = inAtmosphere;
    	
    	q1.cubeMatrix = mat;
    	q2.cubeMatrix = mat;
    	q3.cubeMatrix = mat;
    	q4.cubeMatrix = mat;
    	
    	Material m1 = makeMaterial(path);
    	Material m2 = makeMaterial(path);
    	Material m3 = makeMaterial(path);
    	Material m4 = makeMaterial(path);

    	ViewPort vp1 = renderManager.createPreView("View1", c1);
    	ViewPort vp2 = renderManager.createPreView("View2", c1);
    	ViewPort vp3 = renderManager.createPreView("View3", c1);
    	ViewPort vp4 = renderManager.createPreView("View4", c1);
    	HeightMap hm1 = new HeightMap();
    	q1.Heightmap = hm1.getHeightMap1(t, assetManager,vp1,itexSize, q1);
   	    q2.Heightmap = hm1.getHeightMap1(t, assetManager,vp2,itexSize, q2);
    	q3.Heightmap = hm1.getHeightMap1(t, assetManager,vp3,itexSize, q3);
    	q4.Heightmap = hm1.getHeightMap1(t, assetManager,vp4,itexSize, q4);
    	
    	 q1.mesh.setMaterial(m1);
    	 m1.setVector3("Color", new Vector3f(1,1,1));
    	 m1.setFloat("size", q1.width);
    	 m1.setFloat("intensity", q1.intensity);
     	 m1.setTexture("HeightMap", q1.Heightmap);
    	 m1.setMatrix4("cubeMatrix", mat);
    	 m1.setVector3("meshOffset", q1.meshOffset);
    	 m1.setFloat("scale", scale);

    	 
    	 q2.mesh.setMaterial(m2);
		 m2.setVector3("Color", new Vector3f(1, 0,0));    
		 m2.setFloat("size", q2.width);
    	 m2.setFloat("intensity", q1.intensity);
		 m2.setTexture("HeightMap", q2.Heightmap);
		 m2.setMatrix4("cubeMatrix", mat);
    	 m2.setVector3("meshOffset", q2.meshOffset);
    	 m2.setFloat("scale", scale);
		    			   			   
		 q3.mesh.setMaterial(m3);
		 m3.setVector3("Color", new Vector3f(0f, 0,1f));
		 m3.setFloat("size", q3.width);
    	 m3.setFloat("intensity", q1.intensity);
		 m3.setTexture("HeightMap", q3.Heightmap);
		 m3.setMatrix4("cubeMatrix", mat);
    	 m3.setVector3("meshOffset", q3.meshOffset);
    	 m3.setFloat("scale", scale);
		   						    
		 q4.mesh.setMaterial(m4);
		 m4.setVector3("Color", new Vector3f(0,1f,0));	    
	     m4.setFloat("size", q4.width);
    	 m4.setFloat("intensity", q1.intensity);
		 m4.setTexture("HeightMap", q4.Heightmap);
		 m4.setMatrix4("cubeMatrix", mat);
    	 m4.setVector3("meshOffset", q4.meshOffset);
    	 m4.setFloat("scale", scale);
    	 
    	 
    	 qReturned = p;
    	 
return qReturned;
}

};



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
	surface.removeFromParent();
}	
public void cleanup() {
	
}
public void detachChildren(QuadMesh q){
	if(q.hasChildren){
		detachChildren(q.children.get(0));
		detachChildren(q.children.get(1));
		detachChildren(q.children.get(2));
		detachChildren(q.children.get(3));
	}else{
		rootNode.detachChild(q.mesh);
	}
}
public void attachChildren(QuadMesh q){
	//only attach leaf nodes
	if(q.hasChildren){
		attachChildren(q.children.get(0));
		attachChildren(q.children.get(1));
		attachChildren(q.children.get(2));
		attachChildren(q.children.get(3));
	}else{
		rootNode.attachChild(q.mesh);
	}
}
public float getCenterOffset(FrameBuffer fbo){
	ByteBuffer bb = BufferUtils.createByteBuffer(266256);
	renderManager.getRenderer().readFrameBuffer(fbo, bb);
	BufferedImage awtImage = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
	Screenshots.convertScreenShot(bb, awtImage);
     try {
         ImageIO.write(awtImage, "png", new File(System.currentTimeMillis()+".png"));
     } catch (IOException ex){
     }   
     
     return 0;
     
}



}

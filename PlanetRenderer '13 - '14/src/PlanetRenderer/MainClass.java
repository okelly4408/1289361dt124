package PlanetRenderer;



import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.SimpleBatchNode;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.debug.WireFrustum;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Image;
import com.jme3.texture.TextureCubeMap;

import de.lessvoid.nifty.Nifty;

public class MainClass extends SimpleApplication{

private static MainClass inst;
static boolean checkFrustum = true;
static DirectionalLight sun;
static float radius = 100000;  
static float lodView = 40f;
static float intensity = 14.2f;
static float ftexSize = 258;
static boolean atmosphereOn = true;
private Atmosphere atm;
static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() - 1);
static int speed = 1500;
static NiftyJmeDisplay niftydisplay;
private static int frame_height;
static BitmapText speedText, seedText, distanceText, lodText, frustText;
private boolean textOn = true;
static boolean useWireframe;
static SimpleBatchNode batch;
private  float angle = 3.14159f;
private static AppSettings settings;
private ReCompute re;
private  Vector3f lightDir = new Vector3f( (-FastMath.sin(angle) * radius), FastMath.HALF_PI, FastMath.cos(angle) * radius).normalize();
boolean guiOn = false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		settings = new AppSettings(true);
		MainClass app =  MainClass.getInstance();
		app.setSettings(settings);
		//full screen = 2880 x 1800
		settings.setWidth(1024);
		settings.setHeight(768);
		settings.setSamples(8);
		settings.setStereo3D(false);
		settings.setDepthBits(24);
		frame_height = settings.getHeight();
		settings.setTitle("PlanetRenderer-v1.1-BETA");
		app.setShowSettings(false);
		app.start();

	}
public void simpleInitApp(){

	rootNode.setCullHint(CullHint.Never);
	sun();
	camera();
	//setupSkyBox("blue-glow-1024");

	atm = new Atmosphere(sun, radius);
	re = new ReCompute(radius, assetManager, renderManager);
	stateManager.attach(new LOD_Control(radius, sun));
	stateManager.attach(atm);
	stateManager.attach(re);

			niftydisplay = new NiftyJmeDisplay(
			assetManager, inputManager, audioRenderer, guiViewPort, 2048, 2048);
			
			
			Nifty nifty = niftydisplay.getNifty();
			pConsole c = new pConsole();
			stateManager.attach(c);
			nifty.registerScreenController(c);
			nifty.fromXml("console.xml", "start", c);
	
	speedText = new BitmapText(guiFont, false);          
	speedText.setSize(guiFont.getCharSet().getRenderedSize());      
	speedText.setColor(ColorRGBA.White);                             
	speedText.setText("Speed: "+speed);             
	speedText.setLocalTranslation(0, frame_height, 0);
	
	seedText = new BitmapText(guiFont, false);          
	seedText.setSize(guiFont.getCharSet().getRenderedSize());      
	seedText.setColor(ColorRGBA.White);                             
	seedText.setText("Seed: "+HeightMap.seed);             
	seedText.setLocalTranslation(0, frame_height-guiFont.getCharSet().getRenderedSize(), 0);
	
	distanceText = new BitmapText(guiFont, false);          
	distanceText.setSize(guiFont.getCharSet().getRenderedSize());      
	distanceText.setColor(ColorRGBA.White);                             
	distanceText.setText("Altitude: "+HeightMap.seed);             
	distanceText.setLocalTranslation(0, frame_height-(guiFont.getCharSet().getRenderedSize() * 2), 0);
	
	lodText = new BitmapText(guiFont, false);          
	lodText.setSize(guiFont.getCharSet().getRenderedSize());      
	lodText.setColor(ColorRGBA.White);                             
	lodText.setText("LOD: "+lodView);             
	lodText.setLocalTranslation(0, frame_height-(guiFont.getCharSet().getRenderedSize() * 3), 0);
	
	frustText = new BitmapText(guiFont, false);          
	frustText.setSize(guiFont.getCharSet().getRenderedSize());      
	frustText.setColor(ColorRGBA.White);                             
	frustText.setText("Frustum Cull: "+checkFrustum);             
	frustText.setLocalTranslation(0, frame_height-(guiFont.getCharSet().getRenderedSize() * 4), 0);
	
	guiNode.attachChild(lodText);
	guiNode.attachChild(speedText);
	guiNode.attachChild(seedText);
	guiNode.attachChild(distanceText);
	guiNode.attachChild(frustText);
	addKeyBindings();
	

	
	 

}

private void sun(){
	sun = new DirectionalLight();
	sun.setColor(ColorRGBA.White);
	sun.setDirection(lightDir);
	rootNode.addLight(sun);
}

public void setupSkyBox(String name) {
    Mesh sphere = new Sphere(32, 32, 10f);
    sphere.setStatic();
    Spatial geometry = new Geometry("SkyBox", sphere);
    geometry.setQueueBucket(Bucket.Sky);
    geometry.setShadowMode(ShadowMode.Off);

    Image cube = assetManager.loadTexture("blue-glow-1024.dds").getImage();
    TextureCubeMap cubemap = new TextureCubeMap(cube);

    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Sky.j3md");
    mat.setBoolean("SphereMap", false);
    mat.setTexture("Texture", cubemap);
    mat.setVector3("NormalScale", new Vector3f(1, 1, 1));
    geometry.setMaterial(mat);
    
    rootNode.attachChild(geometry);
    

}


private void camera(){
	cam.setFrustumFar(radius * 2.5f);
	flyCam.setMoveSpeed(speed);
	cam.setLocation(new Vector3f(radius * 2, 0, radius));
	cam.setRotation(new Quaternion(0.0f, 0.90515524f, 0.0f, -0.4250812f));
	renderManager.setCamera(cam, false);
	
	
}

private Geometry createFrustum(Vector3f[] pts,AssetManager assetManager){
updateFrustumPoints2(pts);
WireFrustum frustum = new WireFrustum(pts);
frustum.setLineWidth(20);
Geometry frustumMdl = new Geometry("f", frustum);
frustumMdl.setCullHint(Spatial.CullHint.Never);
frustumMdl.setShadowMode(ShadowMode.Off);
frustumMdl.setMaterial(new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md"));
frustumMdl.getMaterial().setColor("Color", ColorRGBA.Yellow);
frustumMdl.getMaterial().getAdditionalRenderState().setWireframe(false);
rootNode.attachChild(frustumMdl);
return frustumMdl;
}

public static MainClass getInstance() {
    if (inst== null) {
        inst = new MainClass();
    }
    return inst;
}
private void addKeyBindings() {
	inputManager.addMapping("GUIToggle", new KeyTrigger(KeyInput.KEY_GRAVE), new KeyTrigger(KeyInput.KEY_SLASH));
	inputManager.addMapping("UpLOD", new KeyTrigger(KeyInput.KEY_0));
	inputManager.addMapping("DownLOD", new KeyTrigger(KeyInput.KEY_9));
	inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_P));
	inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_L));
	inputManager.addMapping("SunUp", new KeyTrigger(KeyInput.KEY_O));
	inputManager.addMapping("SunDown", new KeyTrigger(KeyInput.KEY_K));
	inputManager.addMapping("wireframeSwitch", new KeyTrigger(KeyInput.KEY_M));
	inputManager.addMapping("textSwitch", new KeyTrigger(KeyInput.KEY_F5));
    inputManager.addListener(actionListener,"Up", "Down", "textSwitch", "wireframeSwitch", "UpLOD", "DownLOD");
    inputManager.addMapping("SpeedUP", new KeyTrigger(KeyInput.KEY_4));
    inputManager.addMapping("SpeedDOWN", new KeyTrigger(KeyInput.KEY_1));
    inputManager.addListener(analogListener,"SpeedUP", "SpeedDOWN", "SunUp", "SunDown");
    inputManager.addListener(consoleListener, "GUIToggle");

 
  }

private AnalogListener analogListener = new AnalogListener() {
    public void onAnalog(String name, float value, float tpf) {
    	if (name.equals("SpeedUP")) {
          speed += 10;
          flyCam.setMoveSpeed(speed);
          }
          if (name.equals("SpeedDOWN")) {
            speed -= 10;
            flyCam.setMoveSpeed(speed);
          }
          
          if(name.equals("SunUp")){
        	  angle += .004;
              sun.setDirection(new Vector3f(-FastMath.sin(angle) * radius, FastMath.HALF_PI,
                                             FastMath.cos(angle) * radius).normalize());
              LOD_Control.sun.setDirection(sun.getDirection());
              Atmosphere.sun.setDirection(sun.getDirection());
          }else if(name.equals("SunDown")){
        	  angle -= .004;
              sun.setDirection(new Vector3f(-FastMath.sin(angle) * radius, FastMath.HALF_PI,
                                             FastMath.cos(angle) * radius).normalize());
              LOD_Control.sun.setDirection(sun.getDirection());
              Atmosphere.sun.setDirection(sun.getDirection());
        	  
          }
          
          
    }
    
      
  };
static int o;
  private ActionListener actionListener = new ActionListener() {
	  public void onAction(String name, boolean keyPressed, float tpf) {
		  if(name.equals("textSwitch") && !keyPressed){
			  if(textOn){
				  guiNode.detachChild(speedText);
				  guiNode.detachChild(seedText);
				  guiNode.detachChild(distanceText);
				  guiNode.detachChild(lodText);
				  guiNode.detachChild(frustText);
				  textOn = false;
			  }else{
				  guiNode.attachChild(speedText);
				  guiNode.attachChild(seedText);
				  guiNode.attachChild(distanceText);
				  guiNode.attachChild(lodText);
				  guiNode.attachChild(frustText);
				  textOn = true;
			  }
		  }
		  
		  if(name.equals("wireframeSwitch") && !keyPressed){
			  if(useWireframe){
				  useWireframe = false;
			  }else{
				  useWireframe = true;
			  }
		  }
		  if(name.equals("UpLOD") && !keyPressed){
			  lodView += 5;
			  lodText.setText("LOD: "+lodView);
		  }else if(name.equals("DownLOD") && !keyPressed){
			  lodView -= 5;
			  lodText.setText("LOD: "+lodView);
		  }
		   if(name.equals("Up") && !keyPressed){
			   o = 1;
			   if(checkFrustum)
			    	  checkFrustum = false;
			    	  else if(!checkFrustum)
			    		  checkFrustum = true;
    	  speed+=10;
    	  flyCam.setMoveSpeed(speed);

      }else if(name.equals("Down") && !keyPressed){
    	  o = 0;
    	  Vector3f[] pts = new Vector3f[8];
    		createFrustum(pts, assetManager);
    	  speed-=10;
    	  flyCam.setMoveSpeed(speed);
    	  
      }
    }
  };
  
public void detachAtmosphere(){
	  atmosphereOn = false;
	  stateManager.detach(atm);
  }
  public void attachAtmosphere(){
	  atmosphereOn = true;
	  stateManager.attach(atm);
	  
  }
  
  private ActionListener consoleListener = new ActionListener() {
	  public void onAction(String name, boolean keyPressed, float tpf) {
		  if(name.equals("GUIToggle") && !keyPressed){
			  if(guiOn){
				  pConsole.console.getTextField().setText("  ");
				 inputManager.addListener(actionListener,"Up", "Down", "textSwitch", "wireframeSwitch", "UpLOD", "DownLOD");
				 inputManager.addListener(analogListener,"SpeedUP", "SpeedDOWN", "SunUp", "SunDown");	
				 inputManager.addListener(re, "Recompute", "Reload");
				  guiOn = false;
				  flyCam.setEnabled(true);
			       flyCam.setDragToRotate(false);
			        inputManager.setCursorVisible(false);
			  guiViewPort.removeProcessor(niftydisplay);
			  }else if(!guiOn){
				 pConsole.console.getTextField().setText("  ");
				 inputManager.removeListener(actionListener);
				 inputManager.removeListener(analogListener);
				 inputManager.removeListener(re);
				  guiOn = true;
				  flyCam.setEnabled(false);
			       flyCam.setDragToRotate(true);
			        inputManager.setCursorVisible(true);
	  			  guiViewPort.addProcessor(niftydisplay);

			  }
		  }
	  }
  };
  
  
  public void simpleUpdate(float tpf){
	 
	  speedText.setText("Speed: "+speed+" m/s");
	  seedText.setText("Seed: "+HeightMap.seed);
	  Vector3f l = cam.getLocation();
	  float length = l.length();
	  float distance = length - radius;
	  distanceText.setText("Altitude: "+ distance);
	  frustText.setText("Frustum Cull: "+ checkFrustum);
	  
	  if(o == 1){
	  Vector3f up = cam.getLocation().normalize();
	  cam.setAxes(up.cross(cam.getDirection()).normalize(), up, cam.getDirection());
	  }
  }
  
  public void setSpeed(int val){
	  flyCam.setMoveSpeed(speed);
	}

  @Override
  public void destroy() {
	  
      super.destroy();
      executor.shutdown();
      System.out.println("Successfully Shut Down");
  }
  
  public  void updateFrustumPoints2(Vector3f[] points){
	  	        int w = cam.getWidth();
	  	        int h = cam.getHeight();
	  	        float n = cam.getFrustumNear();
	  	        float f = cam.getFrustumFar();
	  	        System.out.println(cam.getWorldCoordinates(new Vector2f(0, 0), n));
	  	        points[0] = (cam.getWorldCoordinates(new Vector2f(0, 0), n));
	  	        points[1]=(cam.getWorldCoordinates(new Vector2f(0, h), n));
	  	        points[2]=(cam.getWorldCoordinates(new Vector2f(w, h), n));
	  	        points[3]=(cam.getWorldCoordinates(new Vector2f(w, 0), n));
	  	       
	  	        points[4]=(cam.getWorldCoordinates(new Vector2f(0, 0), f));
	  	        points[5]=(cam.getWorldCoordinates(new Vector2f(0, h), f));
	  	        points[6]=(cam.getWorldCoordinates(new Vector2f(w, h), f));
	            points[7]=(cam.getWorldCoordinates(new Vector2f(w, 0), f));
	           
	       
	   	    }
  
}

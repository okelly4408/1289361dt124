package PlanetRenderer;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingSphere;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;


public class Planet{

	private float radius;
	private float intensity = MainClass.intensity;
	public static QuadMesh top;
	private AssetManager assetManager;
	private RenderManager renderManager;
	private float textureSize;
	public Planet(float radius, AssetManager assetManager, RenderManager renderManager, float textureSize){
		this.renderManager = renderManager;
		this.assetManager = assetManager;
		this.textureSize = textureSize;
		this.radius = radius;
		QuadData.initialize(radius * 2);
		createQuads();
	}

	public void createQuads(){
		Matrix4f topbottom = new Matrix4f();
		topbottom.m00 = 1;
		topbottom.m01 = 0;
		topbottom.m02 = 0;
		topbottom.m03 = 0;
		
		topbottom.m10 = 0;
		topbottom.m11 = 0;
		topbottom.m12 = 0;
		topbottom.m13 = 0;
		
		topbottom.m20 = 0;
		topbottom.m21 = 0;
		topbottom.m22 = 1;
		topbottom.m23 = 0;
		
		topbottom.m30 = 0;
		topbottom.m31 = 0;
		topbottom.m32 = 0;
		topbottom.m33 = 0;
		
		Matrix4f frontback = new Matrix4f();
		frontback.m00 = 1;
		frontback.m01 = 0;
		frontback.m02 = 0;
		frontback.m03 = 0;
		
		frontback.m10 = 0;
		frontback.m11 = 0;
		frontback.m12 = 1;
		frontback.m13 = 0;
		
		frontback.m20 = 0;
		frontback.m21 = 0;
		frontback.m22 = 0;
		frontback.m23 = 0;
		
		frontback.m30 = 0;
		frontback.m31 = 0;
		frontback.m32 = 0;
		frontback.m33 = 0;
		
		Matrix4f rightleft = new Matrix4f();
		rightleft.m00 = 0;
		rightleft.m01 = 0;
		rightleft.m02 = 0;
		rightleft.m03 = 0;
		
		rightleft.m10 = 1;
		rightleft.m11 = 0;
		rightleft.m12 = 0;
		rightleft.m13 = 0;
		
		rightleft.m20 = 0;
		rightleft.m21 = 0;
		rightleft.m22 = 1;
		rightleft.m23 = 0;
		
		rightleft.m30 = 0;
		rightleft.m31 = 0;
		rightleft.m32 = 0;
		rightleft.m33 = 0;
		
		
		
		
		Vector3f p = new Vector3f(0,0,0);
		Vector3f c = new Vector3f(1,1,1);
		String path = "GroundFromSpace.j3md";
		HeightMap.initialize(radius * 2);
		HeightMap hm = new HeightMap();
		// create top and bottom faces
		// vertices are interchangeable and the disparity is applied in class Quad
		Camera cam = new Camera((int)textureSize, (int)textureSize);
		cam.setLocation(new Vector3f (0,0,1));
		cam.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);
		float nradius = -radius;
		float diameter = radius * 2;
		ViewPort vp1 = renderManager.createPreView("View1", cam);
		ViewPort vp2 = renderManager.createPreView("View2", cam);
		ViewPort vp3 = renderManager.createPreView("View3", cam);
		ViewPort vp4 = renderManager.createPreView("View4", cam);
		ViewPort vp5 = renderManager.createPreView("View5", cam);
		ViewPort vp6 = renderManager.createPreView("View6", cam);
		 BoundingSphere tops = new BoundingSphere();
		 BoundingSphere bots = new BoundingSphere();
		 BoundingSphere fros = new BoundingSphere();
		 BoundingSphere bacs = new BoundingSphere();
		 BoundingSphere lefs = new BoundingSphere();
		 BoundingSphere rigs = new BoundingSphere();
		 
		Vector3f tov1 = new Vector3f(nradius,radius, nradius);
		Vector3f tov2 = new Vector3f(nradius, radius, radius);
		Vector3f tov3 = new Vector3f(radius, radius, radius);
		Vector3f tov4 = new Vector3f(radius, radius, nradius);
		
		Vector3f bov1 = new Vector3f(nradius,nradius, nradius);
		Vector3f bov2 = new Vector3f(nradius, nradius, radius);
		Vector3f bov3 = new Vector3f(radius, nradius, radius);
		Vector3f bov4 = new Vector3f(radius, nradius, nradius);
		//public QuadMesh(ViewPort[] childrenViewPorts,float intensity, Texture2D Heightmap, String f ,QuadMesh p, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4,float size,int x, int y, boolean hasChildren, 
		//ArrayList<QuadMesh> children, Vector3f[] l, boolean get, BoundingSphere sphere, boolean isRoot, int[] indices, Geometry mesh, Vector2f heightmapOffset, Vector3f faceIndex,  float centerOff, int queuePosition){
		top = new QuadMesh(false,topbottom, c,false,p,true,0,null,intensity, null, QuadMesh.faces.TOP.toString(), null, tov1, tov2, tov3, tov4, 
		diameter, 0, 0, false, null, null, null, new Vector3f(nradius,radius,nradius), 0,null);
		tops.setCenter(QuadMesh.spherize(QuadMesh.getCenter(top))); tops.setRadius(radius);

		top.center = QuadMesh.getCenter(top);
		top.arcLengthOverSize = QuadMesh.approximateArcLength(top, top.center) / QuadMesh.num_leaves;
		top.mesh = QuadMesh.createMesh(top);

		top.Heightmap = hm.getHeightMap1(1, assetManager, vp1, textureSize,top);
		
		QuadMesh bottom = new QuadMesh(false,topbottom, c,false,p,true,0,null,intensity, null, QuadMesh.faces.BOTTOM.toString(), null, bov1, bov2, bov3, bov4, 
		diameter, 0, 0, false, null, null, null,new Vector3f(nradius,nradius,nradius), 0,null);
		bots.setCenter(QuadMesh.spherize(QuadMesh.getCenter(bottom))); bots.setRadius(radius);
		bottom.center = QuadMesh.getCenter(bottom);
		bottom.arcLengthOverSize = QuadMesh.approximateArcLength(bottom, bottom.center) / QuadMesh.num_leaves;
		bottom.mesh = QuadMesh.createMesh(bottom);
		bottom.Heightmap = hm.getHeightMap1(1, assetManager, vp2, textureSize, bottom);
		QuadMesh.list.add(top);
		//System.out.println(Quad.getCenter(top));
		QuadMesh.list.add(bottom);
		
		// create front and back faces
		Vector3f frv1 = new Vector3f(nradius, nradius, radius);
		Vector3f frv2 = new Vector3f(radius, nradius, radius);
		Vector3f frv3 = new Vector3f(radius, radius, radius);
		Vector3f frv4 = new Vector3f(nradius, radius, radius);
		
		Vector3f bav1 = new Vector3f(nradius, nradius, nradius);
		Vector3f bav2 = new Vector3f(radius, nradius, nradius);
		Vector3f bav3 = new Vector3f(radius, radius, nradius);
		Vector3f bav4 = new Vector3f(nradius, radius, nradius);
		
		QuadMesh front = new QuadMesh(false,frontback,c,false,p,true,0, null, intensity, null, QuadMesh.faces.FRONT.toString(), null, frv1, frv2, frv3, frv4,
		diameter, 0, 0, false, null, null,null,new Vector3f(nradius,nradius,radius),0,null);
		fros.setCenter(QuadMesh.spherize(QuadMesh.getCenter(front))); fros.setRadius(radius);
		front.sphere = fros;
		front.center = QuadMesh.getCenter(front);
		front.arcLengthOverSize = QuadMesh.approximateArcLength(front, front.center) / QuadMesh.num_leaves;
		front.mesh = QuadMesh.createMesh(front);
		front.Heightmap = hm.getHeightMap1(2, assetManager, vp3, textureSize, front);
		
		QuadMesh back = new QuadMesh(false,frontback,c,false,p,true,0, null, intensity, null, QuadMesh.faces.BACK.toString(), null, bav1, bav2, bav3, bav4,
				diameter, 0, 0, false, null, null,null,new Vector3f(nradius,nradius,nradius),0,null);
		bacs.setCenter(QuadMesh.spherize(QuadMesh.getCenter(back))); bacs.setRadius(radius);
		back.sphere = bacs;
		back.center = QuadMesh.getCenter(back);
		back.arcLengthOverSize = QuadMesh.approximateArcLength(back, back.center) / QuadMesh.num_leaves;
		back.mesh = QuadMesh.createMesh(back);
		back.Heightmap = hm.getHeightMap1(2, assetManager, vp4, textureSize, back);

		QuadMesh.list.add(front);
		QuadMesh.list.add(back);
		
		// create right and left faces
		Vector3f riv1 = new Vector3f(radius, nradius, nradius);
		Vector3f riv2 = new Vector3f(radius, nradius, radius);
		Vector3f riv3 = new Vector3f(radius, radius, radius);
		Vector3f riv4 = new Vector3f(radius, radius, nradius);
		
		Vector3f lev1 = new Vector3f(nradius, nradius, nradius);
		Vector3f lev2 = new Vector3f(nradius, nradius, radius);
		Vector3f lev3 = new Vector3f(nradius, radius, radius);
		Vector3f lev4 = new Vector3f(nradius, radius, nradius);
		
		QuadMesh right = new QuadMesh(false,rightleft,c,false,p,true,0, null, intensity, null, QuadMesh.faces.RIGHT.toString(), null, riv1, riv2, riv3, riv4,
				diameter, 0, 0, false, null, null,null, new Vector3f(radius,nradius,nradius),0,null);
		rigs.setCenter(QuadMesh.spherize(QuadMesh.getCenter(right))); rigs.setRadius(radius);
		right.sphere = rigs;
		right.center = QuadMesh.getCenter(right);
		right.arcLengthOverSize = QuadMesh.approximateArcLength(right, right.center) / QuadMesh.num_leaves;
		right.mesh = QuadMesh.createMesh(right);
		right.Heightmap = hm.getHeightMap1(3, assetManager, vp5, textureSize,right);
		QuadMesh left = new QuadMesh(false,rightleft,c,false,p,true,0, null, intensity, null, QuadMesh.faces.LEFT.toString(), null, lev1, lev2, lev3, lev4,
				diameter, 0, 0, false, null, null,null,new Vector3f(nradius,nradius,nradius),0,null);
		lefs.setCenter(QuadMesh.spherize(QuadMesh.getCenter(left))); lefs.setRadius(radius);
		left.sphere = lefs;
		left.center = QuadMesh.getCenter(left);
		left.arcLengthOverSize = QuadMesh.approximateArcLength(left, left.center) / QuadMesh.num_leaves;
		left.mesh = QuadMesh.createMesh(left);
		left.Heightmap = hm.getHeightMap1(3, assetManager, vp6, textureSize, left);

		QuadMesh.list.add(right);
		QuadMesh.list.add(left);
		
		top.mesh.setMaterial(makeMaterial(path));
		bottom.mesh.setMaterial(makeMaterial(path));
		right.mesh.setMaterial(makeMaterial(path));
		left.mesh.setMaterial(makeMaterial(path));
		front.mesh.setMaterial(makeMaterial(path));
		back.mesh.setMaterial(makeMaterial(path));

		 top.mesh.getMaterial().setVector3("Color", new Vector3f(1,.5f,.5f));	    
	     top.mesh.getMaterial().setFloat("size", diameter);
    	 top.mesh.getMaterial().setFloat("intensity",intensity);
		 top.mesh.getMaterial().setTexture("HeightMap", top.Heightmap);
		 top.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 top.mesh.getMaterial().setMatrix4("cubeMatrix", topbottom);

		 
		 
		 bottom.mesh.getMaterial().setVector3("Color", new Vector3f(1,0f,0));	    
	     bottom.mesh.getMaterial().setFloat("size", diameter);
    	 bottom.mesh.getMaterial().setFloat("intensity", intensity);
		 bottom.mesh.getMaterial().setTexture("HeightMap", bottom.Heightmap);
		 bottom.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 bottom.mesh.getMaterial().setMatrix4("cubeMatrix", topbottom);
		 
		 front.mesh.getMaterial().setVector3("Color", new Vector3f(1,1f,0));	    
	     front.mesh.getMaterial().setFloat("size", diameter);
    	 front.mesh.getMaterial().setFloat("intensity", front.intensity);
		 front.mesh.getMaterial().setTexture("HeightMap", front.Heightmap);
		 front.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 front.mesh.getMaterial().setMatrix4("cubeMatrix", frontback);
		 
		 back.mesh.getMaterial().setVector3("Color", new Vector3f(1,1f,1));	    
	     back.mesh.getMaterial().setFloat("size", diameter);
    	 back.mesh.getMaterial().setFloat("intensity", back.intensity);
		 back.mesh.getMaterial().setTexture("HeightMap", back.Heightmap);
		 back.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 back.mesh.getMaterial().setMatrix4("cubeMatrix", frontback);
		 
		 right.mesh.getMaterial().setVector3("Color", new Vector3f(0,0f,1));	    
	     right.mesh.getMaterial().setFloat("size", right.width);
    	 right.mesh.getMaterial().setFloat("intensity", intensity);
		 right.mesh.getMaterial().setTexture("HeightMap", right.Heightmap);
		 right.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 right.mesh.getMaterial().setMatrix4("cubeMatrix", rightleft);
		 
		 left.mesh.getMaterial().setVector3("Color", new Vector3f(0,1f,1));	    
	     left.mesh.getMaterial().setFloat("size", diameter);
    	 left.mesh.getMaterial().setFloat("intensity", left.intensity);
		 left.mesh.getMaterial().setTexture("HeightMap", left.Heightmap);
		 left.mesh.getMaterial().setVector3("meshOffset", new Vector3f(0,0,0));
		 left.mesh.getMaterial().setMatrix4("cubeMatrix", rightleft);
	}
	public void setRadius(float r){
		radius = r;
		
	}
	
	
	public float getRadius(){
		return radius;
	}
	
	
	private Material makeMaterial(String p){
		Material mat = new Material(assetManager, p);
		mat.getAdditionalRenderState().setWireframe(true);
		mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		return mat;
	}
}

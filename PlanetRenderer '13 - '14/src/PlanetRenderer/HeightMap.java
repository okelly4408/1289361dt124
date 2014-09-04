package PlanetRenderer;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapAxis;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;



public class HeightMap{
	static int[] permutation;
	
	//all variables for terrain are stored in maps so that they can be looked up by the console
	static Map<String, Float> mountain_vars;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("h", 0.75f);
		valuesByName.put("off", 1f);
		valuesByName.put("gain",1f);
		valuesByName.put("lac", 2.123f);
		valuesByName.put("num_terraces", 2.35f);
		valuesByName.put("terrace_slope", 1.90f);
		valuesByName.put("lower", 0.4f);
		valuesByName.put("upper", 0.6f);
		mountain_vars = (valuesByName);
	}

	
	static Map<String, Float> crater_vars;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("rim_width", 0.1f);
		valuesByName.put("depth", -0.22f);
		valuesByName.put("radius", 0.30f);
		valuesByName.put("slope", 0.1f);
		valuesByName.put("octaves", 4f);
		valuesByName.put("crater_frequency", 25f);
		crater_vars = (valuesByName);
	}
	
	static Map<String, Float> continent_vars;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("upper", 0.545f);
		valuesByName.put("lower", 0.25f);
		valuesByName.put("frequency", 0.8f);
		continent_vars = (valuesByName);
	}
	
	static Map<String, Float> hmf_vars;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("h", 0.25f);
		valuesByName.put("lac", 2.1341f);
		valuesByName.put("off", 0.85f);
		hmf_vars = (valuesByName);
	}
	
	static Map<String, Float> fbm_vars;
	static{
		Map<String, Float> valuesByName = new HashMap<String, Float>();
		valuesByName.put("pers", 0.6f);
		valuesByName.put("lac", 2.1341f);
		fbm_vars = (valuesByName);
	}
	
	
	static float amp = .5f;
	static long seed;
	static int c = 0;
	private static float ftexSize = MainClass.ftexSize;
	private static int itexSize = (int)ftexSize;

	private static Texture2D permutationTex;
	private static Texture2D gradTex;

	private static Texture2D altpermutationTex;
	private static Texture2D altgradTex;

	private static Texture2D cellTex;
	private static float denom1;
	private static float denom2;

	static float[][] grads = new float[][]{
		{1,1,0},
	    {-1,1,0},
	    {1,-1,0},
	    {-1,-1,0},
	    {1,0,1},
	    {-1,0,1},
	    {1,0,-1},
	    {-1,0,-1}, 
	    {0,1,1},
	    {0,-1,1},
	    {0,1,-1},
	    {0,-1,-1},
	    {1,1,0},
	    {0,-1,1},
	    {-1,1,0},
	    {0,-1,-1}	
	};
public static void initialize(float worldSize){
	float hWorldSize = MainClass.radius;
	 denom1 = hWorldSize * hWorldSize * 2f;
	 denom2 = hWorldSize * hWorldSize * hWorldSize * hWorldSize * 3f;
	 
	 seed = System.currentTimeMillis();
	 
	permutationTex = permutationTexture(seed);
	gradTex = gradientTexture();
	
	cellTex = randomCellTexture(seed);
	
	altpermutationTex = permutationTexture(seed+400);
	altgradTex = gradientTexture();

}

//this method is used when a seed is specified
public static void initialize(float worldSize, long givenSeed){

	seed = givenSeed;
	float hWorldSize = MainClass.radius;
	 denom1 = hWorldSize * hWorldSize * 2f;
	 denom2 = hWorldSize * hWorldSize * hWorldSize * hWorldSize * 3f;
	 
	permutationTex = permutationTexture(givenSeed);
	gradTex = gradientTexture();
	
	cellTex = randomCellTexture(givenSeed);
	
	altpermutationTex = permutationTexture(givenSeed+400);
	altgradTex = gradientTexture();
}

	public Texture2D getHeightMap1(int t , AssetManager assetManager, ViewPort vp, float TEXTURE_SIZE, QuadMesh currQuad){
		Texture2D hm = new Texture2D(itexSize, itexSize, Format.RGBA32F);
		FrameBuffer fbo = new FrameBuffer(itexSize, itexSize, 1);
		fbo.setColorTexture(hm);
		vp.setOutputFrameBuffer(fbo);
		Quad q = new Quad(2,2);
		Material mat = new Material(assetManager, "NoiseMaterial.j3md"); 
		mat.setInt("t", t);
		mat.setFloat("Size", currQuad.width);
		mat.setFloat("H", mountain_vars.get("h")); 
		mat.setFloat("Amp", amp);
		mat.setFloat("Off", mountain_vars.get("off"));
		mat.setFloat("Gain", mountain_vars.get("gain"));
		mat.setFloat("lac", mountain_vars.get("lac"));
		mat.setFloat("num_terraces", mountain_vars.get("num_terraces")); 
		mat.setFloat("terrace_slope", mountain_vars.get("terrace_slope"));
		mat.setFloat("cont_upper", continent_vars.get("upper"));
		mat.setFloat("cont_lower", continent_vars.get("lower"));
		mat.setFloat("mount_upper", mountain_vars.get("upper"));
		mat.setFloat("mount_lower", mountain_vars.get("lower"));
		mat.setFloat("cont_frequency", continent_vars.get("frequency"));
		mat.setFloat("fbm_lac", fbm_vars.get("lac"));
		mat.setFloat("fbm_pers", fbm_vars.get("pers"));
		mat.setFloat("hmf_h", hmf_vars.get("h"));
		mat.setFloat("hmf_lac", hmf_vars.get("lac"));
		mat.setFloat("hmf_off", hmf_vars.get("off"));
		mat.setFloat("radius", crater_vars.get("radius"));
		mat.setFloat("slope", crater_vars.get("slope"));
		mat.setFloat("rim_width", crater_vars.get("rim_width"));
		mat.setFloat("depth", crater_vars.get("depth"));
		mat.setFloat("crater_frequency", crater_vars.get("crater_frequency"));
		mat.setFloat("octaves", crater_vars.get("octaves"));
		mat.setFloat("worldSize", MainClass.radius); 
		mat.setFloat("denom1", denom1);
		mat.setVector3("meshOffset", currQuad.meshOffset); 
		mat.setFloat("denom2", denom2); 
		mat.setVector3("faceOffset", currQuad.faceIndex); 
		mat.setTexture("permutationTexture", permutationTex);
		mat.setTexture("gradientTexture", gradTex); 
		mat.setTexture("altpermutationTexture", altpermutationTex);
		mat.setTexture("altgradientTexture", altgradTex); 
		mat.setTexture("cellRandTex", cellTex);
		mat.setBoolean("mars", LOD_Control.mars);
		Geometry ge1 = new Geometry("Mesh",q);
		ge1.setMaterial(mat);
		vp.attachScene(ge1);
		if(vp.isEnabled()){
		ge1.updateGeometricState();
		}
		currQuad.centerOff = 0f;
	return hm;
	}


	public static int perm2d(int i){
		return permutation[i % 256];
	}
	
	public static Texture2D randomCellTexture(long seed){
		ByteBuffer data = BufferUtils.createByteBuffer( (int)Math.ceil(Format.RGBA16F.getBitsPerPixel() / 8.0) * 256 * 256);
		Image cellImg = new Image();
		cellImg.setWidth(256);
		cellImg.setHeight(256);
		cellImg.setFormat(Format.RGBA16F);
		cellImg.setData(data);
		Random r = new Random(seed);
		ImageRaster cellR = ImageRaster.create(cellImg);
		for(int x = 0; x < 256; x++){
			for(int y = 0; y < 256; y++){
				cellR.setPixel(x, y, new ColorRGBA(r.nextFloat(), r.nextFloat(), r.nextFloat(), r.nextFloat()));
			}
		}
		
		Texture2D cellTexture = new Texture2D(cellImg);
		cellTexture.setWrap(WrapAxis.S, WrapMode.Repeat);
		cellTexture.setWrap(WrapAxis.T, WrapMode.Repeat);
		cellTexture.setMagFilter(MagFilter.Nearest);
		cellTexture.setMinFilter(MinFilter.NearestNoMipMaps);

		
		return cellTexture;
		
	}
	
	public static void init(long seed){		
		Random r = new Random(seed);
		 permutation = new int[256];
		for(int i = 0; i<permutation.length; i++){
			permutation[i] = -1;
		}
		
		for(int i = 0; i< permutation.length; i++){
			while(true){
				int iP = Math.abs(r.nextInt()) % permutation.length;
				if(permutation[iP] == -1){
					permutation[iP] = i;
					break;
				}
			}
		}
} 
	
	public static Texture2D permutationTexture(long seed){
		init(seed);
		ByteBuffer data = BufferUtils.createByteBuffer( (int)Math.ceil(Format.RGBA16F.getBitsPerPixel() / 8.0) * 256 * 256);
		Image permImage = new Image();
		permImage.setWidth(256);
		permImage.setHeight(256);
		permImage.setFormat(Format.RGBA16F);
		permImage.setData(data);
		
		ImageRaster rP = ImageRaster.create(permImage);
		for(int x = 0; x < 256; x++){
			for(int y = 0; y < 256; y++){
				int A = perm2d(x) + y;
                int AA = perm2d(A);
                int AB = perm2d(A + 1);
                int B = perm2d(x + 1) + y;
                int BA = perm2d(B);
                int BB = perm2d(B + 1);
                ColorRGBA c = new ColorRGBA((float)AA/255f, (float)AB/255f, (float)BA/255f, (float)BB/255f);
                rP.setPixel(x, y, c);
				
			}
		} 
		
		Texture2D permutationTable = new Texture2D(permImage);
		permutationTable.setWrap(WrapMode.Repeat);
		permutationTable.setMagFilter(MagFilter.Nearest);
		permutationTable.setMinFilter(MinFilter.NearestNoMipMaps);
		
		return permutationTable;
	}
	
	public static Texture2D gradientTexture(){
		
		ByteBuffer data2 = BufferUtils.createByteBuffer( (int)Math.ceil(Format.RGBA16F.getBitsPerPixel() / 8.0) * 256 * 1);
		Image gradImage = new Image();
		gradImage.setWidth(256);
		gradImage.setHeight(1);
		gradImage.setFormat(Format.RGBA16F);
		gradImage.setData(data2);
		
		ImageRaster rG = ImageRaster.create(gradImage);
		for(int x = 0; x<256; x++){
			for(int y = 0; y < 1; y++){

				ColorRGBA c = new ColorRGBA(grads[permutation[x]%16][0], 
				grads[permutation[x] % 16][1], 
				grads[permutation[x] % 16][2], 1);
				rG.setPixel(x, y, c);
			}
		} 
		Texture2D gradientTable = new Texture2D(gradImage);
		gradientTable.setWrap(WrapAxis.S, WrapMode.Repeat);
		gradientTable.setWrap(WrapAxis.T, WrapMode.Clamp);
		gradientTable.setMagFilter(MagFilter.Nearest);
		gradientTable.setMinFilter(MinFilter.NearestNoMipMaps);
		
		return gradientTable;
		
	}
}
	
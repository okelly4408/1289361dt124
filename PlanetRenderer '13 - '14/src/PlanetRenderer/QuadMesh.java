package PlanetRenderer;
import java.util.ArrayList;

import com.jme3.bounding.BoundingSphere;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.Texture2D;

public class QuadMesh{
	public  Vector3f first,second,third,fourth;
	public int index1, index2;
	public  float width;
	public boolean hasChildren;
	public ArrayList<QuadMesh> children;
	public QuadMesh parent;
	static float num_leaves = 16;
	private static float size = MainClass.radius * 2.0f;
	private static float hsize = size/2;
	private static float nhsize = -hsize;
	private static float denom1 = hsize*hsize*2;
	private static float denom2 = hsize*hsize*hsize*hsize*3;
	public static ArrayList<QuadMesh> list = new ArrayList<QuadMesh>();
	public String face;
	public BoundingSphere sphere;
	public enum faces {TOP, BOTTOM, LEFT, RIGHT, FRONT, BACK};
	public Geometry mesh;
	public float centerOff;
	public Vector3f faceIndex;
	public Texture2D Heightmap;
	public float intensity; 
	public float arcLengthOverSize;
	public Vector3f center;
	public boolean inAtmosphere;
	public boolean inFrustum;
	public Vector3f meshOffset;
	public Vector3f color;
	public Matrix4f cubeMatrix;
	public boolean isBeingSplit;
	public Geometry bsp;
	// Creates a "quad"
	// contains information pertinent to mesh calculation and shader stuff
public QuadMesh
       (boolean isBeingSplit,
        Matrix4f cubeMatrix, 
		Vector3f color,
		boolean inAtmosphere, 
		Vector3f meshOffset, 
		boolean inFrustum, 
		float arcLengthOverSize, 
		Vector3f center, 
		float intensity, 
		Texture2D Heightmap, 
		String f ,
		QuadMesh p, 
		Vector3f v1, 
		Vector3f v2, 
		Vector3f v3, 
		Vector3f v4,
		float size,
		int x, 
		int y, 
		boolean hasChildren, 
		ArrayList<QuadMesh> children,  
		BoundingSphere sphere, 
		Geometry mesh, 
		Vector3f faceIndex,  
		float centerOff,
		Geometry bsp)	
{
		first = v1;
		second = v2;
		third = v3;
		fourth = v4;
		width = size;
		index1 = x;
		index2 = y;
		this.hasChildren = hasChildren;
		this.children = children;
		parent = p;
		face = f;
		this.sphere = sphere;
		this.mesh = mesh;
		this.faceIndex = faceIndex;
		this.centerOff = centerOff;
		this.Heightmap = Heightmap;
		this.intensity = intensity;
		this.arcLengthOverSize = arcLengthOverSize;
		this.center = center;
		this.inFrustum = inFrustum;
		this.meshOffset = meshOffset;
		this.inAtmosphere = inAtmosphere;
		this.color = color;
		this.cubeMatrix = cubeMatrix;
		this.bsp = bsp;

	
	}
	public String toString(QuadMesh q){
		String s = "("+q.first+","+q.second+","+q.third+","+q.fourth+") "+q.width+" "+q.index1+" "+q.index2;
		return s;

	}

	public static  Vector3f[] makeVerts(QuadMesh q, Vector3f p, int x, int y, float increment){
		Vector3f[] r = new Vector3f[4];
		Vector3f back_left = new Vector3f(0,0,0);
		Vector3f front_right = new Vector3f(0,0,0);
		Vector3f front_left = new Vector3f(0,0,0);
		Vector3f back_right = new Vector3f(0,0,0);
		float xf = x;
		float yf = y;
		Vector3f bottom = new Vector3f (nhsize, nhsize, nhsize);
		Vector3f top = new Vector3f(nhsize, hsize, nhsize);
		Vector3f back = new Vector3f(nhsize, nhsize, nhsize);
		Vector3f front = new Vector3f(nhsize, nhsize, hsize);
		Vector3f left = new Vector3f(nhsize, nhsize, nhsize);
		Vector3f right = new Vector3f(hsize, nhsize, nhsize);
if(q.face.equals(faces.BOTTOM.toString())){
	
		 back_left = new Vector3f (xf*increment, 0 , yf*increment).add(p).add(bottom);
		 front_right = new Vector3f ((xf*increment) + increment, 0, (yf*increment)+increment).add(p).add(bottom);
		 front_left = new Vector3f (xf*increment, 0 , (yf*increment)+increment).add(p).add(bottom);
		 back_right = new Vector3f((xf*increment) + increment, 0, yf*increment).add(p).add(bottom);
		 
} else if(q.face.equals(faces.TOP.toString())){
	 back_left = new Vector3f (xf*increment, 0 , yf*increment).add(p).add(top);
	 front_right = new Vector3f ((xf*increment) + increment, 0, (yf*increment)+increment).add(p).add(top);
	 front_left = new Vector3f (xf*increment, 0 , (yf*increment)+increment).add(p).add(top);
	 back_right = new Vector3f((xf*increment) + increment, 0, yf*increment).add(p).add(top);
	 
} else if(q.face.equals(faces.BACK.toString())){
	
	 back_left = new Vector3f (xf*increment, yf*increment , 0).add(p).add(back);
	 front_right = new Vector3f ((xf*increment) + increment, (yf*increment)+increment, 0).add(p).add(back);
	 front_left = new Vector3f (xf*increment, (yf*increment)+increment , 0).add(p).add(back);
	 back_right = new Vector3f((xf*increment) + increment, yf*increment, 0).add(p).add(back);
	 
} else if(q.face.equals(faces.FRONT.toString())){
	
	 back_left = new Vector3f (xf*increment, yf*increment , 0).add(p).add(front);
	 front_right = new Vector3f ((xf*increment) + increment, (yf*increment)+increment, 0).add(p).add(front);
	 front_left = new Vector3f (xf*increment, (yf*increment)+increment , 0).add(p).add(front);
	 back_right = new Vector3f((xf*increment) + increment, yf*increment, 0).add(p).add(front);
	 
} else if(q.face.equals(faces.LEFT.toString())){
	
	 back_left = new Vector3f (0, xf*increment , yf*increment).add(p).add(left);
	 front_right = new Vector3f (0, (xf*increment) + increment, (yf*increment)+increment).add(p).add(left);
	 front_left = new Vector3f (0, xf*increment , (yf*increment)+increment).add(p).add(left);
	 back_right = new Vector3f(0, (xf*increment) + increment, yf*increment).add(p).add(left);
	 
} else if(q.face.equals(faces.RIGHT.toString())){
	
	 back_left = new Vector3f (0, xf*increment , yf*increment).add(p).add(right);
	 front_right = new Vector3f (0, (xf*increment) + increment, (yf*increment)+increment).add(p).add(right);
	 front_left = new Vector3f (0, xf*increment , (yf*increment)+increment).add(p).add(right);
	 back_right = new Vector3f(0, (xf*increment) + increment, yf*increment).add(p).add(right);
	 
}

		r[0] = ((back_left));
		r[1] = ((front_left));
		r[2] = ((front_right));
		r[3] = ((back_right));

		return r;
	}
	
	public static void split(QuadMesh quad, int i, int ii){
		quad.hasChildren = true;
		ArrayList<QuadMesh> temp = new ArrayList<QuadMesh>();
		i*=2;
		ii*=2;
		Vector3f parent = new Vector3f(0,0,0);
		if(quad.face.equals(faces.BOTTOM.toString()) || quad.face.equals(faces.TOP.toString())){
		parent = new Vector3f(i*quad.width/2, 0, ii*quad.width/2);
		}else if(quad.face.equals(faces.LEFT.toString()) || quad.face.equals(faces.RIGHT.toString())){
		parent = new Vector3f(0, i*quad.width/2, ii*quad.width/2);	
		}else if(quad.face.equals(faces.BACK.toString()) || quad.face.equals(faces.FRONT.toString())){
	    parent = new Vector3f( i*quad.width/2, ii*quad.width/2, 0);
		}
		
		Vector3f[] vv = new Vector3f[4];
			for(int x = 0; x<2; x++){
				for(int y = 0; y<2; y++){	
					float growth = quad.width/2;
					BoundingSphere sp = new BoundingSphere();
					vv = makeVerts(quad,parent,x,y,growth);
					Vector3f p1 = vv[0];
					Vector3f p2 = vv[1];
					Vector3f p3 = vv[2];
					Vector3f p4 = vv[3];
					QuadMesh c = new QuadMesh (true,null,null, false,null, true, 0f, null, quad.intensity/2.0f,null, quad.face,quad,p1,p2,p3,p4,
							growth,(x+i),(y+ii),false,null,null,null,null,0,null);
					c.center = QuadMesh.getCenter(c);
					if(x == 0 && y == 0){
						c.color = new Vector3f(1,1,1);
					}else if(x == 0 && y == 1){
						c.color = new Vector3f(1,0,0);
					}else if(x == 1 && y == 0){
						c.color = new Vector3f(0,0,1);
					}else if(x ==1 && y ==1){
						c.color = new Vector3f(0,1,0);
					}
										
					Vector3f meshOffset = new Vector3f(0,0,0);
					if(c.face.equals(faces.BOTTOM.toString()) || c.face.equals(faces.TOP.toString())){
					meshOffset = new Vector3f(c.index1*c.width, 0, c.index2*c.width);
					}else if(c.face.equals(faces.LEFT.toString()) || c.face.equals(faces.RIGHT.toString())){
					meshOffset = new Vector3f(0, c.index1*c.width, c.index2*c.width);	
					}else if(c.face.equals(faces.BACK.toString()) || c.face.equals(faces.FRONT.toString())){
				    meshOffset = new Vector3f( c.index1*c.width, c.index2*c.width, 0);
					}
					c.meshOffset = meshOffset;
															
					c.arcLengthOverSize = QuadMesh.approximateArcLength(c, c.center)/num_leaves;
					if(c.face.equals(faces.TOP.toString())){
						c.faceIndex = new Vector3f(nhsize, hsize ,nhsize);
					}else if(c.face.equals(faces.BOTTOM.toString())){
						c.faceIndex = new Vector3f(nhsize, nhsize, nhsize);
					}else if(c.face.equals(faces.FRONT.toString())){
						c.faceIndex = new Vector3f(nhsize, nhsize, hsize);
					}else if(c.face.equals(faces.BACK.toString())){
						c.faceIndex = new Vector3f(nhsize, nhsize, nhsize);
					}else if(c.face.equals(faces.RIGHT.toString())){
						c.faceIndex = new Vector3f(hsize, nhsize, nhsize);
					}else if (c.face.equals(faces.LEFT.toString())){
						c.faceIndex = new Vector3f(nhsize, nhsize, nhsize);
					}
					sp.setCenter(c.center); 
					sp.setRadius(approximateArcLength(c, c.center)/2.0f);
					c.sphere = sp;
					c.mesh = createMesh(c);
					
					temp.add(c);
			}
				
			}

			quad.children = temp;
	} 	

		public static int getLeafCount(){
			int n = 0;
			for(int i = 0; i<list.size(); i++){
				if(!list.get(i).hasChildren){
					n++;
				}
			}
			return n;
		}
		
		public static int getVisibleLeafCount(){
			int n = 0;
			for(int i = 0; i<list.size(); i++){
				if(!list.get(i).hasChildren && list.get(i).inFrustum){
					n++;
				}
			}
			return n;
		}


	public static Geometry createMesh(QuadMesh q){
		Mesh m = QuadData.mesh;
		//m.setBound(new BoundingSphere(approximateArcLength(q, q.center)/2.0f, q.center));
		Geometry geo = new Geometry("Mesh", m);
		//geo.setModelBound(m.getBound());
		geo.setCullHint(CullHint.Never);

		return geo;
		
	}
	
	
	public static  Vector3f getCenter(QuadMesh q){
		Vector3f center = (((q.first).add((q.second).add((q.third).add((q.fourth))))).divide(4));
		return spherize((center));

	}
	
	//approximates the arc length of a spherical quad
	public static float approximateArcLength(QuadMesh q, Vector3f center){
		
		
		Vector3f sphereV1 = spherize(q.first);
		Vector3f sphereV2 = spherize(q.second);
		Vector3f sphereV3 = spherize(q.third);
		Vector3f sphereV4 = spherize(q.fourth);
		
		Vector3f mid1 = (sphereV1.add(sphereV2)).divide(2);
		Vector3f mid2 = (sphereV3.add(sphereV4)).divide(2);
		
		float l1 = (mid1.subtract(center)).length();
		float l2 = (mid2.subtract(center)).length();
	
		return l1 + l2;
		
	}
	//maps a point on a cube to a point on a sphere
	public static Vector3f spherize(Vector3f v){
		 float newX = (float) (v.x * (Math.sqrt(1-((v.y*v.y)/denom1)-((v.z*v.z)/denom1)+(((v.y*v.y)*(v.z*v.z))/denom2))));
		 float newY = (float) (v.y * (Math.sqrt(1-((v.x*v.x)/denom1)-((v.z*v.z)/denom1)+(((v.x*v.x)*(v.z*v.z))/denom2))));
		 float newZ = (float) (v.z * (Math.sqrt(1-((v.y*v.y)/denom1)-((v.x*v.x)/denom1)+(((v.y*v.y)*(v.x*v.x))/denom2))));
		return new Vector3f(newX, newY, newZ);
	}
		}



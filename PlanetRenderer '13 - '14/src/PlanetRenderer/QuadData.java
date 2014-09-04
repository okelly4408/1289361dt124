package PlanetRenderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

//Generates All of the Data for a Quad
//Data includes: vertices, indices, and tex coords
//this data is instanced each time a quad is created
//the vertices are scaled in a vertex shader for each quad

public class QuadData {
	//vertices per side - 1
	private static float blockSize = 16;
	private static boolean useSkirting = true;
	private static int verticesPerQuad = (int)(blockSize * blockSize * 4);
	private static int verticesPerSkirt = (int)(blockSize * 16);
	private static Vector3f[] quadSkirt;
	private static Vector3f[] vertices;
	public static FloatBuffer vertexBuffer, texCoordBuffer;
	public static IntBuffer indexBuffer;
	static Mesh mesh;
	static float diameter;
	
	public static void initialize(float length){
		diameter = length;
		createVertices();
		createIndices();
		createTexCoords();
		mesh = new Mesh();
		mesh.setBuffer(Type.Position, 3, vertexBuffer);
		mesh.setBuffer(Type.Index, 3, indexBuffer);
		mesh.setBuffer(Type.TexCoord, 2, texCoordBuffer);
	}
	
	private static void createVertices(){
		//get vertices and put into buffer
		vertices = getVertices(diameter);
		if(useSkirting){
		vertexBuffer = BufferUtils.createFloatBuffer((quadSkirt.length + verticesPerQuad) * 3);
		}else{
			vertexBuffer = BufferUtils.createFloatBuffer((verticesPerQuad) * 3);

		}
		for(int i = 0; i<vertices.length; i++){
			vertexBuffer.put(vertices[i].x);
			vertexBuffer.put(vertices[i].y);
			vertexBuffer.put(vertices[i].z);
		}
		if(useSkirting){
		//add skirting onto end of quad's vertexbuffer
		for(int i = 0; i<quadSkirt.length; i++){
			vertexBuffer.put(quadSkirt[i].x);
			vertexBuffer.put(quadSkirt[i].y);
			vertexBuffer.put(quadSkirt[i].z);
		}
		}
	}
	
	private static void createIndices(){

		 ArrayList<Integer> temp = new ArrayList<Integer>();
		 ArrayList<Integer> temp2 = new ArrayList<Integer>();
		 for(int i = 0; i<verticesPerQuad; i+= 4){
				temp.add(i);
				temp.add(i+1);
				temp.add(i+2);
				temp.add(i+2);
				temp.add(i+3);
				temp.add(i);
	    } 
		if(useSkirting){
		 for(int i = 0; i<quadSkirt.length; i+=4){
			 temp2.add(i);
			 temp2.add(i+1);
			 temp2.add(i+3);
			 temp2.add(i+3);
			 temp2.add(i+2);
			 temp2.add(i);

		 } 
		 indexBuffer = BufferUtils.createIntBuffer(temp.size() +  temp2.size());
		 for(int i = 0; i<temp.size(); i++){
				indexBuffer.put(temp.get(i));
			}
		for(int i = 0; i<temp2.size(); i++){
				indexBuffer.put(temp2.get(i) + verticesPerQuad);
			}

		}else{
		 indexBuffer = BufferUtils.createIntBuffer(temp.size());
		 for(int i = 0; i<temp.size(); i++){
				indexBuffer.put(temp.get(i));
			}
		}
		
		
	}
	
	private static void createTexCoords(){
		Vector2f[] texCoordArray;
		if(useSkirting){
		 texCoordArray = getTexCoords(concat(vertices,quadSkirt), diameter);
		}else{
	      texCoordArray = getTexCoords(vertices, diameter);

		}
		texCoordBuffer = BufferUtils.createFloatBuffer(texCoordArray);
	}
	
	public static Vector2f[] getTexCoords(Vector3f[] vertices, float worldSize){
		Vector2f[] texCoords = new Vector2f[vertices.length];
		for(int i = 0; i<vertices.length; i++){
			Vector2f coord = new Vector2f((vertices[i].x)/( worldSize), (vertices[i].z)/( worldSize));
			texCoords[i] = coord;
				
		}
		return texCoords;
	}
	
private static Vector3f[] getVertices(float size){
		float min = size/blockSize;
		float sizeSubMin = size - min;
		float max = size;
		Vector3f[] v = new Vector3f[verticesPerQuad];
		Vector3f[] vv = new Vector3f[4];
		Vector3f[] skirtices = new Vector3f[verticesPerSkirt];
		int n = 0;
		int nq = 0;
		for(int x = 0; x < blockSize; x++){
			for(int y = 0; y < blockSize; y++){
			//creates vertices for quad
				float g = (size/blockSize);
			vv = getPolygon(x,y,g);
			v[n++] = vv[0];
			v[n++] = vv[1];
			v[n++] = vv[2];
			v[n++] = vv[3];
			
			Vector3f v1 = vv[0];
			Vector3f v2 = vv[1];
			Vector3f v3 = vv[2];
			Vector3f v4 = vv[3];
			
			//creates skirts to mend LOD seams
			if(useSkirting){
			if(v1.x == 0 || v1.z == 0 || v1.x == max || v1.z == max){
				Vector3f skirt = new Vector3f(v1.x , -1, v1.z);
				skirtices[nq++] = v1;
				skirtices[nq++] = skirt;
				if(v1.equals(new Vector3f(sizeSubMin,1,0))){
					skirtices[nq++] = v4;
					skirtices[nq++] = new Vector3f(v4.x, -1, v4.z);
				}

				
			}
			if(v2.x == 0 || v2.z == 0 || v2.x == size || v2.z == size){
				Vector3f skirt = new Vector3f(v2.x , -1, v2.z);
				skirtices[nq++] = v2;
				skirtices[nq++] = skirt;

			}
			if(v3.x == 0 || v3.z == 0 || v3.x == max || v3.z == max){
				Vector3f skirt = new Vector3f(v3.x , -1, v3.z);
				if(v3.equals(new Vector3f(min,1,max))){
					skirtices[nq++] = v2;
					skirtices[nq++] = new Vector3f(v2.x, -1, v2.z);
				}
				skirtices[nq++] = v3;
				skirtices[nq++] = skirt;

				
			}
			if(v4.x == 0 || v4.z == 0 || v4.x == max || v4.z == max){
				Vector3f skirt = new Vector3f(v4.x , -1, v4.z);
				if(v4.equals(new Vector3f(min,1,0))){
					skirtices[nq++] = v1;
					skirtices[nq++] = new Vector3f(v1.x, -1, v1.z);
				}else if(v4.equals(new Vector3f(max,1,sizeSubMin))){
					skirtices[nq++] = v3;
					skirtices[nq++] = new Vector3f(v3.x, -1, v3.z);
				}
				skirtices[nq++] = v4;
				skirtices[nq++] = skirt;

			} }
			}
			
		}
		quadSkirt = skirtices;
		
		return v;
	}

	private static  Vector3f[] getPolygon(int x, int y, float increment){
		//creates two triangles
		Vector3f[] r = new Vector3f[4];
		Vector3f back_left = new Vector3f(0,0,0);
		Vector3f front_right = new Vector3f(0,0,0);
		Vector3f front_left = new Vector3f(0,0,0);
		Vector3f back_right = new Vector3f(0,0,0);
		float xf = x;
		float yf = y;

	 back_left = new Vector3f (xf*increment, 1 , yf*increment);
	 front_right = new Vector3f ((xf*increment) + increment, 1, (yf*increment)+increment);
	 front_left = new Vector3f (xf*increment, 1 , (yf*increment)+increment);
	 back_right = new Vector3f((xf*increment) + increment, 1, yf*increment);
	 
		r[0] = ((back_left));
		r[1] = ((front_left));
		r[2] = ((front_right));
		r[3] = ((back_right));

		return r;
	}
	static Vector3f[] concat(Vector3f[] A, Vector3f[] B) {
		   int aLen = A.length;
		   int bLen = B.length;
		   Vector3f[] C= new Vector3f[aLen+bLen];
		   System.arraycopy(A, 0, C, 0, aLen);
		   System.arraycopy(B, 0, C, aLen, bLen);
		   return C;
		}
}

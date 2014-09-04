
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL40.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix4f;

/**
 * Creates a sphere with a tessellation shader
 */
public class Sphere{
	private int indicesLength;
	private int vertsLength;
	private int sphereVBO;
	private int sphereVAO;
	private int sphereIBO;
	private int programID;
	private float tessInner, tessOuter;
	private float radius;
	private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	/**
	 * 
	 * @param program File name of the program to use 
	 * @param tessInner tessellation inner level
	 * @param tessOuter tessellation outer level
	 */
	public Sphere(String program, float radius, float tessInner, float tessOuter){
		programID = ShaderLoader.loadProgram(program, "common.glsl");
		this.tessInner = tessInner;
		this.tessOuter = tessOuter;
		this.radius = radius;
		
		CreateIcosahedron();
	}

public void CreateIcosahedron()
{
     int Faces[] = {
        2, 1, 0,
        3, 2, 0,
        4, 3, 0,
        5, 4, 0,
        1, 5, 0,
        11, 6,  7,
        11, 7,  8,
        11, 8,  9,
        11, 9,  10,
        11, 10, 6,
        1, 2, 6,
        2, 3, 7,
        3, 4, 8,
        4, 5, 9,
        5, 1, 10,
        2,  7, 6,
        3,  8, 7,
        4,  9, 8,
        5, 10, 9,
        1, 6, 10 };

     float Verts[] = {
         0.000f,  0.000f,  1.000f,
         0.894f,  0.000f,  0.447f,
         0.276f,  0.851f,  0.447f,
        -0.724f,  0.526f,  0.447f,
        -0.724f, -0.526f,  0.447f,
         0.276f, -0.851f,  0.447f,
         0.724f,  0.526f, -0.447f,
        -0.276f,  0.851f, -0.447f,
        -0.894f,  0.000f, -0.447f,
        -0.276f, -0.851f, -0.447f,
         0.724f, -0.526f, -0.447f,
         0.000f,  0.000f, -1.000f};

      vertsLength = Verts.length;
      indicesLength = Faces.length;
 FloatBuffer vertices = BufferUtils.createFloatBuffer(vertsLength);
 IntBuffer indices = BufferUtils.createIntBuffer(indicesLength);
    for(int i = 0; i < vertsLength; i++){
    	vertices.put(Verts[i]);
    }
    for(int i = 0; i < indicesLength; i++){
    	indices.put(Faces[i]);
    }
    vertices.flip();
    indices.flip();
    sphereVAO = glGenVertexArrays();
	glBindVertexArray(sphereVAO);
	
	sphereVBO = glGenBuffers();
	glBindBuffer(GL_ARRAY_BUFFER, sphereVBO);
	glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);
	glVertexAttribPointer(0, 3, GL_FLOAT, false, 12, 0);

	
	glBindBuffer(GL_ARRAY_BUFFER, 0);
	glBindVertexArray(0);
	
	sphereIBO = glGenBuffers();

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereIBO);

	glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices , GL_STATIC_DRAW);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

}
public void draw(Matrix4f projection, Matrix4f view){
	projection.store(matrixBuffer); 
	matrixBuffer.flip();
	glUniformMatrix4(glGetUniformLocation(programID, "projectionMatrix"), false, matrixBuffer);
	view.store(matrixBuffer);
	matrixBuffer.flip();
	glUniformMatrix4(glGetUniformLocation(programID, "viewMatrix"), false, matrixBuffer);
	glUniform1f(glGetUniformLocation(programID, "TessLevelInner"), tessInner);
	glUniform1f(glGetUniformLocation(programID, "TessLevelOuter"), tessOuter);
	glBindVertexArray(sphereVAO);	
	glEnableVertexAttribArray(0);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereIBO);	
	glPatchParameteri(GL_PATCH_VERTICES, 3);
	glDrawElements(GL_PATCHES, indicesLength, GL_UNSIGNED_INT, 0);	
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	glDisableVertexAttribArray(0);
	glBindVertexArray(0); 
	glUseProgram(0);	
}
public int getIndicesLength() {
	return indicesLength;
}
public void setIndicesLength(int indicesLength) {
	this.indicesLength = indicesLength;
}
public int getVertsLength() {
	return vertsLength;
}
public void setVertsLength(int vertsLength) {
	this.vertsLength = vertsLength;
}
public int getSphereVBO() {
	return sphereVBO;
}
public void setSphereVBO(int sphereVBO) {
	this.sphereVBO = sphereVBO;
}
public int getSphereVAO() {
	return sphereVAO;
}
public void setSphereVAO(int sphereVAO) {
	this.sphereVAO = sphereVAO;
}
public int getSphereIBO() {
	return sphereIBO;
}
public void setSphereIBO(int sphereIBO) {
	this.sphereIBO = sphereIBO;
}
public int getProgramID() {
	return programID;
}
public void setProgramID(int programID) {
	this.programID = programID;
}
public float getTessInner() {
	return tessInner;
}
public void setTessInner(float tessInner) {
	this.tessInner = tessInner;
}
public float getTessOuter() {
	return tessOuter;
}
public void setTessOuter(float tessOuter) {
	this.tessOuter = tessOuter;
}
public float getRadius() {
	return radius;
}
public void setRadius(float radius) {
	this.radius = radius;
}
public FloatBuffer getMatrixBuffer() {
	return matrixBuffer;
}
public void setMatrixBuffer(FloatBuffer matrixBuffer) {
	this.matrixBuffer = matrixBuffer;
}


}
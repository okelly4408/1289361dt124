import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL20.*;


public class AtmosphereRenderer {
	private Matrix4f view;
	private Matrix4f projection;
	private Camera cam;
	private Sphere s;
	private float radius;
	public AtmosphereRenderer(Matrix4f view, Matrix4f projection, float radius, Camera cam){
		this.view = view;
		this.projection = projection;
		this.cam = cam;
		this.radius = radius;
		AtmosphericPrecomputations.precompute();
		s = new Sphere("atmosphere.glsl", radius, 15, 14);
	}
	public void update(Vector3f lightDir){
		int prog = s.getProgramID();
		Vector3f camLoc = cam.getPosition();
		glUseProgram(prog);
		glUniform1i(glGetUniformLocation(prog, "transmittanceSampler"), AtmosphericPrecomputations.transmittanceUnit);
		glUniform1i(glGetUniformLocation(prog, "inscatterSampler"), AtmosphericPrecomputations.inscatterUnit);
		glUniform1f(glGetUniformLocation(prog, "radius"), radius);
		glUniform3f(glGetUniformLocation(prog, "camera"), camLoc.x, camLoc.y, camLoc.z);
		glUniform3f(glGetUniformLocation(prog, "lightDir"), lightDir.x, lightDir.y, lightDir.z);
		s.draw(projection, view);
		glUseProgram(0);
	}
}

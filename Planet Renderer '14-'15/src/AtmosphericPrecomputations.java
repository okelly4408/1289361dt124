import static java.lang.Math.sqrt;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_3D;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL20.glBlendEquationSeparate;
import static org.lwjgl.opengl.GL20.glDrawBuffers;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT1;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RGB16F;
import static org.lwjgl.opengl.GL30.GL_RGBA16F;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteFramebuffers;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;


public class AtmosphericPrecomputations {
	static final int transmittanceUnit = 0;
	static final int irradianceUnit = 1;
	static final int inscatterUnit = 2;
	static final int deltaEUnit = 3;
	static final int deltaSRUnit = 4;
	static final int deltaSMUnit = 5;
	static final int deltaJUnit = 6;

	private static int transmittanceTexture;//unit 1, T table
	private static int irradianceTexture;//unit 2, E table
	private static int inscatterTexture;//unit 3, S table
	private static int deltaETexture;//unit 4, deltaE table
	private static int deltaSRTexture;//unit 5, deltaS table (Rayleigh part)
	private static  int deltaSMTexture;//unit 6, deltaS table (Mie part)
	private static int deltaJTexture;
	
	 private static final float Rg = 6360.0f;
	 private static final float Rt = 6420.0f;

	 private static final int SKY_W = 64;
	 private static final int SKY_H = 16;

	 private static final int RES_R = 32;
	 private static final int RES_MU = 128;
	 private static final int RES_MU_S = 32;
	 private static final int RES_NU = 8;
	
	public static void precompute(){
		
		glActiveTexture(GL_TEXTURE0 + transmittanceUnit);
		transmittanceTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, transmittanceTexture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, 256, 64, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);
	    
	    glActiveTexture(GL_TEXTURE0 + deltaEUnit);
	    deltaETexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, deltaETexture);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, 64, 16, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);
	    
	    glActiveTexture(GL_TEXTURE0 + deltaSRUnit);
	    deltaSRTexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_3D, deltaSRTexture);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	    glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB16F, RES_MU_S * RES_NU, RES_MU, RES_R, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);

	    glActiveTexture(GL_TEXTURE0 + deltaSMUnit);
	    deltaSMTexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_3D, deltaSMTexture);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	    glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB16F, RES_MU_S * RES_NU, RES_MU, RES_R, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);
	    
	    glActiveTexture(GL_TEXTURE0 + irradianceUnit);
	    irradianceTexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, irradianceTexture);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB16F, SKY_W, SKY_H, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);

	    glActiveTexture(GL_TEXTURE0 + inscatterUnit);
	    inscatterTexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_3D, inscatterTexture);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	    glTexImage3D(GL_TEXTURE_3D, 0, GL_RGBA16F, RES_MU_S * RES_NU, RES_MU, RES_R, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);

	    glActiveTexture(GL_TEXTURE0 + deltaJUnit);
	    deltaJTexture = glGenTextures();
	    glBindTexture(GL_TEXTURE_3D, deltaJTexture);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
	    glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
	    glTexImage3D(GL_TEXTURE_3D, 0, GL_RGB16F, RES_MU_S * RES_NU, RES_MU, RES_R, 0, GL_RGB, GL_FLOAT, (java.nio.ByteBuffer)null);
	    
	    int framebuffer = glGenFramebuffers();
	    glBindFramebuffer(GL_FRAMEBUFFER, framebuffer);
	    glReadBuffer(GL_COLOR_ATTACHMENT0);
	    glDrawBuffer(GL_COLOR_ATTACHMENT0);
	    
	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, transmittanceTexture, 0);
	    glViewport(0,0,256,64);
	    int transmittanceProg = ShaderLoader.loadProgram("transmittance.glsl", "common.glsl");
	    glUseProgram(transmittanceProg);
	    drawQuad();
	    
	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, deltaETexture, 0);
	    glViewport(0,0,64,16);
	    int irradiance1Prog = ShaderLoader.loadProgram("irradiance1.glsl", "common.glsl");
	    glUseProgram(irradiance1Prog);
	    glUniform1i(glGetUniformLocation(irradiance1Prog, "transmittanceSampler"), transmittanceUnit);
	    drawQuad();
	   
	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, deltaSRTexture, 0);
	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, deltaSMTexture, 0);
	    IntBuffer drawBuffs = BufferUtils.createIntBuffer(2);
	    drawBuffs.put(0, GL_COLOR_ATTACHMENT0);
	    drawBuffs.put(1, GL_COLOR_ATTACHMENT1);
	    glDrawBuffers(drawBuffs);
	    glViewport(0, 0, RES_MU_S * RES_NU, RES_MU);
	    int inscatter1Prog = ShaderLoader.loadProgram("inscatter1.glsl", "common.glsl");
	    glUseProgram(inscatter1Prog);
	    glUniform1i(glGetUniformLocation(inscatter1Prog, "transmittanceSampler"), transmittanceUnit);
	    for (int layer = 0; layer < RES_R; ++layer) {
	        setLayer(inscatter1Prog, layer);
	        drawQuad();
	    }
	    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, 0, 0);
	    glDrawBuffer(GL_COLOR_ATTACHMENT0);

	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, irradianceTexture, 0);
	    glViewport(0, 0, SKY_W, SKY_H);
	    int copyIrradianceProg = ShaderLoader.loadProgram("copyIrradiance.glsl", "common.glsl");
	    glUseProgram(copyIrradianceProg);
	    glUniform1f(glGetUniformLocation(copyIrradianceProg, "k"), 0.0f);
	    glUniform1i(glGetUniformLocation(copyIrradianceProg, "deltaESampler"), deltaEUnit);
	    drawQuad();
	    
	    glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, inscatterTexture, 0);
	    glViewport(0, 0, RES_MU_S * RES_NU, RES_MU);
	    int copyInscatter1Prog = ShaderLoader.loadProgram("copyInscatter1.glsl", "common.glsl");
	    glUseProgram(copyInscatter1Prog);
	    glUniform1i(glGetUniformLocation(copyInscatter1Prog, "deltaSRSampler"), deltaSRUnit);
	    glUniform1i(glGetUniformLocation(copyInscatter1Prog, "deltaSMSampler"), deltaSMUnit);
	    for (int layer = 0; layer < RES_R; ++layer) {
	        setLayer(copyInscatter1Prog, layer);
	        drawQuad(); 
	    }
	    int jProg = ShaderLoader.loadProgram("jprog.glsl", "common.glsl");
	    int irradianceNProg = ShaderLoader.loadProgram("irradianceN.glsl", "common.glsl");
	    int inscatterNProg = ShaderLoader.loadProgram("inscatterN.glsl", "common.glsl");
	    for (int order = 2; order <= 5; ++order) {

	        // computes deltaJ (line 7 in algorithm 4.1)
	        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, deltaJTexture, 0);
	        glViewport(0, 0, RES_MU_S * RES_NU, RES_MU);
	        
	        glUseProgram(jProg);
	        glUniform1f(glGetUniformLocation(jProg, "first"), order == 2 ? 1.0f : 0.0f);
	        glUniform1i(glGetUniformLocation(jProg, "transmittanceSampler"), transmittanceUnit);
	        glUniform1i(glGetUniformLocation(jProg, "deltaESampler"), deltaEUnit);
	        glUniform1i(glGetUniformLocation(jProg, "deltaSRSampler"), deltaSRUnit);
	        glUniform1i(glGetUniformLocation(jProg, "deltaSMSampler"), deltaSMUnit);
	        for (int layer = 0; layer < RES_R; ++layer) {
	            setLayer(jProg, layer);
	            drawQuad();
	        }

	        // computes deltaE (line 8 in algorithm 4.1)
	        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, deltaETexture, 0);
	        glViewport(0, 0, SKY_W, SKY_H);

	        glUseProgram(irradianceNProg);
	        glUniform1f(glGetUniformLocation(irradianceNProg, "first"), order == 2 ? 1.0f : 0.0f);
	        glUniform1i(glGetUniformLocation(irradianceNProg, "transmittanceSampler"), transmittanceUnit);
	        glUniform1i(glGetUniformLocation(irradianceNProg, "deltaSRSampler"), deltaSRUnit);
	        glUniform1i(glGetUniformLocation(irradianceNProg, "deltaSMSampler"), deltaSMUnit);
	        drawQuad();

	        // computes deltaS (line 9 in algorithm 4.1)
	        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, deltaSRTexture, 0);
	        glViewport(0, 0, RES_MU_S * RES_NU, RES_MU);
	       
	        glUseProgram(inscatterNProg);
	        glUniform1f(glGetUniformLocation(inscatterNProg, "first"), order == 2 ? 1.0f : 0.0f);
	        glUniform1i(glGetUniformLocation(inscatterNProg, "transmittanceSampler"), transmittanceUnit);
	        glUniform1i(glGetUniformLocation(inscatterNProg, "deltaJSampler"), deltaJUnit);
	        for (int layer = 0; layer < RES_R; ++layer) {
	            setLayer(inscatterNProg, layer);
	            drawQuad();
	        }

	        glEnable(GL_BLEND);
	        glBlendEquationSeparate(GL_FUNC_ADD, GL_FUNC_ADD);
	        glBlendFuncSeparate(GL_ONE, GL_ONE, GL_ONE, GL_ONE);

	        // adds deltaE into irradiance texture E (line 10 in algorithm 4.1)
	        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, irradianceTexture, 0);
	        glViewport(0, 0, SKY_W, SKY_H);
	        glUseProgram(copyIrradianceProg);
	        glUniform1f(glGetUniformLocation(copyIrradianceProg, "k"), 1.0f);
	        glUniform1i(glGetUniformLocation(copyIrradianceProg, "deltaESampler"), deltaEUnit);
	        drawQuad();

	        // adds deltaS into inscatter texture S (line 11 in algorithm 4.1)
	        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, inscatterTexture, 0);
	        glViewport(0, 0, RES_MU_S * RES_NU, RES_MU);
	        int copyInscatterNProg = ShaderLoader.loadProgram("copyInscatterN.glsl", "common.glsl");
	        glUseProgram(copyInscatterNProg);
	        glUniform1i(glGetUniformLocation(copyInscatterNProg, "deltaSSampler"), deltaSRUnit);
	        for (int layer = 0; layer < RES_R; ++layer) {
	            setLayer(copyInscatterNProg, layer);
	            drawQuad();
	        }

	        glDisable(GL_BLEND);
	    } 

		glDeleteTextures(deltaETexture);
		glDeleteTextures(deltaSMTexture);
		glDeleteTextures(deltaSRTexture);
		glDeleteTextures(deltaJTexture);
		
	    glDeleteFramebuffers(framebuffer);
	    glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	private static void setLayer(int prog, int layer)
	{
	    double r = layer / (RES_R - 1.0);
	    r = r * r;
	    r = sqrt(Rg * Rg + r * (Rt * Rt - Rg * Rg)) + (layer == 0 ? 0.01 : (layer == RES_R - 1 ? -0.001 : 0.0));
	    double dmin = Rt - r;
	    double dmax = sqrt(r * r - Rg * Rg) + sqrt(Rt * Rt - Rg * Rg);
	    double dminp = r - Rg;
	    double dmaxp = sqrt(r * r - Rg * Rg);
	    glUniform1f(glGetUniformLocation(prog, "r"), (float)r);
	    glUniform4f(glGetUniformLocation(prog, "dhdH"), (float)(dmin), (float)(dmax), (float)(dminp), (float)(dmaxp));
	    glUniform1i(glGetUniformLocation(prog, "layer"), layer);
	}
	private static void drawQuad(){
		int vao = glGenVertexArrays();
		glBindVertexArray(vao);
		glDrawArrays(GL_POINTS, 0, 1);	
	}
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.opengl.GL40.*;

public class ShaderLoader {
	
	//in pipeline order...
	private static int type_Vertex = GL_VERTEX_SHADER;
	private static int type_TesselationCont = GL_TESS_CONTROL_SHADER;
	private static int type_TesselationEval = GL_TESS_EVALUATION_SHADER;
	private static int type_Geometry = GL_GEOMETRY_SHADER;
	private static int type_Fragment = GL_FRAGMENT_SHADER;

	
	private static String glslVersion = "\n #version 400 core \n";
	
	private static int compileShader(String source, String shader, int type){
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, shader);
		glCompileShader(shaderID);
		String log = glGetShaderInfoLog(shaderID, 10000);
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE){
			String stype = "";
			if(type == type_Vertex) stype = "vertex";
			if(type == type_Fragment) stype = "fragment";
			if(type == type_Geometry) stype = "geometry";
			if(type == type_TesselationCont) stype = "tesselation control";
			if(type == type_TesselationEval) stype = "tesselation evaluation";
			System.err.println("Error Compiling: " + source + ", type: "+stype);
			System.err.println(log);
			System.exit(-1);
		}

		return shaderID;
	}
	
	private static String readFile(String fileName) throws IOException {
		if(!fileName.startsWith("Shaders/")){
			StringBuilder b = new StringBuilder(fileName);
			b.insert(0, "Shaders/");
			fileName = b.toString();
		}
		BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }

	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
	/**
	 * Loads shaders and links them to a program
	 * @param String main shader file extension
	 * @return ProgramID
	 */
	public static int loadProgram(String main){
		int programID = glCreateProgram();
		String s = "";
		boolean hasGeo = false;
		boolean hasTess = false;
		int vertexShader = -1, fragmentShader = -1, geometryShader = -1, tessControlShader = -1, tessEvaluationShader = -1;
			try {
				 s = readFile(main);
			} catch (IOException e) {
				e.printStackTrace();
			}

			BufferedReader bufReader = new BufferedReader(new StringReader(s));

			String line=null;

			try {
				while( (line=bufReader.readLine()) != null )
				{
					if(line.equals("#ifdef VERTEX")){
						StringBuilder b = new StringBuilder(s);
						b.insert(0, "\n #define VERTEX \n");
						b.insert(0,glslVersion);
						String vert = b.toString();
						vertexShader = compileShader(main, vert, type_Vertex);
					}
					if(line.equals("#ifdef TESS_CONTROL")){
						hasTess = true;
						StringBuilder b = new StringBuilder(s);
						b.insert(0, " \n #define TESS_CONTROL \n");
						b.insert(0, glslVersion);
						String tessC = b.toString();
						tessControlShader = compileShader(main, tessC, type_TesselationCont);
					}
					if(line.equals("#ifdef TESS_EVAL")){
						hasTess = true;
						StringBuilder b = new StringBuilder(s);
						b.insert(0, " \n #define TESS_EVAL \n");
						b.insert(0, glslVersion);
						String tessE = b.toString();
						tessEvaluationShader = compileShader(main, tessE, type_TesselationEval);
					}
					if(line.equals("#ifdef GEOMETRY")){
						hasGeo = true;
						StringBuilder b = new StringBuilder(s);
						b.insert(0, " \n #define GEOMETRY \n");
						b.insert(0, glslVersion);
						String geo = b.toString();
						geometryShader = compileShader(main, geo, type_Geometry);
					}
					 if(line.equals("#ifdef FRAGMENT")){
						StringBuilder b = new StringBuilder(s);
						b.insert(0, "\n #define FRAGMENT \n");
						b.insert(0, glslVersion);
						String frag = b.toString();
						fragmentShader = compileShader(main, frag, type_Fragment);
						break;
					}
					
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			if(hasTess && hasGeo){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, tessControlShader);
				glAttachShader(programID, tessEvaluationShader);
				glAttachShader(programID, geometryShader);
				glAttachShader(programID, fragmentShader);
			}
			if(hasTess && !hasGeo){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, tessControlShader);
				glAttachShader(programID, tessEvaluationShader);
				glAttachShader(programID, fragmentShader);
			}
			if(hasGeo && !hasTess){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, geometryShader);
				glAttachShader(programID, fragmentShader);
			}else{
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, fragmentShader);
			}
			
		glBindAttribLocation(programID, 0, "inPosition");
		glBindAttribLocation(programID, 1, "inTexCoords");
		
		glLinkProgram(programID);
		glValidateProgram(programID);
		
		return programID;
	}
	/**
	 * Loads shaders and links them to a program
	 * @param String main shader file extension
	 * @param String common shader file extension for a common shader file to be included
	 * @return ProgramID
	 */
	public static int loadProgram(String main, String common){
		int programID = glCreateProgram();
		String s = "";
		String c = "";
		boolean hasGeo = false, hasTess = false;
		boolean useCommonFrag = false, useCommonGeo = false, useCommonVert = false, useCommonTessE = false, useCommonTessC = false;
		int vertexShader = -1, fragmentShader = -1, geometryShader = -1, tessControlShader = -1, tessEvaluationShader = -1;
			try {
				 s = readFile(main);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				 c = readFile(common);
				 BufferedReader reader = new BufferedReader(new StringReader(c));
				 String line = null;
				 while((line = reader.readLine()) != null){
					 if(line.equals("#ifdef VERTEX")){
						 	useCommonVert = true;
					 }
					 if(line.equals("#ifdef TESS_CONTROL")){
				 			useCommonTessC = true;
				 	}
					 if(line.equals("#ifdef TESS_EVAL")){
				 			useCommonTessE = true;
				 	}
					 if(line.equals("#ifdef FRAGMENT")){
					
						 	useCommonFrag = true;
					 }
				 	if(line.equals("#ifdef GEOMETRY")){
				 			useCommonGeo = true;
				 	}
				 }
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			BufferedReader bufReader = new BufferedReader(new StringReader(s));

			String line=null;

			try {
				while( (line=bufReader.readLine()) != null )
				{
					if(line.equals("#ifdef VERTEX")){
						StringBuilder b = new StringBuilder(s);
						if(useCommonVert){
							b.insert(0, "\n"+c + "\n");		
						}					
						b.insert(0, "\n #define VERTEX \n");
						b.insert(0, glslVersion);
						String vert = b.toString();
						
						vertexShader = compileShader(main, vert, type_Vertex);
					}
					if(line.equals("#ifdef TESS_CONTROL")){
						hasTess = true;
						StringBuilder b = new StringBuilder(s);
						if(useCommonTessC){
							b.insert(0, "\n"+c + "\n");		
						}
						b.insert(0, " \n #define TESS_CONTROL \n");
						b.insert(0, glslVersion);
						String tessC = b.toString();
						tessControlShader = compileShader(main, tessC, type_TesselationCont);
					}
					if(line.equals("#ifdef TESS_EVAL")){
						hasTess = true;
						StringBuilder b = new StringBuilder(s);
						if(useCommonTessE){
							b.insert(0, "\n"+c + "\n");		
						}
						b.insert(0, " \n #define TESS_EVAL \n");
						b.insert(0, glslVersion);
						String tessE = b.toString();
						tessEvaluationShader = compileShader(main, tessE, type_TesselationEval);
					}
					if(line.equals("#ifdef GEOMETRY")){
						hasGeo = true;
						StringBuilder b = new StringBuilder(s);
						if(useCommonGeo){
							b.insert(0, "\n"+c + "\n");		
						}	
						b.insert(0, "\n #define GEOMETRY \n");
						b.insert(0, glslVersion);
						String geo = b.toString();
						geometryShader = compileShader(main, geo, type_Geometry);
					}
					 if(line.equals("#ifdef FRAGMENT")){
						StringBuilder b = new StringBuilder(s);
						if(useCommonFrag){
							b.insert(0, "\n"+c + "\n");		
						}	
						b.insert(0, "\n #define FRAGMENT \n");
						b.insert(0, glslVersion);
						String frag = b.toString();
						fragmentShader = compileShader(main, frag, type_Fragment);
						break;
					}
					
				}
			} catch (IOException e) {

				e.printStackTrace();
			}
			
			if(hasGeo){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, geometryShader);
				glAttachShader(programID, fragmentShader);
			}else{
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, fragmentShader);
			}
			
			if(hasTess && hasGeo){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, tessControlShader);
				glAttachShader(programID, tessEvaluationShader);
				glAttachShader(programID, geometryShader);
				glAttachShader(programID, fragmentShader);
			}
			if(hasTess && !hasGeo){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, tessControlShader);
				glAttachShader(programID, tessEvaluationShader);
				glAttachShader(programID, fragmentShader);
			}
			if(hasGeo && !hasTess){
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, geometryShader);
				glAttachShader(programID, fragmentShader);
			}else{
				glAttachShader(programID, vertexShader);
				glAttachShader(programID, fragmentShader);
			}
			
		glBindAttribLocation(programID, 0, "inPosition");
		glBindAttribLocation(programID, 1, "inTexCoords");
		
		glLinkProgram(programID);
		glValidateProgram(programID);
		
		return programID;
	}
	
}
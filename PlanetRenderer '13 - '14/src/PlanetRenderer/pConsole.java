package PlanetRenderer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Console;
import de.lessvoid.nifty.controls.ConsoleCommands;
import de.lessvoid.nifty.controls.ConsoleCommands.ConsoleCommand;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.lessvoid.nifty.tools.Color;

public class pConsole extends AbstractAppState implements ScreenController{
private Application mainClass;
private ConsoleCommands cc;
static NiftyJmeDisplay niftyDisplay;	
static Console console;
private Properties savedSeeds, savedScattering, savedTerrain, savedWorld;
private int savedSeedsLength;
private int maxKey;
static Color green = new Color(0,1,0,1), 
header = Color.WHITE, 
yellow = new Color(1,1,0,1), 
gray = Color.WHITE;

public void initialize(AppStateManager statemanager, Application app) {
	super.initialize(statemanager, app);
	this.mainClass = app;
	}

public void loadDefaultWorld(){
	InputStream input = null;
	console.output("loading file 'defaultWorld.properties'...", yellow);
	try {
		savedWorld = new Properties();
		input = new FileInputStream("Worlds/defaultWorld.properties");
				
		savedWorld.load(input);
		console.output("loaded file 'defaultWorld.properties' successfully", green);
		int n = 0;
		Enumeration<?> enuKeys = savedWorld.keys();
		while (enuKeys.hasMoreElements()) {
			n++;
			enuKeys.nextElement();
		}
		console.output("file 'defaultWorld.properties' contains "+n+" entries", green);
	} catch (IOException ex) {
		console.outputError("loading file 'defaultWorld.properties' failed");
		console.output("Creating file...", yellow);
		OutputStream output = null;
		try {			 
			boolean suc = false;
			File f = new File("Worlds");
			if(f.exists()){
				console.output("directory found", green);
				suc = true;
			}else{
			 console.output("directory missing, creating...", yellow);
			 suc = new File("Worlds").mkdir();
			}
			if(suc){
		    output = new FileOutputStream("Worlds/defaultWorld.properties");
			
		    savedWorld.put("seed", Long.toString(System.currentTimeMillis()));
		    savedWorld.put("terrain", "defaultTerrain");
		    savedWorld.put("atmosphere", "defaultScattering");
		    
		   savedWorld.store(output, null);
		   
			input = new FileInputStream("Worlds/defaultWorld.properties");			
			
			console.output("successfully created file 'defaultWorld.properties'", green);
			}else{
				console.outputError("failed creating file");	
			}
		} catch (IOException io) {
			console.outputError("failed creating file");	
			return;
			} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
		
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void loadWorldFile(String file){
	InputStream input = null;
	console.output("loading file '"+file+".properties'...", yellow);
	String fileName = file.concat(".properties");
	Properties customWorld;
	try {
		customWorld = new Properties();
		input = new FileInputStream("Worlds/"+fileName);
				
		customWorld.load(input);
		console.output("loaded file '"+fileName+"' successfully", green);
		
		String atmosphereFileName = (String) customWorld.get("atmosphere");
		String terrainFileName = (String) customWorld.get("terrain");
		long seed = Long.parseLong((String) customWorld.get("seed"));
		System.out.println(seed);
		loadScatteringFile(atmosphereFileName);
		loadTerrainFile(terrainFileName, seed);
			
		console.output("world loaded successfully", green);

	} catch (IOException ex) {
		console.outputError("loading file '"+fileName+"' failed");
		return;
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void saveWorldFile(String file){
	console.output("saving file '"+file+".properties'...", yellow);
	OutputStream output = null;
	String fileName = file.concat(".properties");
	Properties customWorld;
	try {
		   customWorld = new Properties();
		   output = new FileOutputStream("Worlds/"+fileName);
		   
		   String terrainFileName = file.concat("_terrain");
		   String atmosphereFileName = file.concat("_atmosphere");
		   
		   saveTerrainFile(terrainFileName);
		   saveScatteringFile(atmosphereFileName);
		   
		   customWorld.put("seed", Long.toString(HeightMap.seed));
		   customWorld.put("terrain", terrainFileName);
		   customWorld.put("atmosphere", atmosphereFileName);
		   
		   customWorld.store(output, null);
		   			
		   console.output("successfully created file '"+fileName+"'", green);

	} catch (IOException ex) {
		console.outputError("saving file '"+fileName+"' failed");
		return;
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
public void loadSavedSeeds(){
	InputStream input = null;
	console.output("loading file 'savedSeeds.properties'...", yellow);
	try {
		savedSeeds = new Properties();
		input = new FileInputStream("savedSeeds.properties");
				
		savedSeeds.load(input);
		console.output("loaded file 'savedSeeds.properties' successfully", green);
		int n = 0;
		Enumeration<?> enuKeys = savedSeeds.keys();
		while (enuKeys.hasMoreElements()) {
			n++;
			enuKeys.nextElement();
		}
		maxKey = getMaxKey();
		savedSeedsLength = n;
		console.output("file 'savedSeeds.properties' contains "+n+" entries", green);
	} catch (IOException ex) {
		console.outputError("loading file 'savedSeeds.properties' failed");
		console.output("Creating file...", yellow);
		OutputStream output = null;
		try {			 
		    output = new FileOutputStream("savedSeeds.properties");
			input = new FileInputStream("savedSeeds.properties");			
			savedSeeds.load(input);
			console.output("successfully created file 'savedSeeds.properties'", green);
		} catch (IOException io) {
			io.printStackTrace();
			return;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
		
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void loadDefaultTerrainVars(){
	InputStream input = null;
	console.output("loading file 'defaultTerrain.properties'...", yellow);
	try {
		savedTerrain = new Properties();
		input = new FileInputStream("Terrains/defaultTerrain.properties");
				
		savedTerrain.load(input);
		console.output("loaded file 'defaultTerrain.properties' successfully", green);
		int n = 0;
		Enumeration<?> enuKeys = savedTerrain.keys();
		while (enuKeys.hasMoreElements()) {
			n++;
			enuKeys.nextElement();
		}
		console.output("file 'defaultTerrain.properties' contains "+n+" entries", green);
	} catch (IOException ex) {
		console.outputError("loading file 'defaultTerrain.properties' failed");
		console.output("Creating file...", yellow);
		OutputStream output = null;
		try {			 
			boolean suc = false;
			File f = new File("Terrains");
			if(f.exists()){
				console.output("directory found", green);
				suc = true;
			}else{
			 console.output("directory missing, creating...", yellow);
			 suc = new File("Terrains").mkdir();
			}
			if(suc){
		    output = new FileOutputStream("Terrains/defaultTerrain.properties");
		    
			savedTerrain.put("m_h", Float.toString(HeightMap.mountain_vars.get("h")));
			savedTerrain.put("m_off", Float.toString(HeightMap.mountain_vars.get("off")));
			savedTerrain.put("m_gain", Float.toString(HeightMap.mountain_vars.get("gain")));
			savedTerrain.put("m_lac", Float.toString(HeightMap.mountain_vars.get("lac")));
			savedTerrain.put("m_num_terraces", Float.toString(HeightMap.mountain_vars.get("num_terraces")));
			savedTerrain.put("m_terrace_slope", Float.toString(HeightMap.mountain_vars.get("terrace_slope")));
			savedTerrain.put("m_lower", Float.toString(HeightMap.mountain_vars.get("lower")));
			savedTerrain.put("m_upper", Float.toString(HeightMap.mountain_vars.get("upper")));
			
			savedTerrain.put("c_frequency", Float.toString(HeightMap.continent_vars.get("frequency")));
			savedTerrain.put("c_lower", Float.toString(HeightMap.continent_vars.get("lower")));
			savedTerrain.put("c_upper", Float.toString(HeightMap.continent_vars.get("upper")));
			
			savedTerrain.put("hmf_h", Float.toString(HeightMap.hmf_vars.get("h")));
			savedTerrain.put("hmf_lac", Float.toString(HeightMap.hmf_vars.get("lac")));
			savedTerrain.put("hmf_off", Float.toString(HeightMap.hmf_vars.get("off")));
			
			savedTerrain.put("fbm_pers", Float.toString(HeightMap.fbm_vars.get("pers")));
			savedTerrain.put("fbm_lac", Float.toString(HeightMap.fbm_vars.get("lac")));
			savedTerrain.put("mars_palette", Boolean.toString(LOD_Control.mars));
			 
			savedTerrain.put("crater_frequency", Float.toString(HeightMap.crater_vars.get("crater_frequency")));
			savedTerrain.put("rim_width", Float.toString(HeightMap.crater_vars.get("rim_width")));
			savedTerrain.put("radius", Float.toString(HeightMap.crater_vars.get("radius")));
			savedTerrain.put("slope", Float.toString(HeightMap.crater_vars.get("slope")));
			savedTerrain.put("depth", Float.toString(HeightMap.crater_vars.get("depth")));
			savedTerrain.put("octaves", Float.toString(HeightMap.crater_vars.get("octaves")));
		    savedTerrain.store(output, null);
		   
			input = new FileInputStream("Terrains/defaultTerrain.properties");			
			
			console.output("successfully created file 'defaultTerrain.properties'", green);
			}else{
				console.outputError("failed creating file");	
			}
		} catch (IOException io) {
			console.outputError("failed creating file");	
			return;
			} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
		
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void loadTerrainFile(String file, long seed){
	InputStream input = null;
	console.output("loading file '"+file+".properties'...", yellow);
	String fileName = file.concat(".properties");
	Properties customTerrain;
	try {
		customTerrain = new Properties();
		input = new FileInputStream("Terrains/"+fileName);
				
		customTerrain.load(input);
		console.output("loaded file '"+fileName+"' successfully", green);
			
		    HeightMap.mountain_vars.put("h", Float.parseFloat((String) customTerrain.get("m_h")));
			HeightMap.mountain_vars.put("off", Float.parseFloat((String) customTerrain.get("m_off")));
			HeightMap.mountain_vars.put("gain", Float.parseFloat((String) customTerrain.get("m_gain")));
			HeightMap.mountain_vars.put("lac", Float.parseFloat((String) customTerrain.get("m_lac")));
			HeightMap.mountain_vars.put("num_terraces", Float.parseFloat((String) customTerrain.get("m_num_terraces")));
			HeightMap.mountain_vars.put("terrace_slope", Float.parseFloat((String) customTerrain.get("m_terrace_slope")));
			HeightMap.mountain_vars.put("lower", Float.parseFloat((String) customTerrain.get("m_lower")));
			HeightMap.mountain_vars.put("upper", Float.parseFloat((String) customTerrain.get("m_upper")));
			
			HeightMap.continent_vars.put("frequency", Float.parseFloat((String) customTerrain.get("c_frequency")));
			HeightMap.continent_vars.put("lower", Float.parseFloat((String) customTerrain.get("c_lower")));
			HeightMap.continent_vars.put("upper", Float.parseFloat((String) customTerrain.get("c_upper")));
			
			HeightMap.hmf_vars.put("hmf_h", Float.parseFloat((String) customTerrain.get("hmf_h")));
			HeightMap.hmf_vars.put("hmf_lac", Float.parseFloat((String) customTerrain.get("hmf_lac")));
			HeightMap.hmf_vars.put("hmf_off", Float.parseFloat((String) customTerrain.get("hmf_off")));
			
			HeightMap.fbm_vars.put("pers", Float.parseFloat((String) customTerrain.get("fbm_pers")));
			HeightMap.fbm_vars.put("lac", Float.parseFloat((String) customTerrain.get("fbm_lac")));
			
			HeightMap.crater_vars.put("radius", Float.parseFloat((String) customTerrain.get("radius")));
			HeightMap.crater_vars.put("depth", Float.parseFloat((String) customTerrain.get("depth")));
			HeightMap.crater_vars.put("crater_frequency", Float.parseFloat((String) customTerrain.get("crater_frequency")));
			HeightMap.crater_vars.put("rim_width", Float.parseFloat((String) customTerrain.get("rim_width")));
			HeightMap.crater_vars.put("slope", Float.parseFloat((String) customTerrain.get("slope")));
			HeightMap.crater_vars.put("octaves", Float.parseFloat((String) customTerrain.get("octaves")));
			
			LOD_Control.mars = Boolean.parseBoolean((String) customTerrain.get("mars_palette"));
			
			if(seed == 0){
		    ReCompute.recompute(HeightMap.seed);
			}else{
		    ReCompute.recompute(seed);
			}

		console.output("file applied successfully", green);

	} catch (IOException ex) {
		console.outputError("loading file '"+fileName+"' failed");
		return;
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void saveTerrainFile(String file){
	console.output("saving file '"+file+".properties'...", yellow);
	OutputStream output = null;
	String fileName = file.concat(".properties");
	Properties customTerrain;
	try {
		   customTerrain = new Properties();
		   output = new FileOutputStream("Terrains/"+fileName);
		   
		    customTerrain.put("m_h", Float.toString(HeightMap.mountain_vars.get("h")));
			customTerrain.put("m_off", Float.toString(HeightMap.mountain_vars.get("off")));
			customTerrain.put("m_gain", Float.toString(HeightMap.mountain_vars.get("gain")));
			customTerrain.put("m_lac", Float.toString(HeightMap.mountain_vars.get("lac")));
			customTerrain.put("m_num_terraces", Float.toString(HeightMap.mountain_vars.get("num_terraces")));
			customTerrain.put("m_terrace_slope", Float.toString(HeightMap.mountain_vars.get("terrace_slope")));
			customTerrain.put("m_lower", Float.toString(HeightMap.mountain_vars.get("lower")));
			customTerrain.put("m_upper", Float.toString(HeightMap.mountain_vars.get("upper")));
			
			customTerrain.put("c_frequency", Float.toString(HeightMap.continent_vars.get("frequency")));
			customTerrain.put("c_lower", Float.toString(HeightMap.continent_vars.get("lower")));
			customTerrain.put("c_upper", Float.toString(HeightMap.continent_vars.get("upper")));
			
			customTerrain.put("hmf_h", Float.toString(HeightMap.hmf_vars.get("h")));
			customTerrain.put("hmf_lac", Float.toString(HeightMap.hmf_vars.get("lac")));
			customTerrain.put("hmf_off", Float.toString(HeightMap.hmf_vars.get("off")));
			
			customTerrain.put("fbm_pers", Float.toString(HeightMap.fbm_vars.get("pers")));
			customTerrain.put("fbm_lac", Float.toString(HeightMap.fbm_vars.get("lac")));
			customTerrain.put("mars_palette", Boolean.toString(LOD_Control.mars));
		   
			customTerrain.put("radius", Float.toString(HeightMap.crater_vars.get("radius")));
			customTerrain.put("rim_width", Float.toString(HeightMap.crater_vars.get("rim_width")));
			customTerrain.put("slope", Float.toString(HeightMap.crater_vars.get("slope")));
			customTerrain.put("octaves", Float.toString(HeightMap.crater_vars.get("octaves")));
			customTerrain.put("depth", Float.toString(HeightMap.crater_vars.get("depth")));
			customTerrain.put("crater_frequency", Float.toString(HeightMap.crater_vars.get("crater_frequency")));
			
		    customTerrain.store(output, null);
		   			
		   console.output("successfully created file '"+fileName+"'", green);

	} catch (IOException ex) {
		console.outputError("saving file '"+fileName+"' failed");
		return;
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


public void loadDefaultScatteringVars(){
	InputStream input = null;
	console.output("loading file 'defaultScattering.properties'...", yellow);
	try {
		savedScattering = new Properties();
		input = new FileInputStream("Atmospheres/defaultScattering.properties");
		
		savedScattering.load(input);
		console.output("loaded file 'defaultScattering.properties' successfully", green);
		int n = 0;
		Enumeration<?> enuKeys = savedScattering.keys();
		while (enuKeys.hasMoreElements()) {
			n++;
			enuKeys.nextElement();
		}
		console.output("file 'defaultScattering.properties' contains "+n+" entries", green);
	} catch (IOException ex) {
		console.outputError("loading file 'defaultScattering.properties' failed");
		console.output("Creating file...", yellow);
		OutputStream output = null;
		try {			 
			boolean suc = false;
			File f = new File("Atmospheres");
			if(f.exists()){
				console.output("directory found", green);
				suc = true;
			}else{
			 console.output("directory missing, creating...", yellow);
			 suc = new File("Atmospheres").mkdir();
			}
			if(suc){
		    output = new FileOutputStream("Atmospheres/defaultScattering.properties");
		    savedScattering.put("Kr", Float.toString(ShaderVars.scattering_floats.get("Kr")));
		    savedScattering.put("Km",  Float.toString(ShaderVars.scattering_floats.get("Km")));
		    savedScattering.put("sun_brightness",  Float.toString(ShaderVars.scattering_floats.get("sun_brightness")));
		    savedScattering.put("g",  Float.toString(ShaderVars.scattering_floats.get("g")));
		    savedScattering.put("scale_depth",  Float.toString(ShaderVars.scattering_floats.get("scale_depth")));
			   
		    savedScattering.put("wave_r", Float.toString(ShaderVars.scattering_vec.get("wavelength").x));
		    savedScattering.put("wave_g", Float.toString(ShaderVars.scattering_vec.get("wavelength").y));
		    savedScattering.put("wave_b", Float.toString(ShaderVars.scattering_vec.get("wavelength").z));
		    
		    savedScattering.put("afs_exposure", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afs_exposure")));
		    savedScattering.put("afa_exposure", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afa_exposure")));
		    savedScattering.put("gfs_exposure", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfs_exposure")));
		    savedScattering.put("gfa_exposure", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfa_exposure")));
		    
		    savedScattering.put("afs_gamma", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afs_gamma")));
		    savedScattering.put("afa_gamma", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afa_gamma")));
		    savedScattering.put("gfs_gamma", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfs_gamma")));
		    savedScattering.put("gfa_gamma", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfa_gamma")));
		    
		   savedScattering.store(output, null);
		   
			input = new FileInputStream("Atmospheres/defaultScattering.properties");			
			
			console.output("successfully created file 'defaultScattering.properties'", green);
			}else{
				console.outputError("failed creating file");	
			}
		} catch (IOException io) {
			console.outputError("failed creating file");	
			return;
			} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	 
		}
		
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void loadScatteringFile(String file){
	InputStream input = null;
	console.output("loading file '"+file+".properties'...", yellow);
	String fileName = file.concat(".properties");
	Properties customScattering;
	try {
		customScattering = new Properties();
		input = new FileInputStream("Atmospheres/"+fileName);
				
		customScattering.load(input);
		console.output("loaded file '"+fileName+"' successfully", green);
		
		ShaderVars.scattering_floats.put("Kr", Float.parseFloat((String) customScattering.get("Kr")));
		ShaderVars.scattering_floats.put("Km", Float.parseFloat((String) customScattering.get("Km")));
		ShaderVars.scattering_floats.put("sun_brightness", Float.parseFloat((String) customScattering.get("sun_brightness")));
		ShaderVars.scattering_floats.put("g", Float.parseFloat((String) customScattering.get("g")));
		ShaderVars.scattering_floats.put("scale_depth", Float.parseFloat((String) customScattering.get("scale_depth")));
		
		LOD_Control.ground_exposures_and_gammas.put("gfs_exposure", Float.parseFloat((String) customScattering.get("gfs_exposure")));
		LOD_Control.ground_exposures_and_gammas.put("gfa_exposure", Float.parseFloat((String) customScattering.get("gfa_exposure")));	
		LOD_Control.ground_exposures_and_gammas.put("gfs_gamma", Float.parseFloat((String) customScattering.get("gfs_gamma")));
		LOD_Control.ground_exposures_and_gammas.put("gfa_gamma", Float.parseFloat((String) customScattering.get("gfa_gamma")));
		
		Atmosphere.sky_exposures_and_gammas.put("afs_exposure", Float.parseFloat((String) customScattering.get("afs_exposure")));
		Atmosphere.sky_exposures_and_gammas.put("afa_exposure", Float.parseFloat((String) customScattering.get("afa_exposure")));	
		Atmosphere.sky_exposures_and_gammas.put("afs_gamma", Float.parseFloat((String) customScattering.get("afs_gamma")));
		Atmosphere.sky_exposures_and_gammas.put("afa_gamma", Float.parseFloat((String) customScattering.get("afa_gamma")));
		
		float r = Float.parseFloat((String) customScattering.get("wave_r"));
		float g = Float.parseFloat((String) customScattering.get("wave_g"));
		float b = Float.parseFloat((String) customScattering.get("wave_b"));
		
		Vector3f wavelength = new Vector3f(r,g,b);
		ShaderVars.scattering_vec.put("wavelength", wavelength);
		
		ShaderVars.initialize(MainClass.radius);
		ReCompute.recomputeGroundScattering();
		Atmosphere.recompute();

		console.output("file applied successfully", green);

	} catch (IOException ex) {
		console.outputError("loading file '"+fileName+"' failed");
		return;
	} finally {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

public void saveScatteringFile(String file){
	console.output("saving file '"+file+".properties'...", yellow);
	OutputStream output = null;
	String fileName = file.concat(".properties");
	Properties customScattering;
	try {
		   customScattering = new Properties();
		   output = new FileOutputStream("Atmospheres/"+fileName);
		   customScattering.put("Kr", Float.toString(ShaderVars.scattering_floats.get("Kr")));
		   customScattering.put("Km",  Float.toString(ShaderVars.scattering_floats.get("Km")));
		   customScattering.put("sun_brightness",  Float.toString(ShaderVars.scattering_floats.get("sun_brightness")));
		   customScattering.put("g",  Float.toString(ShaderVars.scattering_floats.get("g")));
		   customScattering.put("scale_depth",  Float.toString(ShaderVars.scattering_floats.get("scale_depth")));
		   
		   customScattering.put("wave_r", Float.toString(ShaderVars.scattering_vec.get("wavelength").x));
		   customScattering.put("wave_g", Float.toString(ShaderVars.scattering_vec.get("wavelength").y));		   
		   customScattering.put("wave_b", Float.toString(ShaderVars.scattering_vec.get("wavelength").z));
		   
		    customScattering.put("afs_exposure", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afs_exposure")));
		    customScattering.put("afa_exposure", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afa_exposure")));
		    customScattering.put("gfs_exposure", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfs_exposure")));
		    customScattering.put("gfa_exposure", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfa_exposure")));
		    
		    customScattering.put("afs_gamma", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afs_gamma")));
		    customScattering.put("afa_gamma", Float.toString(Atmosphere.sky_exposures_and_gammas.get("afa_gamma")));
		    customScattering.put("gfs_gamma", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfs_gamma")));
		    customScattering.put("gfa_gamma", Float.toString(LOD_Control.ground_exposures_and_gammas.get("gfa_gamma")));
		      
		   customScattering.store(output, null);
		   			
		   console.output("successfully created file '"+fileName+"'", green);

	} catch (IOException ex) {
		console.outputError("saving file '"+fileName+"' failed");
		return;
	} finally {
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}


	public void bind(Nifty nifty, Screen screen) {
		console = screen.findNiftyControl("console", Console.class);
		
		loadSavedSeeds();
		console.output(" ");
		loadDefaultTerrainVars();
		console.output(" ");
		loadDefaultScatteringVars();
		console.output(" ");
		loadDefaultWorld();
		console.output(" ");
		cc = new ConsoleCommands(nifty, console);
		console.output(" ");
		console.output("Planet Renderer v1.0 BETA", header);
        console.output("Use '~' or '/' to toggle console", header);
        console.output("Enter 'help' to see all available commands", header);
        console.output(" ");
        
        ConsoleCommand clear = new clear();
        cc.registerCommand("clear", clear);
        
        ConsoleCommand setWire = new setWire();
        cc.registerCommand("wireframe 0/1", setWire);
        
        ConsoleCommand helpCommand = new HelpCommand();
        cc.registerCommand("help", helpCommand);
        
        ConsoleCommand speedSet = new setSpeed();
        cc.registerCommand("speed <integer>", speedSet);
        
        ConsoleCommand quadCount = new quadCount();
        cc.registerCommand("quadCount", quadCount);
        
        ConsoleCommand saveSeed = new saveSeed();
        cc.registerCommand("save_seed", saveSeed);
        
        ConsoleCommand newWorld = new newWorld();
        cc.registerCommand("new world <optional_seed>", newWorld);
        
        ConsoleCommand quadColors = new quadColors();
        cc.registerCommand("quad colors 0/1", quadColors);
        
        ConsoleCommand hmColors = new hmColors();
        cc.registerCommand("heightmap colors 0/1", hmColors);
        
        ConsoleCommand lodToggle = new lodToggle();
        cc.registerCommand("lod 0/1", lodToggle);
        
        ConsoleCommand frustumToggle = new frustumToggle();
        cc.registerCommand("frustum 0/1", frustumToggle);
        
        ConsoleCommand marsToggle = new marsToggle();
        cc.registerCommand("mars 0/1", marsToggle);
        
        ConsoleCommand camPos = new setCameraPos();
        cc.registerCommand("cam pos <x_y_z>", camPos);
        cc.registerCommand("cam pos home", camPos);
        
        ConsoleCommand setMounttVar = new setMountainVar();
        cc.registerCommand("mountain set h <value>", setMounttVar);
        cc.registerCommand("mountain set off <value>", setMounttVar);
        cc.registerCommand("mountain set gain <value>", setMounttVar);
        cc.registerCommand("mountain set lac <value>", setMounttVar);
        cc.registerCommand("mountain set num_terraces <value>", setMounttVar);
        cc.registerCommand("mountain set terrace_slope <value>", setMounttVar);
        cc.registerCommand("mountain set lower <value>", setMounttVar);
        cc.registerCommand("mountain set upper <value>", setMounttVar);
        
        ConsoleCommand setCraterVar = new setCraterVar();
        cc.registerCommand("crater set radius <value>", setCraterVar);
        cc.registerCommand("crater set slope <value>", setCraterVar);
        cc.registerCommand("crater set depth <value>", setCraterVar);
        cc.registerCommand("crater set cont_frequency <value>", setCraterVar);
        cc.registerCommand("crater set rim_width <value>", setCraterVar);
        cc.registerCommand("crater set octaves <value>", setCraterVar);
        
        ConsoleCommand setHMFVar = new setHMFVar();
        cc.registerCommand("hmf set h <value>", setHMFVar);
        cc.registerCommand("hmf set off <value>", setHMFVar);
        cc.registerCommand("hmf set lac <value>", setHMFVar);
        
        ConsoleCommand setfbmVar = new setfbmVar();
        cc.registerCommand("fbm set pers <value>", setfbmVar);
        cc.registerCommand("fbm set lac <value>", setfbmVar);
        
        ConsoleCommand setContVar = new setContinentVar();
        cc.registerCommand("cont set lower <value>", setContVar);
        cc.registerCommand("cont set upper <value>", setContVar);
        cc.registerCommand("cont set frequency <value>", setContVar);
        
        ConsoleCommand getTerrainVars = new getVars();
        cc.registerCommand("list terrainVars", getTerrainVars);
        cc.registerCommand("list atmosphereVars", getTerrainVars);
        cc.registerCommand("list savedSeeds", getTerrainVars);
        cc.registerCommand("list savedAtmospheres", getTerrainVars);
        cc.registerCommand("list savedTerrains", getTerrainVars);
        cc.registerCommand("list savedWorlds", getTerrainVars);
        
        ConsoleCommand loadSeed = new loadSeed();
        cc.registerCommand("load_seed <key_seed_was_saved_with>", loadSeed);
        cc.registerCommand("load_seed", loadSeed);
        
        ConsoleCommand deleteSeed= new deleteSeed();
        cc.registerCommand("delete_seed <key_world_was_saved_with>", deleteSeed);
        
        ConsoleCommand setAtmosphereVar = new setAtmosphereVar();
        cc.registerCommand("atmosphere set wavelength <r_g_b>", setAtmosphereVar);
        cc.registerCommand("atmosphere set scale_depth <value>", setAtmosphereVar);
        cc.registerCommand("atmosphere set Kr <value>", setAtmosphereVar);
        cc.registerCommand("atmosphere set Km <value>", setAtmosphereVar);
        cc.registerCommand("atmosphere set sun_brightness <value>", setAtmosphereVar);
        cc.registerCommand("atmosphere set g <value>", setAtmosphereVar);
        
        ConsoleCommand setLightVar = new setLightVar();
        cc.registerCommand("light set afs_exposure <value>", setLightVar);
        cc.registerCommand("light set afs_gamma <value>", setLightVar);       
        cc.registerCommand("light set afa_exposure <value>", setLightVar);
        cc.registerCommand("light set afa_gamma <value>", setLightVar);       
        cc.registerCommand("light set gfs_exposure <value>", setLightVar);
        cc.registerCommand("light set gfs_gamma <value>", setLightVar);       
        cc.registerCommand("light set gfa_exposure <value>", setLightVar);
        cc.registerCommand("light set gfa_gamma <value>", setLightVar);
        
        ConsoleCommand toggleAtmosphere = new toggleAtmosphere();
        cc.registerCommand("show atmosphere 0/1", toggleAtmosphere);
        
        ConsoleCommand loadAtmosphere = new loadScatteringVars();
        cc.registerCommand("load_atmosphere <file_name_without_extension>", loadAtmosphere);
        
        ConsoleCommand saveAtmosphere = new saveScatteringVars();
        cc.registerCommand("save_atmosphere <file_name_without_extension>", saveAtmosphere);
        
        ConsoleCommand deleteAtmosphere = new deleteScatteringVars();
        cc.registerCommand("delete_atmosphere <file_name_without_extension>", deleteAtmosphere);
        
        ConsoleCommand loadTerain = new loadTerrainVars();
        cc.registerCommand("load_terrain <file_name_without_extension>", loadTerain);
        
        ConsoleCommand saveTerrain = new saveTerrainVars();
        cc.registerCommand("save_terrain <file_name_without_extension>", saveTerrain);
        
        ConsoleCommand deleteTerrain = new deleteTerrainVars();
        cc.registerCommand("delete_terrain <file_name_without_extension>", deleteTerrain);
        
        ConsoleCommand loadWorld = new loadWorld();
        cc.registerCommand("load_world <file_name_without_extension>", loadWorld);
        
        ConsoleCommand saveWorld = new saveWorld();
        cc.registerCommand("save_world <file_name_without_extension>", saveWorld);
        
        ConsoleCommand deleteWorld = new deleteWorld();
        cc.registerCommand("delete_world <file_name_without_extension>", deleteWorld);
        
        
        ConsoleCommand gc = new gc();
        cc.registerCommand("gc", gc);
       
        cc.enableCommandCompletion(true);
	}
	
	private class loadSeed implements ConsoleCommand {

		@Override
		public void execute(String[] args) {
			//load world <key (optional)> 
		    //if no key is provided, a world is selected randomly from the file
			if(args.length != 1 && args.length != 2){
				console.outputError("command entry error");
				return;
			}else if(args.length == 2 && !isNumeric(args[1])){
				console.outputError("world saved by number "+args[1]+" not found");
				return;
			}else if(args.length == 2 && maxKey < Integer.parseInt(args[1])){
				console.outputError("world not found");
				return;
			}
			
			if(args.length == 1){
				long seed = loadRandomSeed();
				ReCompute.recompute(seed);
				console.output("world with seed "+seed+" successfully loaded", green);				
			}else if(args.length == 2){
				int key = Integer.parseInt(args[1]);
				long seed = loadSeed(key);
				if(seed == 0l){
					return;
				}
				ReCompute.recompute(seed);
				console.output("world with seed "+seed+" successfully loaded", green);	
			}
			
		}
		
	}
	
	private class loadTerrainVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			loadTerrainFile(args[1], 0);
		}
		
	}
	
	private class deleteTerrainVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			
			String fileName = args[1].concat(".properties");
			File f = new File("Terrains/"+fileName); 
			boolean d = f.delete();
			
			if(d) console.output("successfully deleted file '"+fileName+"'", green);
			if(!d) console.outputError("deleting of '"+fileName+"' failed");
		}
		
	}
	
	private class saveTerrainVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			saveTerrainFile(args[1]);
		}
		
	}
	
	private class loadWorld implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			loadWorldFile(args[1]);
		}
		
	}
	
	private class deleteWorld implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			String terrName = args[1].concat("_terrain.properties");
			String atmoName = args[1].concat("_atmosphere.properties");
			
			File fT = new File("Terrains/"+terrName);
			File aT = new File("Atmospheres/"+atmoName);
			File wT = new File("Worlds/"+args[1].concat(".properties"));
			
			boolean b1 = fT.delete();
			boolean b2 = aT.delete();
			boolean b3 = wT.delete();
			
			if(b1 && b2 && b3){ console.output("successfully deleted file '"+wT.getName()+"'", green);}
			else{console.outputError("deleting of '"+wT.getName()+"' failed");}
			
			
			
		}
		
	}
	
	private class saveWorld implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			saveWorldFile(args[1]);
		}
		
	}
	
	private class loadScatteringVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			loadScatteringFile(args[1]);
		}
		
	}
	
	private class deleteScatteringVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			
			String fileName = args[1].concat(".properties");
			File f = new File("Atmospheres/"+fileName); 
			boolean d = f.delete();
			
			if(d) console.output("successfully deleted file '"+fileName+"'", green);
			if(!d) console.outputError("deleting of '"+fileName+"' failed");
		}
		
	}
	
	private class saveScatteringVars implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			saveScatteringFile(args[1]);
		
		}
		
	}
	
	private class saveSeed implements ConsoleCommand {

		@Override
		public void execute(String[] args) {
			//load world <key (optional)> 
		    //if no key is provided, a world is selected randomly from the file
			if(args.length != 1){
				console.outputError("command entry error");
				return;
			}
			int key;
			console.output("saving current seed...", yellow);
			try{
			savedSeedsLength++;
			key = maxKey + 1;
			savedSeeds.put(Integer.toString(maxKey + 1), Long.toString(HeightMap.seed));
			OutputStream out = new FileOutputStream("savedSeeds.properties");
			savedSeeds.store(out, "");
			
			maxKey = getMaxKey();
			}catch(Exception e){
				console.outputError("saving failed");
				return;
			}
			console.output("saved current seed with key "+ key, green);
			
		}
		
	}
	
	private class deleteSeed implements ConsoleCommand {

		@Override
		public void execute(String[] args) {

			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[1])){
				console.outputError("key must be numerical");
				return;
			}
			try{

			savedSeeds.remove((args[1]));
			OutputStream out = new FileOutputStream("savedSeeds.properties");
			savedSeeds.store(out, null);
			savedSeedsLength --;
			
			maxKey = getMaxKey();
			}catch(Exception e){
				console.outputError("deleting failed");
			}
			
			console.output("deleted world with key "+args[1]+" from saved-worlds-file", green);
			
		}
		
	}
	private class setMountainVar implements ConsoleCommand {
  	  @Override
  	  public void execute(final String[] args) {

  		  if (args.length != 4) {
  		        console.outputError("command entry error");
  		        return;
  		      }
  		  else if(!isNumeric(args[3])){
  			 console.outputError("variable '"+args[2] +"' must be numerical");
		        return;
  		  }
  		  else if(!HeightMap.mountain_vars.containsKey(args[2])){
  			console.outputError("variable '"+args[2] +"' not found");
		        return;
  		}
  		
  		
  		HeightMap.mountain_vars.put(args[2], Float.parseFloat(args[3]));
  		ReCompute.recompute(HeightMap.seed);
  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
  		
  	  }
  	}
	
	private class setCameraPos implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 3 && args.length != 5) {
	  			console.outputError("camera position must be 3 dimensional vector");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[2]) && !args[2].equals("home")){
	  			console.outputError("camera position must be 3 dimensional vector");
	  			  return;
	  		  }
	  		  else if(args.length == 5){
	  			  if(!isNumeric(args[2]) || !isNumeric(args[3]) || !isNumeric(args[4])){
	  				console.outputError("camera position must be 3 dimensional vector");
			        return; 
	  			  }
	  		  }
	  		  
	  		  if(args.length == 3 && args[2].equals("home")){
	  			  mainClass.getCamera().setLocation(new Vector3f(MainClass.radius * 2, 0, MainClass.radius));
	  			  mainClass.getCamera().setRotation(new Quaternion(0.0f, 0.90515524f, 0.0f, -0.4250812f));
	  			  console.output("camera positioned at "+mainClass.getCamera().getLocation(), green);
	  		  }else if(args.length == 5){
	  			float x = Float.parseFloat(args[2]);
	  			float y = Float.parseFloat(args[3]);
	  			float z = Float.parseFloat(args[4]);
	  			  mainClass.getCamera().setLocation(new Vector3f(x,y,z));
	  			  console.output("camera positioned at "+mainClass.getCamera().getLocation(), green);
	  		  }
	  		
	  	  }
	  	}
	
	private class setHMFVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  else if(!HeightMap.hmf_vars.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			        return;
	  		}
	  		
	  		
	  		HeightMap.hmf_vars.put(args[2], Float.parseFloat(args[3]));
	  		ReCompute.recompute(HeightMap.seed);
	  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		
	  	  }
	  	}
	
	
	
	private class setContinentVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  else if(!HeightMap.continent_vars.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			        return;
	  		}
	  		
	  		
	  		HeightMap.continent_vars.put(args[2], Float.parseFloat(args[3]));
	  		ReCompute.recompute(HeightMap.seed);
	  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		
	  	  }
	  	}
	
	private class setfbmVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  else if(!HeightMap.fbm_vars.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			        return;
	  		}
	  		
	  		
	  		HeightMap.fbm_vars.put(args[2], Float.parseFloat(args[3]));
	  		ReCompute.recompute(HeightMap.seed);
	  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		
	  	  }
	  	}
	
	private class setCraterVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  else if(!HeightMap.crater_vars.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			        return;
	  		}
	  		
	  		
	  		HeightMap.crater_vars.put(args[2], Float.parseFloat(args[3]));
	  		ReCompute.recompute(HeightMap.seed);
	  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		
	  	  }
	  	}
	
	private class setAtmosphereVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4 && args.length != 6) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(args.length == 4 && !isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  
	  		  else if(!ShaderVars.scattering_floats.containsKey(args[2]) && !ShaderVars.scattering_vec.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			     return;
	  		}
	  		
	  		  if(args.length == 6){
	  			float r = Float.parseFloat(args[3]);
	  			float g = Float.parseFloat(args[4]);
	  			float b = Float.parseFloat(args[5]);
	  			
	  			Vector3f wavelength = new Vector3f(r,g,b);
	  			ShaderVars.scattering_vec.put(args[2], wavelength);
	  			ShaderVars.initialize(MainClass.radius);
	  			ReCompute.recomputeGroundScattering();
	  			Atmosphere.recompute();
	  			
	  			console.output("Variable '"+args[2]+"' set to ("+args[3]+","+args[4]+","+args[5]+")", green);
	  		  }else if(args.length == 4){
	  		
	  		ShaderVars.scattering_floats.put(args[2], Float.parseFloat(args[3]));
	  		ShaderVars.initialize(MainClass.radius);
  			ReCompute.recomputeGroundScattering();
  			Atmosphere.recompute();
	  	    console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		  }
	  	  }
	  	}
	private class setLightVar implements ConsoleCommand {
	  	  @Override
	  	  public void execute(final String[] args) {

	  		  if (args.length != 4) {
	  		        console.outputError("command entry error");
	  		        return;
	  		      }
	  		  else if(!isNumeric(args[3])){
	  			 console.outputError("variable '"+args[2] +"' must be numerical");
			        return;
	  		  }
	  		  else if(!LOD_Control.ground_exposures_and_gammas.containsKey(args[2]) && !Atmosphere.sky_exposures_and_gammas.containsKey(args[2])){
	  			console.outputError("variable '"+args[2] +"' not found");
			        return;
	  		}
	  		if(args[2].startsWith("g")){
	  			LOD_Control.ground_exposures_and_gammas.put(args[2], Float.parseFloat(args[3]));
	  			 console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		}else if(args[2].startsWith("a")){
	  			Atmosphere.sky_exposures_and_gammas.put(args[2], Float.parseFloat(args[3]));
	  			 console.output("Variable '"+args[2]+"' set to "+args[3], green);
	  		}
	  			  		
	  	  }
	  	}
	private class getVars implements ConsoleCommand {
		 
		 public void execute(final String[] args) {
			 if (args.length != 2) {
	 		        console.outputError("command entry error");
	 		        return;
	 		      }
			 if(args[1].equals("terrainVars")){
    		  console.output("-------------------",yellow);
    		  console.output("Terrain Variables",yellow);
    		console.output("-------------------");
    		console.output("Mountain Variables: ",yellow);
    		for (Map.Entry<String,Float> entry : HeightMap.mountain_vars.entrySet()) {
    			
    			  String key = entry.getKey();
    			  Float value = entry.getValue();
    			  console.output(key +" = "+value, gray);
    			}
    		console.output(" ");
    		console.output("Continent Variables: ",yellow);
    		for (Map.Entry<String,Float> entry : HeightMap.continent_vars.entrySet()) {
  			  String key = entry.getKey();
  			  Float value = entry.getValue();
  			  console.output(key +" = "+value, gray);
  			}
    		console.output(" ");
    		console.output("Hybdrid Multifractal Variables: ", yellow);
    		for (Map.Entry<String,Float> entry : HeightMap.hmf_vars.entrySet()) {
    			  String key = entry.getKey();
    			  Float value = entry.getValue();
    			  console.output(key +" = "+value, gray);
    			}
    	    console.output(" ");
    	    console.output("Fractional Brownian Motion Variables: ", yellow);
    	    for (Map.Entry<String,Float> entry : HeightMap.fbm_vars.entrySet()) {
  			  String key = entry.getKey();
  			  Float value = entry.getValue();
  			  console.output(key +" = "+value, gray);
  			}
			 }else if(args[1].equals("atmosphereVars")){
				 console.output("-------------------", yellow);
	    		 console.output("Atmosphere Variables", yellow);
	    		 console.output("-------------------", yellow);
	    		 
	    		 for (Map.Entry<String,Float> entry : ShaderVars.scattering_floats.entrySet()) {
	     			  String key = entry.getKey();
	     			  Float value = entry.getValue();
	     			  console.output(key +" = "+value, gray);
	     			}
	    		 
	    		 for (Map.Entry<String,Vector3f> entry : ShaderVars.scattering_vec.entrySet()) {
	     			  String key = entry.getKey();
	     			  Vector3f value = entry.getValue();
	     			  console.output(key +" = "+value, gray);
	     			}	    		 
	    		 for (Map.Entry<String,Float> entry : Atmosphere.sky_exposures_and_gammas.entrySet()) {
	     			  String key = entry.getKey();
	     			  Float value = entry.getValue();
	     			  console.output(key +" = "+value, gray);
	     			}
	    		 for (Map.Entry<String,Float> entry : LOD_Control.ground_exposures_and_gammas.entrySet()) {
	     			  String key = entry.getKey();
	     			  Float value = entry.getValue();
	     			  console.output(key +" = "+value, gray);
	     			}
	    		
			 }else if(args[1].equals("savedSeeds")){
				 console.output("-------------------", yellow);
	    		 console.output("Saved Seeds", yellow);
	    		 console.output("-------------------", yellow);
				 Enumeration<?> e = savedSeeds.propertyNames();
					while (e.hasMoreElements()) {
						String key = (String) e.nextElement();
						String value = savedSeeds.getProperty(key);
						console.output("Key: " + key + ", Seed: " + value, gray);
					}
			 }else if(args[1].equals("savedAtmospheres")){
				 console.output("-------------------", yellow);
	    		 console.output("Saved Atmospheres", yellow);
	    		 console.output("-------------------", yellow);
				 File[] files = new File("Atmospheres").listFiles();

				 for (File file : files) {
				     if (file.isFile()) {
				         console.output(file.getName());
				     }
				 }
			 }else if(args[1].equals("savedTerrains")){
				 console.output("-------------------", yellow);
	    		 console.output("Saved Terrains", yellow);
	    		 console.output("-------------------", yellow);
				 File[] files = new File("Terrains").listFiles();

				 for (File file : files) {
				     if (file.isFile()) {
				         console.output(file.getName());
				     }
				 }
			 }else if(args[1].equals("savedWorlds")){
				 console.output("-------------------", yellow);
	    		 console.output("Saved Worlds", yellow);
	    		 console.output("-------------------", yellow);
				 File[] files = new File("Worlds").listFiles();

				 for (File file : files) {
				     if (file.isFile()) {
				         console.output(file.getName());
				     }
				 }
			 }
    	  }
	}
	
	private class HelpCommand implements ConsoleCommand {
    	  @Override
    	  public void execute(final String[] args) {
    		 if (args.length != 1) {
		        console.outputError("command entry error");
		        return;
		      }
    		  console.output("-------------------", yellow);
    		  console.output("Supported Commands", yellow);
    		console.output("-------------------", yellow);
    	    for(int i = 0; i<cc.getRegisteredCommands().size(); i++){
    	    	console.output(cc.getRegisteredCommands().get(i), gray);
    	    }
    	  console.output(" ");
    	  }
    	}


	
	private class setWire implements ConsoleCommand{
		@Override
		public void execute(final String[] args){
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[1])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[1]) != 1 && Integer.parseInt(args[1]) != 0){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(1 == Integer.parseInt(args[1])){
				MainClass.useWireframe = true;
				console.output("wireframe on", green);
			}
			if(0 == Integer.parseInt(args[1])){
				MainClass.useWireframe = false;				
				console.output("wireframe off", green);
			}
			
		}
	}
	
	private class lodToggle implements ConsoleCommand{
		@Override
		public void execute(final String[] args){
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[1])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[1]) != 1 && Integer.parseInt(args[1]) != 0){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(1 == Integer.parseInt(args[1])){
				LOD_Control.lodOn = true;
				console.output("LOD detection on", green);
			}
			if(0 == Integer.parseInt(args[1])){
				LOD_Control.lodOn = false;
				console.output("LOD detection off", green);
			}
			
		}
	}
	
	private class frustumToggle implements ConsoleCommand{
		@Override
		public void execute(final String[] args){
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[1])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[1]) != 1 && Integer.parseInt(args[1]) != 0){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(1 == Integer.parseInt(args[1])){
				MainClass.checkFrustum = true;
				console.output("frustum culling on", green);
			}
			if(0 == Integer.parseInt(args[1])){
				MainClass.checkFrustum = false;
				console.output("frustum culling off", green);
			}
			
		}
	}
	
	private class marsToggle implements ConsoleCommand{
		@Override
		public void execute(final String[] args){
			if(args.length != 2){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[1])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[1]) != 1 && Integer.parseInt(args[1]) != 0){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(1 == Integer.parseInt(args[1])){
				if(LOD_Control.mars){
				console.output("Terrain type is mars", green);
				return;
				}else{
				LOD_Control.mars = true;
				console.output("Terrain type switched to mars", green);
				ReCompute.recompute(HeightMap.seed);
				}
			}
			if(0 == Integer.parseInt(args[1])){
				if(!LOD_Control.mars){
					console.output("Terrain type is earth", green);
					return;
					}else{
				LOD_Control.mars = false;
				console.output("Terrain type switched to earth", green);
				ReCompute.recompute(HeightMap.seed);
					}
			}
			
		}
	}
	
	
	private class toggleAtmosphere implements ConsoleCommand{
		@Override
		public void execute(final String[] args){
			if(args.length != 3){
				console.outputError("command entry error");
				return;
			}
			if(!isNumeric(args[2])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[2]) != 1 && Integer.parseInt(args[2]) != 0){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(1 == Integer.parseInt(args[2])){
				if(MainClass.atmosphereOn){
					console.output("atmosphere currently shown", yellow);
					return;
				}else{
				MainClass.getInstance().attachAtmosphere();
				console.output("atmosphere on", green);
				}
			}
			if(0 == Integer.parseInt(args[2])){
				if(!MainClass.atmosphereOn){
					console.output("atmosphere currently not shown", yellow);
					return;
				}else{
				MainClass.getInstance().detachAtmosphere();
				console.output("atmosphere off", green);
				}
			}
			
		}
	}
	
	
	private class setSpeed implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 2){
				console.outputError("command entry error" );
				return;
			} 
			if(!isNumeric(args[1])){
				console.outputError("speed must be in number format");
				return;
			}
			
			MainClass.speed = Integer.parseInt(args[1]);
			MainClass.getInstance().setSpeed(Integer.parseInt(args[1]));
			console.output("speed set to "+args[1]+" m/s", green);
		}
		
	}
	
	private class quadCount implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 1){
				console.outputError("command entry error" );
				return;
			} 
						
			int count = QuadMesh.list.size();
			int visible = QuadMesh.getVisibleLeafCount();
			int leaves = QuadMesh.getLeafCount();
			console.output(count+" nodes in use", green);
			console.output(leaves+ " leaf nodes", green);
			console.output(visible + " visible nodes", green);
			console.output(leaves - visible +" nodes culled", green);
		}
		
	}
	
	private class gc implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 1){
				console.outputError("command entry error" );
				return;
			} 
						
			System.gc();
			console.output("garbage collector ran", green);
		}
		
	}
	
	
	private class quadColors implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 3){
				console.outputError("command entry error" );
				return;
			} 
			if(!isNumeric(args[2])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[2]) != 0 && Integer.parseInt(args[2]) != 1){
				console.outputError("0 = off, 1 = on");
				return;
			}
						
			if(Integer.parseInt(args[2]) == 0){
				LOD_Control.debugColor = false;
				console.output("quad debug colors off", green);
			}else{
				LOD_Control.debugColor = true;
				console.output("quad debug colors on", green);
			}
			
		}
		
	}
	
	private class hmColors implements ConsoleCommand{

		@Override
		public void execute(String[] args) {
			if(args.length != 3){
				console.output("command entry error" );
				return;
			} 
			if(!isNumeric(args[2])){
				console.outputError("0 = off, 1 = on");
				return;
			}
			if(Integer.parseInt(args[2]) != 0 && Integer.parseInt(args[2]) != 1){
				console.outputError("0 = off, 1 = on");
				return;
			}
						
			if(Integer.parseInt(args[2]) == 0){
				LOD_Control.hmView = false;
				console.output("heightmap output colors off", green);
			}else{
				LOD_Control.hmView = true;
				console.output("heightmap output colors on", green);
			}
			
		}
		
	}
		
	
	private class newWorld implements ConsoleCommand{
		//generates a new random world
		@Override
		public void execute(String[] args) {
			long seed = System.currentTimeMillis();
			if(args.length != 3 && args.length != 2){
				console.outputError("command entry error" );
				return;
			} 
			if(args.length == 3 && !isNumeric(args[2])){
				seed = args[2].hashCode();
			}
			if(args.length == 3 && isNumeric(args[2])){
				seed = Long.parseLong(args[2]);
			}
			
			if(args.length == 2){
				pConsole.console.output("creating new world...", pConsole.yellow);
				ReCompute.recompute();
				console.output("new world created with seed "+HeightMap.seed, green);
			}else if(args.length == 3){
				//if a seed is provided, use it to generate the world
				ReCompute.recompute(seed);
				if(isNumeric(args[2])){
				console.output("new world created with seed "+args[2], green);
				}else{
				console.output("new world created with seed '"+args[2]+"' or "+seed, green);	
				}
			}
		}
		
	}
	
	private class clear implements ConsoleCommand{

		@Override
		public void execute(String[] arg0) {
			console.clear();
			console.output("Planet Renderer v1.0 BETA", header);
	        console.output("Use '~' or '/' to toggle console", header);
	        console.output("Enter 'help' to see all available commands", header);
	        console.output(" ");
	        
		}
		
	}
	
	public long loadRandomSeed(){
		//called if no key is provided when world is loaded
		Random r = new Random();
		long seed = 0;
		int seedNumber = r.nextInt(savedSeedsLength) + 1;
		console.output("loading world with key "+seedNumber+"...", yellow);
		try{
		seed = Long.parseLong((String) savedSeeds.get(Integer.toString(seedNumber)));
		}catch(Exception e){
			console.outputError("loading world failed");
		}
		return seed;
		
	}
	
	public long loadSeed(int keyNumber){
		//loads a world based on a provided key number
		long seed = 0;
		console.output("loading world with key "+keyNumber+"...", yellow);
		try{
		seed = Long.parseLong((String) savedSeeds.get(Integer.toString(keyNumber)));
		}catch(Exception e){
			console.outputError("loading world failed");
		}

		return seed;
		
	}
	
	public int getMaxKey(){
		 int max = 0;
		 Enumeration<?> e = savedSeeds.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				int pre = Integer.parseInt(key);
				
				max = Math.max(max, pre);
				
			}
			
		return max;
	}
	
	public void onEndScreen() {		
	}	
	public void onStartScreen() {		
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    @SuppressWarnings("unused")
		float f = Float.parseFloat(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
		
}

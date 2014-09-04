package PlanetRenderer;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;

public class PostProcess {

		public PostProcess(){
			Texture2D screen = new Texture2D(1024, 768, Format.RGBA32F);
			FrameBuffer fbo = new FrameBuffer(1024, 768, 1);
			fbo.setColorTexture(screen);
	//		ViewPort vp = MainClass.getInstance().getViewPort();
	//		vp.setOutputFrameBuffer(fbo);
			
	//		MainClass.getInstance().getRenderManager().createMainView("View", MainClass.getInstance().getCamera());

			Quad q = new Quad(2,2);
			Geometry g = new Geometry("Quad", q);
			Material mat = new Material(MainClass.getInstance().getAssetManager(), "Post.j3md");
			mat.setTexture("screen", screen);
			g.setMaterial(mat);
			
			if(MainClass.getInstance().getViewPort().isEnabled()){
				g.updateGeometricState();
				}
		}
		
}

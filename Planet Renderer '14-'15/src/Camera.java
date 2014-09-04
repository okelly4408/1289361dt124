
import static java.lang.Math.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Camera {

	private float x = 0;
	private float y = 0;
	private float z = 0;
	private float mouseSens = 0;
	private float speed = 0;
	private float pitch = 0;
	private float yaw = 0;
	private float roll = 0;
	
	public Camera(float x, float y, float z, float mouseSens, float speed) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.mouseSens = mouseSens;
		this.speed = speed;
	}
	
	public void processMouse() {

		float mouseDX = Mouse.getDX() * mouseSens * 0.16f;
		float mouseDY = Mouse.getDY() * mouseSens * 0.16f;
		if (yaw + mouseDX >= 360) {
			yaw = yaw + mouseDX - 360;
		}
		else if (yaw + mouseDX < 0) {
			yaw = 360 - yaw + mouseDX;
		}
		else {
			yaw += mouseDX;
			pitch -= mouseDY;
		}
	
	}
	

	
	public void processKeyboard(float delta) {
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft =  Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_Q);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_Z);

        if (keyUp && keyRight && !keyLeft && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            moveFromLook(0, 0, -speed * delta * 0.003f);
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            moveFromLook(-speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            moveFromLook(speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            moveFromLook(0, 0, speed * delta * 0.003f);
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, 0);
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, 0);
        }
        if (flyUp && !flyDown) {
            y += speed * delta * 0.003f;
        }
        if (flyDown && !flyUp) {
            y -= speed * delta * 0.003f;
        }
	}


	
	public void moveFromLook(float dx, float dy, float dz) {
		this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
		this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
		this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
	}

	
	public void setPosition(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	public void setRotation(float pitch, float yaw, float roll) {
		this.pitch = pitch;
		this.yaw = yaw;
		this.roll = roll;
	}
	
	public Matrix4f update(float delta){
		processMouse();
		processKeyboard(delta);
		return applyTranslations();
	}
	
	
	
	public Matrix4f applyTranslations() {
		Matrix4f viewMatrix = new Matrix4f();
		Matrix4f.rotate((float) toRadians(pitch), new Vector3f(1, 0, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) toRadians(yaw), new Vector3f(0, 1, 0), viewMatrix, viewMatrix);
		Matrix4f.rotate((float) toRadians(roll), new Vector3f(0, 0, 1), viewMatrix, viewMatrix);
		Matrix4f.translate(new Vector3f(-x, -y, -z), viewMatrix, viewMatrix);
		return viewMatrix;
	}

	
	public Vector3f getPosition(){
		return new Vector3f(x,y,z);
	}

	
	public float getPitch() {
		return pitch;
	}

	
	public float getYaw() {
		return yaw;
	}

	
	public float getRoll() {
		return roll;
	}



}

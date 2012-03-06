package edu.stanford.cs248.project

import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project
import org.lwjgl.input.Mouse

import scala.math._

class Camera extends Entity { 
  
  var centerX = 0.0f
  var centerY = 0.0f
  var centerZ = 0.0f
  
  // Spherical coordinates with center as the origin
  var camR = 127f
  var camTheta = (60.0/180.0*Pi).asInstanceOf[Float]
  var camPhi = (5.0/4.0*Pi).asInstanceOf[Float]
  
  def loadGLMatrices() {
    GL11.glMatrixMode(GL11.GL_PROJECTION)
    GL11.glLoadIdentity()
    
    // 90 degrees vertical fov, 16:9 aspect ratio
    // clip at 0.1 and 500
    Project.gluPerspective(90, 16.0f/9.0f, 0.1f, 500f)
    
    GL11.glMatrixMode(GL11.GL_MODELVIEW)
    GL11.glLoadIdentity()
    
    val camX = (camR*cos(camPhi)*sin(camTheta) + centerX).asInstanceOf[Float]
    val camY = (camR*sin(camPhi)*sin(camTheta) + centerY).asInstanceOf[Float]
    val camZ = (camR*cos(camTheta) + centerZ).asInstanceOf[Float]
    
    Project.gluLookAt(camX, camY, camZ, centerX, centerY, centerZ, 0, 0, 1) 
  }
  
  override def update() {
    val dx = Mouse.getDX()
    val dy = Mouse.getDY()
    
    if(Mouse.isButtonDown(1)) {    
      val yInvert = 3.0 // no invert
      val mouseSensitivity = 0.005
      
      camTheta += (dy*mouseSensitivity*yInvert).asInstanceOf[Float]
      camPhi   += (dx*mouseSensitivity).asInstanceOf[Float]
      
      // limit movement of camera
      camTheta = max(min(camTheta, (Pi/2).asInstanceOf[Float]), 0)
      camPhi   = (camPhi % (2*Pi)).asInstanceOf[Float] 
      
      println("Camera (r, t, p) = (%f, %f, %f)".format(camR, camTheta, camPhi))
    }
  }
}

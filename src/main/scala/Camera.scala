package edu.stanford.cs248.project

import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project

import scala.math._

class Camera { 
  
  var centerX = 0.0f
  var centerY = 0.0f
  var centerZ = 0.0f
  
  // Spherical coordinates with center as the origin
  var camR = 3.0f
  var camTheta = (30.0/180.0*Pi).asInstanceOf[Float]
  var camPhi = 0.0f
  
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
}

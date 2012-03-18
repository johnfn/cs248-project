package edu.stanford.cs248.project

import org.lwjgl.opengl._
import org.lwjgl.util.glu.Project
import org.lwjgl.input.Mouse

import GL11._
import GL13._
import GL20._

import scala.math._

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.opengl._

class Camera extends Entity {

  var centerX = 0.0f
  var centerY = 0.0f
  var centerZ = 0.0f

  // Spherical coordinates with center as the origin
  var camR = 5f
  var camTheta = (60.0/180.0*Pi).asInstanceOf[Float]
  var camPhi = (5.0/4.0*Pi).asInstanceOf[Float]

  def farClip = 60.0f

  def loadGLMatrices() {
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    multPerspectiveMatrix()

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    multModelViewMatrix()
  }

  def loadIntoTextureMatrices() = {
    glActiveTexture(GL_TEXTURE0)
    glMatrixMode(GL_TEXTURE)
    glLoadIdentity()
    multPerspectiveMatrix()
    
    glActiveTexture(GL_TEXTURE1)
    glMatrixMode(GL_TEXTURE)
    glLoadIdentity()
    multModelViewMatrix()

    glActiveTexture(GL_TEXTURE0)
    glMatrixMode(GL_MODELVIEW)
  }

  def multPerspectiveMatrix() {
    // 90 degrees vertical fov, 16:9 aspect ratio
    // clip at 0.1 and 500
    Project.gluPerspective(90, 16.0f/9.0f, 0.1f, farClip)
  }

  def passInUniforms(shader: Shader) {
    glUniform1f(glGetUniformLocation(shader.id, "farClip"), farClip)
  }

  def multModelViewMatrix() {
    val camX = (camR*cos(camPhi)*sin(camTheta) + centerX).asInstanceOf[Float]
    val camY = (camR*sin(camPhi)*sin(camTheta) + centerY).asInstanceOf[Float]
    val camZ = (camR*cos(camTheta) + centerZ).asInstanceOf[Float]

    Project.gluLookAt(camX, camY, camZ, centerX, centerY, centerZ, 0, 0, 1)
  }

  override def update(m: EntityManager) {
    val dx = Mouse.getDX()
    val dy = Mouse.getDY()
    val CAM_LAG:Float = 15f

    val pr:Protagonist = m.entities.filter(_.traits.contains("protagonist")).head.asInstanceOf[Protagonist]
    centerX = centerX + (pr.x - centerX) / CAM_LAG
    centerY = centerY + (pr.y - centerY) / CAM_LAG
    centerZ = centerZ + (pr.z - centerZ) / CAM_LAG

    if(Mouse.isButtonDown(1)) {
      val yInvert = 3.0 // no invert
      val mouseSensitivity = 0.005

      camTheta += (dy*mouseSensitivity*yInvert).asInstanceOf[Float]
      camPhi   += -(dx*mouseSensitivity).asInstanceOf[Float]

      // limit movement of camera
      camTheta = max(min(camTheta, (Pi-0.001f).asInstanceOf[Float]), 0.001f)
      camPhi   = (camPhi % (2*Pi)).asInstanceOf[Float]

      //println("Camera (r,t,p) = (%f,%f,%f)".format(camR, camTheta, camPhi))
    }
  }
}

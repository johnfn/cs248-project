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

  var camX = 0.0f
  var camY = 0.0f
  var camZ = 0.0f

  // Spherical coordinates with center as the origin
  var camR = 5f
  var camTheta = (60.0/180.0*Pi).asInstanceOf[Float]
  var camPhi = (5.0/4.0*Pi).asInstanceOf[Float]

  def nearClip = 0.1f
  def farClip = 60.0f

  def loadGLMatrices() {
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    multPerspectiveMatrix()

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    multModelViewMatrix()
  }

  def putModelViewMatrixIntoTextureMat(texUnit: Int) = {
    glActiveTexture(GL_TEXTURE0+texUnit)
    glMatrixMode(GL_TEXTURE)
    glLoadIdentity()

    multPerspectiveMatrix()

    glMatrixMode(GL_MODELVIEW)
    glActiveTexture(GL_TEXTURE0)
  }

  def multPerspectiveMatrix() {
    // 90 degrees vertical fov, 16:9 aspect ratio
    // clip at 0.1 and 60
    Project.gluPerspective(90, 16.0f/9.0f, nearClip, farClip)
  }

  def passInUniforms(shader: Shader) {
    glUniform1f(glGetUniformLocation(shader.id, "farClip"), farClip)
  }

  def updateCamPos() {
    camX = centerX
    camY = centerY
    camZ = 10.0f
    /*
    camX = (camR*cos(camPhi)*sin(camTheta) + centerX).asInstanceOf[Float]
    camY = (camR*sin(camPhi)*sin(camTheta) + centerY).asInstanceOf[Float]
    camZ = (camR*cos(camTheta) + centerZ).asInstanceOf[Float]
    */
  }

  def multModelViewMatrix() {
    updateCamPos()

    Project.gluLookAt(eye.x, eye.y, eye.z, lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z)
  }

  def up() = {
    import org.lwjgl.util.vector._

    new Vector3f(0, 1, 0)
  }

  def eye() = {
    import org.lwjgl.util.vector._

    updateCamPos()

    new Vector4f(camX, camY, camZ, 1)
  }

  def lookAt() = {
    import org.lwjgl.util.vector._

    new Vector3f(centerX, centerY, centerZ)
  }

  override def update(m: EntityManager) {
    val dx = Mouse.getDX()
    val dy = Mouse.getDY()
    val CAM_LAG:Float = 15f

    val pr:Protagonist = m.entities.filter(_.traits.contains("protagonist")).head.asInstanceOf[Protagonist]
    centerX = centerX + (pr.x - centerX) / CAM_LAG
    centerY = centerY + (pr.y - centerY) / CAM_LAG
    centerZ = 0.0f //centerZ + (pr.z - centerZ) / CAM_LAG

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

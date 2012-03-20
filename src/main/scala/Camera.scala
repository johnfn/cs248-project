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

import org.lwjgl.util._
import org.lwjgl.util.vector._

class Camera extends Entity {
  var viewMatrix: Matrix4f = new Matrix4f()

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
  def farClip = 1600.0f

  def loadGLMatrices() {
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity()
    multPerspectiveMatrix()

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    multModelViewMatrix()

    storeViewMat()
  }

  def storeViewMat() = {
    import java.nio._
    import org.lwjgl._

    viewMatrix = new Matrix4f()
    var buf: FloatBuffer = BufferUtils.createFloatBuffer(16 * 4);

    // Get the current model view matrix from OpenGL.
    glGetFloat(GL_MODELVIEW_MATRIX, buf);

    buf.rewind();

    viewMatrix.m00 = buf.get(0)
    viewMatrix.m01 = buf.get(1)
    viewMatrix.m02 = buf.get(2)
    viewMatrix.m03 = buf.get(3)

    viewMatrix.m10 = buf.get(4)
    viewMatrix.m11 = buf.get(5)
    viewMatrix.m12 = buf.get(6)
    viewMatrix.m13 = buf.get(7)

    viewMatrix.m20 = buf.get(8)
    viewMatrix.m21 = buf.get(9)
    viewMatrix.m22 = buf.get(10)
    viewMatrix.m23 = buf.get(11)

    viewMatrix.m30 = buf.get(12)
    viewMatrix.m31 = buf.get(13)
    viewMatrix.m32 = buf.get(14)
    viewMatrix.m33 = buf.get(15)
  }

  def facingCamMat() = {
    import java.nio._
    import org.lwjgl._

    var result = new Matrix4f(viewMatrix)

     val d: Float = math.sqrt(result.m00 * result.m00 +
                              result.m01 * result.m01 +
                              result.m02 * result.m02 ).asInstanceOf[Float]

    result.m00 = d
    result.m11 = d
    result.m22 = d

    result.m01 = 0.0f
    result.m02 = 0.0f
    result.m03 = 0.0f
    result.m12 = 0.0f
    result.m13 = 0.0f
    result.m23 = 0.0f

    result.m10 = 0.0f
    result.m20 = 0.0f
    result.m21 = 0.0f

    result.m33 = 1.0f

    var buf: FloatBuffer = BufferUtils.createFloatBuffer(16 * 4);
    result.store(buf)
    buf.rewind()

    buf
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

  def updateCamPos() {
    camX = (camR*cos(camPhi)*sin(camTheta) + centerX).asInstanceOf[Float]
    camY = (camR*sin(camPhi)*sin(camTheta) + centerY).asInstanceOf[Float]
    camZ = (camR*cos(camTheta) + centerZ).asInstanceOf[Float]
    
    if(Main.curLevel != null) {
      val minZ = Main.curLevel.height(camX, camY) + 2.0f
      camZ = max(camZ, minZ)
    }
  }

  def multModelViewMatrix() {
    updateCamPos()

    Project.gluLookAt(eye.x, eye.y, eye.z, lookAt.x, lookAt.y, lookAt.z, up.x, up.y, up.z)
  }

  def up() = {
    import org.lwjgl.util.vector._

    new Vector3f(0, 0, 1)
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

  override def traits() = List("camera", "update")

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
      camTheta = max(
        min(camTheta, (Pi/2.0f-0.001f).asInstanceOf[Float]), 0.001f)
      camPhi   = (camPhi % (2*Pi)).asInstanceOf[Float]

      //println("Camera (r,t,p) = (%f,%f,%f)".format(camR, camTheta, camPhi))
    }
  }
}

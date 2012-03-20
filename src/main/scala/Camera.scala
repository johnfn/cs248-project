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

    viewMatrix = storeViewMat()
  }

  def storeViewMat() = {
    import java.nio._
    import org.lwjgl._

    var viewMatrix: Matrix4f = new Matrix4f()
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

    viewMatrix
  }

  def facingCamMat(px: Float, py: Float, pz: Float) = {
    import java.nio._
    import org.lwjgl._
    import org.lwjgl.util.vector._

    var buf: FloatBuffer = BufferUtils.createFloatBuffer(16 * 4);
    var viewmat: Matrix4f = storeViewMat()

    // Get the current model view matrix from OpenGL.
    glGetFloat(GL_MODELVIEW_MATRIX, buf);

    var camPos = new Vector3f(-buf.get(12), -buf.get(13), -buf.get(14))
    var camUp = new Vector3f(buf.get(1), buf.get(5), buf.get(9))

    viewmat.m30 = 0.0f
    viewmat.m31 = 0.0f
    viewmat.m32 = 0.0f

    viewmat.transpose()

    camPos.x = viewmat.m00 * camPos.x + viewmat.m10 * camPos.y + viewmat.m20 * camPos.z
    camPos.y = viewmat.m01 * camPos.x + viewmat.m11 * camPos.y + viewmat.m21 * camPos.z
    camPos.z = viewmat.m02 * camPos.x + viewmat.m12 * camPos.y + viewmat.m22 * camPos.z


    var result: Matrix4f = new Matrix4f()
    var right: Vector3f = new Vector3f()
    var look = new Vector3f( -px + camPos.x
                           , -py + camPos.y
                           , -pz + camPos.z)
    look.normalise();

    var upvec = camUp

    /* right = look x upvec */
    Vector3f.cross(upvec, look, right);
    right.normalise();

    /* Recompute upvec as: upvec = right x look */
    Vector3f.cross(look, right, upvec);

    result.m00 = right.x;
    result.m10 = right.y;
    result.m20 = right.z;
    result.m30 = 0.0f

    result.m01 = up.x;
    result.m11 = up.y;
    result.m21 = up.z;
    result.m31 = 0.0f

    result.m02 = look.x;
    result.m12 = look.y;
    result.m22 = look.z;
    result.m32 = 0.0f

    result.m03 = px
    result.m13 = py
    result.m23 = pz
    result.m33 = 1.0f


    var resbuf: FloatBuffer = BufferUtils.createFloatBuffer(16 * 4);
    result.store(resbuf)
    resbuf.rewind()

    resbuf
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

  var prevMouseX = -1
  var prevMouseY = -1
  override def update(m: EntityManager) {
    val dx = Mouse.getDX()
    val dy = Mouse.getDY()
    val CAM_LAG:Float = 15f

    val pr:Protagonist = m.entities.filter(_.traits.contains("protagonist")).head.asInstanceOf[Protagonist]
    centerX = centerX + (pr.x - centerX) / CAM_LAG
    centerY = centerY + (pr.y - centerY) / CAM_LAG
    centerZ = centerZ + (pr.z - centerZ) / CAM_LAG

    if(Mouse.isButtonDown(1)) {
      Mouse.setGrabbed(true)
      val yInvert = 3.0 // no invert
      val mouseSensitivity = 0.005

      camTheta += (dy*mouseSensitivity*yInvert).asInstanceOf[Float]
      camPhi   += -(dx*mouseSensitivity).asInstanceOf[Float]

      // limit movement of camera
      camTheta = max(
        min(camTheta, (Pi/2.0f-0.001f).asInstanceOf[Float]), 0.001f)
      camPhi   = (camPhi % (2*Pi)).asInstanceOf[Float]

      //println("Camera (r,t,p) = (%f,%f,%f)".format(camR, camTheta, camPhi))


      // Set the cursor position and reset the delta tracker
      Mouse.setCursorPosition(Mouse.getX-dx, Mouse.getY-dy)
      Mouse.getDX()
      Mouse.getDY()
    } else {
      Mouse.setGrabbed(false)
    }
  }
}

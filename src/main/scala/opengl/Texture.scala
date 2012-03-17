package edu.stanford.cs248.project.opengl

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.{ByteBuffer, IntBuffer, ByteOrder}

import org.lwjgl.opengl._

import GL11._
import GL12._
import GL13._
import GL20._

// right now just means 2D texture
trait Texture {

  val id = glGenTextures()

  def width: Int
  def height: Int
  def initData : ByteBuffer = null

  def internalFormat: Int
  def format: Int
  def dataType: Int

  def filter = GL_NEAREST

  def init() = {
    // do all tex initialization work on texture unit 0
    bind(0)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format,
      dataType, initData)
  }

  def bind(texUnit: Int) = {
    glActiveTexture(GL_TEXTURE0 + texUnit)
    glBindTexture(GL_TEXTURE_2D, id)
    //println("active tex, texid : %d %d".format(texUnit, id))
  }

  def bindAndSetShader(texUnit: Int, shader: Shader, name: String) = {
    bind(texUnit)
    glUniform1i(glGetUniformLocation(shader.id, name), texUnit)
  }
}

class ImageTexture(rcPath: String) extends Texture {
  val img = ImageIO.read(getClass.getResource(rcPath))

  def width = img.getWidth
  def height = img.getHeight

  def internalFormat = GL_RGBA
  def format = GL_RGBA
  def dataType = GL_UNSIGNED_BYTE

  override def initData = Texture.rgbaGlBytes(img)
}

class BlankTexture(val width: Int, val height: Int,
                   val internalFormat: Int, val format: Int, val dataType: Int)
  extends Texture
{
  // use for FBOs, so need linear filtering
  override def filter = GL11.GL_LINEAR
}

class ColorTexture(r: Int, g: Int, b: Int) extends Texture {
  def width = 1
  def height = 1
  def internalFormat = GL_RGBA
  def format = internalFormat
  def dataType = GL_UNSIGNED_BYTE

  override def initData = {
    val res =
      ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.nativeOrder())

    res.put(r.toByte).put(g.toByte).put(b.toByte).put(255.toByte).rewind()

    res
  }
}

object Texture {
  def rgbaGlBytes(img: BufferedImage) = {
    val (width, height) = (img.getWidth, img.getHeight)

    val res =
      ByteBuffer.allocateDirect(width*height*4).order(ByteOrder.nativeOrder())

    val argbary = img.getRGB(0, 0, width, height, null, 0, width)

    for(y <- (height-1) to 0 by -1; x <- 0 until width) {
      val argb = argbary(y*width+x)
      res.put((argb >>> 16).asInstanceOf[Byte])
      res.put((argb >>>  8).asInstanceOf[Byte])
      res.put((argb >>>  0).asInstanceOf[Byte])
      res.put((argb >>> 24).asInstanceOf[Byte])
    }

    res.rewind()
    res
  }
}

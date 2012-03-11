package edu.stanford.cs248.project.opengl

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.{ByteBuffer, IntBuffer, ByteOrder}

import org.lwjgl.opengl._

// right now just means 2D texture
trait Texture {
  import GL11._
  import GL13._
  
  val texId = glGenTextures()
  
  def width: Int
  def height: Int
  def initData : ByteBuffer = null
  
  def format: Int
  def dataType: Int
  
  def init() = {
    // do all tex initialization work on texture unit 0
    bind(0)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
    
    glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format,
      dataType, initData)
  }
  
  def bind(texUnit: Int) = {
    glActiveTexture(GL_TEXTURE0 + texUnit)
    glBindTexture(GL_TEXTURE_2D, texId)
  }
}

class ImageTexture(rcPath: String) extends Texture {
  import GL11._
  
  val img = ImageIO.read(getClass.getResource(rcPath))
  
  val width = img.getWidth
  val height = img.getHeight
  
  def format = GL_RGBA
  def dataType = GL_UNSIGNED_BYTE
  
  override def initData = Texture.rgbaGlBytes(img)
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

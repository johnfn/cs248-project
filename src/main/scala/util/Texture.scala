package edu.stanford.cs248.project.util

import javax.imageio.ImageIO
import java.nio.{ByteBuffer, IntBuffer, ByteOrder}

class Texture(val rcname: String) {
  val img = ImageIO.read(getClass.getResource(rcname))
  
  val width = img.getWidth()
  val height = img.getHeight()
}

class TextureGray(rcname: String) extends Texture(rcname) {
  // one byte per pixel
  val glBytes : ByteBuffer = {
    val res = 
      ByteBuffer.allocateDirect(width*height).order(ByteOrder.nativeOrder())
    
    val ary = img
      .getRGB(0, 0, width, height, null, 0, width)
      .map(_.asInstanceOf[Byte])
    
    // write to byte buffer flipped
    // x and y in opengl coordinates
    for(y <- (height-1) to 0 by -1; x <- 0 until width) {
      res.put(ary(y*width+x))
    }
    
    res
  }
}

class TextureARGB(rcname: String) extends Texture(rcname) {
  val glBytes : IntBuffer = {
    val res = 
      ByteBuffer.allocateDirect(width*height).order(ByteOrder.nativeOrder())
    
    val argbary = img.getRGB(0, 0, width, height, null, 0, width)
    
    for(y <- (height-1) to 0 by -1; x <- 0 until width) {
      val argb = argbary(y*width+x)
      res.put((argb >>> 16).asInstanceOf[Byte])
      res.put((argb >>>  8).asInstanceOf[Byte])
      res.put((argb >>>  0).asInstanceOf[Byte])
      res.put((argb >>> 24).asInstanceOf[Byte])
    }
    
    res.asIntBuffer()
  }
}


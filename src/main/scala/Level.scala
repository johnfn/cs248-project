package edu.stanford.cs248.project

import javax.imageio.ImageIO

import scala.math._

import org.lwjgl.opengl._

class Level(val name: String) extends Entity 
{
  val heightImg = ImageIO.read(getClass.getResource("/levels/"+name+"_h.png"))

  val xSize = heightImg.getWidth()
  val ySize = heightImg.getHeight()
  
  var vertexVboId : Int = 0
  var indexVboId : Int = 0
  
  val nVerts = xSize*ySize  
  val nQuads = (xSize-1)*(ySize-1)
      
  val verPosSize = java.lang.Float.SIZE/8 * 3
  val normalSize = java.lang.Float.SIZE/8 * 3
  val colorSize  = java.lang.Byte.SIZE/8 * 3
  
  val strideSize = verPosSize + normalSize + colorSize
  
  val elemIdSize = java.lang.Integer.SIZE/8
  
  override def doInitGL() = {
    import ARBBufferObject._
    import ARBVertexBufferObject._
    
    // Get image pixels
    // This just gets the right-most 8 bits, corresponding to the blue channel
    // This is okay, as the image is grayscale.
    val heightPixs = heightImg
      .getRGB(0, 0, xSize, ySize, null, 0, xSize)
      .map(_ & 0xff)
      
    // emulate CLAMP for byte retrieval
    def clamp(value: Int, valueMax: Int) = min(max(0, value), valueMax) 
    def imageByte(x: Int, y: Int) = {
      heightPixs(clamp(y, ySize-1)*xSize + clamp(x, xSize-1))
    }
    
    // get a vbo ids
    vertexVboId = glGenBuffersARB()
    indexVboId  = glGenBuffersARB()
    
    // bind and allocate vbo for vertices
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBufferDataARB(GL_ARRAY_BUFFER_ARB, strideSize*nVerts, GL_STATIC_DRAW_ARB)
    
    // Get the VRAM mapped vertex buffer
    val vBuf = glMapBufferARB(GL_ARRAY_BUFFER_ARB, 
      GL_WRITE_ONLY_ARB, strideSize*nVerts, null)
      
    // insert one vertex per pixel in the heightmap
    // note in this case, the top-left of the image corresponds to (0,0)
    // and x+ and y+ are right and down in the image
    // This is different from opengl's texture coordinate system
    for(y <- 0 until ySize; x <- 0 until xSize) {
      // vertex position
      vBuf
        .putFloat(x)
        .putFloat(y)
        .putFloat((imageByte(x,y)).asInstanceOf[Float]*0.3f)
      
      val dx = imageByte(x+1, y) - imageByte(x-1, y)
      val dy = imageByte(x, y+1) - imageByte(x, y-1)
      
      // store normal vector. not normalized so we can retrieve slopes
      vBuf
        .putFloat(-dx)
        .putFloat(-dy)
        .putFloat(1)
      
      // store color bytes just for testing
      vBuf
        .put(imageByte(x,y).asInstanceOf[Byte])
        .put(imageByte(x,y).asInstanceOf[Byte])
        .put(imageByte(x,y).asInstanceOf[Byte])
    }
    
    // unmap and unbind for vertices vbo
    glUnmapBufferARB(GL_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    
    // bind and allocate for 
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 
      nQuads*4*elemIdSize, GL_STATIC_DRAW_ARB)
    
    val iBuf = glMapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, nQuads*4*elemIdSize, null)
    
    def elemId(x: Int, y: Int) = y*xSize + x
    for(y <- 0 until ySize-1; x <- 0 until xSize-1) {
      // NOTE: making sure this is CCW winding pointing in the +z
      iBuf.putInt(elemId(x  , y  ))
      iBuf.putInt(elemId(x+1, y  ))
      iBuf.putInt(elemId(x+1, y+1))
      iBuf.putInt(elemId(x  , y+1))
    }
    
    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)
  }
  
  override def renderGL() = {
    import GL11._
    import GL12._
    import ARBBufferObject._
    import ARBVertexBufferObject._

    // enable relevant client states
    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_NORMAL_ARRAY)
    glEnableClientState(GL_COLOR_ARRAY)
    
    // bind vertex vbo and index vbo
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    
    // set pointers to data
    glVertexPointer(3, GL_FLOAT, strideSize, 0)
    glNormalPointer(GL_FLOAT, strideSize, verPosSize)
    glColorPointer(3, GL_UNSIGNED_BYTE, strideSize, verPosSize+normalSize)
    
    // bind element data
    glDrawElements(GL_QUADS, nQuads*4, GL_UNSIGNED_INT, 0)
    
    // unbind buffers
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)
    
    // disable client states
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glDisableClientState(GL_COLOR_ARRAY)
    
  }
}

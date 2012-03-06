package edu.stanford.cs248.project

import javax.imageio.ImageIO

import scala.math._

import org.lwjgl.opengl._

class Level(val name: String) extends Entity 
{
  val xSize = heightImg.getWidth()
  val ySize = heightImg.getHeight()
  
  val heightImg = ImageIO.read(getClass.getResource("levels/"+name+"_h.png"))
  
  var vertexVboId : Int = 0
  var indexVboId : Int = 0
  
  override def doInitGL() = {
    import ARBBufferObject._
    import ARBVertexBufferObject._
    
    // Get image pixels
    // This just gets the right-most 8 bits, corresponding to the blue channel
    // This is okay, as the image is grayscale.
    val heightPixs = heightImg
      .getRGB(0, 0, xSize, ySize, null, 0, xSize)
      .map(_.asInstanceOf[Byte])
      
    // emulate CLAMP for byte retrieval
    def clamp(value: Int, valueMax: Int) = min(max(0, value), valueMax) 
    def imageByte(x: Int, y: Int) : Byte = {
      heightPixs(clamp(y, ySize-1)*xSize + clamp(x, xSize-1))
    }
    
    val verPosSize = java.lang.Short.SIZE/8 * 3
    val normalSize = java.lang.Float.SIZE/8 * 3
    
    val strideSize = verPosSize + normalSize
    
    val nVerts = xSize*ySize
    
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
        .putShort(x.asInstanceOf[Short])
        .putShort(y.asInstanceOf[Short])
        .putShort(imageByte(x,y))
      
      val dx = imageByte(x+1, y) - imageByte(x-1, y)
      val dy = imageByte(x, y+1) - imageByte(x, y-1)
      
      // store normal vector. not normalized so we can retrieve slopes
      vBuf
        .putFloat(-dx)
        .putFloat(-dy)
        .putFloat(1)
    }
    
    // unmap and unbind for vertices vbo
    glUnmapBufferARB(GL_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    
    val nQuads = (xSize-1)*(ySize-1)*4
    
    // bind and allocate for 
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 
      nQuads*java.lang.Integer.SIZE/8, GL_STATIC_DRAW_ARB)
    
    val iBuf = glMapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, nQuads*java.lang.Integer.SIZE/8, null)
    
    def elemId(x: Int, y: Int) = y*xSize + x
    for(y <- 0 until ySize-1; x <- 0 until xSize-1) {
      // NOTE: making sure this is CCW winding pointing in the +z
      vBuf.putInt(elemId(x  , y  ))
      vBuf.putInt(elemId(x+1, y  ))
      vBuf.putInt(elemId(x+1, y+1))
      vBuf.putInt(elemId(x  , y+1))
    }
    
    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)
  }
}

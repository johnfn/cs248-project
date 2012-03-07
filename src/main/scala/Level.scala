package edu.stanford.cs248.project

import javax.imageio.ImageIO

import scala.math._

import org.lwjgl.opengl._

import edu.stanford.cs248.project.util._

class Level(val name: String) extends Entity 
{
  val heightMap = new ImageMapGrayscale("/levels/"+name+"_h.png")
  val deltaXMap = heightMap.deltaXMap
  val deltaYMap = heightMap.deltaYMap
  
  val xSize = heightMap.xSize
  val ySize = heightMap.ySize
  
  val zNormalQuads = xSize*ySize
  val xNormalQuads = deltaXMap.ary.count(_ != 0) // quads facing the x axis
  val yNormalQuads = deltaYMap.ary.count(_ != 0)
  val nQuads = xNormalQuads + yNormalQuads + zNormalQuads
  val nVerts = nQuads*4
  
  var vertexVboId : Int = 0
  var indexVboId : Int = 0
  
  val elemIdSize = java.lang.Integer.SIZE/8
  
  val zScale = 0.03f
  
  override def doInitGL() = {
    import ARBBufferObject._
    import ARBVertexBufferObject._
    
    // get a vbo ids
    vertexVboId = glGenBuffersARB()
    indexVboId  = glGenBuffersARB()
    
    // bind and allocate vbo for vertices
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBufferDataARB(GL_ARRAY_BUFFER_ARB, Vertex.strideSize*nVerts, 
      GL_STATIC_DRAW_ARB)
    
    // Get the VRAM mapped vertex buffer
    val vBuf = glMapBufferARB(GL_ARRAY_BUFFER_ARB, 
      GL_WRITE_ONLY_ARB, Vertex.strideSize*nVerts, null)
      
    // insert one vertex per pixel in the heightmap
    // note in this case, the top-left of the image corresponds to (0,0)
    // and x+ and y+ are right and down in the image
    // This is different from opengl's texture coordinate system
    val floorCorners = Array(
      (-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))
    
    for(y <- 0 until ySize; x <- 0 until xSize) {
      val xf = x.asInstanceOf[Float]
      val yf = y.asInstanceOf[Float]
      val zf = heightMap.valueAt(x,y)*zScale
      val dzdx = deltaXMap.valueAt(x,y)*zScale
      val dzdy = deltaYMap.valueAt(x,y)*zScale
      
      val hc = heightMap.valueAt(x,y).asInstanceOf[Byte]
      
      // paint floor tiles
      floorCorners.foreach { case(dx, dy) =>
        Vertex(
          xf+dx, yf+dy, zf, 
          0, 0, 1, 
          255.toByte, 0, 0,
          dx+0.5f, dy+0.5f)
          .insertIntoBuf(vBuf)
      }
      
      // paint x facing walls
      if(dzdx != 0) {
        val nx = if(dzdx > 0) -1.0f else 1.0f
        floorCorners.foreach { case(dy, dz) =>
          Vertex(
            xf+0.5f, yf+dy*nx, zf+(dz+0.5f)*dzdx,
            nx, 0, 0,
            0, 255.toByte, 0,
            dy+0.5f, (dz+0.5f)*dzdx) // texture coordinates should tile wall
            .insertIntoBuf(vBuf)
        }
      }
      
      // paint y facing walls
      if(dzdy != 0) {
        val ny = if(dzdy < 0) -1.0f else 1.0f
        floorCorners.foreach { case(dx, dz) =>
          Vertex(
            xf+dx*ny, yf+0.5f, zf+(dz+0.5f)*dzdy,
            0, ny, 0,
            0, 0, 255.toByte,
            dx+0.5f, (dz+0.5f)*dzdy) // texture coordinates should tile wall
            .insertIntoBuf(vBuf)
        }
      }
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
    
    (0 until nVerts).map(iBuf.putInt(_))
    
    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)
    
    println("Loaded Level %s with %d quads".format(name, nQuads))
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
    glVertexPointer(3, GL_FLOAT, Vertex.strideSize, Vertex.posOffset)
    glNormalPointer(GL_FLOAT, Vertex.strideSize, Vertex.norOffset)
    glColorPointer(3, GL_UNSIGNED_BYTE, Vertex.strideSize, Vertex.colOffset)
    
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

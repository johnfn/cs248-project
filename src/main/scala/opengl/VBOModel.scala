package edu.stanford.cs248.project.opengl

import java.nio.ByteBuffer

import org.lwjgl.opengl._

import edu.stanford.cs248.project._
import edu.stanford.cs248.project.util._

trait VBOModel {
  import ARBBufferObject._
  import ARBVertexBufferObject._

  // get vbo ids
  val vertexVboId = glGenBuffersARB()
  val indexVboId  = glGenBuffersARB()

  // implementing classes need to define the # of vertices and elements
  def name: String
  var nVerts = 0
  var nIdxs = 0 // number of vertex indices in index buffer
  def drawMode : Int = GL11.GL_QUADS

  var initialized = false

  val idxSize = java.lang.Integer.SIZE/8

  // two abstract methods that need to be implemented by any models
  def getVertices() : scala.collection.Iterable[Vertex]
  def getIndices() : scala.collection.Iterable[Int]

  def init() = {
    // get the vertices and indices and set their size
    val verts = getVertices()
    nVerts = verts.size
    val indices = getIndices()
    nIdxs = indices.size
    
    // bind and allocate vbo for vertices
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBufferDataARB(GL_ARRAY_BUFFER_ARB, Vertex.strideSize*nVerts,
      GL_STATIC_DRAW_ARB)

    // Get the VRAM mapped vertex buffer
    val vBuf = glMapBufferARB(GL_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, Vertex.strideSize*nVerts, null)

    verts.foreach(_.insertIntoBuf(vBuf))

    // unmap and unbind for vertices vbo
    glUnmapBufferARB(GL_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)

    // bind and allocate for
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      nIdxs*idxSize, GL_STATIC_DRAW_ARB)

    val iBuf = glMapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, nIdxs*idxSize, null)

    indices.foreach(iBuf.putInt(_))

    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)

    println("Loaded VBO %s with %d vertices.".format(name, nVerts))
  }

  // Will draw what's in the VBO. User needs to specify matrices beforehand
  def drawCall(shader: Shader) = {
    import GL11._
    import GL12._

    // initialize if never been initialized
    if(!initialized) {
      init()
      initialized = true
    }

    // enable relevant client states
    glEnableClientState(GL_VERTEX_ARRAY)
    glEnableClientState(GL_NORMAL_ARRAY)

    // bind vertex vbo and index vbo
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)

    // set pointers to data
    glVertexPointer(3, GL_FLOAT, Vertex.strideSize, Vertex.posOffset)
    glNormalPointer(GL_FLOAT, Vertex.strideSize, Vertex.norOffset)
    
    additionalPredraw(shader)
    
    // bind element data
    glDrawElements(drawMode, nIdxs, GL_UNSIGNED_INT, 0)

    // unbind buffers
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)

    // disable client states
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
  }

  def additionalPredraw(shader: Shader) = {}
}

abstract class TexturedVBOModel(textureName: String) 
  extends VBOModel 
{
  var texId = 0
  def texUnit = 0  
  
  val tex = new ImageTexture("/textures/"+textureName+".png")
  
  override def init() = {
    import GL11._
    import GL13._
    import GL30._
    
    super.init()
    tex.init()
  }
  
  override def additionalPredraw(shader: Shader) = {
    import GL11._
    import GL13._
    import GL20._
    
    tex.bind(texUnit)
    glUniform1i(glGetUniformLocation(shader.progId, "difTex"), texUnit)
    
    // Bind texture coordinates
    glEnableVertexAttribArray(glGetAttribLocation(shader.progId, "texcoordIn"))
    glVertexAttribPointer(glGetAttribLocation(shader.progId, "texcoordIn"),
      2, GL_FLOAT, false, Vertex.strideSize, Vertex.texOffset)
  }
}

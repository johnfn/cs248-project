package edu.stanford.cs248.project.entity

import java.nio.ByteBuffer

import org.lwjgl.opengl._

trait VBOModel {
  import ARBBufferObject._
  import ARBVertexBufferObject._

  // get vbo ids
  val vertexVboId = glGenBuffersARB()
  val indexVboId  = glGenBuffersARB()

  // implementing classes need to define the # of vertices and elements
  def name: String
  def nVerts : Int
  def nIdxs : Int // number of vertex indices in index buffer
  def drawMode : Int = GL11.GL_QUADS

  var initialized = false

  val idxSize = java.lang.Integer.SIZE/8

  // two abstract methods that need to be implemented by any models
  def populateVerBuffer(vBuf: ByteBuffer)
  def populateIdxBuffer(iBuf: ByteBuffer)

  def init() = {
    // bind and allocate vbo for vertices
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBufferDataARB(GL_ARRAY_BUFFER_ARB, Vertex.strideSize*nVerts,
      GL_STATIC_DRAW_ARB)

    // Get the VRAM mapped vertex buffer
    val vBuf = glMapBufferARB(GL_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, Vertex.strideSize*nVerts, null)

    populateVerBuffer(vBuf)

    // unmap and unbind for vertices vbo
    glUnmapBufferARB(GL_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)

    // bind and allocate for
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      nIdxs*idxSize, GL_STATIC_DRAW_ARB)

    val iBuf = glMapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, nIdxs*idxSize, null)

    populateIdxBuffer(iBuf)

    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)

    println("Loaded VBO %s with %d vertices.".format(name, nVerts))
  }

  // Will draw what's in the VBO. User needs to specify matrices beforehand
  def drawCall() = {
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
    glEnableClientState(GL_COLOR_ARRAY)

    // bind vertex vbo and index vbo
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)

    // set pointers to data
    glVertexPointer(3, GL_FLOAT, Vertex.strideSize, Vertex.posOffset)
    glNormalPointer(GL_FLOAT, Vertex.strideSize, Vertex.norOffset)
    glColorPointer(3, GL_UNSIGNED_BYTE, Vertex.strideSize, Vertex.colOffset)

    // bind element data
    glDrawElements(drawMode, nIdxs, GL_UNSIGNED_INT, 0)

    // unbind buffers
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)

    // disable client states
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glDisableClientState(GL_COLOR_ARRAY)
  }

}

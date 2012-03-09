package edu.stanford.cs248.project

import javax.imageio.ImageIO

import scala.math._

import org.lwjgl.opengl._

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.util._

import org.lwjgl._
import input._
import Keyboard._
import scala.math._

class Protagonist() extends Entity {
  val JUMP_HEIGHT = .2f
  val GRAVITY = 0.02f

  var x = 0.0f
  var y = 0.0f
  var z = 0.0f

  var vz = 0.0f

  override def traits() = List("protagonist", "render", "update")

  //TODO: Should eventually move this into util.
  def renderVerticies(vs: Array[Vertex]) = {
    import GL11._
    import GL12._
    import ARBBufferObject._
    import ARBVertexBufferObject._

    val nVerts = 4
    val elemIdSize = java.lang.Integer.SIZE / 8

    val vertexVboId = glGenBuffersARB()
    val indexVboId  = glGenBuffersARB()

    glBindBufferARB(GL_ARRAY_BUFFER_ARB, vertexVboId)
    glBufferDataARB(GL_ARRAY_BUFFER_ARB, Vertex.strideSize*nVerts,
      GL_STATIC_DRAW_ARB)

    val vBuf = glMapBufferARB(GL_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, Vertex.strideSize*nVerts, null)

	vs.foreach { _.insertIntoBuf(vBuf) }

    // --------

    // unmap and unbind for vertices vbo
    glUnmapBufferARB(GL_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)

    // bind and allocate for
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, indexVboId)
    glBufferDataARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      nVerts*elemIdSize, GL_STATIC_DRAW_ARB)
    //TODO change other part of this code

    val iBuf = glMapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB,
      GL_WRITE_ONLY_ARB, nVerts*elemIdSize, null)

    (0 until nVerts).map(iBuf.putInt(_))

    glUnmapBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)


	// --- do actual rendering

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
    glDrawElements(GL_QUADS, nVerts, GL_UNSIGNED_INT, 0)

    // unbind buffers
    glBindBufferARB(GL_ARRAY_BUFFER_ARB, 0)
    glBindBufferARB(GL_ELEMENT_ARRAY_BUFFER_ARB, 0)

    // disable client states
    glDisableClientState(GL_VERTEX_ARRAY)
    glDisableClientState(GL_NORMAL_ARRAY)
    glDisableClientState(GL_COLOR_ARRAY)
  }

  override def doInitGL() = {
  }

  override def update(m:EntityManager) = {
    // This is a common enough idiom that it may be worth abstracting out.
    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]
    val onGround:Boolean = (z <= lv.height(x, y))

  	var newx = x
  	var newy = y
  	var newz = z

  	if (onGround) {
      vz = 0
  		if (isKeyDown(KEY_SPACE)) {
  			vz += JUMP_HEIGHT
  		} else {
  			newz = lv.height(x, y)
  		}

  	} else {
      vz -= GRAVITY
    }

		newz += vz

    if (newz <= lv.height(x, y)) {
      newz = lv.height(x, y)
    }

  	if (isKeyDown(KEY_W)) newx += 1
  	if (isKeyDown(KEY_S)) newx -= 1

  	if (isKeyDown(KEY_A)) newy += 1
  	if (isKeyDown(KEY_D)) newy -= 1

  	if (lv.inBounds(newx, newy)) {
  		if (lv.height(newx, newy) - newz <= .5) {
  			x = newx
  			y = newy
  		}
  	}

  	z = newz
  }

  override def renderGL() = {
    val floorCorners = Array((-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

    val vs:Array[Vertex] = floorCorners.map { case(dx, dy) =>
		Vertex(x + dx, y + dy, z,
			  0, 0, 1,
			  250, 0, 0,
			  dx + 0.5f, dy + 0.5f)
		}

	renderVerticies(vs)
  }
}
package edu.stanford.cs248.project

import scala.math._

import org.lwjgl.opengl._
import java.nio.ByteBuffer

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.opengl._

import org.lwjgl._
import input._
import Keyboard._
import scala.math._
import com.threed.jpct._
import com.threed.jpct.util._
import math._

class SkyModel(val x: Float, val y: Float, val z: Float, val size: Float, val texture: String)
	extends TexturedVBOModel(new ImageTexture(texture),
		 					 new ColorTexture(0, 0, 0)) {
	val name = "skybox"
	val WIDTH = size

	def offset(lists: List[List[Int]], offx: Float, offy: Float) = {
		lists.map { case List(x, y) => List(x.toFloat * 0.25f + offx, y.toFloat * 0.33f + offy)}
	}

	override def getVertices() = {

val texcoords = List(
	// front
        offset(List(List(0, 0), List(1, 0), List(1, 1), List(0, 1)), 0.5f, 0.33f),

    // Render the left quad
        offset(List(List(0, 0), List(1, 0), List(1, 1), List(0, 1)), 0.25f, 0.33f),

    // Render the back quad
        offset(List(List(0, 0), List(1, 0), List(1, 1), List(0, 1)), 0.00f, 0.33f),

    // Render the right quad
        offset(List(List(0, 0), List(1, 0), List(1, 1), List(0, 1)), 0.75f, 0.33f),

    // Render the top quad
        offset(List(List(0, 1), List(0, 0), List(1, 0), List(1, 1)), 0.5f, 0.66f),

    // Render the bottom quad
        offset(List(List(0, 0), List(0, 1), List(1, 1), List(1, 0)), 0.5f, 0.0f)
    ).flatten(identity)

val vertices = List(
	List(  0.5f, -0.5f, -0.5f ),
	List( -0.5f, -0.5f, -0.5f ),
	List( -0.5f,  0.5f, -0.5f ),
	List(  0.5f,  0.5f, -0.5f ),

	List(  0.5f, -0.5f,  0.5f ),
	List(  0.5f, -0.5f, -0.5f ),
	List(  0.5f,  0.5f, -0.5f ),
	List(  0.5f,  0.5f,  0.5f ),

	List( -0.5f, -0.5f,  0.5f ),
	List(  0.5f, -0.5f,  0.5f ),
	List(  0.5f,  0.5f,  0.5f ),
	List( -0.5f,  0.5f,  0.5f ),

	List( -0.5f, -0.5f, -0.5f ),
	List( -0.5f, -0.5f,  0.5f ),
	List( -0.5f,  0.5f,  0.5f ),
	List( -0.5f,  0.5f, -0.5f ),

	List( -0.5f,  0.5f, -0.5f ),
	List( -0.5f,  0.5f,  0.5f ),
	List(  0.5f,  0.5f,  0.5f ),
	List(  0.5f,  0.5f, -0.5f ),

	List( -0.5f, -0.5f, -0.5f ),
	List( -0.5f, -0.5f,  0.5f ),
	List(  0.5f, -0.5f,  0.5f ),
	List(  0.5f, -0.5f, -0.5f )
	)

		vertices.zipWithIndex.map({ case (vertex, i) =>
			Vertex(vertex(0) * WIDTH, vertex(1) * WIDTH, vertex(2) * WIDTH - 0.5f,
				0.0f, 0.0f, 1.0f,
				   //normals(i / 4)(0), normals(i / 4)(1), normals(i / 4)(2),
				   texcoords(i)(0),texcoords(i)(1))
			})
	}

	override def getIndices() = 0 until 24
}

class SkyBox() extends VBOModelEntity {
	val WIDTH = 0.5f

	x = 0.0f
	y = 0.0f
	z = 0.0f

	def render(camera: Camera, shader: Shader) = {
		import GL11._
		import GL20._

	    glPushMatrix()
	    glPushAttrib(GL_ENABLE_BIT)
	    glEnable(GL_TEXTURE_2D)
	    glDisable(GL_DEPTH_TEST)
	    glDisable(GL_LIGHTING)
	    glDisable(GL_BLEND)
	    glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
	    glLoadIdentity()

	    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
	    camera.loadGLMatrices()
	    glRotatef(90.0f, 1.0f, 0.0f, 0.0f)

	    renderGL(shader)
	    glPopAttrib()
	    glPopMatrix()
	}

	val model = new SkyModel(x, y, z, 800.0f, "/textures/skybox.jpg")
}
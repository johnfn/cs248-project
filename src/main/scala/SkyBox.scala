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

class SkyModel(val x: Float, val y: Float, val z: Float)
	extends TexturedVBOModel(new ImageTexture("/textures/skybox.jpg"),
		 					 new ColorTexture(0, 0, 0)) {
	val name = "skybox"
	val WIDTH = 10.0f

	override def getVertices() = {
		var texcoords = List(

	//front
		List(
	        List(0, 0),
	        List(1, 0),
	        List(1, 1),
	        List(0, 1)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f + 0.5f, y.toFloat / 3.0f + 1.0f / 3.0f)} ,

	    // Render the back quad
	    List(
	        List(0, 0),
	        List(1, 0),
	        List(1, 1),
	        List(0, 1)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f, y.toFloat / 3.0f)} ,

	    // Render the top quad
	    List(
	        List(0, 1),
	        List(0, 0),
	        List(1, 0),
	        List(1, 1)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f + 0.5f, y.toFloat / 3.0f)} ,

	    // Render the bottom quad
	    List(
	        List(0, 0),
	        List(0, 1),
	        List(1, 1),
	        List(1, 0)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f + 0.5f, y.toFloat / 3.0f + 2.0f / 3.0f)} ,

	    // Render the left quad
	    List(
	        List(0, 0),
	        List(1, 0),
	        List(1, 1),
	        List(0, 1)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f + 0.25f, y.toFloat / 3.0f + 1.0f / 3.0f)} ,

	    // Render the right quad
	    List(
	        List(0, 0),
	        List(1, 0),
	        List(1, 1),
	        List(0, 1)
	        ).map {case List(x, y) => List(x.toFloat / 4.0f + 0.75f, y.toFloat / 3.0f + 1.0f / 3.0f)}
		)

		var normals = List(
			List( 0.0f, 0.0f,  1.0f),
			List( 0.0f, 0.0f, -1.0f),
			List( 0.0f,-1.0f,  0.0f),
			List( 0.0f, 1.0f,  0.0f),
			List( 1.0f, 0.0f,  0.0f),
			List(-1.0f, 0.0f,  0.0f)
			)

		var vertices = List(
		    // Front face
	    	List(
			    List(-1.0f, -1.0f,  1.0f),
			    List( 1.0f, -1.0f,  1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List(-1.0f,  1.0f,  1.0f)
	    	),

		    // Back face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List(-1.0f,  1.0f, -1.0f),
			    List( 1.0f,  1.0f, -1.0f),
			    List( 1.0f, -1.0f, -1.0f)
		    ),

		    // Top face
		    List(
			    List(-1.0f,  1.0f, -1.0f),
			    List(-1.0f,  1.0f,  1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List( 1.0f,  1.0f, -1.0f)
		    ),

		    // Bottom face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List( 1.0f, -1.0f, -1.0f),
			    List( 1.0f, -1.0f,  1.0f),
			    List(-1.0f, -1.0f,  1.0f)
		    ),

		    // Right face
		    List(
			    List( 1.0f, -1.0f, -1.0f),
			    List( 1.0f,  1.0f, -1.0f),
			    List( 1.0f,  1.0f,  1.0f),
			    List( 1.0f, -1.0f,  1.0f)
		    ),

		    // Left face
		    List(
			    List(-1.0f, -1.0f, -1.0f),
			    List(-1.0f, -1.0f,  1.0f),
			    List(-1.0f,  1.0f,  1.0f),
			    List(-1.0f,  1.0f, -1.0f)
		    )
		  );

		vertices.zipWithIndex.map({ case (face, i) =>
			face.zipWithIndex.map({ case (vertex, j) =>
				Vertex(vertex(0) * WIDTH + 5.0f, vertex(1) * WIDTH + 5.0f, vertex(2) * WIDTH + 5.0f,
					   normals(i)(0), normals(i)(1), normals(i)(2),
					   texcoords(i)(j)(0),texcoords(i)(j)(1))
					})
				}).flatten(identity)
	}

	override def getIndices() = 0 until 24
}

class SkyBox() extends VBOModelEntity {
	val WIDTH = 0.5f

	x = 1.0f
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

	    glCullFace(GL_FRONT)
	    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
	    camera.loadGLMatrices()
	    x = camera.centerX
	    y = camera.centerY
	    z = camera.centerZ

	    renderGL(shader)
	    glPopAttrib()
	    glPopMatrix()

	    glCullFace(GL_BACK)

	}

	val model = new SkyModel(x, y, z)
}
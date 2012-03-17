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

class SquareModel(val x: Float, val y: Float, val z: Float) extends VBOModel {

	val name = "cubemodel"

	override def getVertices() = {
		val floorCorners = Array((-0.5f, -0.5f), (0.5f, -0.5f), (0.5f, 0.5f), (-0.5f, 0.5f))

		floorCorners.map { case(dx, dy) =>
			Vertex(dx, dy, z,
				0, 0, 1,
				dx + 0.5f, dy + 0.5f)
		}
	}

	override def getIndices() = (0 until 4)
}

class CrystalModel()
	extends TexturedVBOModel(new ImageTexture("/textures/plant.jpg"),
							 new ColorTexture(0, 0, 0)) {

	// The (0) is to get the first model out of a potential list of models.
	var polymanager:PolygonManager = Loader.load3DS("models/plant.3ds",2.5f)(0).getPolygonManager()
	val SCALE_FACTOR = 25.0f

	override def name() = "crystal"

	def getVertices() = {
		var vertVec = new scala.collection.immutable.VectorBuilder[Vertex]()

		for (poly <- 0 until polymanager.getMaxPolygonID(); vert <- 0 until 3) {
			val verts:SimpleVector = polymanager.getTransformedVertex(poly, vert)
			val normal_verts:SimpleVector = polymanager.getTransformedNormal(poly)
			val texture_verts:SimpleVector = polymanager.getTextureUV(poly, vert)

			vertVec ++= List(Vertex(verts.x / SCALE_FACTOR, verts.y / SCALE_FACTOR, verts.z / SCALE_FACTOR,
				               normal_verts.x, normal_verts.y, normal_verts.z,
				               texture_verts.x, texture_verts.y))

			// This may be helpful if we have multiple textures.
			//(polymanager.getPolygonTexture(poly))
		}

		vertVec.result()
	}

	def getIndices() = (0 until nVerts)
}

class Crystal(val x: Float, val y: Float, val z: Float) extends VBOModelEntity {
	//val model = new SquareModel(x, y, z) // old color: , List(0, 0, 250))
	val model = new CrystalModel()
	var plant:Object3D = null
	var polymanager:PolygonManager = null
	val texDiff = new ImageTexture("/textures/plant.jpg")
	val texSpec = new ColorTexture(0, 0, 0)
	val DIFF_UNIT = 0
	val SPEC_UNIT = 1

	/*
	override def doInitGL() = {
		polymanager = Loader.load3DS("models/plant.3ds",2.5f)(0).getPolygonManager()

		var objs:Array[Object3D] = Loader.load3DS("models/plant.3ds",2.5f);

		texDiff.init()
		texSpec.init()
	}
	*/

	/*
	override def renderGL(shader: Shader) = {
		import GL11._
		import GL13._
		import GL20._

		val SCALE_FACTOR = 15.0f

		// Need to eventually merge this code with the code in TexturedVBOModel.
		// I could just write out every vertex to a VBO... might save a lot of code duplication...

		texDiff.bind(DIFF_UNIT)
		texDiff.bind(SPEC_UNIT)

	    glUniform1i(glGetUniformLocation(shader.id, "texDif"), DIFF_UNIT)
	    glUniform1i(glGetUniformLocation(shader.id, "texSpc"), SPEC_UNIT)

	    // Bind texture coordinates
	    glEnableVertexAttribArray(glGetAttribLocation(shader.id, "texcoordIn"))
	    glVertexAttribPointer(glGetAttribLocation(shader.id, "texcoordIn"),
	      2, GL_FLOAT, false, Vertex.strideSize, Vertex.texOffset)

		glTranslatef(5, 5, 0)
		glBegin(GL_TRIANGLES)
		for (poly <- 0 until polymanager.getMaxPolygonID(); vert <- 0 until 3) {
			val verts:SimpleVector = polymanager.getTransformedVertex(poly, vert)
			val texture_verts:SimpleVector = polymanager.getTextureUV(poly, vert)
			glVertex3f(verts.x / SCALE_FACTOR, verts.y / SCALE_FACTOR, verts.z / SCALE_FACTOR)
			glTexCoord2f(texture_verts.x, texture_verts.y)
			// This may be helpful if we have multiple textures.
			//(polymanager.getPolygonTexture(poly))
		}
		glEnd()

	}
	*/

	override def traits() = List("render", "update", "crystal")
}
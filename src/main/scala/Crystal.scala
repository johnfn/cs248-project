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
	val model = new CrystalModel()

	override def traits() = List("render", "update", "crystal")
}
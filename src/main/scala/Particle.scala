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

class ParticleModel(val x: Float, val y: Float, val z: Float)
	extends TexturedVBOModel( new ImageTexture("/textures/particle.png")
							, new ColorTexture(0, 0, 0)) {
	val name = "pmodel"

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

class Particle(p_x: Float, p_y: Float, p_z: Float) extends VBOModelEntity {
	x = p_x
	y = p_y
	z = p_z

	def particle_update() = {

	}

	val model = new ParticleModel(x, y, z)

	override def traits() = List("particle")
}
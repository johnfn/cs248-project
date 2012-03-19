package edu.stanford.cs248.project

import scala.math._

import org.lwjgl.opengl._
import java.nio.ByteBuffer

import edu.stanford.cs248.project.entity._
import edu.stanford.cs248.project.opengl._

import scala.util._
import org.lwjgl._
import input._
import Keyboard._
import scala.math._
import com.threed.jpct._
import com.threed.jpct.util._
import math._

class Enemy(enemy_x: Float, enemy_y: Float) extends VBOModelEntity {
	x = enemy_x
	y = enemy_y
	z = 0.0f
	var model = new SquareModel(x, y, z)
	var ticks = 0
	var destination = (x, y)
	val rand = new Random()

	def randDistance(min: Integer, max: Integer) = {
		(min + rand.nextInt(max - min)) * (if (rand.nextBoolean()) -1 else 1)
	}

	def sign(x: Float) = {
		if (x > 0) {
			1
		} else if (x < 0) {
			-1
		} else {
			0
		}
	}

	def chooseNewDestination() = {
		if (rand.nextBoolean()) {
			destination = (destination._1 + randDistance(1, 5), destination._2)
		} else {
			destination = (destination._1, destination._2 + randDistance(1, 5))
		}
	}

	def hurtProtagonist(m: EntityManager) = {
	    val p:Protagonist = m.entities.filter(_.traits.contains("protagonist")).head.asInstanceOf[Protagonist]

	    if (p.x == x && p.y == y) {
	    	p.hurt()
	    }
	}

	override def update(m: EntityManager) = {
		ticks += 1

		/* Trivial AI: Choose a random direction and walk that way. Repeat. */
		if (ticks % 200 == 0) {
		    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]

			if (destination == (x, y)) {
				// Choose a random direction
				chooseNewDestination()
			} else {
				// Walk that way
				val newx = x + sign(destination._1 - x)
				val newy = y + sign(destination._2 - y)

				if (lv.inBounds(newx, newy) && lv.height(newx, newy) - z <= .5){
					x = newx
					y = newy
					z = lv.height(newx, newy)

					hurtProtagonist(m)
				} else {
					chooseNewDestination()
				}
			}
		}
	}

	override def traits() = List("render", "update", "enemy")
}
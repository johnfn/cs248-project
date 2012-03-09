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

class Protagonist() extends VBOModelEntity {
  val JUMP_HEIGHT = .2f
  val GRAVITY = 0.02f

  var x = 0.0f
  var y = 0.0f
  var z = 0.0f
  val model = new SquareModel(x, y, z, List(250, 0, 0))

  var vz = 0.0f

  override def traits() = List("protagonist", "render", "update")

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
}
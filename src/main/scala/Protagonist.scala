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

class Ghost() extends VBOModelEntity {
  var x = 1.0f
  var y = 0.0f
  var z = 0.0f
  val model = new SquareModel(x, y, z)//, List(100, 0, 0))

  def setPosition(newx: Float, newy: Float, newz: Float) = {
    x = newx
    y = newy
    z = newz
  }
}

class Protagonist(val ghost: Ghost) extends VBOModelEntity {
  val JUMP_HEIGHT = .2f
  val GRAVITY = 0.02f

  var x = 0.0f
  var y = 0.0f
  var z = 0.0f
  val model = new SquareModel(x, y, z)//, List(250, 0, 0))

  var vz = 0.0f
  var ticks = 0

  override def traits() = List("protagonist", "render", "update")

  def move(lv: Level) = {
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

    if (isKeyDown(KEY_W)) newx += 1.0f
    if (isKeyDown(KEY_S)) newx -= 1.0f

    if (isKeyDown(KEY_A)) newy += 1.0f
    if (isKeyDown(KEY_D)) newy -= 1.0f

    if (lv.inBounds(newx, newy)) {
      if (lv.height(newx, newy) - newz <= .5) {
        x = newx
        y = newy
      }
    }

    z = newz
  }

  def moveGhost(m: EntityManager, lv: Level) = {
    val c:Crystal = m.entities.filter(_.traits.contains("crystal")).head.asInstanceOf[Crystal]

    val newx = (c.x - x) + c.x
    val newy = (c.y - y) + c.y

    // Keep the z position of the crystal. This could lead to some interesting
    // puzzle solving possibilities.
    ghost.setPosition(newx, newy, c.z)
  }

  def teleport(m: EntityManager, lv: Level) = {
    if (isKeyDown(KEY_Z)) {
      val zAtGhost = lv.height(ghost.x, ghost.y)
      // Don't teleport onto something higher than you are && ensure we're on the map still.
      if (zAtGhost <= ghost.z && lv.inBounds(ghost.x, ghost.y)) {
        x = ghost.x
        y = ghost.y
        z = ghost.z
      }
    }
  }

  override def update(m:EntityManager) = {
    // This is a common enough idiom that it may be worth abstracting out.
    val lv:Level = m.entities.filter(_.traits.contains("level")).head.asInstanceOf[Level]
    ticks += 1

    if (ticks % 5 == 0) {
      move(lv)
      moveGhost(m, lv)
      teleport(m, lv)
    }
  }
}
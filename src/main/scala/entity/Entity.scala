package edu.stanford.cs248.project.entity

import edu.stanford.cs248.project._

trait Entity {
  def update(m: EntityManager) = {}

  var initGLDone = false

  def checkInit() = if(!initGLDone) {
    doInitGL()
    initGLDone = true
  }

  def traits() = List("render", "update")
  def doInitGL() = {}
  def renderGL(shader: Shader) = {}
  def deleteGL() = {}
}

class EntityManager {
  var entities: List[Entity] = List()

  def add(e: Entity) = {
    e.checkInit()
    entities = e :: entities
  }

  def updateAll() = {
    entities.filter(_.traits.contains("update")).foreach(_.update(this))
  }

  def renderAll(shader: Shader) = {
    entities.filter(_.traits.contains("render")).foreach(_.renderGL(shader))
  }
}

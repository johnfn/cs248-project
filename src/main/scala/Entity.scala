package edu.stanford.cs248.project

trait Entity {
  def update() = {}
  
  var initGLDone = false
  
  def checkInit() = if(!initGLDone) {
    doInitGL()
    initGLDone = true
  }
  
  def doInitGL() = {}
  def renderGL() = {}
  def deleteGL() = {}
}

class EntityManager {
  var entities: List[Entity] = List()
  
  def add(e: Entity) = {
    e.checkInit()
    entities = e :: entities
  }
  
  def updateAll() = entities.foreach(_.update())
  def renderAll() = entities.foreach(_.renderGL())
}

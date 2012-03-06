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

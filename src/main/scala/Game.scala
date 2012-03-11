package edu.stanford.cs248.project

import org.lwjgl._
import opengl.{Display,GL11,DisplayMode, GLContext}
import GL11._
import input._
import math._
import Keyboard._
import scala.util.control.Breaks._

import edu.stanford.cs248.project.entity._

object Main {
  val GAME_TITLE = "My Game"
  val FRAMERATE = 60
  val width = 1280
  val height = 720

  val camera = new Camera()
  val manager = new EntityManager()
  val shader = new Shader("phong", "phong")

  def main(args:Array[String]) = {
    var fullscreen = false
    for(arg <- args){
      arg match{
        case "-fullscreen" =>
          fullscreen = true
      }
    }

    init(fullscreen)
    addObjects()
    run()
    gameOver()
  }

  def init(fullscreen:Boolean) = {
    Display.setTitle(GAME_TITLE)
    Display.setFullscreen(fullscreen)
    Display.setVSyncEnabled(true)
    Display.setDisplayMode(new DisplayMode(width,height))
    Display.create()

    GLContext.useContext()
    if(!GLContext.getCapabilities().GL_ARB_vertex_buffer_object) {
      println("OpenGL context doesn't support VBOs.")
      System.exit(-1)
    }
    
    if(!shader.init()) {
      println("""Shader "%s/%s" initialization failed"""
        .format(shader.vertName, shader.fragName))
      System.exit(-1)
    } else {
      shader.use()
    }

    glViewport(0, 0, width, height)
    glEnable(GL_DEPTH_TEST)
    glEnable(GL_CULL_FACE)
    glCullFace(GL_BACK)
    
    Mouse.setGrabbed(true)

  }

  def addObjects() = {
    val ghost = new Ghost()

    manager.add(camera)
    manager.add(new Level("level1"))
    manager.add(new Crystal(3.0f, 3.0f, 0.0f))
    manager.add(ghost)
    manager.add(new Protagonist(ghost))
  }

  def gameOver() = {
    Display.destroy()
    System.exit(0)
  }

  def updateGame() = {
    manager.updateAll()
  }

  def renderGame() = {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)

    camera.loadGLMatrices()

    manager.renderAll(shader)
  }

  def run() = {
    val fpsPrintInterval = 5000;
    var lastPrintTime = System.nanoTime()/1000000
    var framesDrawnSinceLastPrint = 0 
    
    while(!(isKeyDown(KEY_ESCAPE) || Display.isCloseRequested)) {
      updateGame()
      Display.update()
      renderGame()
      Display.sync(FRAMERATE)
      framesDrawnSinceLastPrint += 1
     
      val tNow = System.nanoTime()/1000000
      if(tNow > lastPrintTime+fpsPrintInterval) {
        println("FPS: %f"
          .format(framesDrawnSinceLastPrint.toDouble/(tNow-lastPrintTime)*1000))
        lastPrintTime = tNow
        framesDrawnSinceLastPrint = 0
      }
    }
  }
}

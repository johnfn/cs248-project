import org.lwjgl._
import opengl.{Display,GL11,DisplayMode}
import GL11._
import input._
import math._

object Main{
  val GAME_TITLE = "My Game"
  val FRAMERATE = 60
  val width = 640
  val height = 480

  val player = new Player(0,0);

  var finished = false


  class Player(nx:Float,ny:Float){
    var x:Float = nx
    var y:Float = ny
    var z:Float = 1.0f

    def applyPos {
        glTranslatef(x,y,z)
    }

    def draw = {
      glColor3f(1, 0, 0)
      glBegin(GL_QUADS)
      glVertex3f(-1.0f, z, -1.0f)
      glVertex3f(-1.0f, z,  1.0f)
      glVertex3f( 1.0f, z,  1.0f)
      glVertex3f( 1.0f, z, -1.0f)
      glEnd()
    }
  }

  def main(args:Array[String]){
    var fullscreen = false
    for(arg <- args){
      arg match{
        case "-fullscreen" =>
          fullscreen = true
      }
    }

    init(fullscreen)
    run
    gameOver()
  }

  def init(fullscreen:Boolean){

    println("init Display")
    Display.setTitle(GAME_TITLE)
    Display.setFullscreen(fullscreen)
    Display.setVSyncEnabled(true)
    Display.setDisplayMode(new DisplayMode(width,height))
    Display.create

    println("init gl")
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_LIGHTING)
    glEnable(GL_LIGHT0)
    adjustcam
  }

  def gameOver() {
    Display.destroy()
    System.exit(0)
  }

  def adjustcam(){
    //val v = Display.getDisplayMode.getWidth.toFloat/Display.getDisplayMode.getHeight.toFloat
    val v = 1.0f
    val width = 10
    val height = 10
    printf("v:%f",v)
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity
    //0, width, height
    glOrtho(0, width, 0, height, -1, 100);
    //glTranslatef(0.0f,0.0f, 6.0f);                 // Move Left 1.5 Units And Into The Screen 6.0
    glMatrixMode(GL_MODELVIEW)
  }

  def cleanup(){
    Display.destroy
  }

  def run(){
    while(!finished){
      Display.update

      logic
      render

      Display.sync(FRAMERATE)

      println(finished)
    }
  }

  def logic(){
      // in scala we can locally import all methods from Keyboard.
      import Keyboard._

    if(isKeyDown(KEY_ESCAPE))
      finished = true
    if(Display.isCloseRequested)
      finished = true

    // rx and rx store our keyboard input as direction
    var ry = 0
    var rx = 0

    // keys are IKJL for up down left right

    if(isKeyDown(KEY_I))
      ry += 1
    if(isKeyDown(KEY_K))
      ry -= 1
    if(isKeyDown(KEY_J))
      rx -= 1
    if(isKeyDown(KEY_L))
      rx += 1

    player.x += rx;
    player.y += ry;
    /*
    // this makes the direction relative to the camera position
    // it is a simple rotation matrix you may know from linear algebra
    val ax = rx*cos(-rotation.toRadians)-ry*sin(-rotation.toRadians)
    val ay = rx*sin(-rotation.toRadians)+ry*cos(-rotation.toRadians)

    player.x += 0.1f*ax.toFloat
    player.y += 0.1f*ay.toFloat
    */

    // this rotates our camera around the center
  }

  def render(){
    glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glLoadIdentity

    glTranslatef(0,0,-20)
    glRotatef(-70,1,0,0)

    glPushMatrix
    player.applyPos
    //rotate the player just for fun
    player.draw
    glPopMatrix

    //without background, motion is not visible
    // a green grid is nice and retro
    glColor3f(0,1,0)
  }
}

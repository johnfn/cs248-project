import org.lwjgl._
import opengl.{Display,GL11,DisplayMode}
import GL11._
import input._
import math._
import Keyboard._

object Main{
  val GAME_TITLE = "My Game"
  val FRAMERATE = 60
  val width = 640
  val height = 480

  val player = new Player(100, 100, 16, 16)
  val map = new Map(0, 0, 20, 20)
  val manager = new Manager()

  var finished = false

  trait controllable extends Entity{
    def control() = {
      var rx = 0
      var ry = 0

      if(isKeyDown(KEY_I)) ry += 1
      if(isKeyDown(KEY_K)) ry -= 1
      if(isKeyDown(KEY_J)) rx -= 1
      if(isKeyDown(KEY_L)) rx += 1

    }
  }

  class Manager() {
    var entities:List[Entity] = List()

    def get(entity_type:String):List[Entity] = {
      entities.filter(e => e.traits.contains(entity_type))
    }

    def one(entity_type:String):Entity = {
      val list:List[Entity] = get(entity_type)
      assert(list.length == 1)

      list(0)
    }

    def add(entity:Entity):Unit = {
      entities = entities :+ entity
    }

    def update_all():Unit = {
      get("update").map(_.update(this))
    }

    def draw_all():Unit = {
      get("draw").sortBy(_.depth).foreach(_.draw)
    }
  }

  abstract class Entity(var x:Float, var y:Float, var width:Float, var height:Float) {
    var vx:Float = 0
    var vy:Float = 0
    var traits:List[String] = List("update", "draw")

    def touchesPoint(_x:Float, _y:Float):Boolean = {
      x <= _x && _x <= x + width && y <= _y && _y <= y + height
    }

    def touchesEntity(other:Entity):Boolean = {
       ( touchesPoint(other.x, other.y)
      || touchesPoint(other.x, other.y + other.height)
      || touchesPoint(other.x + other.width, other.y)
      || touchesPoint(other.x + other.width, other.y + other.height))
    }

    def render;
    def update(m:Manager);
    def depth:Int;

    /* Overridden in stuff like Map to be more accurate */
    def collidesWith(other:Entity):Boolean = {
      assert(other.width == width)
      assert(other.height == height)

      touchesEntity(other)
    }

    def draw = {
      glPushMatrix()
      glTranslatef(x, y, 0)
      render
      glPopMatrix()
    }
  }

  class Tile(x:Float, y:Float, width:Float, height:Float, t:Float) extends Entity(x, y, width, height) {
    override def update(m:Manager) = {}

    override def render = {
      glColor3f(0.0f, t, 1.0f)
      glBegin(GL_QUADS)
      glVertex3f(0.0f, 0.0f, 0.0f)
      glVertex3f(0.0f, height, 0.0f)
      glVertex3f(width, height, 0.0f)
      glVertex3f(width, 0.0f, 0.0f)
      glEnd()
    }

    override def depth:Int = 5;

    override def collidesWith(other:Entity):Boolean = {
      t == 0 && touchesEntity(other)
    }
  }

  class Map(x:Float, y:Float, width:Float, height:Float) extends Entity(x, y, width, height) {
    traits = List("update", "draw", "map")
    var data:Array[Array[Int]] = Array.ofDim[Int](width.toInt, height.toInt).zipWithIndex.map {case (elem, i) => elem.map((e) => 1)}
    for (j <- 0 until width.toInt) {
      data(j)(15) = 0
    }
    var tiles:Array[Tile] = data.zipWithIndex.map{ case (line, i) => line.zipWithIndex.map{ case (elem, j) => new Tile(i * 16, j * 16, 16, 16, elem.toFloat)}}.reduce(_ ++ _)

    override def draw = {
      tiles.map(e => e.draw)
    }

    override def render() = {}
    override def update(m:Manager) = {}
    override def depth:Int = 5;

    override def collidesWith(e:Entity):Boolean = {
      tiles.count(_.collidesWith(e)) > 0
    }
  }

  class Player(var _x:Float, var _y:Float, width:Float, height:Float) extends Entity(_x, _y, width, height){
    val speed = 5
    def render = {
      glColor3f(1.0f,1.0f,1.0f)

      glBegin(GL_QUADS)
      glVertex2f(0, 0)
      glVertex2f(0, height)
      glVertex2f(width, height)
      glVertex2f(width, 0)
      glEnd()
    }

    override def depth:Int = 99;

    def update(m:Manager) = {
      val map = manager.one("map")
      var ry = 5
      var rx = 0

      // keys are IKJL for up down left right

      if(isKeyDown(KEY_I)) ry -= speed
      if(isKeyDown(KEY_K)) ry += speed
      if(isKeyDown(KEY_J)) rx -= speed
      if(isKeyDown(KEY_L)) rx += speed

      x += rx;
      if (map.collidesWith(this)) {
        x -= rx;
      }

      y += ry;
      if (map.collidesWith(this)) {
        y -= ry;
      }
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
    Display.setTitle(GAME_TITLE)
    Display.setFullscreen(fullscreen)
    Display.setVSyncEnabled(true)
    Display.setDisplayMode(new DisplayMode(width,height))
    Display.create

    //glDisable(GL_DEPTH_TEST) //This may annoy me in the future.
    //glEnable(GL_LIGHTING)
    //glEnable(GL_LIGHT0)
    adjustcam

    manager.add(player)
    manager.add(map)
  }

  def gameOver() {
    Display.destroy()
    System.exit(0)
  }

  def adjustcam(){
    glMatrixMode(GL_PROJECTION)
    glLoadIdentity
    glOrtho(0, width, height, 0, -1, 1);

    glMatrixMode(GL_MODELVIEW)
    glLoadIdentity()
    glViewport(0, 0, width, height)
  }

  def cleanup(){
    Display.destroy
  }

  def run(){
    while(!(isKeyDown(KEY_ESCAPE) || Display.isCloseRequested)) {
      Display.update

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      manager.update_all()
      manager.draw_all()

      Display.sync(FRAMERATE)
    }
  }
}

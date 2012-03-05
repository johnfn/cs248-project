package edu.stanford.cs248.project.terrain

import edu.stanford.cs248.project.util._

abstract class HeightMap {
  def xSize: Int
  def ySize: Int
  
  // 8-bit grayscale texture with signed byte heights
  // NOTE: heights are SIGNED, intepreted as varying from -128 to 127 inclusive
  def heights: Texture
}

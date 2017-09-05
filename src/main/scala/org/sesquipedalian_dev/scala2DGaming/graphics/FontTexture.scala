/**
  * Copyright 2017 sesquipedalian.dev@gmail.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.sesquipedalian_dev.scala2DGaming.graphics

import java.awt.Font.TRUETYPE_FONT
import java.awt.geom.AffineTransform
import java.awt.{FontMetrics, Graphics2D, RenderingHints}
import java.awt.image.{AffineTransformOp, BufferedImage}

import org.lwjgl.system.MemoryUtil

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL12._
import org.lwjgl.opengl.GL30._
import org.lwjgl.opengl.GL42._
import org.lwjgl.stb.STBImage._
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.util.{ThrowsExceptionOnGLError, cleanly}

import scala.util.{Failure, Success}

// struct for storing info about the location of a given character on the texture atlas
case class Glyph(
  x: Float,
  y: Float,
  width: Float,
  height: Float
)

// load a true type font for use in GL
class FontTexture(
  resourceFontFile: String, // resource file path to locate the ttf
  point: Int = 1   // font size
) extends ThrowsExceptionOnGLError
{
  var glyphs = Map[Char, Glyph]()

  var textureHandle: Option[Int] = None

  def init(): Unit = {
    // load font from ttf file
    val resourceStream = getClass.getResourceAsStream(resourceFontFile)
    val initialFont = java.awt.Font.createFont(TRUETYPE_FONT, resourceStream)
    val sizedFont = initialFont.deriveFont(point.toFloat) // TODO style? bold, italic etc

    // get the font metrics on a graphics context to determine the true height / width of this character
    val textureImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    val g = textureImage.createGraphics()
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
    g.setFont(sizedFont)
    val fontMetrics = g.getFontMetrics
    g.dispose()

    // figure out the needed height / width of our image
    var totalWidth = 0
    var totalHeight = 0
    val charImages = for {
      asciiIndex <- 32 until 127
    } yield {
      val c = asciiIndex.toChar
      val cImage: BufferedImage = createCharImage(sizedFont, fontMetrics, c)

      totalWidth += cImage.getWidth()
      totalHeight = math.max(totalHeight, cImage.getHeight())
      (c -> cImage)
    }

    // now that we've got the overall height, we can create the texture atlas (combined together all the glyphs)
    val textureAtlas = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB)
    val g2 = textureAtlas.createGraphics()
    var currentXIndex = 0
    for {
      (char, charImage) <- charImages
    } {
      val cWidth = charImage.getWidth()
      val cHeight = charImage.getHeight()
      val yIndex = totalHeight - cHeight
      val newGlyph = Glyph(  // normalize to texture coords
        currentXIndex.toFloat / totalWidth,
        yIndex.toFloat / totalHeight,
        (currentXIndex + cWidth).toFloat / totalWidth,
        (cHeight).toFloat / totalHeight
      )
//      println(s"creating glyph $char $newGlyph")

      g2.drawImage(charImage, currentXIndex, 0, null)
      glyphs += (char -> newGlyph)
      currentXIndex += cWidth + 1 /* spacer between chars */
    }
    // now g2 has all the font stuff we want drawn on it

    // Flip image Horizontal to make the origin the bottom left? I don't know about all this
    val transform = AffineTransform.getScaleInstance(1, -1)
    transform.translate(0, -totalHeight)
    val operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
    val flippedTextureAtlas = operation.filter(textureAtlas, null)
//    val flippedTextureAtlas = textureAtlas

    // put pixels into a byte buffer to send to the GPU
    val pixelBuffer = MemoryUtil.memAlloc(totalHeight * totalWidth)
    for {
      y <- 0 until totalHeight
      x <- 0 until totalWidth
    } {
      val pixel = flippedTextureAtlas.getRGB(x, y)
//      println(String.format("loading font texture - RGB! %d/%d = %x %d", new Integer(x), new Integer(y), new Integer(pixel), new Integer(pixel)))
      pixelBuffer.put(((pixel >> 24) & 0xFF).toByte) // we only really need alpha so we can pick custom text colors
    }
    pixelBuffer.flip()

    checkError()
    // create / bind a texture handle to use this alias
    textureHandle = Some(glGenTextures())
    checkError()
    textureHandle.foreach(glBindTexture(GL_TEXTURE_2D, _))
    checkError()

//    println(s"font thing ${pixelBuffer.remaining()}")

    // load up the texture data
    glTexImage2D(GL_TEXTURE_2D, 0,
      GL_R8,
      totalWidth, totalHeight,
      0, // border
      GL_RED, GL_UNSIGNED_BYTE,
      pixelBuffer
    )
    checkError()

    // how to unpack the RGBA bytes
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
    checkError()

    // texture parameters
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)
    checkError()

    // free the CPU memory for the texture
    MemoryUtil.memFree(pixelBuffer)

    // free up the texture unit
    glBindTexture(GL_TEXTURE_2D, 0)
    checkError()
  }

  def createCharImage(font: java.awt.Font, fontMetrics: FontMetrics, c: Char): BufferedImage = {
    val charWidth = fontMetrics.charWidth(c)
    val charHeight = fontMetrics.getHeight()

    // create BufferedImage of the character
    val cImage = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB)
    val g2 = cImage.createGraphics()
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
    g2.setFont(font)
    g2.setPaint(java.awt.Color.WHITE)
    g2.drawString(c.toString, 0, fontMetrics.getAscent())
    g2.dispose()

    // return the generated image
    cImage
  }

  def cleanup(): Unit = {
    textureHandle.foreach(glDeleteTextures)
  }
}

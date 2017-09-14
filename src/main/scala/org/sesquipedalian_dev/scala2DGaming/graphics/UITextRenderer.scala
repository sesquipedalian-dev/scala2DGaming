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

import java.awt.Color

import org.lwjgl.opengl.GL11.{GL_FLOAT, GL_TEXTURE_2D, glBindTexture}
import org.lwjgl.opengl.GL20._
import org.lwjgl.system.MemoryUtil

class UITextRenderer(
  val uiWidth: Int,
  _uiHeight: Int
) extends Renderer {
  var aspectRatio: Option[Float] = None
  def uiHeight: Float = {
    camera.flatMap(_.aspectRatio).map(f => {
      val result: Float = _uiHeight.toFloat / f
      println(s"uiHeight calc $uiHeight $f = $result")
      result
    }).getOrElse(_uiHeight)
  }
  final val FLOAT_PER_VERTEX = 7
  final val VERTEX_PER_CHAR = 4
  final val EL_PER_CHAR = 6

  case class TextInfo(
    name: String,
    size: Int,
    var texture: Option[FontTexture] = None
  )

  val textSizes: List[TextInfo] = List(
    TextInfo(UITextRenderer.SMALL, 16),
    TextInfo(UITextRenderer.MEDIUM, 32),
    TextInfo(UITextRenderer.LARGE, 64)
  )

  def MAX_CHARS_PER_DRAW = 1024 / (VERTEX_PER_CHAR * FLOAT_PER_VERTEX )
  def vertexBufferSize: Int = MAX_CHARS_PER_DRAW * (VERTEX_PER_CHAR * FLOAT_PER_VERTEX )
  def elementBufferSize: Int = MAX_CHARS_PER_DRAW * EL_PER_CHAR
  def vertexShaderRscName: String = "/shaders/text.vert"
  def fragmentShaderRscName: String = "/shaders/text.frag"

  var camera: Option[UICamera] = None

  override def init(): Unit = {
    super.init()
    checkError()

    programHandle.foreach(glUseProgram)

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val stride = 7 * java.lang.Float.BYTES
    setUpVertexArrayAttrib("position", GL_FLOAT, 2, stride, 0)
    setUpVertexArrayAttrib("textColor", GL_FLOAT, 3, stride, 2 * java.lang.Float.BYTES)
    setUpVertexArrayAttrib("texCoordinate", GL_FLOAT, 2, stride, 5 * java.lang.Float.BYTES)
    setUpShaderUniform("texImage", 0)

    drawCalls = textSizes.map(struct => {
      val font = new FontTexture("/fonts/Consolas.ttf", struct.size)
      font.init()
      struct.texture = Some(font)

      struct.name -> DrawCallInfo(
        MemoryUtil.memAllocInt(elementBufferSize),
        MemoryUtil.memAllocFloat(vertexBufferSize),
        0,
        () => {
          font.textureHandle.foreach(th => {
            glBindTexture(GL_TEXTURE_2D, th)
          })
        }
      )
    }).toMap

    // create our camera
    camera = Some(new UICamera(uiWidth, _uiHeight, 1))
    camera.foreach(camera => {
      camera.register()
      programHandle.foreach(camera.init)
    })
    register()
  }

  override def render(): Unit = {
    programHandle.foreach(glUseProgram)
    camera.foreach(_.updateScreenSize(programHandle, "projection"))

    HasUiRendering.render(this)

    super.render()
  }

  def drawTextOnWorld(x: Float, y: Float, text: String, color: Color, size: String): Unit = {
    for {
      (c, index) <- text.zipWithIndex
      drawCall <- drawCalls.get(size)
      fontInfo <- textSizes.find(_.name == size)
      glyph <- fontInfo.texture.flatMap(_.glyphs.get(c))
    } {
      val vertexBuffer = drawCall.vertexBuffer
      val elBuffer = drawCall.elBuffer

      if (vertexBuffer.remaining < (VERTEX_PER_CHAR * FLOAT_PER_VERTEX) || elBuffer.remaining < EL_PER_CHAR) {
        flushVertexData(size)
      }

      val x1 = x + (fontInfo.size * index)
      val x2 = x + (fontInfo.size * (index + 1))
      val y1 = y
      val y2 = y + (fontInfo.size)

//      println(s"Renderin' da text $x1/$x2 $y1 $y2 $size")

      vertexBuffer.put(x + (fontInfo.size * index)).put(y)
        .put(color.getRed).put(color.getGreen).put(color.getBlue)
        .put(glyph.x).put(glyph.height)
      vertexBuffer.put(x + (fontInfo.size * (index + 1))).put(y)
        .put(color.getRed).put(color.getGreen).put(color.getBlue)
        .put(glyph.width).put(glyph.height)
      vertexBuffer.put(x + (fontInfo.size * (index + 1))).put(y + fontInfo.size)
        .put(color.getRed).put(color.getGreen).put(color.getBlue)
        .put(glyph.width).put(glyph.y)
      vertexBuffer.put(x + (fontInfo.size * (index))).put(y + fontInfo.size)
        .put(color.getRed).put(color.getGreen).put(color.getBlue)
        .put(glyph.x).put(glyph.y)

      val currentVertIndex = drawCall.numObjectsThisDraw / EL_PER_CHAR * VERTEX_PER_CHAR
      elBuffer.put(currentVertIndex).put(currentVertIndex + 1).put(currentVertIndex + 2)
      elBuffer.put(currentVertIndex + 2).put(currentVertIndex + 3).put(currentVertIndex)

      drawCall.numObjectsThisDraw += EL_PER_CHAR
    }
  }

  override def flushVertexData(key: String): Unit = {
    super.flushVertexData(key)
  }

  override def cleanup(): Unit = {
    super.cleanup()
    textSizes.foreach(_.texture.foreach(_.cleanup()))
    camera.foreach(_.cleanup)
  }

  UITextRenderer.singleton = Some(this)
}

object UITextRenderer {
  final val LARGE: String = "LARGE"
  final val MEDIUM: String = "MEDIUM"
  final val SMALL: String = "SMALL"

  var singleton: Option[UITextRenderer] = None
}

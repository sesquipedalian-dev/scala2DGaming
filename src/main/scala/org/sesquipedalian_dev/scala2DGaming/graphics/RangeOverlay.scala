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

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15.{GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER, glBindBuffer, glBufferSubData}
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.util.cleanly

/*
  Rendering layer for drawing overlays in world texture space.  E.g. this can be used
  to display the range of a turret.

 */
class RangeOverlay(worldWidth: Int, worldHeight: Int, textureSize: Int) extends Renderer {
  final val BYTES_PER_VERTEX = java.lang.Float.BYTES * 9;
  override def vertexBufferSize = 4096000 / BYTES_PER_VERTEX // max of 4mb per draw call
  override def elementBufferSize = 0 // don't use el buffer
  override def vertexShaderRscName = "/shaders/range.vert"
  override def fragmentShaderRscName = "/shaders/range.frag"
  override def geometryShaderRscName: Option[String] = Some("/shaders/range_geometry.glsl")

  override def init(): Unit = {
    super.init()

    programHandle.foreach(glUseProgram)
    checkError()

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val stride = 9 * java.lang.Float.BYTES
    setUpVertexArrayAttrib("pos", GL_FLOAT, 2, stride, 0)
    setUpVertexArrayAttrib("color", GL_FLOAT, 4, stride, 2 * java.lang.Float.BYTES)
    setUpVertexArrayAttrib("minAngle", GL_FLOAT, 1, stride, 6 * java.lang.Float.BYTES)
    setUpVertexArrayAttrib("maxAngle", GL_FLOAT, 1, stride, 7 * java.lang.Float.BYTES)
    setUpVertexArrayAttrib("length", GL_FLOAT, 1, stride, 8 * java.lang.Float.BYTES)

    drawCalls = Map(
      "" -> DrawCallInfo(
        MemoryUtil.memAllocInt(elementBufferSize),
        MemoryUtil.memAllocFloat(vertexBufferSize),
        0,
        () => {}
      )
    )

    register()
  }

  override def render(): Unit = {
    programHandle.foreach(glUseProgram)
    WorldSpritesRenderer.singleton.flatMap(_.camera).foreach(_.updateScreenSize(programHandle, "projection"))

    HasRangeOverlayRendering.render(this)

    super.render()
  }

  override def cleanup(): Unit = {
    // free all the memory we used
    super.cleanup()
  }

  // this renderer doesn't use an element array and it renders points
  override def flushVertexData(key: String = ""): Unit = {
    drawCalls.get(key).foreach(drawCall => {
      val vertexBuffer = drawCall.vertexBuffer
      val numObjectsThisDraw = drawCall.numObjectsThisDraw

      if (numObjectsThisDraw > 0) {
        vao.flatMap(v => programHandle.map(ph => (v, ph))).foreach(t => {
          val (v, p) = t
          glBindVertexArray(v)
          glUseProgram(p)

          drawCall.callback()

          vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
          vertexBuffer.flip()
          glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)
          checkError()

          glDrawArrays(GL_POINTS, 0, numObjectsThisDraw)
          checkError()

          // clear the vertex data for this pass
          vertexBuffer.clear()
          drawCall.numObjectsThisDraw = 0
        })
      }
    })
  }

  def draw(x: Float, y: Float, color: Color, minAngleRadians: Float, maxAngleRadians: Float, range: Float): Unit = {
    drawCalls.get("").foreach(drawCall => {
      val vertexBuffer = drawCall.vertexBuffer
      if(vertexBuffer.remaining < (BYTES_PER_VERTEX)) {
        flushVertexData()
      }

      vertexBuffer.put(x * textureSize).put(y * textureSize)
        .put(color.getRed.toFloat / 0xFF).put(color.getGreen.toFloat / 0xFF)
        .put(color.getBlue.toFloat / 0xFF).put(color.getAlpha.toFloat / 0xFF)
        .put(minAngleRadians).put(maxAngleRadians).put(range * textureSize)

      drawCall.numObjectsThisDraw += 1
    })
  }

  RangeOverlay.singleton = Some(this)
}

object RangeOverlay {
  var singleton: Option[RangeOverlay] = None
}
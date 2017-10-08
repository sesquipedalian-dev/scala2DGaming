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
import java.nio.FloatBuffer

import org.lwjgl.glfw.GLFW._
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL20._
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.entities.{CanBuild, HasCostToBuild, Location, PendingBuild}
import org.sesquipedalian_dev.scala2DGaming.game.Commander
import org.sesquipedalian_dev.scala2DGaming.input.MouseInputHandler
import org.sesquipedalian_dev.util._
import org.sesquipedalian_dev.util.registry.HasRegistrySingleton

/*
  Rendering layer for drawing overlays in world texture space.  E.g. this can be used
  to display the range of a turret.
 */
class BuildOverlay(
  worldWidth: Int,
  worldHeight: Int,
  textureSize: Int
) extends Renderer
  with ConvertsScreenAndWorldCoords
  with MouseInputHandler
{

  final val BYTES_PER_VERTEX = java.lang.Float.BYTES * 2;
  override def vertexBufferSize = 4096000 / BYTES_PER_VERTEX // max of 4mb per draw call
  override def elementBufferSize = vertexBufferSize * 6 * java.lang.Float.BYTES // don't use el buffer
  override def vertexShaderRscName = "/shaders/buildCursor.vert"
  override def fragmentShaderRscName = "/shaders/buildCursor.frag"

  override def init(): Unit = {
    super.init()

    programHandle.foreach(glUseProgram)
    checkError()

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val stride = 2 * java.lang.Float.BYTES
    setUpVertexArrayAttrib("pos", GL_FLOAT, 2, stride, 0)

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

  var currentBuilder: Option[CanBuild] = None

  def currentWorldLoc(): Option[Location] = {
    toWorld(Location(currentX, currentY)).map(l => Location(Math.floor(l.x).toFloat, Math.floor(l.y).toFloat))
  }

  def isValidBuildLoc(): Boolean = {
    val worldLoc = currentWorldLoc()
    val result = (currentBuilder zip worldLoc).exists(p => {
      val (builder, loc) = p

      builder.canBuildOn(loc)
    })
    trace"isValidBuildLoc? $currentX $currentY $worldLoc $result"
    result
  }

  override def render(): Unit = currentBuilder.foreach(builder => {
    programHandle.foreach(glUseProgram)
    WorldSpritesRenderer.singleton.flatMap(_.camera).foreach(_.updateScreenSize(programHandle, "projection"))

    // also render green / red depending on whether this is a valid location to build on
    val color = if(isValidBuildLoc()) {
      new Color(0, 0, 255)
    }  else {
      new Color(255, 0, 0)
    }

    // set geoColor uniform
    val geoColorLoc = programHandle.map(glGetUniformLocation(_, "geoColor"))
    checkError()
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(3)

      val r = color.getRed.toFloat / 255
      val g = color.getGreen.toFloat / 255
      val b = color.getBlue.toFloat / 255
      buf.put(r).put(g).put(b)
      buf.flip()
      trace"hooking up color uniform $r $g $b"

      geoColorLoc.foreach(glUniform3fv(_, buf))
      checkError()
    })

    // draw the thing
    draw(currentWorldLoc().get.x, currentWorldLoc().get.y)

    super.render()
  })

  override def cleanup(): Unit = {
    // free all the memory we used
    super.cleanup()
  }

  def draw(x: Float, y: Float): Unit = {
    drawCalls.get("").foreach(drawCall => {
      val vertexBuffer = drawCall.vertexBuffer
      val elBuffer = drawCall.elBuffer
      if(vertexBuffer.remaining < (4 * 5) || elBuffer.remaining < (2 * 3)) {
        flushVertexData("")
      }

      vertexBuffer.put(x * textureSize).put(y * textureSize)
      vertexBuffer.put((x + 1) * textureSize).put(y * textureSize)
      vertexBuffer.put((x + 1) * textureSize).put((y + 1) * textureSize)
      vertexBuffer.put(x * textureSize).put((y + 1) * textureSize)

      val currentVertIndex = drawCall.numObjectsThisDraw / 6 * 4
      elBuffer.put(currentVertIndex).put(currentVertIndex + 1).put(currentVertIndex + 2)
      elBuffer.put(currentVertIndex + 2).put(currentVertIndex + 3).put(currentVertIndex)

      drawCall.numObjectsThisDraw += 6
    })
  }

  BuildOverlay.register(this)

  override def handleAction(windowHandle: Long, button: Int, action: Int) = {
    if(action == GLFW_RELEASE && button == GLFW_MOUSE_BUTTON_LEFT && isValidBuildLoc()) {
      info"building a thing!? $currentX $currentY"
      (currentBuilder zip currentWorldLoc).foreach(p => {
        val (builder, loc) = p
        if(builder.buildTimeSeconds > 0) {
          builder match {
            case x: HasCostToBuild => Commander.changeMoney(-x.cost)
            case _ =>
          }
          new PendingBuild(loc, builder.buildTimeSeconds, builder)
        } else {
          builder.buildOn(loc)
        }
      })
      true
    } else {
      false
    }
  }
}

object BuildOverlay extends HasRegistrySingleton {
  override type ThisType = BuildOverlay
  def enable(canBuild: CanBuild): Unit = singleton.foreach(_.currentBuilder = Some(canBuild))
  def disable(): Unit = singleton.foreach(_.currentBuilder = None)
}
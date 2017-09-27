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

import java.nio.{FloatBuffer, IntBuffer}

import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20._
import org.lwjgl.opengl.GL30.{glBindVertexArray, glDeleteVertexArrays, glGenVertexArrays}
import org.lwjgl.opengl.GL32._
import org.lwjgl.system.{MemoryStack, MemoryUtil}
import org.sesquipedalian_dev.util.{Logging, ThrowsExceptionOnGLError, cleanly}

case class DrawCallInfo(
  elBuffer: IntBuffer,
  vertexBuffer: FloatBuffer,
  var numObjectsThisDraw: Int,
  callback: () => Unit // perform any necessary setup
)

trait Renderer extends Renderable with ThrowsExceptionOnGLError with Logging {
  def vertexBufferSize: Int
  def elementBufferSize: Int
  def vertexShaderRscName: String
  def fragmentShaderRscName: String
  def geometryShaderRscName: Option[String] = None

  // give easy way to reference the draw call info - text index
  var drawCalls: Map[String, DrawCallInfo] = Map()

  var programHandle: Option[Int] = None

  // vertex array object - keep track of memory layout of vertex array buffers
  var vao: Option[Int] = None

  // vertex buffer object - communicate vertices to GFX
  var vbo: Option[Int] = None

  // element array buffer - once vertices are defined in the buffer, elements let us reuse those vertices.
  var ebo: Option[Int] = None

  def init(): Unit = {
    glEnable(GL_BLEND)
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

    // set up Vertex Array Object - defines memory layout for sending vertex data to GPU
    vao = Some(glGenVertexArrays())
    vao.foreach(glBindVertexArray)

    // NOTE the other array buffers need to be defined REAL CLOSE to the VAO for whatever reason

    vbo = Some(glGenBuffers())
    vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
    glBufferData(GL_ARRAY_BUFFER, vertexBufferSize * java.lang.Float.BYTES, GL_STATIC_DRAW)

    ebo = Some(glGenBuffers())
    ebo.foreach(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _))
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBufferSize * java.lang.Integer.BYTES, GL_STATIC_DRAW)

    // create vertex shader, load up the source for it, and compile
    val vertexShaderHandle: Int = glCreateShader(GL_VERTEX_SHADER)
    val vertexShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream(vertexShaderRscName)
    ).mkString
    glShaderSource(vertexShaderHandle, vertexShaderSource)
    glCompileShader(vertexShaderHandle)

    // create fragment shader, load up the source for it, and compile
    val fragmentShaderHandle: Int = glCreateShader(GL_FRAGMENT_SHADER)
    val fragmentShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream(fragmentShaderRscName)
    ).mkString
    glShaderSource(fragmentShaderHandle, fragmentShaderSource)
    glCompileShader(fragmentShaderHandle)

    val geoShaderHandle = geometryShaderRscName.map(geoRsc => {
      val handle: Int = glCreateShader(GL_GEOMETRY_SHADER)
      val source = io.Source.fromInputStream(
        getClass.getResourceAsStream(geoRsc)
      ).mkString
      glShaderSource(handle, source)
      glCompileShader(handle)
      handle
    })

    // check compile status of the shaders
    cleanly(MemoryStack.stackPush())(stack => {
      val vertexCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(vertexShaderHandle, GL_COMPILE_STATUS, vertexCompiled)

      val fragmentCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(fragmentShaderHandle, GL_COMPILE_STATUS, fragmentCompiled)

      val geometryCompiled = geoShaderHandle.map(handle => {
        val compiled = stack.mallocInt(1)
        glGetShaderiv(handle, GL_COMPILE_STATUS, compiled)
        compiled.get(0)
      })

      if((vertexCompiled.get(0) <= 0) || (fragmentCompiled.get(0) <= 0) || geometryCompiled.exists(_ <= 0)) {
        error"""error compiling shaders! ${glGetShaderInfoLog(vertexShaderHandle)}
          ${glGetShaderInfoLog(fragmentShaderHandle)}
          ${geoShaderHandle.map(glGetShaderInfoLog)}
          """
      }
    })

    // create shader program and attach the shaders we created
    programHandle = Some(glCreateProgram())
    programHandle.foreach(glAttachShader(_, vertexShaderHandle))
    programHandle.foreach(glAttachShader(_, fragmentShaderHandle))
    (programHandle zip geoShaderHandle).foreach(p => glAttachShader(p._1, p._2))

    // link the program
    programHandle.foreach(glLinkProgram)
    checkError()
    cleanly(MemoryStack.stackPush())(stack => {
      val linkStatus: IntBuffer = stack.mallocInt(1)
      programHandle.foreach(glGetProgramiv(_, GL_LINK_STATUS, linkStatus))

      if(linkStatus.get(0) <= 0) {
        error"error linking shader program! ${programHandle.map(glGetProgramInfoLog)}"
      }
    })

    glDeleteShader(vertexShaderHandle)
    checkError()
    glDeleteShader(fragmentShaderHandle)
    checkError()
    geoShaderHandle.foreach(glDeleteShader)
    checkError()
  }

  def setUpVertexArrayAttrib(name: String, glAttribType: Int, size: Int, stride: Int, offset: Int): Unit = {
    (vao zip programHandle).foreach(p => {
      val (vaoHandle, prog) = p
      glUseProgram(prog)
      checkError()
      glBindVertexArray(vaoHandle)
      checkError()

      val attribLoc = glGetAttribLocation(prog, name)
      if(attribLoc != -1) {
        checkError()
        glEnableVertexAttribArray(attribLoc)
        checkError()
        glVertexAttribPointer(attribLoc, size, glAttribType, false, stride, offset)
        checkError()
      } else {
        // silent ignore - this attribute wasn't needed
        info"shader attribute not used $name"
      }
    })
    checkError()
  }

  def setUpShaderUniform(name: String, value: Int): Unit = {
    (vao zip programHandle).foreach(p => {
      val (vaoHandle, prog) = p
      val uniformLoc = glGetUniformLocation(prog, name)
      glUniform1i(uniformLoc, value)
    })
    checkError()
  }

  def setUpShaderUniform(name: String, value: Float): Unit = {
    (vao zip programHandle).foreach(p => {
      val (vaoHandle, prog) = p
      val uniformLoc = glGetUniformLocation(prog, name)
      glUniform1f(uniformLoc, value)
    })
    checkError()
  }

  def render(): Unit = {
    // if we've set up stuff to draw this frame, send it to the GPU
    drawCalls.keys.foreach(flushVertexData)
  }

  def cleanup(): Unit = {
    vao.foreach(glDeleteVertexArrays)
    vbo.foreach(glDeleteBuffers)
    ebo.foreach(glDeleteBuffers)
    programHandle.foreach(glDeleteProgram)
    drawCalls.foreach(drawCall => {
      MemoryUtil.memFree(drawCall._2.vertexBuffer)
      MemoryUtil.memFree(drawCall._2.elBuffer)
    })
  }

  def flushVertexData(key: String): Unit = {
    drawCalls.get(key).foreach(drawCall => {
      val vertexBuffer = drawCall.vertexBuffer
      val elBuffer = drawCall.elBuffer
      val numObjectsThisDraw = drawCall.numObjectsThisDraw

      if(numObjectsThisDraw > 0) {
        vao.flatMap(v => programHandle.map(ph => (v, ph))).foreach(t => {
          val (v, p) = t
          glBindVertexArray(v)
          glUseProgram(p)

          drawCall.callback()

          vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
          vertexBuffer.flip()
          glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer)
          checkError()

          ebo.foreach(glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, _))
          elBuffer.flip()
          glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, elBuffer)
          checkError()

          glDrawElements(GL_TRIANGLES, numObjectsThisDraw, GL_UNSIGNED_INT, 0)
          checkError()

          // clear the vertex data for this pass
          vertexBuffer.clear()
          elBuffer.clear()
          drawCall.numObjectsThisDraw = 0
        })
      }
    })
  }
}

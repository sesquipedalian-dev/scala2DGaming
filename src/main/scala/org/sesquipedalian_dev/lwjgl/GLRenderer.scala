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
package org.sesquipedalian_dev.lwjgl

import java.nio.{FloatBuffer, IntBuffer}

import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.{glfwGetCurrentContext, glfwGetFramebufferSize, glfwSwapBuffers}
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11._
import org.lwjgl.opengl.GL15._
import org.lwjgl.opengl.GL20.{glGetAttribLocation, glGetUniformLocation, glUniformMatrix4fv, _}
import org.lwjgl.opengl.GL30.{glDeleteVertexArrays, _}
import org.lwjgl.system.MemoryStack
import util.{cleanly, _}

// wrap GL
class GLRenderer(textureSize: Int, worldWidth: Int, worldHeight: Int) {
  final val MAX_ZOOM: Float = 1f

  // vertex array object - keep track of memory layout of vertex array buffers
  var vao: Option[Int] = None

  // shader program handle
  var programHandle: Option[Int] = None

  // vertex buffer object - communicate vertices to GFX
  var vbo: Option[Int] = None

  // zoom factor for camera
  var zoom: Float = MAX_ZOOM
  var cameraXTranslate: Float = 0f
  var cameraYTranslate: Float = 0f

  def init(): Unit = {
    GL.createCapabilities()

    vao = Some(glGenVertexArrays())
    vao.foreach(glBindVertexArray)

    // set up a Vertex Buffer Object with a triangle
    // NOTE: for reasons unclear to me, moving this to later in the init routine causes access violations
    cleanly(MemoryStack.stackPush())(stack => {
//      val vertices: FloatBuffer = stack.mallocFloat(3 * 6)
//      vertices.put(0f).put(0f).put(0f).put(1f).put(0f).put(0f)
//      vertices.put(textureSize).put(0f).put(0f).put(0f).put(1f).put(0f)
//      vertices.put(0f).put(textureSize).put(0f).put(0f).put(0f).put(1f)
//      vertices.flip()

      val vertices: FloatBuffer = stack.mallocFloat(12 * 6)
      // top left
      vertices.put(0f).put(0f).put(0f).put(0f).put(0f).put(1f)
      vertices.put(textureSize).put(0f).put(0f).put(0f).put(1f).put(0f)
      vertices.put(0f).put(textureSize).put(0f).put(1f).put(0f).put(0f)

      // bottom right
      vertices.put(worldWidth * textureSize).put(worldHeight * textureSize).put(0f).put(1f).put(0f).put(0f)
      vertices.put((worldWidth - 1) * textureSize).put(worldHeight * textureSize).put(0f).put(0f).put(1f).put(0f)
      vertices.put(worldWidth * textureSize).put((worldHeight - 1) * textureSize).put(0f).put(0f).put(0f).put(1f)

      // bottom left
      vertices.put(0).put(worldHeight * textureSize).put(0f).put(1f).put(0f).put(0f)
      vertices.put(0).put((worldHeight - 1) * textureSize).put(0f).put(0f).put(1f).put(0f)
      vertices.put(textureSize).put(worldHeight * textureSize).put(0f).put(0f).put(0f).put(1f)

      // top right
      vertices.put(worldWidth * textureSize).put(0f).put(0f).put(1f).put(0f).put(0f)
      vertices.put(worldWidth * textureSize).put(textureSize).put(0f).put(0f).put(1f).put(0f)
      vertices.put((worldWidth - 1) * textureSize).put(0f).put(0f).put(0f).put(0f).put(1f)

      vertices.flip()


      vbo = Some(glGenBuffers())
      vbo.foreach(glBindBuffer(GL_ARRAY_BUFFER, _))
      glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW)
    })

    // create vertex shader, load up the source for it, and compile
    val vertexShaderHandle: Int = glCreateShader(GL_VERTEX_SHADER)
    val vertexShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream("/basic.vert")
    ).mkString
    glShaderSource(vertexShaderHandle, vertexShaderSource)
    glCompileShader(vertexShaderHandle)

    // create fragment shader, load up the source for it, and compile
    val fragmentShaderHandle: Int = glCreateShader(GL_FRAGMENT_SHADER)
    val fragmentShaderSource = io.Source.fromInputStream(
      getClass.getResourceAsStream("/basic.frag")
    ).mkString
    glShaderSource(fragmentShaderHandle, fragmentShaderSource)
    glCompileShader(fragmentShaderHandle)

    // check compile status of the shaders
    cleanly(MemoryStack.stackPush())(stack => {
      val vertexCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(vertexShaderHandle, GL_COMPILE_STATUS, vertexCompiled)

      val fragmentCompiled: IntBuffer = stack.mallocInt(1)
      glGetShaderiv(fragmentShaderHandle, GL_COMPILE_STATUS, fragmentCompiled)

      if((vertexCompiled.get(0) <= 0) || (fragmentCompiled.get(0) <= 0)) {
        throw new Exception("error compiling shaders!")
      }
    })

    // create shader program and attach the shaders we created
    programHandle = Some(glCreateProgram())
    programHandle.foreach(glAttachShader(_, vertexShaderHandle))
    programHandle.foreach(glAttachShader(_, fragmentShaderHandle))

    // link the program
    programHandle.foreach(glLinkProgram)
    cleanly(MemoryStack.stackPush())(stack => {
      val linkStatus: IntBuffer = stack.mallocInt(1)
      programHandle.foreach(glGetProgramiv(_, GL_LINK_STATUS, linkStatus))

      if(linkStatus.get(0) <= 0) {
        throw new Exception("error linking shader program")
      }
    })

    glDeleteShader(vertexShaderHandle)
    glDeleteShader(fragmentShaderHandle)
    programHandle.foreach(glUseProgram)

    // tell shader program where to bind the shader attributes to the buffer data we're going to pass in
    val posAttrib = programHandle.map(glGetAttribLocation(_, "position"))
    posAttrib.foreach(pos => {
      glEnableVertexAttribArray(pos)
      glVertexAttribPointer(pos, 3, GL_FLOAT, false, 6 * java.lang.Float.BYTES, 0)
    })

    val colAttrib = programHandle.map(glGetAttribLocation(_, "color"))
    colAttrib.foreach(col => {
      glEnableVertexAttribArray(col)
      glVertexAttribPointer(col, 3, GL_FLOAT, false, 6 * java.lang.Float.BYTES, 3 * java.lang.Float.BYTES)
    })

    // set model-to-world transform matrix - identity for now
    val uniModelAttrib = programHandle.map(glGetUniformLocation(_, "model"))
    val model = new Matrix4f()
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      model.get(buf)
      uniModelAttrib.foreach(glUniformMatrix4fv(_, false, buf))
    })

    setCamera()
    updateScreenSize()
  }

  def updateScreenSize(): Unit = {
    // get screen height / width for ortho projection
    var width: Float = 0f
    var height: Float = 0f
    cleanly(MemoryStack.stackPush())(stack => {
      val wh = glfwGetCurrentContext()
      val w: IntBuffer = stack.mallocInt(1)
      val h: IntBuffer = stack.mallocInt(1)
      glfwGetFramebufferSize(wh, w, h)
      width = w.get()
      height = h.get()
    })

    // set camera-to-projection transform - ortho
    val uniProjection = programHandle.map(glGetUniformLocation(_, "projection"))
    val projection = new Matrix4f()
    projection.ortho2D(0, width, height, 0)
    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      projection.get(buf)
      uniProjection.foreach(glUniformMatrix4fv(_, false, buf))
    })

    // set window viewport so ortho projection lines up
    glViewport(0, 0, width.toInt, height.toInt)

    // after all the ortho tweaks, we've got a 2d coordinate system where 0,0 is top left of the screen,
    // +x = right, +y = down,
    // and world coords map 1-to-1 to screen coords
  }

  // returns current x translate, y translate, and zoom for camera
  def getCamera(): (Float, Float, Float) = (cameraXTranslate, cameraYTranslate, zoom)

  def setCamera(xTranslate: Float = 0, yTranslate: Float = 0, _zoom: Float = 2f): Unit = {
    val maxZ = MAX_ZOOM
    val minZ = Main.SCREEN_WIDTH.toFloat / (textureSize * worldWidth)
    zoom = math.max(minZ, math.min(_zoom, maxZ))

    // limit translation to not move beyond the bounds of the world space
    val minX = 0f
    val minY = 0f

    val screenScale = (zoom / minZ)

    // the idea here is to find how many world pixels one screen takes up - then you can move
    // that many screens in world coordinates (minus 1 for the screen starting at the origin
    val oneScreenX = worldWidth * textureSize / screenScale
    val maxX = (screenScale - 1) * oneScreenX * -1
    val oneScreenY = worldHeight * textureSize / screenScale
    val maxY = (screenScale - 1) * oneScreenY * -1

    cameraXTranslate = math.min(0f, math.max(maxX, xTranslate))
    cameraYTranslate = math.min(0f, math.max(maxY, yTranslate))
  }

  def render(): Unit = {
    // clear the screen for the new render
    glClear(GL_COLOR_BUFFER_BIT)
    glClearColor(0f, 0f, 0f, 1f) // black background

    // set world-to-camera transform - also identity
    val uniView = programHandle.map(glGetUniformLocation(_, "view"))
    val viewMatrix = new Matrix4f
    viewMatrix.scale(zoom, zoom, 1f) // zoom
    viewMatrix.translate(cameraXTranslate, cameraYTranslate, 0f) // translate

    cleanly(MemoryStack.stackPush())(stack => {
      val buf: FloatBuffer = stack.mallocFloat(4 * 4)
      viewMatrix.get(buf)
      uniView.foreach(glUniformMatrix4fv(_, false, buf))
    })

    // render anything
    (vao zip programHandle).foreach(p2 => {
      val (v, p) = p2
      glBindVertexArray(v)
      glUseProgram(p)
      glDrawArrays(GL_TRIANGLES, 0, 12)
    })
  }

  def cleanup(): Unit = {
    vbo.foreach(glDeleteBuffers)
    vao.foreach(glDeleteVertexArrays)
    programHandle.foreach(glDeleteProgram)
  }
}

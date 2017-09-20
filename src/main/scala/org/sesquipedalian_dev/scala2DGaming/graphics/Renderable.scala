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

import org.lwjgl.opengl.GL11._
import org.sesquipedalian_dev.scala2DGaming.util.Logging

import scala.collection.mutable.ListBuffer

trait Renderable {
  def render(): Unit
  def cleanup(): Unit

  def register(): Unit = {
    Renderable.all += this
  }
}

object Renderable extends Logging {
  val all: ListBuffer[Renderable] = ListBuffer()
  def render(): Unit = {
    // clear the screen for the new render
    glClear(GL_COLOR_BUFFER_BIT)
    glClearColor(0f, 0f, 0f, 1f) // black background

    all.foreach(r => {
      trace"rendering: $r"
      r.render()
      checkError(r, 2)
    })
  }
  def cleanup(): Unit = all.foreach(r => {r.cleanup(); checkError(r, 3)})

  def checkError(self: Renderable, step: Int): Unit = {
    val glErrorEnum = glGetError()
    if(glErrorEnum != GL_NO_ERROR) {
      error"renderer got error $self $glErrorEnum $step"
    }
  }
}
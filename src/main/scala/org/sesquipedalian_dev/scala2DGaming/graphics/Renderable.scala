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

import scala.collection.mutable.ListBuffer
import org.lwjgl.opengl.GL11._

trait Renderable {
  var programHandle: Option[Int] = None
  def init(): Unit
  def render(): Unit
  def cleanup(): Unit

  Renderable.all += this
}

object Renderable {
  val all: ListBuffer[Renderable] = ListBuffer()
  def setProgram(progHandle: Int): Unit = all.foreach(_.programHandle = Some(progHandle))
  def init(): Unit = all.foreach(r => {r.init(); checkError(r, 1)})
  def render(): Unit = all.foreach(r => {r.render(); checkError(r, 2)})
  def cleanup(): Unit = all.foreach(r => {r.cleanup(); checkError(r, 3)})

  def checkError(self: Renderable, step: Int): Unit = {
    val glErrorEnum = glGetError()
    if(glErrorEnum != GL_NO_ERROR) {
      println(s"renderer got error $self $glErrorEnum $step")
    }
  }
}
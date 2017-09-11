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
package org.sesquipedalian_dev.scala2DGaming.input
import org.lwjgl.glfw.GLFW._

class LoggingMouseCursorHandler extends MouseInputHandler {
  override def handleMove(windowHandle: Long, xPos: Double, yPos: Double, lbState: Int, rbState: Int): Boolean = { /* true if consumed */
    super.handleMove(windowHandle, xPos, yPos, lbState, rbState)
//    println(s"got mouse cursor event $xPos $yPos $lbState $rbState")
    false
  }

  override def handleAction(windowHandle: Long, button: Int, action: Int) = {
//    println(s"got mouse action $button $action $currentX $currentY")
    false
  }
}

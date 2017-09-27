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
package org.sesquipedalian_dev.util

import javafx.scene.layout.Region

class JavaFXExtensions {
  implicit class HasHeightWidthExtension(obj: Region) {
    def forceWidth(width: Double): Unit = {
      obj.setPrefWidth(width)
      obj.setMinWidth(Region.USE_PREF_SIZE)
      obj.setMaxWidth(Region.USE_PREF_SIZE)
    }

    def forceHeight(height: Double): Unit = {
      obj.setPrefHeight(height)
      obj.setMinHeight(Region.USE_PREF_SIZE)
      obj.setMaxHeight(Region.USE_PREF_SIZE)
    }

    def forceComputedWidth(): Unit = {
      obj.setPrefWidth(Region.USE_COMPUTED_SIZE)
      obj.setMinWidth(Region.USE_COMPUTED_SIZE)
      obj.setMaxWidth(Region.USE_COMPUTED_SIZE)
    }

    def forceComputedHeight(): Unit = {
      obj.setPrefHeight(Region.USE_COMPUTED_SIZE)
      obj.setMinHeight(Region.USE_COMPUTED_SIZE)
      obj.setMaxHeight(Region.USE_COMPUTED_SIZE)
    }
  }
}

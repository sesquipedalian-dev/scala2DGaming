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
package org.sesquipedalian_dev.scala2DGaming.entities.enemies

import org.sesquipedalian_dev.scala2DGaming.Main
import org.sesquipedalian_dev.scala2DGaming.Main.{WORLD_HEIGHT, WORLD_WIDTH}
import org.sesquipedalian_dev.scala2DGaming.entities.Location
import org.sesquipedalian_dev.scala2DGaming.game.{HasGameUpdate, TimeOfDay}

// thing to make bad guys on some timer
class BadGuySpawner(
  location: Location,
  secondsPerSpawn: Float
) extends HasGameUpdate {
  var spawnTimer = secondsPerSpawn * TimeOfDay.SLOW
  override def update(deltaTimeSeconds: Double): Unit = {
    spawnTimer -= deltaTimeSeconds.toFloat
    if(spawnTimer <= 0) {
      spawnTimer = secondsPerSpawn * TimeOfDay.SLOW
//       TESTING - after one spawn give up
//      spawnTimer = Double.MaxValue

      // test bad guy
      val targetY = Main.random.map(r => r.nextInt(Main.WORLD_HEIGHT)).getOrElse(25)
      new BadGuy(location, Some(Location(Main.WORLD_WIDTH - 1, targetY)), Location(WORLD_WIDTH, WORLD_HEIGHT), 50)
    }
  }
}

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
package org.sesquipedalian_dev.scala2DGaming.entities.soldiers

import org.sesquipedalian_dev.scala2DGaming.game.TimeOfDay

// manage groups' activites -
// each GoodGuyGroup has a Schedule.  The Schedule says what Activity that unit should be doing
// at what times of the day.  The schedules are mapped out in 24 hour increments
class Schedule {
  var activities: List[Activities.Type] =
    Activities.GUARD +: // 00:00 - 01:00 finish guarding for the night
    (
      List.fill(8)(Activities.SLEEP) ++ // 01:00 - 09:00 sleep for 8 hours
      List.fill(15)(Activities.GUARD) // 09:00 - 0:00 - Guard all day
    )

  def set(hour: Int, activity: Activities.Type): Unit = {
    activities = (activities.take(hour) :+ activity) ++ activities.drop(hour + 1)
  }

  def get(hour: Int): Activities.Type = {
    if(0 <= hour && hour < 24) {
      activities(hour)
    } else {
      Activities.GUARD
    }
  }

  def get(): Activities.Type = TimeOfDay.singleton.map(tod => {
    val currentHour = Math.floor(tod.currentTimeOfDay / 60 / 60).toInt
    get(currentHour)
  }).getOrElse(Activities.GUARD)
}

object Activities {
  // TODO some activities should not be user selectable (like IDLE)
  type Type = String
  val SLEEP = "Sleep"
  val GUARD = "Guard"
  val IDLE = "Idle"

  def apply(): List[String] = List(
    SLEEP,
    GUARD,
    IDLE
  )

  // get 'color string
  def apply(activityName: String): String = activityName match {
    case SLEEP => "darkgoldenrod"
    case GUARD => "red"
    case IDLE => "purple"
    case _ => "grey"
  }
}

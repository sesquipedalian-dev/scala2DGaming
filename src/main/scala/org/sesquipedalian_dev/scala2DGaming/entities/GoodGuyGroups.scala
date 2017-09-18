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
package org.sesquipedalian_dev.scala2DGaming.entities

import javax.naming.Name

// manage the group deployments of the good guys
// groups share a schedule of activities
class GoodGuyGroups {
  var groups: Map[String, GoodGuyGroup] = Map()
  GoodGuyGroups.singleton = Some(this)
}

case class GoodGuyGroup(
  name: String,
  var guys: List[GoodGuy] = Nil
) {
  val schedule: Schedule = new Schedule()
}

object GoodGuyGroups {
  var singleton: Option[GoodGuyGroups] = None

  def groups: Map[String, GoodGuyGroup] = singleton.map(_.groups).getOrElse(Map())
  def groupForGuy(guy: GoodGuy): Option[GoodGuyGroup] = groups.find(p => p._2.guys.contains(guy)).map(_._2)
  def add(newGroup: String) = singleton.foreach(s => {
    if(!s.groups.contains(newGroup)) { // don't allow to add key that's already there
      s.groups = s.groups + (newGroup -> new GoodGuyGroup(newGroup))
    }
  })
  def tryRemove(removeGroup: String) = singleton.foreach(s => {
    val group = s.groups.get(removeGroup)
//    println(s"tryRemove entry $removeGroup $groups $group")
    group.foreach(g => {
      if(g.guys.isEmpty) {
        s.groups -= removeGroup
      }
    })
  })

  def moveToGroup(guyName: String, group: String) = {
//    println(s"moveToGroup entry $guyName, $group, ${singleton.map(_.groups)}")
    singleton.foreach(s => {
      val oldGuy = s.groups.toList.flatMap(g => g._2.guys.map(p => g._1 -> p)).find(g => g._2.name == guyName)
//      println(s"moveToGroup old guy!? $oldGuy")
      oldGuy.foreach {
        case (foundGroup, foundGuy) if foundGroup != group /* don't allow add and drop from same group */ => {
          s.groups.foreach({
            case (gName, groupObj) if gName == foundGroup => {
              groupObj.guys = groupObj.guys.filterNot(_ == foundGuy)
            }
            case (gName, groupObj) if gName == group => {
              groupObj.guys = groupObj.guys :+ foundGuy
            }
            case x => x
          })
        }
        case _ =>
      }
    })
  }
}
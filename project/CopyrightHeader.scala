/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, which was derived from Akka.
 */

/*
 * Copyright (C) 2018-2022 Lightbend Inc. <https://www.lightbend.com>
 */

import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import de.heikoseeberger.sbtheader.{ CommentCreator, CommentStyle, HeaderPlugin, NewLine }
import sbt.Keys._
import sbt._

trait CopyrightHeader extends AutoPlugin {

  override def requires: Plugins = HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  protected def headerMappingSettings: Seq[Def.Setting[_]] =
    Seq(Compile, Test).flatMap { config =>
      inConfig(config)(
        Seq(
          headerLicense := Some(HeaderLicense.Custom(apacheHeader)),
          headerMappings := headerMappings.value ++ Map(
            HeaderFileType.scala -> cStyleComment,
            HeaderFileType.java -> cStyleComment,
            HeaderFileType.conf -> hashLineComment,
            HeaderFileType("template") -> cStyleComment)))
    }

  override def projectSettings: Seq[Def.Setting[_]] = Def.settings(headerMappingSettings, additional)

  def additional: Seq[Def.Setting[_]] =
    Def.settings(Compile / compile := {
        (Compile / headerCreate).value
        (Compile / compile).value
      },
      Test / compile := {
        (Test / headerCreate).value
        (Test / compile).value
      })

  val apacheFromAkkaSourceHeader: String =
    """Licensed to the Apache Software Foundation (ASF) under one or more
      |license agreements; and to You under the Apache License, version 2.0:
      |
      |  https://www.apache.org/licenses/LICENSE-2.0
      |
      |This file is part of the Apache Pekko project, which was derived from Akka.
      |""".stripMargin

  val apacheHeader: String =
    """Licensed to the Apache Software Foundation (ASF) under one or more
      |contributor license agreements. See the NOTICE file distributed with
      |this work for additional information regarding copyright ownership.
      |The ASF licenses this file to You under the Apache License, Version 2.0
      |(the "License"); you may not use this file except in compliance with
      |the License. You may obtain a copy of the License at
      |
      |   http://www.apache.org/licenses/LICENSE-2.0
      |
      |Unless required by applicable law or agreed to in writing, software
      |distributed under the License is distributed on an "AS IS" BASIS,
      |WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      |See the License for the specific language governing permissions and
      |limitations under the License.
      |""".stripMargin

  val apacheSpdxHeader: String = "SPDX-License-Identifier: Apache-2.0"

  val cStyleComment: CommentStyle = HeaderCommentStyle.cStyleBlockComment.copy(commentCreator = new CommentCreator() {

    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(existedText) if isValidCopyrightAnnotated(existedText) =>
          existedText
        case Some(existedText) if isOnlyLightbendCopyrightAnnotated(existedText) =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(apacheFromAkkaSourceHeader,
            existingText) + NewLine * 2 + existedText
        case Some(existedText) =>
          throw new IllegalStateException(s"Unable to detect Copyright for header:[$existedText]")
        case None =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(text, existingText)
      }
      formatted.trim
    }
  })

  val hashLineComment = HeaderCommentStyle.hashLineComment.copy(commentCreator = new CommentCreator() {

    // deliberately hardcode use of apacheSpdxHeader and ignore input text
    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(currentText) if isApacheCopyrighted(currentText) =>
          currentText
        case Some(currentText) =>
          HeaderCommentStyle.hashLineComment.commentCreator(apacheSpdxHeader, existingText) + NewLine * 2 + currentText
        case None =>
          HeaderCommentStyle.hashLineComment.commentCreator(apacheSpdxHeader, existingText)
      }
      formatted.trim
    }
  })

  private def isApacheCopyrighted(text: String): Boolean =
    text.contains("Licensed to the Apache Software Foundation (ASF)") ||
    text.contains("www.apache.org/licenses/license-2.0") ||
    text.contains("Apache-2.0")

  private def isLightbendCopyrighted(text: String): Boolean =
    text.contains("Lightbend Inc.")

  private def isValidCopyrightAnnotated(text: String): Boolean = {
    isApacheCopyrighted(text)
  }

  private def isOnlyLightbendCopyrightAnnotated(text: String): Boolean = {
    isLightbendCopyrighted(text) && !isApacheCopyrighted(text)
  }
}

object CopyrightHeader extends CopyrightHeader

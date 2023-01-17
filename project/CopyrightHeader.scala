/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * license agreements; and to You under the Apache License, version 2.0:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is part of the Apache Pekko project, derived from Akka.
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

  def headerFor(year: String): String =
    s"Copyright (C) $year Lightbend Inc. <https://www.lightbend.com>"

  def apacheHeader: String =
    """Licensed to the Apache Software Foundation (ASF) under one or more
      |license agreements; and to You under the Apache License, version 2.0:
      |
      |  https://www.apache.org/licenses/LICENSE-2.0
      |
      |This file is part of the Apache Pekko project, derived from Akka.
      |""".stripMargin

  val cStyleComment: CommentStyle = HeaderCommentStyle.cStyleBlockComment.copy(commentCreator = new CommentCreator() {

    override def apply(text: String, existingText: Option[String]): String = {
      val formatted = existingText match {
        case Some(existedText) if isValidCopyRightAnnotated(existedText) =>
          existedText
        case Some(existedText) if isOnlyLightbendCopyRightAnnotated(existedText) =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(text, existingText) + NewLine * 2 + existedText
        case Some(existedText) =>
          throw new IllegalStateException(s"Unable to detect copyright for header:[${existedText}]")
        case None =>
          HeaderCommentStyle.cStyleBlockComment.commentCreator(text, existingText)
      }
      formatted.trim
    }

    private def isApacheCopyRighted(text: String): Boolean =
      text.contains("Licensed to the Apache Software Foundation (ASF)") ||
      text.contains("www.apache.org/licenses/license-2.0")

    private def isLightbendCopyRighted(text: String): Boolean =
      text.contains("Lightbend Inc.")

    private def isValidCopyRightAnnotated(text: String): Boolean = {
      isApacheCopyRighted(text)
    }

    private def isOnlyLightbendCopyRightAnnotated(text: String): Boolean = {
      isLightbendCopyRighted(text) && !isApacheCopyRighted(text)
    }
  })
}

object CopyrightHeader extends CopyrightHeader

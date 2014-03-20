package com.joypeg.graphdeps.neo4j

import com.joypeg.graphdeps.{Neo4jData, CurrentModule, GraphDependencyPlugin}
import sbt._
import Keys._
import scala.util.Try

/**
 * @author Diego Zambelli Sessona
 * @since 14/03/2014 23:48
 */
object Neo4jGraphDependencies extends GraphDependencyPlugin with Neo4jCyperScriptMaker {

  val neo4jInternalName = SettingKey[String]("neoDepInternalName", "Name of the company")
  val neo4jInternalOrgs = SettingKey[Seq[String]]("neoDepInternalOrgs", "Internal organizations")
  val neo4jTagsLabels   = SettingKey[Map[String,String]]("neoDepExtraLabels", "Extra labels for the organization")
  val neo4jCypherScript = SettingKey[File]("neo4jCypherScript", "The cypher script file generated")
  val neo4jLoadResults  = SettingKey[File]("neo4jLoadResults", "Result of loading the data in neo4j")

  val neo4jWriteDependencies = TaskKey[Unit]("neo4jWriteDependencies", "Write the cypher script file")
  val neo4jShowDependencies  = TaskKey[Unit]("neo4jShowDependencies", "Show all nodes and relations in console")
  val neo4jLoadDependencies  = TaskKey[Unit]("neo4jLoadDependencies", "Write the cypher script file and load data")

  neo4jCypherScript := (baseDirectory in Compile).value / "neodependencies" / "nodesandrelations.cyp"
  neo4jLoadResults  := (baseDirectory in Compile).value / "neodependencies" / "loadresults.txt"

  lazy val neo4jDepsSetting = Seq(
    neo4jInternalName := "Company",
    neo4jInternalOrgs := Seq.empty,
    neo4jTagsLabels   := Map.empty,
    neo4jLoadDependencies  <<= loadNodesAndRelations.dependsOn(neo4jWriteDependencies),
    neo4jShowDependencies  <<= showNodesAndRelations,
    neo4jWriteDependencies <<= writeNodesAndRelations
  )

  private[this] def loadNodesAndRelations = (neo4jCypherScript, neo4jLoadResults, streams).map {
    (cypher, results, s) => {
      logTaskHeader("neo4jLoadDependencies", s)
      Try { getSysProperty("neo4j.home") }.map{
        case None =>
          s.log.error("Cannot find neo4j.home in your system settings")
        case Some(neosh) =>
          val exitCdode = s"$neosh/bin/neo4j-shell -file ${cypher.getAbsolutePath} > ${results.getAbsolutePath}".!
          s.log.info("neo4j-shell process terminated with Exit code: " + exitCdode)
      }.recover {
        case ex => s.log.error(ex.getMessage)
      }.get
    }
  }

  private[this] def writeNodesAndRelations = (libraryDependencies, name, organization, scalaBinaryVersion,
                                              neo4jInternalName, neo4jInternalOrgs, neo4jTagsLabels,
                                              streams, neo4jCypherScript, neo4jLoadResults).map {
    (modules, name, org, scalaBinVer, neoName, neoOrg, neoTags, s, script, results) =>
      logTaskHeader("neo4jWriteDependencies", s)
      createAndWriteCyperScript(
        modules, CurrentModule(name, org, scalaBinVer),
        Neo4jData(neoName, neoOrg, neoTags), script
      ).map { res =>
        s.log.info(s"Cypher script wrote in: ${script.getAbsolutePath}")
      }.recover{
        case ex => s.log.error(ex.getMessage)
      }.get
  }

  private[this] def showNodesAndRelations = (libraryDependencies, name, organization, scalaBinaryVersion,
                                             neo4jInternalName, neo4jInternalOrgs, neo4jTagsLabels, streams).map {
    (modules, name, org, scalaBinVer, neoName, neoOrg, neoTags, s) => {
      logTaskHeader("neo4jShowDependencies", s)
      getScriptLines(modules, CurrentModule(name, org, scalaBinVer), Neo4jData(neoName, neoOrg, neoTags))
        .foreach(s.log.info(_))
    }
  }

}

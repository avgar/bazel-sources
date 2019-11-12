package com.wixpress.build.sources

import com.wix.bazel.e2e.{Workspace, WorkspaceMatchers}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.io.Source

class WixSourceIT extends SpecificationWithJUnit with WorkspaceMatchers {

  "Wix Source" should {
    "produce .source file" >> {
      "when source files exist" in new ctx {
        workspaceA.addFile("sourceA.java")
        workspaceA.addFile("sourceB.java")
        workspaceA.addFile("1.java")
        workspaceA.addFile("2.java")
        workspaceA.addFile("3.java")
        workspaceA.addFile("4.java")
        workspaceA.addFile("5.java")

        workspaceB.addFile("6.java")
        workspaceB.addBuildFile(
          """
            |package(default_visibility = ["//visibility:public"])
            |java_library(
            |  name = "dep3",
            |  srcs = ["6.java"],
            |
            |  deps = []
            |)""".stripMargin)


        workspaceA.addBuildFile(
          s"""load(":wix_sources.bzl", "source_files")
            |
            |java_library(
            |  name = "dep1",
            |  srcs = ["sourceA.java",
            |          "1.java",
            |          "2.java",
            |          "sourceB.java"])
            |
            |java_library(
            |  name = "dep2",
            |  srcs = ["sourceA.java",
            |          "2.java",
            |          "sourceB.java"])
            |
            |java_library(
            |  name = "sources1",
            |  srcs = ["sourceA.java",
            |          "2.java",
            |          "sourceB.java"],
            |
            |  deps = ["//:dep1", "//:dep2", "@${externalRepo}//:dep3"]
            |)
            |source_files(
            |    name = "sources_file1",
            |    artifact_id = "artifact_id",
            |    group_id = "group_id",
            |    main_artifact_name = ":sources1",
            |)
            |
            |java_library(
            |  name = "sources2",
            |  srcs = ["sourceB.java",
            |          "sourceA.java",
            |          "1.java",
            |          "2.java"],
            |
            |  deps = ["//:dep2", "//:dep1", "@${externalRepo}//:dep3"]
            |)
            |
            |source_files(
            |    name = "sources_file2",
            |    artifact_id = "artifact_id2",
            |    group_id = "group_id",
            |    main_artifact_name = ":sources2",
            |)""".stripMargin)


        workspaceA.build("//:sources_file1")
        workspaceA.build("//:sources_file2")

        val source1Lines = lines(s"${workspaceA.currentWorkspace}/bazel-bin/sources_file1.sources.txt")
        val source2Lines = lines(s"${workspaceA.currentWorkspace}/bazel-bin/sources_file2.sources.txt")

        source2Lines must be_===(source1Lines)
      }
    }

    "list external repo files" >> {
      "when source files exist" in new ctx {

        workspaceB.addFile("6.java")
        workspaceB.addBuildFile(
          """
            |package(default_visibility = ["//visibility:public"])
            |java_library(
            |  name = "dep3",
            |  srcs = ["6.java"],
            |
            |  deps = []
            |)""".stripMargin)

        workspaceA.addBuildFile(
          s"""load(":wix_sources.bzl", "source_files")
            |
            |java_library(
            |  name = "sources1",
            |  srcs = [],
            |  runtime_deps = ["@${externalRepo}//:dep3"]
            |)
            |source_files(
            |    name = "sources_file1",
            |    artifact_id = "artifact_id",
            |    group_id = "group_id",
            |    main_artifact_name = ":sources1",
            |)
            """.stripMargin)

        workspaceA.build("//:sources_file1")

        val source1Lines = lines(s"${workspaceA.currentWorkspace}/bazel-bin/sources_file1.sources.txt")

        source1Lines must containAllOf(Seq(
          s"external/$externalRepo/BUILD.bazel",
          s"external/$externalRepo/6.java"
        ))
      }
    }
  }

  def lines(file: String) = Source.fromFile(file).getLines.drop(2) toList


  trait ctx extends Scope {
    val externalRepo = "repoB"
    val workspaceB = aWorkspace()
    val workspaceA = aWorkspace(Option((externalRepo, workspaceB.currentWorkspace().toString)))

    def aWorkspace(localRepo: Option[(String, String)] = None): Workspace = {
      val workspace = Workspace.newRemoteCompatibleWorkspace(None, localRepo).withScala()
      workspace.scratchFile(s"wix_sources/src/main/BUILD.bazel", "")
      workspace.copyFromRunfiles(
        "bazel_tooling/wix_sources/src/main/wix_sources_aspect.bzl", "wix_sources/src/main/wix_sources_aspect.bzl")
      workspace.copyFromRunfiles(
        "bazel_tooling/wix_sources/src/main/wix_sources.bzl", "wix_sources.bzl")
      workspace
    }

  }
}
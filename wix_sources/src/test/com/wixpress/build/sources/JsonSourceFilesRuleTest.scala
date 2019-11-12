package test.com.wixpress.build.sources

import java.nio.file.Paths

import com.wixpress.hoopoe.json.JsonMapper
import org.specs2.mutable.SpecificationWithJUnit

import scala.io.Source

class JsonSourceFilesRuleTest extends SpecificationWithJUnit {

  val basePath = "wix_sources/src/test/com/wixpress/build/sources/setup"

  "JsonSourceFilesRuleTest" should {

    "create a source file with expected direct sources" in {
      val name = "direct_sources"
      val expectedSources = List(s"$basePath/direct_sources/NonDependantClass.scala")
      val expectedBuildFiles = List( s"$basePath/direct_sources/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))
    }

    "create a source file with expected dep sources" in {
      val name = "dep_sources"
      val expectedSources = List(s"$basePath/$name/Source.scala")
      val expectedBuildFiles = List( s"$basePath/$name/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))

    }

    "create an empty source file with for third party dep sources" in { //also test self package
      val name = "third_party_dep"
      val expectedSources = List()
      val expectedBuildFiles = List( s"$basePath/$name/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))

    }

    "create multi line result " in {
      val name = "multi_line_result"
      val expectedSources = List(s"$basePath/$name/SourceA.scala", s"$basePath/$name/SourceB.scala")
      val expectedBuildFiles = List( s"$basePath/$name/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))

    }

    "create a source file with expected runtime dep sources" in {
      val name = "runtime_dep"
      val expectedSources = List(s"$basePath/$name/RSource.scala")
      val expectedBuildFiles = List( s"$basePath/$name/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))

    }


    "create an empty source json file for target no_sources" in {
      val name = "no_sources"

      val expectedBuildFiles = List(s"$basePath/$name/BUILD.bazel")
      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(List.empty), buildFiles = Option(expectedBuildFiles)))
    }

    "create a source file with expected json resources sources" in {
      val name = "resources_src"
      val expectedSources = List(s"$basePath/$name/config.erb")
      val expectedBuildFiles = List(s"$basePath/$name/BUILD.bazel")

      jsonSourcesFileContent(name) must beEqualTo(sources(files = Option(expectedSources), buildFiles = Option(expectedBuildFiles)))

    }
  }

  private def sources(groupId: String = "group_id", artifactId: String = "artifact_id", files: Option[List[String]], buildFiles: Option[List[String]])
   = Sources(groupId, artifactId,  files, buildFiles)

  private def jsonSourcesFile(target: String) =
    Paths.get(System.getenv("TEST_SRCDIR"))

      .resolve("bazel_tooling")
      .resolve("wix_sources")
      .resolve("src")
      .resolve("test")
      .resolve("com/wixpress/build/sources/setup")
      .resolve(target)
      .resolve(s"$target.sources.json").toFile


  val mapper = JsonMapper.global

  private def jsonSourcesFileContent(fileName: String): Sources = {

    val json = Source.fromFile(jsonSourcesFile(fileName)).mkString
    mapper.readValue(json, classOf[Sources])

  }

}
case class Sources(groupId: String, artifactId: String, sources: Option[List[String]], buildFiles: Option[List[String]])


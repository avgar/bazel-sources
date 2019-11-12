package test.com.wixpress.build.sources

import java.nio.file.Paths

import org.specs2.mutable.SpecificationWithJUnit

import scala.io.Source
/*
*  THIS FILE WILL BE DELETED ONCE PUSHTAK WILL USE JSON SOURCES
*/

class SourceFilesRuleTest extends SpecificationWithJUnit {

  val basePath = "wix_sources/src/test/com/wixpress/build/sources/setup"

  "SourcesFilesRule" should {
    "create an empty source file for target no_sources" in {
      val name = "no_sources"

      sourcesFile(name).exists() must beTrue
      sourcesFileContent(name) must containTheSameElementsAs(Seq("group_id", "artifact_id", s"$basePath/$name/BUILD.bazel"))
    }


    "create a source file with expected direct sources" in {
      val name = "direct_sources"
      val expectedSources = List(s"$basePath/direct_sources/NonDependantClass.scala")
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

    "create a source file with expected dep sources" in {
      val name = "dep_sources"
      val expectedSources = List(s"$basePath/$name/Source.scala", s"$basePath/$name/BUILD.bazel")
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

    "create an empty source file with for third party dep sources" in {
      val name = "third_party_dep"
      val expectedSources = List()
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

    "create multi line result " in {
      val name = "multi_line_result"
      val expectedSources = List(s"$basePath/$name/SourceA.scala", s"$basePath/$name/SourceB.scala", s"$basePath/$name/BUILD.bazel")
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

    "create a source file with expected runtime dep sources" in {
      val name = "runtime_dep"
      val expectedSources = List(s"$basePath/$name/RSource.scala", s"$basePath/$name/BUILD.bazel")
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

    "create a source file with expected resources sources" in {
      val name = "resources_src"
      val expectedSources = List(s"$basePath/$name/config.erb", s"$basePath/$name/BUILD.bazel")
      val actualSources = sourcesFileContent(name)

      actualSources must containAllOf(expectedSources)
    }

  }

  private def sourcesFile(target: String) =
    Paths.get(System.getenv("TEST_SRCDIR"))

    .resolve("bazel_tooling")
    .resolve("wix_sources")
    .resolve("src")
    .resolve("test")
    .resolve("com/wixpress/build/sources/setup")
    .resolve(target)
    .resolve(s"$target.sources.txt").toFile

  private def sourcesFileContent(fileName: String) = {

    Source.fromFile(sourcesFile(fileName)).getLines toList
  }

}


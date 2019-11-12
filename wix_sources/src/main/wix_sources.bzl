load("//wix_sources/src/main:wix_sources_aspect.bzl", "collect_source_files_aspect", "SourceFiles")

def _owner_to_bazel_file(fileLabel):
    workspace = fileLabel.workspace_root
    package = fileLabel.package
    if 0 < len(workspace):
      workspace = workspace + '/'
    if 0 < len(package):
     package = package + '/'
    return workspace + package + 'BUILD.bazel'


def _collect_source_files_rule_impl(ctx):
    metadata = [ctx.attr.group_id, ctx.attr.artifact_id]
    paths =  sorted([f.path for f in ctx.attr.main_artifact_name[SourceFiles].transitive_source_files.to_list()])
    owners =  sorted(depset([_owner_to_bazel_file(f.owner) for f in ctx.attr.main_artifact_name[SourceFiles].transitive_source_files.to_list()] + [_owner_to_bazel_file(ctx.label)]).to_list())

    ctx.actions.write(ctx.outputs.sources, "\n".join(metadata + paths + owners))
    ctx.actions.write(ctx.outputs.source_files, "{\"groupId\": \"%s\", \"artifactId\": \"%s\", \"sources\": %s, \"buildFiles\": %s}" % (ctx.attr.group_id, ctx.attr.artifact_id, paths, owners))
    return DefaultInfo(
          runfiles=ctx.runfiles(files=[ctx.outputs.sources, ctx.outputs.source_files])
      )

source_files = rule(
    implementation = _collect_source_files_rule_impl,
    attrs = {
        'main_artifact_name' : attr.label(aspects = [collect_source_files_aspect, ]),
        "group_id" : attr.string(mandatory=True),
        "artifact_id" : attr.string(mandatory=True),
    },
    outputs={"sources": "%{name}.sources.txt", "source_files": "%{name}.sources.json"},
)



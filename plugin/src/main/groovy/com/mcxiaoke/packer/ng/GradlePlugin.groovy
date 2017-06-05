package com.mcxiaoke.packer.ng

import com.android.build.gradle.api.BaseVariant
import com.android.builder.Version
import org.gradle.api.Plugin
import org.gradle.api.Project

// Android PackerNg Plugin Source
class GradlePlugin implements Plugin<Project> {
    static final String TAG = "PackerNg"
    static final String PLUGIN_NAME = "packer"

    Project project

    @Override
    void apply(Project project) {
        this.project = project
        if (!project.plugins.hasPlugin("com.android.application")) {
            throw new PluginException(
                    "'com.android.application' plugin must be applied", null)
        }
        if (new StringVersion(Version.ANDROID_GRADLE_PLUGIN_VERSION)
                < new StringVersion("2.2.0")) {
            throw new PluginException(
                    "'com.android.tools.build:gradle' must be v2.2.0 or above", null)
        }
        project.configurations.create(PLUGIN_NAME).extendsFrom(project.configurations.compile)
        project.extensions.create(PLUGIN_NAME, GradleExtension)
        project.afterEvaluate {
            project.android.applicationVariants.all { BaseVariant variant ->
                addTasks(variant)
            }
        }
    }

    static boolean isV2SigningEnabled(BaseVariant vt) {
        boolean e1 = false
        boolean e2 = false
        def s1 = vt.buildType.signingConfig
        if (s1 && s1.signingReady) {
            e1 = s1.v2SigningEnabled
        }
        def s2 = vt.mergedFlavor.signingConfig
        if (s2 && s2.signingReady) {
            e2 = s2.v2SigningEnabled
        }
        return e1 || e2
    }

    void addTasks(BaseVariant vt) {
        debug("addTasks() for ${vt.name}")
        def variantTask = project.task("generate${vt.name.capitalize()}Channels",
                type: GradleTask) {
            variant = vt
            extension = project.packer
            dependsOn vt.assemble
        }

        debug("addTasks() new variant task:${variantTask.name}")

        def buildTypeName = vt.buildType.name
        if (vt.name != buildTypeName) {
            def taskName = "generate${buildTypeName.capitalize()}Channels"
            def task = project.tasks.findByName(taskName)
            if (task == null) {
                task = project.task(taskName)
            }
            task.dependsOn(variantTask)
            debug("addTasks() build type task ${taskName}")
        }

    }

    void debug(String msg) {
        project.logger.info(msg)
    }

}

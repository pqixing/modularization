package com.pqixing.modularization.plugins

import com.pqixing.modularization.Default
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.tasks.UpdateDetail
import com.pqixing.modularization.tasks.UpdateLog
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-20.
 */

class Document extends BasePlugin {
    @Override
    void apply(Project project) {
        super.apply(project)
        println("name Document : ${NormalUtils.getBranchName(project)}  lastCommit : ${NormalUtils.getLastCommit(project)}")

        def updateDoc = project.task("updateDoc", type: UpdateLog) {
            compileGroup = Default.groupName
            envs = ["test": Default.maven_url_test, "release": Default.maven_url_release,"debug":Default.maven_url_debug]
        }
        UpdateLog.findModules(project.file("readme")).each {
            project.task("log-$it", type: UpdateDetail) {
                compileGroup = Default.groupName
                envs = ["test": Default.maven_url_test, "release": Default.maven_url_release,"debug":Default.maven_url_debug]
                moduleName = "router"
                doLast {
                    updateDoc.execute()
                }
            }
        }
    }

    @Override
    Set<String> getIgnoreFields() {
        return ["updatelog"]
    }
}

package com.pqixing.modularization.git


import com.pqixing.modularization.utils.GitUtils

/**
 * Created by pqixing on 17-12-20.
 * 同步文档的任务
 */

class CloneAllTask extends GitTask {

    @Override
    String onGitProject(String gitName, String gitUrl, File gitDir) {
        if (gitDir.exists()) return "already exists"
        String result = com.pqixing.modularization.gradle.utils.GitUtils.run("git clone -b ${branchName} $gitUrl", gitDir.parentFile)
        if(result.startsWith("fatal:")) {
            result = com.pqixing.modularization.gradle.utils.GitUtils.run("git clone $gitUrl", gitDir.parentFile)
        }
        return result
    }
}

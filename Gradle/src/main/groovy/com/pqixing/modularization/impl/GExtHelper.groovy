package com.pqixing.modularization.impl


import com.pqixing.modularization.iterface.IExtHelper
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.jetbrains.annotations.NotNull

public class GExtHelper implements IExtHelper {

    public Object getExtValue(Project project, String key) {
        try {
            return project.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Project project, String key, String value) {
        try {
            project.ext."$key" = value
        } catch (Exception e) {
        }
    }

    public Object getExtValue(Gradle gradle, String key) {
        try {
            return gradle.ext."$key"
        } catch (Exception e) {
        }
        return null
    }

    @Override
    Object setExtValue(Gradle gradle, String key, String value) {
        try {
            gradle.ext."$key" = value
        } catch (Exception e) {
        }
    }

    @Override
    void setExtMethod(Project project, String method, Action action) {
        project.ext."$method" = { action?.execute(it) }
    }

    @Override
    void addRepositories(Project project, @NotNull List<String> dependMaven) {
        project.repositories {
            dependMaven.each { l ->
                maven { url l }
            }
        }
    }

    @Override
    void addSourceDir(Project project, String dir) {
        project.android.sourceSets.main.java.srcDirs += dir
    }

    @Override
    void setSourceDir(Project project, String dir) {
        project.android.sourceSets.main.java.srcDirs = [dir]
        project.android.sourceSets.main.res.srcDirs = ["res2"]
        project.android.sourceSets.main.jniLibs.srcDirs = ["jniLibs2"]
        project.android.sourceSets.main.assets.srcDirs = ["assets2"]
    }

    @Override
    void setMavenInfo(Project project, String maven_url, String userName, String password, String groupId, String artifactId, String version, String name) {
        def deployer = project.uploadArchives.repositories.mavenDeployer
        def pom = deployer.pom
        def repository = deployer.repository

        repository.url = maven_url
        repository.authentication.userName = userName
        repository.authentication.password = password
        pom.groupId = groupId
        pom.artifactId = artifactId
        pom.version = version
        pom.name = name
    }
}

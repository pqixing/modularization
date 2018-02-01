package com.pqixing.modularization.dependent

import com.pqixing.modularization.base.BaseExtension
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.configs.GlobalConfig
import org.gradle.api.Project
/**
 * Created by pqixing on 17-12-25.
 */

class Module extends BaseExtension {
    String moduleName
    String artifactId
    /**
     * 依赖模式
     * @see  runtimeOnly,compileOnly,implementation,compile
     */
    String scope = "runtimeOnly"

    String groupId = GlobalConfig.groupName
    String version = "+"
    /**
     * 最后更新时间
     */
    long updateTime
    /**
     * 更新说明
     */
    String gitLog
    /**强制使用当前版本
     */
    boolean focus
    LinkedList<Map<String, String>> excludes = new LinkedList<>()
    /**
     * 依赖中的依赖树
     */
    Set<Module> modules = new HashSet<>()

    void excludeGroup(String[] groups) {
        groups.each {
            excludes += ["group": it]
        }
    }

    void excludeModule(String[] modules) {
        modules.each {
            excludes += ["module": it]
        }
    }

    void exclude(Map<String, String> exclude) {
        excludes += exclude
    }

    String getUpdateTimeStr() {
        return new Date(updateTime).toLocaleString()
    }


    Module() {
        super(BasePlugin.rootProject)
    }

    @Override
    LinkedList<String> getOutFiles() {
        return null
    }
}
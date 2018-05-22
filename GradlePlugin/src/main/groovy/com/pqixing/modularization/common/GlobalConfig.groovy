package com.pqixing.modularization.common

import com.pqixing.modularization.Keys
import com.pqixing.modularization.base.BasePlugin
import com.pqixing.modularization.net.Net
import com.pqixing.modularization.utils.CheckUtils
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.TextUtils
import com.pqixing.modularization.wrapper.ProjectWrapper

/**
 * Created by pqixing on 17-12-7.
 * 全局配置，主要在gradle.propeties中的配置信息
 */
class GlobalConfig {
    /**
     * 是否打印日志
     */
    final
    static List<String> NOTE_silentLog = ["是否禁用日志", "true:禁用", "false:不禁用（日志仅在调试时使用，其他时间可以禁用）"]
    static boolean silentLog = true
    /**
     * 默认git操作目标
     * include，all
     */
    final
    static List<String> NOTE_target = ["git操作目标范畴", "include:只操作当前导入的代码", "all:影响本地所有存在的代码(仅包含配置在default.xml的工程)"]
    static String target = "all"
    /**
     * 分支名称
     */
    final static List<String> NOTE_branchName = ["git操作的分支名称", "branchName:分支名称,默认master"]
    static String branchName = "master"
    final static List<String> NOTE_excludeGit = ["需要不被控制的git名称"]
    static Set<String> excludeGit = []

    /**
     * 是否开启离线模式，如果开启了离线模式，网络请求默认全部都使用本地的。如果本地不存在缓存时，则会抛出异常
     */
    public static boolean offlineMode = false
    /**
     * 网络缓存5分钟
     */
    public static long netCacheTime = 1000 * 60 * 3
    /**
     * 是否在同步前，更新一遍版本号
     */
    public static boolean updateBeforeSync = true
    //集成默认的依赖库
    public static List<String> autoImpl = ["dcnet", "dccommon", "dcuser", "router", "mvpbase"]
    /**
     * 组别名称
     */
    public static String groupName = "com.dachen.android"

    /**
     * 依赖方式改变
     * localFirst,localOnly,mavenOnly,mavenFirst
     */
    final static List<String> NOTE_dependentModel = ["模块依赖方式，默认: mavenOnly"
                                                     , "mavenOnly:只使用maven仓库进行依赖，不关联本地代码"
                                                     , "mavenFirst:优先使用maven仓库进行依赖，当仓库不存在依赖时，关联本地代码依赖"
                                                     , "localFirst:优先使用本地导入模块代码，没有导入则使用maven仓库"
                                                     , "localOnly:只使用本地导入的模块，禁用ｍａｖｅｎ仓库"
    ]
    public static String dependentModel = "mavenOnly"

    /**
     * 上传所需的用户名
     */
    public static String mavenUser = "admin"
    public static String mavenPassword = "admin123"
    /**
     * 当依赖缺失时，是否拦截报错，默认为true
     */
    final
    static List<String> NOTE_abortDependentLose = ["是否拦截依赖缺少的异常,默认true", "true:当有依赖模块缺少时，抛出异常，方便查找问题", "false:不拦截错误，方便代码导入AS，但是缺少依赖构建过程出现的类缺失异常问题很难定位"]
    public static boolean abortDependentLose = true
    /**
     * 预设仓库地址
     */
    public static Map<String, String> preMavenUrl = [
            "release": "http://192.168.3.7:9527/nexus/content/repositories/android",
            "snap"   : "http://192.168.3.7:9527/nexus/content/repositories/androidsnap",
            "test"   : "http://192.168.3.7:9527/nexus/content/repositories/androidtest"]
    /**
     * 默认的内网镜像依赖组
     */
    static String mavenGroupUrl = "http://192.168.3.7:9527/nexus/content/groups/androidgroup/"
    /**
     * 文档管理仓库,支持git,自动下载，并且从中读取default.xml,default.gradle等文件，自动同步文档导该git中
     */
    static String docGitUrl = "http://pengqixing:pengqixing@192.168.3.200/android/Document.git"
    /**
     * 文档存放目录
     */
    static String docDirName = "docDir"
    /**
     * 是否读取git日志
     */
    static boolean gitLog = true
    /**
     * 指定的版本文件
     */
    static String focusVersions = ""
    /**
     * 分支名称
     */
    final static List<String> NOTE_focusVersions = ["指定的版本号文件"]

    /**
     * 初始化配置
     * @return
     */
    public static void init() {
        ProjectWrapper wrapper = ProjectWrapper.with(BasePlugin.rootProject)
        String remote = wrapper.get(Keys.REMOTE_CONFIG)
        if (!CheckUtils.isEmpty(remote)) {//有远程配置，优先使用
            if (remote.startsWith(Keys.PREFIX_NET)) updateConfig(Net.get(remote, true))
            else updateConfig(FileUtils.read(remote))
        }
        File configFile = new File(wrapper.project.rootDir, Keys.GLOBAL_CONFIG_NAME)
        if (configFile.exists()) {
            updateConfig(configFile.text)
        } else {
            writeGlobal("#", configFile)
        }
        File buildFile = new File(wrapper.project.rootDir, "$BuildConfig.dirName/$Keys.HIDE_CONFIG_NAME")
        if (buildFile.exists()) updateConfig(buildFile.text)
        updateConfigFromEnv()
        writeGlobal("", new File(BuildConfig.rootOutDir, Keys.GLOBAL_CONFIG_NAME))
    }

    public static void updateConfigFromEnv() {
        def shell = new GroovyShell()
        GlobalConfig.class.getDeclaredFields().each {
            if (it == null) return
            try {
                String p = TextUtils.getSystemEnv(it.getName())
                if (p == null) return
                it.setAccessible(true)
                if (it.type == String.class) {
                    it.set(null, p)
                } else {
                    it.set(null, shell.evaluate(p))
                }

            } catch (Exception e) {
            }
        }
    }
    /**
     * 输出模板Global文件
     * @param outFile
     */
    public static void writeGlobal(String preFix, File outFile) {
        StringBuilder sb = new StringBuilder("#$Keys.TAG_AUTO_ADD \n")

        def maps = GlobalConfig.staticProperties
        maps.findAll { it.key.startsWith("NOTE_") }.each { p ->
            p.value.each { sb.append("  #note:$it\n") }
            String realKey = p.key.replace("NOTE_", "")
            Object value = maps.find { it.key == realKey }?.value
            sb.append("$preFix$realKey = ${getValueStr(value)} \n\n")
        }
        FileUtils.write(outFile, sb.toString())
    }


    public static String getValueStr(Object value) {
        StringBuilder valueStr = new StringBuilder()
        if (value instanceof String) {
            valueStr.append("\"${value}\"")
        } else if (value instanceof Collection) {
            valueStr.append("[")
            value.each { s ->
                valueStr.append("\"${s}\",")
            }
            if (valueStr.length() > 1) valueStr.deleteCharAt(valueStr.length() - 1)
            valueStr.append("]")
        } else if (value instanceof Map) {
            valueStr.append("[")
            value.each { map ->
                valueStr.append("\"${map.key}\":\"${map.value}\",")
            }
            if (valueStr.length() > 1) valueStr.deleteCharAt(valueStr.length() - 1)
            valueStr.append("]")
        } else valueStr.append("$value")
        return valueStr.toString()
    }

    public static HashMap<String, Object> getStaticProperties() {
        def p = new HashMap<String, Object>()
        GlobalConfig.class.getDeclaredFields().each {
            if (it == null) return
            try {
                it.setAccessible(true)
                p.put(it.name, it.get(null))
            } catch (Exception e) {
            }
        }
        return p
    }

    private static void updateConfig(String configStr) {
        Properties config = new Properties()
        config.load(FileUtils.coverStream(configStr))
        GlobalConfig.staticProperties.each { p ->
            updateKey(p.key, config)
        }
    }

    private static void updateKeyByValue(String key, Object value) {
        if (value == null) return
        try {
            GlobalConfig."$key" = new GroovyShell().evaluate(value)
        } catch (Exception e) {

        }
    }

    private static void updateKey(String key, Properties config) {
        if (config.containsKey(key)) {
            updateKeyByValue(key, config.getProperty(key))
        }
    }
}
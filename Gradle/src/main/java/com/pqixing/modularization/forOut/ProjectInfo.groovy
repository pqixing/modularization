package com.pqixing.modularization.forOut
/**
 * 工程配置文件
 * 所有模块，以：加模块名称 extc setting = ":dComm"+":dcNet"
 */
class ProjectInfo {

    /**  manager 的用户名称*/
    String gitUserName = null

    /**  manager 密码 */
    String gitPassWord = null

    /**  当前分支名称*/
    String curBranch = "master"
    /** 强行同步分支 */
    boolean syncBranch = true

    /**   是否拦截依赖缺少的异常 , 默认true
     true: 当有依赖模块缺少时 ， 抛出异常 ， 方便查找问题
     false: 不拦截错误 ， 方便代码导入AS ， 但是缺少依赖构建过程出现的类缺失异常问题很难定位
     **/
    boolean allowLose = false

    /**  是否使用日志
     true:打印日志
     **/
    boolean log = true

    /**
     *  模块依赖方式，默认: mavenOnly
     * mavenOnly:只使用maven仓库进行依赖，不关联本地代码
     * mavenFirst:优先使用maven仓库进行依赖，当仓库不存在依赖时，关联本地代码依赖
     * localFirst:优先使用本地导入模块代码，没有导入则使用maven仓库
     * localOnly:只使用本地导入的模块，禁用ｍａｖｅｎ仓库
     */
    String dependentModel = "localFirst"

    /** 指定一个版本号的map文件 ， 则会优先使用该文件中的信息进行版本号管理 **/
    String versionFile = ""

    /**工程编译目录，使用不同的编译目录便于同时执行多个编译事件，例如ToMaven和构建*/
    String buildDir = ""
}
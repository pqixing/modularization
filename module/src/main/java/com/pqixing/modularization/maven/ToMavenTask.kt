package com.pqixing.modularization.maven

import com.pqixing.help.Tools
import com.pqixing.help.XmlHelper
import com.pqixing.model.Compile
import com.pqixing.model.Module
import com.pqixing.modularization.base.BaseTask
import com.pqixing.modularization.base.PXExtends
import com.pqixing.modularization.setting.ImportPlugin.Companion.getArgs
import com.pqixing.modularization.setting.ImportPlugin.Companion.rootXPlugin
import com.pqixing.modularization.utils.GitUtils
import com.pqixing.modularization.utils.ResultUtils
import com.pqixing.tools.FileUtils
import com.pqixing.tools.TextUtils
import com.pqixing.tools.UrlUtils
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.util.*

open class ToMavenTask : BaseTask() {
    val args = project.getArgs()
    var resultStr = ""
    val px = project.extensions.getByType(PXExtends::class.java)
    val module = px.module
    val forMaven = module.forMaven

    override fun prepare() {
        super.prepare()
        if (forMaven) {
            val uploadTask = px.maven.uploadTask
            val up1 = project.tasks.findByName(uploadTask)
            val up2 = project.rootProject.tasks.findByName(uploadTask)
            up2?.mustRunAfter(up1)
            this.dependsOn(up2, up1)
        }
    }

    /**
     * toMaven时,忽略检查项目, oldType 兼容旧版ide插件,newType 类型可以组合使用
     * oldType 0, newType:0   :UnCheck null
     * oldType 1, newType:1<<4:UnCheck branch 不校验新分支第一次提交是否需要升级版本号
     * oldType 2, newType:1<<5:UnCheck version  不校验是否和上次代码是否相同,允许提交重复
     * oldType 3, newType:1<<6:UnCheck change  不检验本地是否存在未提交修改
     */
    var unCheck = 0

    /**
     * 当准备运行该任务之前，先检测
     */
    override fun whenReady() {
        val maven = px.maven
        val branch = module.branch()
        val revCommit = maven.lastRev

        //如果是可以正常上传的，检测是否合法
        try {
            unCheck = project.getArgs().config.toMavenUnCheck.toInt()
        } catch (e: Exception) {
            Tools.println(e.toString())
        }


        val open = GitUtils.open(File(args.env.codeRootDir, module.project.path))
        if (open == null) {
            Tools.printError(-1, "${module.project.path} Git open fail, please check")
            return
        }

        checkLocalDps(module.compiles.toHashSet())

        checkLoseDps(px.dpsManager.loseList)

        checkGitStatus(open, module)


        val baseVersion = maven.version!!.substringBeforeLast(".")
        checkBaseVersion(baseVersion)

        if (revCommit == null) {
            Tools.printError(-1, "${module.name} Can not load git info!!")
            return
        }

        if (module.forMaven) {
            checkLastLog(revCommit, maven.artifactId, branch, baseVersion, maven.version.substringAfterLast(".").toInt())
        }

        resultStr = "$branch:${maven.artifactId}:${maven.version}"
        FileUtils.delete(project.buildDir)

        val allVersion: MutableMap<String, Any?> = args.vm.readBranchVersion(module.branch()).toMutableMap()

        allVersion["last_commit"] = revCommit?.name
        allVersion["basic_commit"] = revCommit?.name

        //设置上传的版本号的文件
        args.vm.storeToUp(project.rootXPlugin().getExtends(PXExtends::class.java).maven.artifactFile ?: return, allVersion)
    }

    private fun checkGitStatus(git: Git, module: Module) {
        if (!GitUtils.checkIfClean(git, getRelativePath(module.path))) Tools.println(unCheck(3), "${module.name} Code not clean")
    }

    fun getRelativePath(path: String): String? {
        val of = path.indexOf("/")
        return if (of > 0) return path.substring(of + 1) else null
    }

    /**
     * 检查上一个提交版本的日志，如果日志一样，则不允许上传
     */
    private fun checkLastLog(revCommit: RevCommit?, artifactId: String, branch: String, baseVersion: String, v: Int) {
        revCommit ?: return

        //检查Maven仓库最后的一个版本的信息
        var lastVersion = v - 1
        var matchBranch = branch
        val match = project.getArgs().manifest.fallbacks
        var i = match.indexOf(matchBranch)
        while (lastVersion < 0 && i < match.size) {
            matchBranch = if (i < 0) branch else match[i]
            lastVersion = project.getArgs().vm.getNewerVersion(matchBranch, artifactId, baseVersion)
            i++
        }
        //一条记录都没有，新组件
        if (lastVersion < 0) return

        //如果匹配到的版本不是当前分支，则提示升级版本号
        if (matchBranch != branch) {
            Tools.println(unCheck(1), "$artifactId Not allow user the same base version on new branch")
        }
        val params = UrlUtils.getParams(args.vm.getPom(project, matchBranch, artifactId, "$baseVersion.$lastVersion").name)
        val hash = params["hash"] ?: ""
        val commitTime = params["commitTime"]?.toInt() ?: 0
        if (hash == revCommit.name || revCommit.commitTime < commitTime) {
            //距离上次提交没有变更时,视为成功
            ResultUtils.writeResult("$matchBranch:$artifactId:$baseVersion.$lastVersion The code are not change", 0, unCheck(2) != 0)
        }
    }

    /**
     * 检查是否需要忽略错误
     * @return 返回结果 0,uncheckType, <0 , request check
     */
    private fun unCheck(oldType: Int): Int {
        if (unCheck == 0) return -1
        if (unCheck < 4) return unCheck - oldType

        val newType = 1 shl (oldType + 3)
        return 0.coerceAtMost((unCheck and newType) - 1)
    }

    private fun checkBaseVersion(baseVersion: String) {
        if (!TextUtils.isBaseVersion(baseVersion)) Tools.printError(-1, "ToMavenCheckTask $baseVersion is not base version, try x.x etc: 1.0")
    }

    private fun checkLoseDps(loseList: MutableList<String>) {
        if (loseList.isNotEmpty()) {
            Tools.printError(-1, "${project.name}  There are some dependency lose!! -> $loseList")
        }
    }

    private fun checkLocalDps(compiles: HashSet<Compile>) {
        val map = compiles.filter { it.local }.map { it.name }
        if (map.isNotEmpty()) {
            Tools.printError(-1, "${project.name} Contain local project, please remove it before upload -> $map")
        }
    }


    override fun runTask() {
        Thread.sleep(1000)
        //更新本地版本信息
        XmlHelper.loadVersionFromNet(args.env.rootDir.absolutePath)
        ResultUtils.writeResult(resultStr)
    }
}
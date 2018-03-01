package com.pqixing.modularization.utils

import com.pqixing.modularization.Keys
import com.pqixing.modularization.common.BuildConfig
import com.pqixing.modularization.common.GlobalConfig

/**
 * Created by pqixing on 17-11-30.
 */

class FileUtils {

    /**
     * 如果某个
     * @param str
     * @return
     */
    static File appendIfNotExists(String str) {
        return null
    }

    /**
     * 根据包名获取路径
     * @param dir
     * @param pkgName
     * @return
     */
    static File getFileForClass(String dir, String fullName) {
        return new File(dir, "${fullName.replace(".", "/")}.java")
    }

    static String readCache(String url) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        if (fileName.exists()) return fileName.text
    }
    /**
     * 缓存是否有效
     * @param url
     * @return
     */
    static boolean cacheVail(String url) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        return fileName.exists() && (System.currentTimeMillis() - fileName.lastModified() < GlobalConfig.netCacheTime)
    }

    static void saveCache(String url, String cache) {
        File fileName = new File(BuildConfig.netCacheDir, TextUtils.numOrLetter(url))
        write(fileName, cache)
    }

    /**
     * 拼接url
     * @param urls
     * @return
     */
    static String urls(String[] urls) {
        StringBuilder newUrl = new StringBuilder()
        for (String url : urls) {
            newUrl.append(url).append("/")
        }

        return newUrl.substring(0, newUrl.size() - 1)
    }

    static Properties readMaps(File file) {
        Properties p = new Properties()
        if (file.exists()) p.load(file.newInputStream())
        return p
    }

    static String read(String file) {
        return read(new File(file))
    }
    /**
     * 读取文件
     * @param file
     * @return
     */
    static String read(File file) {
        if (file.exists() && file.isFile()) return file.text
        return ""
    }
    /**
     * 输出文件
     * @param file
     * @param data
     */
    static String write(File file, String data) {
        if (file.exists() && file.text == data) return file.path

        if (file.exists()) file.delete()
        file.parentFile.mkdirs()
        Writer out = file.newWriter(Keys.CHARSET)
        out.write(data)
        out.flush()
        out.close()
        return file.path
    }
    /**
     * 字符串转为输入流
     * @param coverStr
     * @return
     */
    static InputStream coverStream(String coverStr) {
        return new File(write(new File(BuildConfig.rootOutDir, "cover/${TextUtils.onlyName}"), coverStr)).newInputStream()
    }
}

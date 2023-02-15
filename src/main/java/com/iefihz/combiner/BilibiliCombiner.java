package com.iefihz.combiner;

import com.iefihz.tool.JsonTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * @author He Zhifei
 * @date 2021/8/5 7:36
 */
public class BilibiliCombiner {

    public static void main(String[] args) throws Exception {
        // 指定缓存路径，如果为单一合并则需指定到具体视频号的路径：-DdownloadDir=xxx\download
        String downloadDir = System.getProperty("downloadDir");

        // 配置ffmpeg具体位置：-DffmpegPath=xxx\ffmpeg.exe
        String ffmpegPath = System.getProperty("ffmpegPath");
        if (isBlank(downloadDir) || isBlank(ffmpegPath)) {
            return;
        }

        try {
            // 单一合并
            if (!isBatchDir(downloadDir)) {
                singleCombine(downloadDir, ffmpegPath, false);
                return;
            }

            // 批量合并
            File downloadDirFile = new File(downloadDir);
            if (downloadDirFile.isDirectory()) {
                File[] files = downloadDirFile.listFiles();
                for (File file : files) {
                    if (file != null) {
                        singleCombine(file.getAbsolutePath(), ffmpegPath, true);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    /**
     * 子文件夹少于一半以c_开头，则认为是批量处理的路径，反之单一合并
     * @param downloadDir
     * @return
     */
    private static boolean isBatchDir(String downloadDir) {
        File files = new File(downloadDir);
        File[] allChildrenDirs = files.listFiles(pathname -> {
            if (pathname != null && pathname.isDirectory()) {
                return true;
            }
            return false;
        });
        File[] childrenDirsStartWithC_ = files.listFiles(pathname -> {
            if (pathname != null && pathname.isDirectory() &&
                    pathname.getName().startsWith("c_")) {
                return true;
            }
            return false;
        });
        return (allChildrenDirs.length / 2) >= childrenDirsStartWithC_.length;
    }

    /**
     * 空白字符串判断
     * @param filePath
     * @return
     */
    private static boolean isBlank(String filePath) {
        if (filePath == null || filePath.trim().equals("")) {
            return true;
        }
        return false;
    }

    /**
     * 单一合并
     * @param root 具体某个视频号目录
     * @param ffmpegPath ffmpeg.exe位置
     * @param isBatched 是否批量合并
     * @throws Exception
     */
    private static void singleCombine(String root, String ffmpegPath, Boolean isBatched) throws Exception {
        if (isBlank(root)) {
            return;
        }
        File rFile = new File(root);
        String rName = rFile.getName();
        if (isBlank(rName)) {
            return;
        }
        if (isBatched && rName.contains("_")) {
            /**
             * 批量合并，不允许待合并目录重命名为包含_字段的目录名。
             * 合并前的目录以b站的视频号为名，不包含_，且合并输出目录包含_，需要排除合并后的目录
             */
            return;
        }
        String pAbsolutePath = rFile.getParentFile().getAbsolutePath();
        File[] files = rFile.listFiles();
        String title = null;
        String outputPath = null;
        for (File file : files) {
            if (file != null && file.isDirectory()) {
                String desc = file.getAbsolutePath() + "/entry.json";
                File descFile = new File(desc);
                if (!descFile.exists()) {
                    continue;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(descFile)));
                Map<String, Object> map = JsonTools.toMap(reader.readLine(), String.class, Object.class);
                if (title == null) {
                    title = rName + "_" + (String) map.get("title");
                    System.out.println("============================ 合并开始 ============================");
                    File newRoot = new File(pAbsolutePath, title);
                    outputPath = newRoot.getAbsolutePath();
                    if (!newRoot.exists()) {
                        newRoot.mkdir();
                        System.out.println("创建目录：" + outputPath + " -- 完成");
                    }
                }

                Map<String, Object> pageData = (Map<String, Object>) map.get("page_data");
                String x = "P" + pageData.get("page") + "-" + pageData.get("part");

                File[] listFiles = file.listFiles();
                for (File f : listFiles) {
                    if (f.isDirectory()) {
                        // 合并音频、视频，输出mp4
                        combineAudioAndVideo(ffmpegPath,
                                f.getAbsolutePath() + "\\audio.m4s",
                                f.getAbsolutePath() + "\\video.m4s",
                                outputPath + "\\" + x + ".mp4");
                    }
                }
            }
        }
        if (outputPath != null) {
            System.out.println("合并后文件位置：" + outputPath);
            System.out.println("============================ 合并结束 ============================\n");
        }
    }

    /**
     * 合并视频和音频
     * @param ffmpegPath ffmpeg.exe位置
     * @param audio 音频
     * @param video 视频
     * @param output 输出文件
     * @throws Exception
     */
    private static void combineAudioAndVideo(String ffmpegPath, String audio, String video, String output) throws Exception {
        File old = new File(output);
        if (old.isFile() && old.exists()) {
            old.delete();
        }
        String command = ffmpegPath + " -i \"" + audio + "\" -i \"" + video + "\" -vcodec copy -acodec copy \"" + output + "\"";
        Runtime.getRuntime().exec(command);
        System.out.println(output + " -- 完成");
    }
}

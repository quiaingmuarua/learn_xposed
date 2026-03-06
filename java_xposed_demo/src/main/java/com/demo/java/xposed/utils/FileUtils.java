package com.demo.java.xposed.utils;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class FileUtils {

    /**
     * 获取文件的最后修改时间戳（毫秒）
     *
     * @param filePath 文件路径
     * @return 文件的最后修改时间戳（毫秒），如果文件不存在则返回 -1
     */
    public static String getFileLastModifiedTimestamp(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            // 返回文件的最后修改时间
            return String.valueOf(file.lastModified()/1000);
        } else {
            // 文件不存在，返回 -1 表示错误
            return "";
        }
    }


    public static void writeToFile(String filePath, String content) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LogUtils.show("Failed to create directory: " + parentDir);
            }
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            // fileWriter.flush(); // 可选，因为 try-with-resources 会自动 flush+close
        } catch (IOException e) {
            LogUtils.printStackErrInfo("writeToFile_err", e);
        }
    }

    public static String readString(File file, Charset charset)  {
        try {
            return new String(Files.readAllBytes(file.toPath()), charset);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}


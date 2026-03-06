package com.demo.java.xposed.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    /**
     * 安全地克隆 InputStream 到 byte[]，不会破坏原始流，可用于日志或内容分析。
     *
     * @param input 原始 InputStream
     * @return 克隆得到的 byte[] 数据
     * @throws IOException 读取失败时抛出
     */
    public static byte[] cloneInputStreamToByteArray(InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int read;
        while ((read = input.read(temp)) != -1) {
            buffer.write(temp, 0, read);
        }

        return buffer.toByteArray();
    }

    /**
     * 包装为 ByteArrayInputStream，用于无副作用后续使用。
     *
     * @param input 原始 InputStream
     * @return ByteArrayInputStream 副本
     * @throws IOException
     */
    public static ByteArrayInputStream cloneInputStream(InputStream input) throws IOException {
        byte[] data = cloneInputStreamToByteArray(input);
        return new ByteArrayInputStream(data);
    }
}

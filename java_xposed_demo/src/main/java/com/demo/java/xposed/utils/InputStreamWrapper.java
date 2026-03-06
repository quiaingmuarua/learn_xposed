package com.demo.java.xposed.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class InputStreamWrapper {
    private PushbackInputStream pushbackInputStream;
    private ByteArrayOutputStream cacheStream;
    private byte[] cache;

    public InputStreamWrapper(InputStream originalInputStream) throws IOException {
        this.pushbackInputStream = new PushbackInputStream(originalInputStream, 1024);
        this.cacheStream = new ByteArrayOutputStream();
        this.cache = new byte[0];
    }

    public String readAndCache() throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = pushbackInputStream.read(buffer)) != -1) {
            cacheStream.write(buffer, 0, bytesRead);
        }
        cacheStream.flush();
        cache = cacheStream.toByteArray();

        // 将缓存的数据推回到流中
        pushbackInputStream.unread(cache);

        return bytesToHex(cache);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public InputStream getOriginalInputStream() {
        return pushbackInputStream;
    }


    /**
     * 读取InputStream并返回其内容，同时将流重置为开始位置。
     *
     * @param inputStream 要读取的InputStream
     * @return InputStream的内容
     * @throws IOException 如果发生I/O错误
     */
    public static byte[] readAndReset(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        // 标记当前的位置
        if (inputStream.markSupported()) {
            inputStream.mark(Integer.MAX_VALUE); // 使用最大值来确保能够标记整个流
        }

        byte[] data = new byte[1024*20];
        int bytesRead;

        // 读取流的内容
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        // 将流重置到标记位置
        if (inputStream.markSupported()) {
            inputStream.reset();
        }

        return buffer.toByteArray();
    }

    public static  String readStringHex(InputStream inputStream){

        try {
            return bytesToHex(readAndReset(inputStream));
        } catch (IOException e) {
            return  e.toString();
        }
    }

    /**
     * 打印InputStream的内容，并重置流到开始位置。
     *
     * @param inputStream 要打印内容的InputStream
     * @throws IOException 如果发生I/O错误
     */
    public static void printInputStream(InputStream inputStream) throws IOException {
        byte[] content = readAndReset(inputStream);
        System.out.println(new String(content));
    }

    public static void main(String[] args) throws IOException {
//        String data = "Hello, World!";
//        InputStream originalInputStream = new ByteArrayInputStream(data.getBytes());
//
//        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(originalInputStream);
//
//        // 读取并缓存数据
//        String cachedData = inputStreamWrapper.readAndCache();
//        System.out.println("Cached Data: " + cachedData);
//
//        // 再次使用原始的 InputStream 读取数据
//        byte[] buffer = new byte[1024];
//        int bytesRead = inputStreamWrapper.getOriginalInputStream().read(buffer);
//        System.out.println("Read again: " + new String(buffer, 0, bytesRead));
        String input = "Hello, this is a test string.";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());

        try {
            // 第一次读取和打印InputStream内容
            printInputStream(inputStream);

            // 再次读取和打印InputStream内容
            printInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

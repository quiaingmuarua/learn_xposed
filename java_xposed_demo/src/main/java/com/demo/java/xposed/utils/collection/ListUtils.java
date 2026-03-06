package com.demo.java.xposed.utils.collection;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

    /**
     * 获取列表的最后 N 个元素。
     *
     * @param list 输入的列表
     * @param n    要获取的元素数量
     * @param <T>  列表中元素的类型
     * @return 最后 N 个元素的列表，或整个列表（如果长度小于等于 N）
     */
    public static <T> List<T> getLastNElements(List<T> list, int n) {
        if (list == null || list.isEmpty() || n <= 0) {
            return new ArrayList<>(); // 返回空列表
        }

        int size = list.size();
        if (size <= n) {
            return new ArrayList<>(list); // 如果列表小于等于 n，返回整个列表
        } else {
            return new ArrayList<>(list.subList(size - n, size)); // 返回最后 N 个元素
        }
    }

    public static void main(String[] args) {
        // 测试工具方法
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            list.add(i); // 添加 1 到 200 的元素
        }

        List<Integer> last50 = getLastNElements(list, 50);
        System.out.println("Last 50 elements: " + last50);

        // 测试小于 100 的情况
        List<Integer> smallList = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            smallList.add(i); // 添加 1 到 50 的元素
        }

        List<Integer> last100FromSmallList = getLastNElements(smallList, 100);
        System.out.println("Last 100 elements from small list: " + last100FromSmallList);
    }
}

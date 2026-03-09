package com.demo.java.xposed.rcs.shared;

@FunctionalInterface
public interface CommandHandler<T, V> {
    V handle(T request, CommandContext context) throws Exception;
}

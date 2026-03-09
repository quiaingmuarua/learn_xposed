package com.example.command.core;

@FunctionalInterface
public interface CommandHandler<T, V> {
    V handle(T request, CommandContext context) throws Exception;
}

package com.example.sekiro.messages.shared;

@FunctionalInterface
public interface CommandHandler<T, V> {
    V handle(T request, CommandContext context) throws Exception;
}

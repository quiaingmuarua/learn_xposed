package com.demo.java.xposed.utils;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import okhttp3.*;

public class ReliableWebSocketClient extends WebSocketListener {

    private static final String TAG = "Xposed_ReliableWebSocketClient";

    private static ReliableWebSocketClient instance;

    private final Context context;
    private final String serverUrl;
    private final OkHttpClient httpClient;
    private WebSocket webSocket;

    private boolean manualClose = false;
    private int retryCount = 0;
    private final long baseDelay = 2000;     // 起始重连间隔 2s
    private final long maxDelay = 30000;     // 最大重连间隔 30s
    private final int MAX_RETRY = 30;   // 最大重连次数
    private final Handler handler = new Handler(Looper.getMainLooper());

    private enum State { DISCONNECTED, CONNECTING, CONNECTED }
    private State currentState = State.DISCONNECTED;

    public interface WebSocketEventListener {
        void onOpen();
        void onMessage(String message);
        void onClosed(String reason);
        void onFailure(String error);
    }

    private @Nullable WebSocketEventListener listener;

    public static synchronized ReliableWebSocketClient getInstance(Context ctx, String url) {
        if (instance == null) {
            instance = new ReliableWebSocketClient(ctx.getApplicationContext(), url);
        }
        Log.i(TAG, "ReliableWebSocketClient instance created " + instance + " URL=" + url);
        return instance;
    }

    private ReliableWebSocketClient(Context ctx, String url) {
        this.context = ctx;
        this.serverUrl = url;

        this.httpClient = new OkHttpClient.Builder()
                .pingInterval(20, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    public void setListener(WebSocketEventListener l) {
        this.listener = l;
    }

    public synchronized void connect() {
        if (currentState == State.CONNECTING || currentState == State.CONNECTED) return;

        Log.i(TAG, "Connecting to " + serverUrl);
        currentState = State.CONNECTING;
        manualClose = false;

        Request request = new Request.Builder().url(serverUrl).build();
        httpClient.newWebSocket(request, this);
    }

    public void send(String message) {
        if (webSocket != null && currentState == State.CONNECTED) {
            Log.i(TAG, "Sending: " + message);
            webSocket.send(message);
        }
    }

    public synchronized void close() {
        manualClose = true;
        retryCount = 0;
        if (webSocket != null) {
            webSocket.close(1000, "Manual close");
        }
    }

    private void scheduleReconnect() {
        if (retryCount >= MAX_RETRY) {
            Log.w(TAG, "已达到最大重连次数 (" + MAX_RETRY + ")，停止重连");
            return;
        }

        long delay = Math.min((1L << retryCount) * baseDelay, maxDelay);
        Log.i(TAG, "尝试第 " + (retryCount + 1) + " 次重连，延迟 " + delay + "ms");
        handler.postDelayed(this::connect, delay);
        retryCount++;
    }

    // WebSocket Callbacks

    @Override
    public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
        this.webSocket = ws;
        currentState = State.CONNECTED;
        retryCount = 0;  // ✅ 重置重试次数
        Log.i(TAG, "WebSocket connected");
        if (listener != null) listener.onOpen();

    }


    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        Log.i(TAG, "Received: " + text);
        if (listener != null) listener.onMessage(text);
    }

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        Log.i(TAG, "WebSocket closed: " + reason);
        currentState = State.DISCONNECTED;
        if (listener != null) listener.onClosed(reason);
        if (!manualClose) scheduleReconnect();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
        Log.e(TAG, "WebSocket failure: " + t.getMessage());
        currentState = State.DISCONNECTED;
        if (listener != null) listener.onFailure(t.getMessage());
        if (!manualClose) scheduleReconnect();
    }
}

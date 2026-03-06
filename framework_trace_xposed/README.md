# Framework Trace Xposed Module

A small Xposed/LSPosed module that traces the Activity launch chain across
system_server and app processes. It propagates a trace id via Intent extras and
prints structured NDJSON logs to logcat.

## Features
- System server hooks for `ActivityTaskManagerService.startActivity*`.
- App process hooks for Instrumentation and ActivityThread launch paths.
- Trace propagation via the `__ft_trace` Intent extra.
- Structured NDJSON logs (tag `FrameHook`) for easy parsing.
- Sample output: `src/main/assets/log_sample.log`.

## Entry Point
- Xposed init class: `com.example.framework.trace.HookEntry`
- Declared in `src/main/assets/xposed_init`.

## Hook Coverage
- System server: `ActivityTaskManagerService.startActivity*`
- App process:
  - `Instrumentation.execStartActivity`
  - `Instrumentation.callActivityOnCreate`
  - `LaunchActivityItem.execute` (Android 9+)
  - `ActivityThread.handleLaunchActivity` (legacy)

## Build Notes
- `compileSdk`: 34, `minSdk`: 24
- Xposed API jar should be placed in `libs/` (e.g. `XposedBridgeApi-89.jar`).
- Dependency is already declared as `compileOnly fileTree(dir: 'libs', include: ['*.jar'])`.

## Configuration
- Target app is hardcoded in `ActivityHooks.appliesTo`:
  - `com.example.javademo`
- Update that package name to trace a different app.

## Logging
- Log tag: `FrameHook`
- Format: one JSON object per line (NDJSON)
- Useful switches in `LogUtil`:
  - `ENABLE_STACK` and `STACK_TOP_N`
  - `ENABLE_DEDUP` and `DEDUP_WINDOW_MS`
  - `ENABLE_LAUNCH_DEDUP_NO_TRACE`

## Quick Usage
1. Build and install the module APK.
2. Enable it in LSPosed/Xposed.
3. Launch the target app.
4. Inspect logcat with tag `FrameHook` for trace output.
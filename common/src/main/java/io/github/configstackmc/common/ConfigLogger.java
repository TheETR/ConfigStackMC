package io.github.configstackmc.common;

public interface ConfigLogger {
    void warn(String message);
    void info(String message);
    static ConfigLogger noop() { return new ConfigLogger() { public void warn(String m) {} public void info(String m) {} }; }
}

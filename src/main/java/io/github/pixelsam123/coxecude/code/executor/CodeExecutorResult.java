package io.github.pixelsam123.coxecude.code.executor;

public record CodeExecutorResult(
    int statusCode,
    String stdoutAndStderr
) {
}

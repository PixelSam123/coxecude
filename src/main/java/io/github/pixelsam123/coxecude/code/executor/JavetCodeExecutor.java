package io.github.pixelsam123.coxecude.code.executor;

import com.caoccao.javet.exceptions.JavetCompilationException;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.exceptions.JavetTerminatedException;
import com.caoccao.javet.interception.logging.BaseJavetConsoleInterceptor;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.values.V8Value;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

import static io.github.pixelsam123.coxecude.utils.UtilsAsync.*;

@ApplicationScoped
public class JavetCodeExecutor implements CodeExecutor {

    private static final long execTimeoutDuration = 5L;

    private final IJavetEnginePool<V8Runtime> javetEnginePool;

    public JavetCodeExecutor(IJavetEnginePool<V8Runtime> javetEnginePool) {
        this.javetEnginePool = javetEnginePool;
    }

    @Override
    public CodeExecutorResult exec(String code) {
        StringBuilder stdoutAndStderr = new StringBuilder();

        try (
            IJavetEngine<V8Runtime> javetEngine = javetEnginePool.getEngine();
            V8Runtime runtime = javetEngine.getV8Runtime()
        ) {
            runtime.resetContext();

            BaseJavetConsoleInterceptor consoleInterceptor =
                new BaseJavetConsoleInterceptor(runtime) {
                    @Override
                    public void consoleDebug(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleError(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleInfo(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }

                    }

                    @Override
                    public void consoleLog(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleTrace(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }
                    }

                    @Override
                    public void consoleWarn(V8Value... v8Values) {
                        for (V8Value value : v8Values) {
                            stdoutAndStderr.append(value.toString()).append('\n');
                        }
                    }
                };

            setTimeout(runtime::terminateExecution, Duration.ofSeconds(execTimeoutDuration));

            consoleInterceptor.register(runtime.getGlobalObject());

            try {
                runtime.getExecutor(code).executeVoid();
            } catch (JavetCompilationException | JavetExecutionException e) {
                stdoutAndStderr.append(e.getScriptingError().toString()).append('\n');

                return new CodeExecutorResult(1, stdoutAndStderr.toString());
            } catch (JavetTerminatedException e) {
                stdoutAndStderr.append("Timed out. Details:\n").append(e.getMessage()).append('\n');

                return new CodeExecutorResult(1, stdoutAndStderr.toString());
            } finally {
                consoleInterceptor.unregister(runtime.getGlobalObject());
            }

            return new CodeExecutorResult(0, stdoutAndStderr.toString());
        } catch (JavetException e) {
            return new CodeExecutorResult(500, e.toString());
        }
    }

}

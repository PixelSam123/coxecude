package io.github.pixelsam123.coxecude.code.executor;

import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class LuajCodeExecutor implements CodeExecutor {

    private static Globals serverGlobals;

    public LuajCodeExecutor() {
        // Create server globals with just enough library support to compile user scripts.
        serverGlobals = new Globals();
        serverGlobals.load(new JseBaseLib());
        serverGlobals.load(new PackageLib());
        serverGlobals.load(new JseStringLib());

        // To load scripts, we occasionally need a math library in addition to compiler support.
        // To limit scripts using the debug library, they must be closures, so we only install LuaC.
        serverGlobals.load(new JseMathLib());
        LoadState.install(serverGlobals);
        LuaC.install(serverGlobals);
    }

    /**
     * Function from the link below
     *
     * @see <a href="https://github.com/luaj/luaj/blob/master/examples/jse/SampleSandboxed.java">luaj SampleSandboxed example</a>
     */
    @Override
    public CodeExecutorResult exec(String code) {
        // Each script will have its own set of globals, which should
        // prevent leakage between scripts running on the same server.
        Globals userGlobals = new Globals();
        userGlobals.load(new JseBaseLib());
        userGlobals.load(new PackageLib());
        userGlobals.load(new Bit32Lib());
        userGlobals.load(new TableLib());
        userGlobals.load(new JseStringLib());
        userGlobals.load(new JseMathLib());

        // This library is dangerous as it gives unfettered access to the
        // entire Java VM, so it's not suitable within this lightweight sandbox.
        // userGlobals.load(new LuajavaLib());

        // Starting coroutines in scripts will result in threads that are
        // not under the server control, so this library should probably remain out.
        // userGlobals.load(new CoroutineLib());

        // These are probably unwise and unnecessary for scripts on servers,
        // although some date and time functions may be useful.
        // userGlobals.load(new JseIoLib());
        // userGlobals.load(new JseOsLib());

        // Loading and compiling scripts from within scripts may also be
        // prohibited, though in theory it should be fairly safe.
        // LoadState.install(userGlobals);
        // LuaC.install(userGlobals);

        // The debug library must be loaded for hook functions to work, which
        // allow us to limit scripts to run a certain number of instructions at a time.
        // However, we don't wish to expose the library in the user globals,
        // so it is immediately removed from the user globals once created.
        userGlobals.load(new DebugLib());
        LuaValue sethook = userGlobals.get("debug").get("sethook");
        userGlobals.set("debug", LuaValue.NIL);

        // Redirect stdout to a StringBuilder
        ByteArrayOutputStream stdoutAndStderr = new ByteArrayOutputStream();
        PrintStream stdoutAndStderrPS =
            new PrintStream(stdoutAndStderr, true, StandardCharsets.UTF_8);
        userGlobals.STDOUT = stdoutAndStderrPS;
        userGlobals.STDERR = stdoutAndStderrPS;

        // Set up the script to run in its own lua thread, which allows us
        // to set a hook function that limits the script to a specific number of cycles.
        // Note that the environment is set to the user globals, even though the
        // compiling is done with the server globals.
        LuaValue chunk;
        try {
            chunk = serverGlobals.load(code, "main", userGlobals);
        } catch (LuaError e) {
            return new CodeExecutorResult(1, e.getMessage());
        }
        LuaThread thread = new LuaThread(userGlobals, chunk);

        // Set the hook function to immediately throw an Error, which will not be
        // handled by any Lua code other than the coroutine.
        LuaValue hookfunc = new ZeroArgFunction() {
            public LuaValue call() {
                // A simple lua error may be caught by the script, but a
                // Java Error will pass through to top and stop the script.
                throw new Error("Script overran resource limits.");
            }
        };
        final int instructionCount = 100;
        sethook.invoke(LuaValue.varargsOf(new LuaValue[]{
            thread, hookfunc,
            LuaValue.EMPTYSTRING, LuaValue.valueOf(instructionCount)
        }));

        // When we resume the thread, it will run up to 'instructionCount' instructions
        // then call the hook function which will error out and stop the script.
        Varargs result = thread.resume(LuaValue.NIL);

        if (!result.checkboolean(1)) {
            return new CodeExecutorResult(1, stdoutAndStderr + result.checkjstring(2));
        }

        return new CodeExecutorResult(0, stdoutAndStderr.toString());
    }

}

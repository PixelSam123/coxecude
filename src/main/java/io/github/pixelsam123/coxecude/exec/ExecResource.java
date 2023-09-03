package io.github.pixelsam123.coxecude.exec;

import io.github.pixelsam123.coxecude.code.executor.CodeExecutor;
import io.github.pixelsam123.coxecude.code.executor.CodeExecutorResult;
import io.github.pixelsam123.coxecude.code.executor.JavetCodeExecutor;
import io.github.pixelsam123.coxecude.code.executor.LuajCodeExecutor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/")
public class ExecResource {

    private final Map<String, CodeExecutor> langToCodeExecutor;

    public ExecResource(JavetCodeExecutor javetCodeExecutor, LuajCodeExecutor luajCodeExecutor) {
        langToCodeExecutor = Map.ofEntries(
            Map.entry("js", javetCodeExecutor),
            Map.entry("lua", luajCodeExecutor)
        );
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ExecResponse> hello(ExecRequest execRequest) {
        return Uni.createFrom().item(() -> {
            CodeExecutor codeExecutor = langToCodeExecutor.get(execRequest.lang());
            if (codeExecutor == null) {
                return new ExecResponse(-1, "Invalid language");
            }

            CodeExecutorResult result = codeExecutor.exec(execRequest.code());
            return new ExecResponse(result.statusCode(), result.stdoutAndStderr());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

}

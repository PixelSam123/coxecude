package io.github.pixelsam123.coxecude.exec;

import io.github.pixelsam123.coxecude.code.executor.CodeExecutorResult;
import io.github.pixelsam123.coxecude.code.executor.JavetCodeExecutor;
import io.github.pixelsam123.coxecude.code.executor.LuajCodeExecutor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ExecResource {

    private final JavetCodeExecutor javetCodeExecutor;

    public ExecResource(JavetCodeExecutor javetCodeExecutor) {
        this.javetCodeExecutor = javetCodeExecutor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ExecResponse> hello(ExecRequest execRequest) {
        return Uni.createFrom().item(() -> {
            CodeExecutorResult result = switch (execRequest.lang()) {
                case "js" -> javetCodeExecutor.exec(execRequest.code());
                case "lua" -> new LuajCodeExecutor().exec(execRequest.code());
                default -> new CodeExecutorResult(-1, "invalid language");
            };

            return new ExecResponse(result.statusCode(), result.stdoutAndStderr());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

}

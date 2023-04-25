package io.github.pixelsam123.coxecude.exec;

import io.github.pixelsam123.coxecude.code.executor.CodeExecutor;
import io.github.pixelsam123.coxecude.code.executor.CodeExecutorResult;
import io.github.pixelsam123.coxecude.code.executor.JavetCodeExecutor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class ExecResource {

    private final CodeExecutor codeExecutor;

    public ExecResource(JavetCodeExecutor codeExecutor) {
        this.codeExecutor = codeExecutor;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ExecResponse> hello(ExecRequest execRequest) {
        return Uni.createFrom().item(() -> {
            CodeExecutorResult result = codeExecutor.exec(execRequest.code());

            return new ExecResponse(result.statusCode(), result.stdoutAndStderr());
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool());
    }

}

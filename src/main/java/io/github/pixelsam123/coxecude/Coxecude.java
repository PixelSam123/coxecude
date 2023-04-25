package io.github.pixelsam123.coxecude;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEngineConfig;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
    info = @Info(
        title = "coxecude",
        version = "0.1.0",
        description = "Server exposing a REST API to execute code through various VMs/interpreters"
    )
)
public class Coxecude extends Application {

    @ApplicationScoped
    public IJavetEnginePool<V8Runtime> javetEnginePool() {
        JavetEngineConfig config = new JavetEngineConfig();
        config.setJSRuntimeType(JSRuntimeType.V8);

        return new JavetEnginePool<>(config);
    }

    public void dispose(@Disposes IJavetEnginePool<V8Runtime> javetEnginePool)
        throws JavetException {
        javetEnginePool.close();
    }

}

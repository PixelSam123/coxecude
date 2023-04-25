package io.github.pixelsam123.coxecude.exec;

import javax.validation.constraints.NotNull;

public record ExecRequest(
    @NotNull String lang,
    @NotNull String code
) {
}

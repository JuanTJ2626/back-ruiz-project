package com.ruiz.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final String recurso;
    private final String campo;
    private final Object valor;

    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(String.format("%s no encontrado con %s: '%s'", recurso, campo, valor));
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }

    public String getRecurso() {
        return recurso;
    }

    public String getCampo() {
        return campo;
    }

    public Object getValor() {
        return valor;
    }
}

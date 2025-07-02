package com.twoploapps.a2plomessenger.Models;

public class MensajeAI {
    private String mensaje;
    private String de; // "usuario" o "ia"
    private long timestamp;

    public MensajeAI() {}

    public MensajeAI(String mensaje, String de, long timestamp) {
        this.mensaje = mensaje;
        this.de = de;
        this.timestamp = timestamp;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getDe() {
        return de;
    }

    public long getTimestamp() {
        return timestamp;
    }
}


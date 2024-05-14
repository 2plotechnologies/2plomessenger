package com.twoploapps.a2plomessenger.Models;

public class MensajeCanal {
    private String de;
    private String mensaje;
    private String tipo;
    private String mensajeID;
    private String fecha;
    private String hora;

    public MensajeCanal(){

    }

    public MensajeCanal(String de, String mensaje, String tipo, String mensajeID, String fecha, String hora) {
        this.de = de;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.mensajeID = mensajeID;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensajeID() {
        return mensajeID;
    }

    public void setMensajeID(String mensajeID) {
        this.mensajeID = mensajeID;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }
}

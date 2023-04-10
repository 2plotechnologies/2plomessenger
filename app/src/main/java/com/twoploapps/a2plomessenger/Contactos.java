package com.twoploapps.a2plomessenger;

public class Contactos {

    private String nombre;
    private String ciudad;
    private String estado;
    private String imagen;
    private String PC;
    private String PI;

    public Contactos() {

    }

    public Contactos(String nombre, String ciudad, String estado, String imagen,String PC, String PI) {
        this.nombre = nombre;
        this.ciudad = ciudad;
        this.estado = estado;
        this.imagen = imagen;
        this.PC = PC;
        this.PI = PI;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
    public String getPC() {
        return PC;
    }

    public void setPC(String PC) {
        this.PC = PC;
    }

    public String getPI() {
        return PI;
    }

    public void setPI(String PI) {
        this.PI = PI;
    }
}

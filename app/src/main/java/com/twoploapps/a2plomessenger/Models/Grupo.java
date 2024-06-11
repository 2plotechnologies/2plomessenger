package com.twoploapps.a2plomessenger.Models;

public class Grupo {
    private String Id;
    private String Nombre;
    private String Descripcion;
    private String Imagen;
    private String CreadorId;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String nombre) {
        Nombre = nombre;
    }

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public String getImagen() {
        return Imagen;
    }

    public void setImagen(String imagen) {
        Imagen = imagen;
    }

    public String getCreadorId() {
        return CreadorId;
    }

    public void setCreadorId(String creadorId) {
        CreadorId = creadorId;
    }

    public Grupo(){}

    public Grupo(String id, String nombre, String descripcion, String imagen, String creadorId) {
        Id = id;
        Nombre = nombre;
        Descripcion = descripcion;
        Imagen = imagen;
        CreadorId = creadorId;
    }
}

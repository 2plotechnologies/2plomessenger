package com.twoploapps.a2plomessenger.Models;

public class Canal {

    private String Id;
    private String Nombre;
    private String Descripcion;
    private String Imagen;
    private String Creador_Id;

    public Canal() {

    }

    public Canal(String id, String nombre, String descripcion, String imagen, String creador_Id) {
        Id = id;
        Nombre = nombre;
        Descripcion = descripcion;
        Imagen = imagen;
        Creador_Id = creador_Id;
    }

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

    public String getCreador_Id() {
        return Creador_Id;
    }

    public void setCreador_Id(String creador_Id) {
        Creador_Id = creador_Id;
    }
}

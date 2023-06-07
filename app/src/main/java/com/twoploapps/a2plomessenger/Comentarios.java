package com.twoploapps.a2plomessenger;

public class Comentarios {
    public Comentarios(String nombre_usuario, String comentario, String id_usuario, String id_del_post, String id_comentario) {
        this.nombre_usuario = nombre_usuario;
        this.comentario = comentario;
        this.id_usuario = id_usuario;
        this.id_del_post = id_del_post;
        this.id_comentario = id_comentario;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public String getId_del_post() {
        return id_del_post;
    }

    public void setId_del_post(String id_del_post) {
        this.id_del_post = id_del_post;
    }

    private String nombre_usuario;
    private String comentario;
    private String id_usuario;
    private String id_del_post;

    public String getId_comentario() {
        return id_comentario;
    }

    public void setId_comentario(String id_comentario) {
        this.id_comentario = id_comentario;
    }

    private String id_comentario;
    public Comentarios(){

    }
}

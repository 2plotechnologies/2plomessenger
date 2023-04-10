package com.twoploapps.a2plomessenger;

public class Posts {
    private String texto;
    private String imagen;
    private String nomuser;
    private String iduser;
    private String postId;
    private long fecha; // Nueva propiedad fecha de tipo long

    public Posts() {
    }

    public Posts(String texto, String imagen, String nomuser, String iduser, String postId, long fecha) {
        this.texto = texto;
        this.imagen = imagen;
        this.nomuser = nomuser;
        this.iduser = iduser;
        this.postId = postId;
        this.fecha = fecha;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNomuser() {
        return nomuser;
    }

    public void setNomuser(String nomuser) {
        this.nomuser = nomuser;
    }

    public String getIduser() {
        return iduser;
    }

    public void setIduser(String iduser) {
        this.iduser = iduser;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public boolean hasImage() {
        return imagen != null;
    }

    public boolean hasText() {
        return texto != null;
    }
}

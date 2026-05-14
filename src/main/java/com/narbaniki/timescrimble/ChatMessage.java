package com.narbaniki.timescrimble;

public class ChatMessage {
    private String pseudo;
    private String contenu;
    private String type;

    public ChatMessage() {}

    public ChatMessage(String pseudo, String contenu, String type) {
        this.pseudo = pseudo;
        this.contenu = contenu;
        this.type = type;
    }
    
    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
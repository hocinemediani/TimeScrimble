package com.narbaniki.timescrimble;

import java.util.List;

public class DrawMessage {
    
    private List<Segment> lignes;
    private String pseudo;

    public List<Segment> getLignes() {
        return lignes;
    }

    public void setLignes(List<Segment> lignes) {
        this.lignes = lignes;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    /* Ca c'est une classe imbriquée, comme dans simple's adventure la classe Tile dans Tiles si vous vous en souvenez. */
    public static class Segment {
        private double x1;
        private double y1;
        private double x2;
        private double y2;
        private String couleur;
        private int epaisseur;

        public double getX1() {
            return x1;
        }

        public void setX1(double x1) {
            this.x1 = x1;
        }

        public double getY1() {
            return y1;
        }

        public void setY1(double y1) {
            this.y1 = y1;
        }

        public double getX2() {
            return x2;
        }

        public void setX2(double x2) {
            this.x2 = x2;
        }

        public double getY2() {
            return y2;
        }

        public void setY2(double y2) {
            this.y2 = y2;
        }

        public String getCouleur() {
            return couleur;
        }

        public void setCouleur(String couleur) {
            this.couleur = couleur;
        }

        public int getEpaisseur() {
            return epaisseur;
        }

        public void setEpaisseur(int epaisseur) {
            this.epaisseur = epaisseur;
        }
    }
}
package com.narbaniki.timescrimble;

import java.util.List;

/** Un DrawMessage est un message envoyé par l'utilisateur dessinateur<br>
 * ou par le serveur en tant que broadcast afin d'actualiser la représentation<br>
 * du dessin pour tous les joueurs.<br>
 * Il est composé de 2 éléments :<br>
 * - Le pseudonyme du joueur envoyant le message, afin de vérifier que seul<br>
 * le joueur dessinateur envoie des messages de dessin.<br>
 * - La liste des segments ayant été ajoutés depuis le précédent message.
 */
public class DrawMessage {
    
    /** La liste des segments ajoutés depuis le précédent message. */
    private List<Segment> lignes;
    /** Le pseudonyme de l'utilisateur envoyant le DrawMessage. */
    private String pseudo;


    /** Retourne la liste des segments associée au DrawMessage.
     * @return La liste des segments associée au DrawMessage
     */
    public List<Segment> getLignes() {
        return lignes;
    }


    /** Modifie la liste des segments pour la liste {@code lignes}.
     * @param lignes La liste des segments à affecter au DrawMessage
     */
    public void setLignes(List<Segment> lignes) {
        this.lignes = lignes;
    }

    /** Retourne le pseudonyme de l'utilisateur envoyant le DrawMessage.
     * @return Le pseudonyme de l'utilisateur envoyant le DrawMessage
     */
    public String getPseudo() {
        return pseudo;
    }


    /** Modifie le pseudonyme pour le pseudonyme {@code pseudo}.
     * @param pseudo Le pseudonyme à affecter au DrawMessage
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }


    /** La classe imbriquée segment est utilisée par le DrawMessage afin de<br>
     * représenter une ligne sur le canvas HTML.
     */
    public static class Segment {
        
        /** L'abscisse du point de départ du segment. */
        private double x1;
        /** L'ordonnée du point de départ du segment. */
        private double y1;
        /** L'abscisse du point d'arrivée du segment. */
        private double x2;
        /** L'ordonnée du point d'arrivée du segment. */
        private double y2;
        /** La couleur utilisée pour le segment. */
        private String couleur;
        /** L'épaisseur utilsiée pour le segment. */
        private int epaisseur;


        /** Retourne l'abscisse du point de départ du segment.
         * @return L'abscisse du point de départ du segment
         */
        public double getX1() {
            return x1;
        }


        /** Modifie l'abscisse du point de départ du segment pour la valeur {@code x1}.
         * @param x1 L'abscisse du point de départ à affecter au segment
         */
        public void setX1(double x1) {
            this.x1 = x1;
        }


        /** Retourne l'ordonnée du point de départ du segment.
         * @return L'ordonnée du point de départ du segment
         */
        public double getY1() {
            return y1;
        }


        /** Modifie l'ordonnée du point de départ du segment pour la valeur {@code y1}.
         * @param y1 L'ordonnée du point de départ à affecter au segment
         */
        public void setY1(double y1) {
            this.y1 = y1;
        }


        /** Retourne l'abscisse du point d'arrivée du segment.
         * @return L'abscisse du point d'arrivée du segment
         */
        public double getX2() {
            return x2;
        }


        /** Modifie l'abscisse du point d'arrivée du segment pour la valeur {@code x2}.
         * @param x2 L'abscisse du point d'arrivée à affecter au segment
         */
        public void setX2(double x2) {
            this.x2 = x2;
        }


        /** Retourne l'ordonnée du point d'arrivée du segment.
         * @return L'ordonnée du point d'arrivée du segment
         */
        public double getY2() {
            return y2;
        }


        /** Modifie l'ordonnée du point d'arrivée du segment pour la valeur {@code y2}.
         * @param y2 L'ordonnée du point d'arrivée à affecter au segment
         */
        public void setY2(double y2) {
            this.y2 = y2;
        }


        /** Retourne la couleur utilisée pour le segment.
         * @return La couleur utilisée pour le segment
         */
        public String getCouleur() {
            return couleur;
        }


        /** Modifie la couleur du segment pour la couleur {@code couleur}.
         * @param couleur La couleur à affecter au segment
         */
        public void setCouleur(String couleur) {
            this.couleur = couleur;
        }


        /** Retourne l'épaisseur utilisée pour le segment.
         * @return L'épaisseur utilisée pour le segment
         */
        public int getEpaisseur() {
            return epaisseur;
        }


        /** Modifie l'épaisseur du segment pour l'épaisseur {@code epaisseur}.
         * @param epaisseur L'épaisseur à affecter au segment
         */
        public void setEpaisseur(int epaisseur) {
            this.epaisseur = epaisseur;
        }
    }
}

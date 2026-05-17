package com.narbaniki.timescrimble;

/** Un ChatMessage représente un message du chat à envoyer
 * ou un message du chat réceptionné par le serveur.<br>
 * Ce dernier contient plusieurs informations telles que :<br>
 * - Le pseudonyme de l'utilisateur l'ayant envoyé,<br>
 * - Le contenu du message sous la forme d'un String,<br>
 * - Et le type de message (JOIN, CHAT, SUCCES, etc).
 */
public class ChatMessage {

    /** Le pseudonyme de l'utilisateur ayant envoyé le message. */
    private String pseudo;
    /** Le contenu du message. */
    private String contenu;
    /** Le type du message */
    private String type;


    /** Constructeur vide pour JPA afin de pouvoir utiliser les<br>
     * ChatMessage en tant que DataTransferObject.
     */
    public ChatMessage() {}


    /** Créé une instance de ChatMessage.
     * @param pseudo Le pseudonyme de l'utilisateur envoyant le message
     * @param contenu Le contenu du message
     * @param type Le type du message
     */
    public ChatMessage(String pseudo, String contenu, String type) {
        this.pseudo = pseudo;
        this.contenu = contenu;
        this.type = type;
    }
    

    /** Retourne le pseudonyme de l'utilisateur envoyant le message.
     * @return Le pseudonyme de l'utilisateur envoyant le message
     */
    public String getPseudo() {
        return pseudo;
    }


    /** Change le pseudonyme de l'utilisateur envoyant le message pour<br>
     * le pseudonyme {@code pseudo}.
     * @param pseudo Le pseudonyme à affecter au message
     */
    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }


    /** Retourn le contenu du message.
     * @return Le contenu du message
     */
    public String getContenu() {
        return contenu;
    }


    /** Change le contenu du message pour le contenu {@code contenu}.
     * @param contenu Le contenu à affecter au message
     */
    public void setContenu(String contenu) {
        this.contenu = contenu;
    }


    /** Retourne le type du message.
     * @return Le type du message
     */
    public String getType() {
        return type;
    }


    /** Change le type du message pour le type {@code type}.
     * @param type Le type à affecter au message
     */
    public void setType(String type) {
        this.type = type;
    }
}

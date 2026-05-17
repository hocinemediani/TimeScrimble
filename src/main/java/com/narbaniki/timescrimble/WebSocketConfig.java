package com.narbaniki.timescrimble;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** La classe WebSocketConfig permet de configurer le serveur WebSocket de l'application.<br>
 * Elle définit les endpoints permettant aux clients de se connecter<br>
 * en temps réel, ainsi que le routage des messages (message broker) utilisé pour<br>
 * diffuser les événements de jeu (dessin, chronomètre, chat, statuts).
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /** Enregistre les points de terminaison STOMP auxquels les clients peuvent se connecter.<br>
     * L'endpoint principal "/ws" est configuré ici avec l'activation de SockJS pour assurer<br>
     * une compatibilité de repli avec les navigateurs ne supportant pas nativement les WebSockets.
     * @param registry Le registre permettant de configurer les points de terminaison STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }


    /** Configure le message broker pour le routage des données.<br>
     * Définit les préfixes pour les requêtes envoyées par les clients vers le serveur ("/app")<br>
     * et active un broker simple en mémoire pour diffuser les messages aux clients abonnés ("/topic").
     * @param registry Le registre permettant de configurer le courtier de messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
}
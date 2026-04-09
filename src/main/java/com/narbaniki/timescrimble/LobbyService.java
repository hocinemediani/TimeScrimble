package com.narbaniki.timescrimble;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class LobbyService {

    private final PartieRepository partieRepository;

    public LobbyService(PartieRepository partieRepository) {
        this.partieRepository = partieRepository;
    }

    public List<Partie> allPrivate(){
        return this.partieRepository.findAllByEstPriveeFalse();
    }

    public void create(String nom, Integer nb, Boolean priv, Utilisateur user) {
        Partie partie = new Partie();
        partie.setNom(nom);
        if (nb < 2) {
            nb = 2;
        } else if (nb > 8) {
            nb = 8;
        }
        partie.setNbJoueursMax(nb);
        partie.setEstPrivee(priv);
        partie.setMotADeviner("Ilian");
        Joueur joueur = new Joueur(user, partie);
        partie.getJoueurs().add(joueur);
        partieRepository.save(partie);
    }

    public void join(String code, Utilisateur user) {
            Partie partie = partieRepository.findByCode(code);
            if (partie == null) {
                System.out.printf("Aucune partie n'existe avec ce code : %s.%n", code);
                throw new IllegalArgumentException("Partie non trouvée avec ce code.");
            }
            if (partie.getJoueurs().size() >= partie.getNbJoueursMax()) {
                System.out.println("La partie est déjà pleine.");
                throw new IllegalStateException("La partie est déjà pleine.");
            }    
            Joueur joueur = new Joueur(user, partie);
            partie.getJoueurs().add(joueur);
            partieRepository.save(partie);
    }
    
}

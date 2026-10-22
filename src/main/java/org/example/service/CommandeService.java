package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(Commande.class);
    }

    // Les produits proviennent d'un autre EntityManager (donc détachés) : @MapsId
    // impose de les rattacher au contexte de persistance avant le persist() de la commande.
    public void create(Commande commande) {
        EntityManager em = GenericDAO.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (CommandeProduit ligne : commande.getLignes()) {
                ligne.setProduit(em.getReference(Produit.class, ligne.getProduit().getId()));
            }
            em.persist(commande);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}

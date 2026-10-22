package org.example.service;

import org.example.dao.GenericDAO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

public class RapportVentesService {

    private static final EntityManagerFactory emf = GenericDAO.getEntityManagerFactory();

    public List<Object[]> chiffreAffairesParCategorie(LocalDate debut, LocalDate fin) {
        EntityManager em = emf.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<CommandeProduit> ligne = cq.from(CommandeProduit.class);

            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<CommandeProduit, Commande> commande = ligne.join("commande");

            Expression<BigDecimal> chiffreAffaires =
                    cb.sum(cb.prod(produit.<BigDecimal>get("prix"), cb.toBigDecimal(ligne.<Integer>get("quantite"))));

            cq.multiselect(produit.get("categorie").get("nom"), chiffreAffaires);
            cq.where(cb.between(commande.<LocalDate>get("dateCommande"), debut, fin));
            cq.groupBy(produit.get("categorie").get("nom"));
            cq.orderBy(cb.desc(chiffreAffaires));

            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> rapportMensuel(YearMonth mois) {
        return chiffreAffairesParCategorie(mois.atDay(1), mois.atEndOfMonth());
    }
}

package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.Client;
import org.example.entities.Commande;
import org.example.entities.Fournisseur;
import org.example.entities.Produit;
import org.example.entities.User;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;
import org.example.util.HibernateUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class GestionCommercialeApp {

    public static void main(String[] args) {

        // ---------- Étape 4 : entité Categorie et première insertion ----------
        GenericDAO<Categorie, Long> categorieDAO = new GenericDAO<>(Categorie.class);
        Categorie cat = new Categorie("Électronique");
        categorieDAO.create(cat);
        System.out.println("Catégorie créée avec l'ID : " + cat.getId());

        // ---------- Étape 6 : service CRUD générique ----------
        CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
        Categorie nouvelle = new Categorie("Informatique");
        catService.create(nouvelle);
        System.out.println("Nouvelle catégorie ID = " + nouvelle.getId());

        // ---------- Étape 7 : Produit et relation ManyToOne / OneToMany ----------
        ProduitService produitService = new ProduitService();
        Produit clavier = new Produit("Clavier", new BigDecimal("49.99"), nouvelle);
        Produit souris = new Produit("Souris", new BigDecimal("19.90"), nouvelle);
        Produit ecran = new Produit("Écran 24 pouces", new BigDecimal("149.00"), nouvelle);
        Produit televiseur = new Produit("Téléviseur LED", new BigDecimal("499.00"), cat);
        produitService.create(clavier);
        produitService.create(souris);
        produitService.create(ecran);
        produitService.create(televiseur);
        System.out.println("Produit créé : " + clavier);

        // ---------- Étape 8 : Named Queries, JPQL et Criteria API ----------
        List<Produit> produitsInformatique = produitService.findByCategorie(nouvelle.getId());
        System.out.println("Produits de la catégorie Informatique (" + produitsInformatique.size() + ") :");
        for (Produit p : produitsInformatique) {
            System.out.println("   " + p);
        }

        System.out.println("Recherche par libellé 'cl' : " + produitService.searchByLibelle("cl"));
        System.out.println("Recherche critères (prix entre 50 et 200) : "
                + produitService.search(null, new BigDecimal("50"), new BigDecimal("200")));

        // ---------- Étape 9 : pagination et héritage JOINED ----------
        System.out.println("Page 1 (3 par page) : " + produitService.findPage(1, 3));
        System.out.println("Page 2 (3 par page) : " + produitService.findPage(2, 3));

        CrudService<User, Long> userService = new CrudService<>(User.class);
        userService.create(new Client("Alice Martin", LocalDate.of(2024, 3, 15)));
        userService.create(new Fournisseur("Béta SARL", "12 rue des Fleurs, Casablanca"));
        System.out.println("Utilisateurs (User est abstrait, JOINED) :");
        for (User u : userService.getAll()) {
            System.out.println("   " + u.getClass().getSimpleName() + " -> " + u);
        }

        // ---------- Étape 10 : N:N avec entité associative et clé composée ----------
        CommandeService commandeService = new CommandeService();
        Produit p1 = produitService.getById(clavier.getId());
        Produit p2 = produitService.getById(souris.getId());
        Commande cmd = new Commande();
        cmd.setDateCommande(LocalDate.now());
        cmd.addProduit(p1, 3);
        cmd.addProduit(p2, 5);
        commandeService.create(cmd);
        System.out.println("Commande créée avec ID = " + cmd.getId());
        cmd.getLignes().forEach(ligne -> System.out.println("   " + ligne));

        // ---------- Étape 11 : transactions et contexte de persistance ----------
        EntityManagerFactory emf = GenericDAO.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Produit produitInvalide = new Produit("Clavier", new BigDecimal("-49.99"), catService.getById(nouvelle.getId()));
            em.persist(produitInvalide);

            if (produitInvalide.getPrix().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Prix négatif");
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.err.println("Transaction annulée : " + e.getMessage());
        } finally {
            em.close();
        }
        System.out.println("Produits en base après le rollback : " + produitService.getAll().size());

        // ---------- Étape 12 : rapport de ventes mensuel (Criteria API) ----------
        RapportVentesService rapportService = new RapportVentesService();
        YearMonth mois = YearMonth.now();
        System.out.println("Chiffre d'affaires par catégorie pour " + mois + " :");
        for (Object[] ligne : rapportService.rapportMensuel(mois)) {
            System.out.println("   " + ligne[0] + " -> " + ligne[1]);
        }

        // ---------- Libération des ressources ----------
        GenericDAO.getEntityManagerFactory().close();
        HibernateUtil.shutdown();
    }
}

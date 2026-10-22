package org.example.dao;

import org.example.entities.Categorie;
import org.example.entities.Produit;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProduitDaoTest {
    private static CrudService<Categorie, Long> catService;
    private static ProduitService prodService;

    @BeforeClass
    public static void init() {
        catService = new CrudService<>(Categorie.class);
        prodService = new ProduitService();
    }

    @Test
    public void testCreateProduitAvecCategorie() {
        Categorie cat = new Categorie("Papeterie");
        catService.create(cat);
        Produit p = new Produit("Stylo", new BigDecimal("1.20"), cat);
        prodService.create(p);
        assertNotNull(p.getId());
        Produit fetched = prodService.findByIdWithCategorie(p.getId());
        assertEquals("Papeterie", fetched.getCategorie().getNom());
    }
}

package code;

import code.Colonne;

public class Table {
     private String nom;
     private Colonne[] colonnes;

     public String getNom() {
          return this.nom;
     }

     public void setNom(String nom) {
          this.nom = nom;
     }

     public Colonne[] getColonnes() {
          return this.colonnes;
     }

     public void setColonnes(Colonne[] colonnes) {
          this.colonnes = colonnes;
     }
}

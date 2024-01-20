package code;

import java.util.HashMap;

import javax.lang.model.type.ReferenceType;

public class Colonne {
     private String nom;
     private String type;
     private Java references_java = new Java();
     private Cs references_cs = new Cs();

     public String getNom() {
          return this.nom;
     }

     public void setNom(String nom) {
          this.nom = nom;
     }

     public String getType() {
          return this.type;
     }

     public void setType(String type, String language) {
          if (language.equals(references_java.getClass().getSimpleName())) {
               String configValue = references_java.getConfig(type);
               if (configValue != null) {
                    this.type = configValue;
               } else {
                    this.type = type;
               }
          } else if (language.equals(references_cs.getClass().getSimpleName())) {
               String configValue = references_cs.getConfig(type);
               if (configValue != null) {
                    this.type = configValue;
               } else {
                    this.type = type;
               }
          } else {
               this.type = type;
          }
     }

     public Colonne() {
     }
}

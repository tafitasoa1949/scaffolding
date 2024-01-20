package code;

import java.sql.*;
import java.text.CollationElementIterator;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import code.Colonne;
import code.Table;

public class Main {
     private Java config_java = new Java();

     public Main() {

     }

     public static Colonne[] getAllColonne(String language, String table, Connection con) throws Exception {
          Colonne[] list_colonne = null;
          try {
               DatabaseMetaData metaData = con.getMetaData();
               ResultSet rs = metaData.getColumns(null, null, table, null);
               int counteur = 0;
               while (rs.next()) {
                    counteur++;
               }
               list_colonne = new Colonne[counteur];
               // System.out.println("count " + counteur);
               rs = metaData.getColumns(null, null, table, null);
               int index = 0;
               while (rs.next()) {
                    Colonne colonne = new Colonne();
                    colonne.setNom(rs.getString("COLUMN_NAME"));
                    colonne.setType(rs.getString("TYPE_NAME"), changeMaj1erLettre(language));
                    ResultSet rsGetForeignKey = metaData.getImportedKeys(null, null, table);
                    boolean isForeignKey = false;
                    while (rsGetForeignKey.next()) {
                         String foreignKeyName = rsGetForeignKey.getString("FKCOLUMN_NAME");
                         if (colonne.getNom().equalsIgnoreCase(foreignKeyName)) {
                              String referencesTable = rsGetForeignKey.getString("PKTABLE_NAME");
                              colonne.setType(changeMaj1erLettre(referencesTable.toString()),
                                        changeMaj1erLettre(language));
                              colonne.setNom(referencesTable);
                              isForeignKey = true;
                              break;
                         }
                    }
                    rsGetForeignKey.close();
                    list_colonne[index] = colonne;
                    // System.out.println("Colonne : " + colonne.getNom() + " Type : " +
                    // colonne.getType());
                    index++;
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return list_colonne;
     }

     public static Table[] getTable(String language, String database, Connection con) throws Exception {
          Table[] list_tables = null;
          try {
               DatabaseMetaData metaData = con.getMetaData();
               ResultSet resultSet = metaData.getTables(null, null, "%", new String[] { "TABLE" });
               int counteur = 0;
               while (resultSet.next()) {
                    counteur++;
               }
               list_tables = new Table[counteur];
               resultSet = metaData.getTables(null, null, "%", new String[] { "TABLE" });
               int index = 0;
               while (resultSet.next()) {
                    Table table = new Table();
                    table.setNom(resultSet.getString("TABLE_NAME"));
                    Colonne[] list_Colonnes = getAllColonne(language, table.getNom(), con);
                    table.setColonnes(list_Colonnes);
                    list_tables[index] = table;
                    index++;
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return list_tables;
     }

     public static String changeMaj1erLettre(String lettre) {
          return lettre.substring(0, 1).toUpperCase() + lettre.substring(1);
     }

     public static String readBodyFile(String nomFichier) throws Exception {
          StringBuilder contenu = new StringBuilder();
          try (BufferedReader reader = new BufferedReader(new FileReader(nomFichier))) {
               String ligne;
               while ((ligne = reader.readLine()) != null) {
                    contenu.append(ligne).append("\n");
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return contenu.toString();
     }

     public static StringBuilder generateAttribut(Colonne[] colonnes) {
          StringBuilder col = new StringBuilder();
          try {
               for (Colonne colonne : colonnes) {
                    col.append("\tprivate ").append(colonne.getType()).append(" ").append(colonne.getNom())
                              .append(";\n");
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return col;
     }

     public static StringBuilder generateGetter(Colonne[] colonnes) {
          StringBuilder getter = new StringBuilder();
          try {
               for (Colonne colonne : colonnes) {
                    getter.append("\tpublic ").append(colonne.getType())
                              .append(" get").append(changeMaj1erLettre(colonne.getNom()))
                              .append("() {\n").append("\t\treturn ").append(colonne.getNom()).append(";\n")
                              .append("\t}\n\n");
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return getter;
     }

     public static StringBuilder generatSetter(Colonne[] colonnes) {
          StringBuilder setter = new StringBuilder();
          try {
               for (Colonne colonne : colonnes) {
                    setter.append("\tpublic void ").append("set").append(changeMaj1erLettre(colonne.getNom()))
                              .append("(").append(colonne.getType()).append(" ").append(colonne.getNom())
                              .append(") {\n").append("\t\tthis.").append(colonne.getNom()).append(" = ")
                              .append(colonne.getNom()).append(";\n").append("\t}\n\n");
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
          return setter;
     }

     public String replaceVariable(String language, String contenu, String nomPackage, Table table)
               throws Exception {
          contenu = contenu.replace("#package#", nomPackage);
          contenu = contenu.replace("#class#", changeMaj1erLettre(table.getNom()));
          contenu = contenu.replace("#colonnes#", generateAttribut(table.getColonnes()));
          contenu = contenu.replace("#getters#", generateGetter(table.getColonnes()));
          contenu = contenu.replace("#setters#", generatSetter(table.getColonnes()));
          if (changeMaj1erLettre(language).equals(this.config_java.getClass().getSimpleName())) {
               contenu = contenu.replace("#package_name#", this.config_java.getPackage_name());
               contenu = contenu.replace("#import_name#", this.config_java.getImport_name());
          }
          return contenu;
     }

     public void generateClass(String language, String nomPackage, Table table, String url, Connection con)
               throws Exception {
          try {
               String nomFichierTemplate = "template/template.templ";
               String cheminPackage = url.replace(".", File.separator) + nomPackage.replace(".", File.separator);
               String nomFichierSortie = cheminPackage + File.separator + changeMaj1erLettre(table.getNom()) + "."
                         + language;
               String contenuTemplate = readBodyFile(nomFichierTemplate);
               String contenuFinal = this.replaceVariable(language, contenuTemplate, nomPackage, table);
               File dossierPackage = new File(cheminPackage);
               if (!dossierPackage.exists()) {
                    dossierPackage.mkdirs();
               }
               try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomFichierSortie))) {
                    writer.write(contenuFinal);
               } catch (Exception e) {
                    e.printStackTrace();
               }
          } catch (Exception e) {
               e.printStackTrace();
          }
     }

     public void scaffolding(String database, String nom_package, String url, String language) throws Exception {
          Connection con = null;
          try {
               con = Connexion.getconnection(database);
               Table[] list_table = getTable(language, database, con);
               for (int i = 0; i < list_table.length; i++) {
                    this.generateClass(language, nom_package, list_table[i], url, con);
               }
          } catch (Exception e) {
               e.printStackTrace();
          } finally {
               if (con != null) {
                    con.close();
               }
          }
     }

     public static void main(String[] args) throws Exception {
          String database = "gestion_stock";
          String nom_package = "projet.generate";
          String url = "D:/ITU/L3/Mr Naina/Framework/Test/";
          String lanquage = "cs";
          new Main().scaffolding(database, nom_package, url, lanquage);
     }
}

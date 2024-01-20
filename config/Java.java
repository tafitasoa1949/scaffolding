package code;

import java.util.HashMap;
import javax.lang.model.type.ReferenceType;

public class Java {
     private String package_name = "package";
     private String import_name = "import";
     private HashMap<String, String> config = new HashMap<>();

     public String getPackage_name() {
          return this.package_name;
     }

     public String getImport_name() {
          return this.import_name;
     }

     public String getConfig(String typeKey) {
          return config.get(typeKey);
     }

     public Java() {
          this.config.put("varchar", "String");
          this.config.put("timestamp", "java.sql.Timestamp");
          this.config.put("int4", "int");
          this.config.put("serial", "int");
          this.config.put("numeric", "Double");
          this.config.put("double", "Double");
     }
}

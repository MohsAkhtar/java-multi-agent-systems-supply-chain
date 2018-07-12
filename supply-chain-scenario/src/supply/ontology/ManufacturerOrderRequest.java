package supply.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ManufacturerOrderRequest
* @author ontology bean generator
* @version 2017/11/28, 20:55:56
*/
public class ManufacturerOrderRequest implements AgentAction {

   /**
* Protege name: forManufacturer
   */
   private AID forManufacturer;
   public void setForManufacturer(AID value) { 
    this.forManufacturer=value;
   }
   public AID getForManufacturer() {
     return this.forManufacturer;
   }

}

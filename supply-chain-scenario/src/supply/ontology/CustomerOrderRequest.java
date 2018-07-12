package supply.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CustomerOrderRequest
* @author ontology bean generator
* @version 2017/11/28, 20:55:56
*/
public class CustomerOrderRequest implements AgentAction {

   /**
* Protege name: forCustomer
   */
   private AID forCustomer;
   public void setForCustomer(AID value) { 
    this.forCustomer=value;
   }
   public AID getForCustomer() {
     return this.forCustomer;
   }

}

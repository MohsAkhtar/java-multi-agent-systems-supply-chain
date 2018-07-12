package supply.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: StockAvailability
* @author ontology bean generator
* @version 2017/11/28, 20:55:56
*/
public class StockAvailability implements Concept {

   /**
* Protege name: component
   */
   private Component component;
   public void setComponent(Component value) { 
    this.component=value;
   }
   public Component getComponent() {
     return this.component;
   }

   /**
* Protege name: quantity
   */
   private int quantity;
   public void setQuantity(int value) { 
    this.quantity=value;
   }
   public int getQuantity() {
     return this.quantity;
   }

   /**
* Protege name: cost
   */
   private int cost;
   public void setCost(int value) { 
    this.cost=value;
   }
   public int getCost() {
     return this.cost;
   }

}

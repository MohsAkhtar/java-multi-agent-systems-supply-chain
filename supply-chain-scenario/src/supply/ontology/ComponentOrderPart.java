package supply.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: ComponentOrderPart
* @author ontology bean generator
* @version 2017/11/28, 20:55:56
*/
public class ComponentOrderPart implements Concept {

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

}

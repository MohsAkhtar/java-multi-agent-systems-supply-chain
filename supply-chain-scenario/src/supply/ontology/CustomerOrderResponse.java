package supply.ontology;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: CustomerOrderResponse
* @author ontology bean generator
* @version 2017/11/28, 20:55:56
*/
public class CustomerOrderResponse implements Concept {

   /**
* Protege name: customerOrderLine
   */
   private List customerOrderLine = new ArrayList();
   public void addCustomerOrderLine(ComponentOrderPart elem) { 
     List oldList = this.customerOrderLine;
     customerOrderLine.add(elem);
   }
   public boolean removeCustomerOrderLine(ComponentOrderPart elem) {
     List oldList = this.customerOrderLine;
     boolean result = customerOrderLine.remove(elem);
     return result;
   }
   public void clearAllCustomerOrderLine() {
     List oldList = this.customerOrderLine;
     customerOrderLine.clear();
   }
   public Iterator getAllCustomerOrderLine() {return customerOrderLine.iterator(); }
   public List getCustomerOrderLine() {return customerOrderLine; }
   public void setCustomerOrderLine(List l) {customerOrderLine = l; }

}

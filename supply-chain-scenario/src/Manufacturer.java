import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class Manufacturer extends Agent{
	
	// String to place order in
	private String manufacturerOrder;
	// The list of known supplier agents
	private AID[] supplierAgents;
	//  items currently in warehouse stock
	private ComponentList catalogue;
	private int day, stock;
	boolean orderStock;
	String stockToOrder;
	// manufacturers balance
	private double balance;
	
	// Parameters
	int w = 15;
	
	@Override
    protected void setup() {

        // Printout a welcome message
		System.out.println("Hello! Manufacturer "+getAID().getName()+" is ready");

		// Create the catalogue

		// CSV file passed as argument for what items manufacturer has to stock
				Object[] args = getArguments();
				if (args != null && args.length > 0) {
					String csvFilePath = (String)args[0];
					catalogue = new ComponentList(csvFilePath);
					
				} else {
					System.err.println("No file name presented");
					// agent is deleted
					doDelete();
				}
				
				// TickerBehaviour to display total profit and day
 				addBehaviour(new TickerBehaviour(this, 5000) {
 					protected void onTick() {
 						// Calculates how much manufacturer has to pay warehouse penalty per day, and subtracts from balance
 						stock = catalogue.getWarehouseStock();
 						int penalty = stock*w;
 						balance -= penalty;
 						
 						// terminates agent if day has reached 90
 						if (day == 90) {
 							System.out.println("\n======= Day " + day + ", Total profit: " + balance+" ========\n");
 							day = 90;
 							// termination of agent
 							System.out.println(getAID().getName() + " terminating.");
							doDelete();
 						}else {
 							day += 1;
 	 						System.out.println("\nDay " + day + ", Warehouse Penalty: £" + penalty +", Total profit: £" + balance);
 	 						System.out.println("Current Stock Is: " + stock);
 						}
 					}
 				} );
 		 			
 					
 		 				
 		 				// Add a TickerBehaviour that schedules a request to supplier
 		 				addBehaviour(new TickerBehaviour(this, 2500) {
 		 					protected void onTick() {
 		 						if(orderStock == true) {
 		 						manufacturerOrder = stockToOrder;
 		 	 		 			System.out.println("\n"+getAID().getName() + ") Order is: '" + manufacturerOrder + "'");
 		 						// Updates supplier agets list
 		 						DFAgentDescription temp = new DFAgentDescription();
 		 						ServiceDescription sd = new ServiceDescription();
 		 						sd.setType("supplier");
 		 						temp.addServices(sd);
 		 						try {
 		 							DFAgentDescription[] res = DFService.search(myAgent, temp); 
 		 							System.out.println(getAID().getName() + ") Found the following " + res.length + " supplier agents:");
 		 							supplierAgents = new AID[res.length];
 		 							for (int i = 0; i < res.length; ++i) {
 		 								supplierAgents[i] = res[i].getName();
 		 								System.out.println(supplierAgents[i].getName());
 		 							}
 		 						}
 		 						catch (FIPAException fe) {
 		 							fe.printStackTrace();
 		 						}

 		 						// Perform the request
 		 						myAgent.addBehaviour(new RequestManufacturerOrder());
 		 						orderStock = false;
 		 						}
 		 					}
 		 				} );
 				
     // Register this Supplier service in the Yellow Pages
     		DFAgentDescription dfd = new DFAgentDescription();
     		dfd.setName(getAID());
     		ServiceDescription sd = new ServiceDescription();
     		sd.setType("manufacturer");
     		sd.setName("JADE-manufacturer");
     		dfd.addServices(sd);
     		try {
     			DFService.register(this, dfd);
     		}
     		catch (FIPAException fe) {
     			fe.printStackTrace();
     		}
     		
     	// behaviour dealing with customer queries
    		addBehaviour(new OrderRequestCustomer());

    		// behaviour dealing with customer orders
    		addBehaviour(new OrderPurchasedCustomer());
	}
	
		protected void takeDown() {
			// agent deregistered
			try {
				DFService.deregister(this);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			System.out.println("Manufacturer "+getAID().getName()+" terminating");
		}
		
		// Behaviour which deals with processing orders from customers
		private class OrderRequestCustomer extends CyclicBehaviour {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// recieved and processing cfp msg
					String title = msg.getContent();
					ACLMessage reply = msg.createReply();
					
					ComponentList order = new ComponentList(title, false);
					double price = -1.0;
					if (catalogue.contains(order)) {
						ComponentList orderPrice = catalogue.getOrderPrice(order);
						price = orderPrice.getPrice();
						
					}
					if (price >= 0.0) {
						// Order is in catalogue and can be fulfilled, price is sent as reply
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(String.valueOf(price));
						balance += price;
					}
					else {
						// order is not in catalogue so cannot be fulfilled
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
						// order stock from supplier
						orderStock = true;
						stockToOrder = title;
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		}

		// class which deals with completing orders from customer
		
		private class OrderPurchasedCustomer extends CyclicBehaviour {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// ACCEPT_PROPOSAL received from customer
					String title = msg.getContent();
					ACLMessage reply = msg.createReply();

					ComponentList order = new ComponentList(title, false);
					
					double price = -1.0;
					if (catalogue.contains(order)) {  
						// the order is available, remove items from stock
						catalogue.subtract(order);   
						
						reply.setPerformative(ACLMessage.INFORM); // inform customer
						System.out.println("Order: '" + title + "' sold to agent " 
						  + msg.getSender().getName()
						  + " total profit £" + balance);
					}	else {
						// items requested not available
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		}  
		
		// Behaviour that gets manufacturer to ask suppliers to fulfil an order

		private class RequestManufacturerOrder extends Behaviour {
			private AID supplier;     // supplier agent who will fulfil order 
			private double supplierBestPrice;   // the suppliers price for the order
			private int numberReplies = 0; // Number of replies from Supplier agents
			private MessageTemplate mt; 
			private int step = 0;

			public void action() {
				switch (step) {
				case 0:
					// Sends cfp to suppliers
					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
					for (int i = 0; i < supplierAgents.length; ++i) {
						cfp.addReceiver(supplierAgents[i]);
					} 
					cfp.setContent(manufacturerOrder);
					cfp.setConversationId("manufacturer-order");
					cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
					myAgent.send(cfp);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("manufacturer-order"),
							MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
					step = 1;
					break;
				case 1:
					// Receive proposals/refusals
					ACLMessage reply = myAgent.receive(mt);
					if (reply != null) {
						// replys
						if (reply.getPerformative() == ACLMessage.PROPOSE) {
							// offer from supplier
							double price = Double.parseDouble(reply.getContent());
							if (supplier == null || price < supplierBestPrice) {
								supplierBestPrice = price;
								supplier = reply.getSender();
							}
						}
						numberReplies++;
						if (numberReplies >= supplierAgents.length) {
							// replies received
							step = 2; 
						}
					}
					else {
						block();
					}
					break;
				case 2:
					// accept purchase offer
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(supplier);
					order.setContent(manufacturerOrder);
					order.setConversationId("manufacturer-order");
					order.setReplyWith("order" + System.currentTimeMillis());
					myAgent.send(order);
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("manufacturer-order"),
							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 3;
					break;
				case 3:  
					
					// reply of purchase order
					reply = myAgent.receive(mt);
					if (reply != null) {
						if (reply.getPerformative() == ACLMessage.INFORM) {
							// successful order
							System.out.println("\n"+getAID().getName() + ") '" + manufacturerOrder + "' successfully bought from agent " + reply.getSender().getName());
							System.out.println("Loss of £" + supplierBestPrice);
							ComponentList orderManufacturer = new ComponentList(manufacturerOrder, false);
							catalogue.add(orderManufacturer);
							balance -= supplierBestPrice;
						}
						else {
							System.out.println("\n"+getAID().getName() + ") Attempt failed: Could not fulfil order '" + manufacturerOrder + "'");
						}

						step = 4;
					}
					else {
						block();
					}
					break;
				}        
			}

			public boolean done() {
				if (step == 2 && supplier == null) {
					System.out.println("Attempt failed: '" + manufacturerOrder + "' could not be fulfilled");
				}
				return ((step == 2 && supplier == null) || step == 4);
			}
		}
		
}

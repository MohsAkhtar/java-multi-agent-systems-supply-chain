import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.*;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import supply.ontology.*;

public class Customer extends Agent{
	
	// Order string for customer t pass through messages
	private String customerOrder;
	// The list of manufacturer agents (only one)
	private AID[] manufacturerAgents;
	// Checks if customer has to order a new order
	boolean newOrder = true;
	int day;
 	protected void setup() {

 		System.out.println("\nCustomer agent " + getAID().getName() + " is starting.");
 		
 		// Read null as a start-up argument
 		Object[] args = getArguments();
 		if (args == null) {
 			float min = 0;
 	 		float max = 1;
 	 		
 				
 			// TickerBehaviour which requests to manufacturer whether order can be fulfilled
 			addBehaviour(new TickerBehaviour(this, 2500) {
 					protected void onTick() {
 						if(newOrder == true) {
 				 	 		Random rn = new Random();
 				 	        
 				 	 		// Setup quantity randomly between 1-50
 				 	        int qty = rn.nextInt(50);
 				 	 	    float randomOrder = rn.nextFloat() * (max - min) + min;
 				
 				 			customerOrder = (String) orderCustomer(randomOrder, qty);
 				 			System.out.println(getAID().getName() + ") Customer order is: '" + customerOrder + "'");
 			 	 		}
 						System.out.println("\n"+getAID().getName() + ") Is ordering: '" + customerOrder.toString() + "'");
 						// Update whether orders can be fulfilled
 						DFAgentDescription temp = new DFAgentDescription();
 						ServiceDescription sd = new ServiceDescription();
 						sd.setType("manufacturer");
 						temp.addServices(sd);
 						try {
 							DFAgentDescription[] res = DFService.search(myAgent, temp); 
 							manufacturerAgents = new AID[res.length];
 							for (int i = 0; i < res.length; ++i) {
 								manufacturerAgents[i] = res[i].getName();
 								System.out.println(manufacturerAgents[i].getName());
 							}
 						}
 						catch (FIPAException fe) {
 							fe.printStackTrace();
 						}

 						// Perform the request
 						myAgent.addBehaviour(new RequestCustomerOrder());
 					}
 				} );
 		// TickerBehaviour to iterate day
				addBehaviour(new TickerBehaviour(this, 5000) {
					protected void onTick() {
						if (day == 90) {
							// termination of agent
							System.out.println(getAID().getName() + " terminating.");
							doDelete();
						}else {
							day += 1;
						}
					}
				} );
 		}
		else {
			// termination of agent
			System.out.println(getAID().getName() + " terminating.");
			doDelete();
		}
 	}
 	 	
 	protected void takeDown() {
 		System.out.println("Customer "+getAID().getName()+" terminating");
 	}
 	
 	// Randomises customer computer specification and quantity for each order
 	public String orderCustomer(float rand, int qty) {

		String cpu, motherboard, memory, hardrive;
		if (rand<0.5) {
			cpu = "Mintel CPU"+","+qty+",0.0;";
			motherboard = "Mintel Motherboard"+","+qty+",0.0;";
			memory = "RAM 4Gb"+","+qty+",0.0;";
			hardrive = "HDD 1TB"+","+qty+",0.0;";
		} else {
			cpu = "IMD CPU"+","+qty+",0.0;";
			motherboard = "IMD CPU"+","+qty+",0.0;";
			memory = "RAM 16Gb"+","+qty+",0.0;";
			hardrive = "HDD 2TB"+","+qty+",0.0;";
		}
		String[] order = {cpu+motherboard+memory+hardrive};
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < order.length; i++) {
		   s.append(order[i]);
		}
		String orderToString = s.toString();
		return orderToString;
	}
 	
 	
 // Behaviour that gets customer to ask manufacturer to fulfil an order

	private class RequestCustomerOrder extends Behaviour {
		private AID manufacturer;     // manufacturer agent to send orders too
		private double orderPrice;   // price of order
		private int numberReplies = 0; // total replies form manufacturer
		private MessageTemplate mt; 
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Sends cfp to manufacturer
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < manufacturerAgents.length; ++i) {
					cfp.addReceiver(manufacturerAgents[i]);
				} 
				cfp.setContent(customerOrder);
				cfp.setConversationId("customer-order");
				cfp.setReplyWith("cfp"+System.currentTimeMillis());
				myAgent.send(cfp);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("customer-order"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// receive all proposals and refusals
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// reply
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// The offer from manufacturer
						double price = Double.parseDouble(reply.getContent());
						if (manufacturer == null || price < orderPrice) {
							orderPrice = price;
							manufacturer = reply.getSender();
						}
					}
					numberReplies++;
					if (numberReplies >= manufacturerAgents.length) {
						// checks all replies received
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// sends accept proposal to manufacturer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(manufacturer);
				order.setContent(customerOrder);
				order.setConversationId("customer-order");
				order.setReplyWith("order" + System.currentTimeMillis());
				myAgent.send(order);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("customer-order"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// order purchase reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// if order successful print
						System.out.println(getAID().getName() + ") '" + customerOrder + "' successfully bought from agent " + reply.getSender().getName());
						System.out.println("Price = £" + orderPrice);
						// order satisfied customer can now request new order
						newOrder = true;
					}
					else {
						System.out.println(getAID().getName() + ") Attempt failed: Could not buy '" + customerOrder + "'");
						newOrder = false;
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
			if (step == 2 && manufacturer == null) {
				System.out.println("Attempt failed: '" + customerOrder + "' could not be fulfilled");
			}
			return ((step == 2 && manufacturer == null) || step == 4);
		}
	}
 	
	}



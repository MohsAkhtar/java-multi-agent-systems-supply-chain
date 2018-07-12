import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Supplier extends Agent{

	private ComponentList catalogue;

	
	@Override
    protected void setup() {

        // print welcome 
		System.out.println("\nHello! Supplier "+getAID().getName()+" is ready");

		// CSV file containing stock and prices is passed as argument
				Object[] args = getArguments();
				if (args != null && args.length > 0) {
					String csvFilePath = (String)args[0];
					catalogue = new ComponentList(csvFilePath);
				} else {
					System.err.println("No csv file passed as argument");
					doDelete();
				}
				
				
     // Register in yellow pagess supplier
     		DFAgentDescription dfd = new DFAgentDescription();
     		dfd.setName(getAID());
     		ServiceDescription sd = new ServiceDescription();
     		sd.setType("supplier");
     		sd.setName("JADE-supplier");
     		dfd.addServices(sd);
     		try {
     			DFService.register(this, dfd);
     		}
     		catch (FIPAException fe) {
     			fe.printStackTrace();
     		}
     		
     	// behaviour for manufacturer order reuests
    		addBehaviour(new OrderRequestManufacturer());

    		// behaviour for fulfilling order
    		addBehaviour(new OrderPurchaseManufacturer());
		}
	
		protected void takeDown() {
			// termination message
			System.out.println("Supplier "+getAID().getName()+" terminating");
		}
		
		
		// Behaviour which deals with processing orders from manufacturers
		private class OrderRequestManufacturer extends CyclicBehaviour {
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
					}
					else {
						// order is not in catalogue so cannot be fulfilled
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				}
				else {
					block();
				}
			}
		}  
		
		// class which deals with completing orders from manufacturer
		private class OrderPurchaseManufacturer extends CyclicBehaviour {
			public void action() {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// ACCEPT_PROPOSAL received from manufacturer
					String title = msg.getContent();
					ACLMessage reply = msg.createReply();

					ComponentList order = new ComponentList(title, false);
					double price = -1.0;
					if (catalogue.contains(order)) {  
						// the order is available, remove items from stock
						catalogue.subtract(order);   
						reply.setPerformative(ACLMessage.INFORM); 
						System.out.println("\n"+getAID().getName() + ") '" + title + "' sold to agent " 
						  + msg.getSender().getName());
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
		
}

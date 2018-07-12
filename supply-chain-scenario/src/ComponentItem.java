

public class ComponentItem {
	private String name;
	private double priceComponent;
	private int quantity;
	
	public ComponentItem(String n, double p, int q) {
		name = n;
		priceComponent = p;
		quantity = q;
	}
	
	public String getName() {
		return name;
	}
		
	public int getQuantity() {
		return quantity;
	}
	
	public double getPriceComponent() {
		return priceComponent;
	}
	
	public void setQuantity(int q) {
		quantity = q;
	}
	
	public double getPrice() {
		return priceComponent * quantity;
	}
}

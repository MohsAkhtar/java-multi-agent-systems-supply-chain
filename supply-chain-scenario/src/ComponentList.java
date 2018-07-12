

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ComponentList {
	private List<ComponentItem> componentList;
	private static final int numberItemsRow = 2;
	
	public ComponentList(final List<ComponentItem> c) {
		componentList = c;
	}
	
	public ComponentList(String csvFile) {
		List<List<String>> grid = csvFileRead(csvFile);
		componentList = stringGridToComponentlist(grid);
	}
	
	public ComponentList(String input, boolean distinguishFromOtherConstructor) {
		List<List<String>> grid = stringRead(input);
		componentList = stringGridToComponentlist(grid);
	}
		
	public List<ComponentItem> getComponentLists() {
		return componentList;
	}
	
	public double getPrice() {
		double price = 0.0;
		for (ComponentItem componentOrder: componentList) {
			price  += componentOrder.getPrice();
		}
		return price;
	}
	
	public int getWarehouseStock() {
		int warehouseStock = 0;
		for (ComponentItem part: componentList) {
			warehouseStock += part.getQuantity();
		}
		return warehouseStock;
	}
	
	
// Reads in csv file and converst to a 2d string list
	static List<List<String>> csvFileRead(String csvFile) {
		List<List<String> > grid = new ArrayList<List<String> >();
		int numberLine = 0;
		try {
			BufferedReader csvInput = new BufferedReader(new FileReader(new File(csvFile)));
			try {
				String line;
				while ((line = csvInput.readLine()) != null) {
					List<String> row = new ArrayList<String>();
					StringTokenizer tk = new StringTokenizer(line, ",");
					while (tk.hasMoreTokens())  {
						row.add(tk.nextToken().trim());
					}
					if (row.size() < numberItemsRow) {
						System.err.println(csvFile + ":" + (numberLine+1) + " has less than " + numberItemsRow + " components: " + line);
					}
					grid.add(row);
					++numberLine;
				}
			} 
			finally {
				csvInput.close();
			}
		}
		catch (IOException e) {
			System.err.println("Error opening " + csvFile + ": " + e);
		}
		return grid;
	}

	// reads string which is semi-colon seperated and comma seperated into 2d list
	private static List<List<String>> stringRead(String input) {
		List<List<String> > grid = new ArrayList<List<String> >();
		int numberLine = 0;
	
		StringTokenizer tkSemiColon = new StringTokenizer(input, ";");
		while (tkSemiColon.hasMoreTokens())  {
			String line = tkSemiColon.nextToken().trim();
			List<String> row = new ArrayList<String>();
			StringTokenizer tkComma = new StringTokenizer(line, ",");
			while (tkComma.hasMoreTokens())  {
				row.add(tkComma.nextToken().trim());
			}
			if (row.size() < numberItemsRow) {
				System.err.println(" " + (numberLine+1) + " has less than " + numberItemsRow + " components: " + line);
			}
			grid.add(row);
			++numberLine;
		}
		return grid;
	}
	
	
	private static List<ComponentItem> stringGridToComponentlist(List<List<String>> grid) {
		List<ComponentItem> componentLists = new ArrayList<ComponentItem>();
		for (List<String> row: grid) {
			assert(row.size() >= numberItemsRow);
			try {
				String name = row.get(0);
				int qty = Integer.parseInt(row.get(1));
				double price = 0.0;
				if (row.size() > 2) {
					price = Double.parseDouble(row.get(2));
				}
				ComponentItem component = new ComponentItem(name, price, qty);
				componentLists.add(component);
			} catch (Exception e) {
				System.err.println("Error parsing string:" + e);
			}
		}
		return componentLists;
	}
	
	// checks if component is in catalgue
	public ComponentItem getComponentName(String name) {
		for (ComponentItem p: componentList) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}
	
	// checks if component is in other list (for more than one agent supplier)
	public boolean contains(final ComponentList otherList) {
		for (ComponentItem otherComponent: otherList.getComponentLists()) {
			ComponentItem thisComponent = getComponentName(otherComponent.getName());
			if (thisComponent == null) {
				return false;
			} else if (thisComponent.getQuantity() < otherComponent.getQuantity()) {
				return false;
			}
		}
		return true;
	}
	
	// removes item from list
	public void subtract(final ComponentList otherList) {
		for (ComponentItem otherPart: otherList.getComponentLists()) {
			ComponentItem thisPart = getComponentName(otherPart.getName());
			// Must be guaranteed by a check against contains before calling this function
			assert(thisPart != null && thisPart.getQuantity() >= otherPart.getQuantity());
			thisPart.setQuantity(thisPart.getQuantity() - otherPart.getQuantity());
		}
	}
	
	// adds component t catalogue
	public void add(final ComponentList otherList) {
		for (ComponentItem otherComponent: otherList.getComponentLists()) {
			ComponentItem thisComponent = getComponentName(otherComponent.getName());
			// Must be guaranteed by a check against contains before calling this function
			//assert(thisPart != null && thisPart.getNumber() >= otherPart.getNumber());
			thisComponent.setQuantity(thisComponent.getQuantity() + otherComponent.getQuantity());
		}
	}
	
	// calculates total price of an order
	public ComponentList getOrderPrice(final ComponentList order) {
		List<ComponentItem> componentListPrice = new ArrayList<ComponentItem>();
		for (ComponentItem orderComponent: order.getComponentLists()) {
			ComponentItem thisComponent = getComponentName(orderComponent.getName());
			assert(thisComponent != null && thisComponent.getQuantity() >= orderComponent.getQuantity());
			ComponentItem componentPrice = new ComponentItem(orderComponent.getName(), thisComponent.getPriceComponent(), orderComponent.getQuantity());
			componentListPrice.add(componentPrice);
		}
		return new ComponentList(componentListPrice);
	}
	
	
	public String getString() {
		String componentListString = "";
		for (ComponentItem component: getComponentLists()) {
			String componentString = component.getName() + "," + component.getQuantity() + "," + component.getPriceComponent();
			componentListString += componentString + ";";
		}
		return componentListString;
	}
	
	
}

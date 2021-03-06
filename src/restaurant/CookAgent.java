package restaurant;

import agent.Agent;
import java.util.*;
import restaurant.gui.CookGui;


/**
 * Restaurant Cook Agent
 */

public class CookAgent extends Agent {
	public List<Order> orders
	= new ArrayList<Order>();
	public List<MyMarket> markets = new ArrayList<MyMarket>();
	public List<ItemOrder> itemOrders = new ArrayList<ItemOrder>();

	private HostAgent host;
	private String name;
	private Timer timer = new Timer();
	private boolean orderedItems;
	private CookGui cookGui;
	
	Food steak = new Food("steak", 15, 3, 1, 1);
	Food chicken = new Food("chicken", 20, 3, 1, 1);
	Food salad = new Food("salad", 5, 3, 2, 1);
	Food pizza = new Food("pizza", 10, 3, 3, 1);
	
	Map<String, Food> foods = new HashMap<String, Food>();
	
	public enum OrderState
	{Pending, Cooking, Done, Finished};
	public enum FoodState
	{Enough, MustBeOrdered, Ordered, WaitingForOrder, ReceivedOrder};

	 /**
	 * Constructor
	 *
	 * @param name Agent name for messages
	 */
	public CookAgent(String name) {
		super();
		this.name = name;
		orderedItems = false;
		
		foods.put("steak", steak);
		foods.put("chicken", chicken);
		foods.put("salad", salad);
		foods.put("pizza", pizza);
	}
	
	/**
	 * Returns the Agent's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Hack to establish connection to HostAgent.
	 * 
	 * @param h Reference to HostAgent
	 */
	public void setHost(HostAgent h) {
		host = h;
	}
	
	/**
	 * Assigns a GUI to Agent
	 * 
	 * @param g Reference to CookGui
	 */
	public void setGui(CookGui g) {
		cookGui = g;
	}
	
	/**
	 * Hack to establish connection with MarketAgents
	 * 
	 * @param m Reference to MarketAgent
	 */
	public void addMarket(MarketAgent m) {
		markets.add(new MyMarket(m));
	}
	
	// Messages
	
	/**
	 * Tells the CookAgent to produce an order
	 * 
	 * @param waiter Reference to WaiterAgent delivering the order
	 * @param choice String representing the customer's choice
	 * @param table Number of the table assigned to the customer
	 */
	public void msgHereIsOrder(WaiterAgent waiter, String choice, int table) {
		orders.add(new Order(waiter, choice, table, OrderState.Pending));
		stateChanged();
	}
	
	/**
	 * Tells the CookAgent how much of an order can be fulfilled so he knows whether to order from another MarketAgent
	 * 
	 * @param orders List of ItemOrders that can be fulfilled
	 */
	public void msgHereIsWhatICanFulfill(List<ItemOrder> orders) {
		for (Food f : foods.values()) {
			if (f.state == FoodState.Ordered) {
				f.state = FoodState.MustBeOrdered;
			}
		}
		for (ItemOrder o : orders) {
			foods.get(o.getFood()).setState(FoodState.WaitingForOrder);
		}
		stateChanged();
	}
	
	/**
	 * Tells the cook that an order has arrived from the market
	 * 
	 * @param orders List of ItemOrders that have arrived
	 */
	public void msgOrderDelivered(List<ItemOrder> orders) {
		List<ItemOrder> temp = new ArrayList<ItemOrder>();
		for (ItemOrder o : orders) {
			temp.add(o);
		}
		for (ItemOrder o : temp) {
			foods.get(o.getFood()).amount += o.getAmount();
			print(o.getFood() + " inventory: " + foods.get(o.getFood()).amount);
			foods.get(o.getFood()).state = FoodState.ReceivedOrder;
		}
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		try {
			if (orderedItems == false) {
				orderedItems = true;
				orderFoodFromMarket();
				return true;
			}
			for (Food food : foods.values()) {
				if (food.getState() == FoodState.ReceivedOrder) {
					addFood(food);
					return true;
				}
			}
			for (Food food : foods.values()) {
				if (food.getState() == FoodState.MustBeOrdered) {
					orderFoodFromMarket();
					return true;
				}
			}
			for (Order order : orders) {
				if (order.getState() == OrderState.Done) {
					plateIt(order);
					return true;
				}
			}
			for (Order order : orders) {
				if (order.getState() == OrderState.Pending) {
					cookIt(order);
					return true;
				}
			}
		} catch (ConcurrentModificationException e) {
			return false;
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	/**
	 * Starts cooking an order. If inventory of that specific food is empty, notify the waiter.
	 * 
	 * @param o Reference to Order
	 */
	private void cookIt(Order o) {
		if (foods.get(o.choice).getAmount() == 0) {
			print("We're out of " + o.choice);
			o.waiter.msgOutOfFood(o.choice, o.table);
			o.setState(OrderState.Finished);
			return;
		}
		o.setState(OrderState.Cooking);
		cookGui.DoCookFood(o.choice);
		timer.schedule(new CookingTimerTask(o) {
			@Override
			public void run() {
				order.setState(OrderState.Done);
				stateChanged();
			}
		},
		foods.get(o.choice).getCookingTime() * 1000);
		foods.get(o.choice).setAmount(foods.get(o.choice).getAmount()-1);
		print(foods.get(o.choice).type + " inventory: " + foods.get(o.choice).amount);
		if (foods.get(o.choice).amount <= foods.get(o.choice).low && foods.get(o.choice).state == FoodState.Enough) {
			foods.get(o.choice).setState(FoodState.MustBeOrdered);
		}
	}
	
	/**
	 * Takes a finished order and puts it on the plating area and notifies the waiter
	 * 
	 * @param o Reference to Order
	 */
	private void plateIt(Order o) {
		print(o.choice + " is done");
		cookGui.DoPlateFood(o.choice);
		o.getWaiter().msgOrderDone(o.getChoice(), o.getTable());
		o.setState(OrderState.Finished);
	}
	
	/**
	 * Checks the inventory for any items that are low and orders those items from the market
	 */
	private void orderFoodFromMarket() {
		for (Food food : foods.values()) {
			if ((food.getState() == FoodState.MustBeOrdered || food.getState() == FoodState.Enough) && food.amount <= food.low) {
				itemOrders.add(new ItemOrder(food.type, food.capacity - food.amount));
				food.setState(FoodState.Ordered);
			}
		}
		int index = markets.size()-1;
		if (markets.size() > 1) {
			for (int i = markets.size()-2; i>=0; i--) {
				if (markets.get(i).orderedFrom <= markets.get(index).orderedFrom) {
						index = i;
				}
			}
		}
		print("I am ordering from " + markets.get(index).market.getName());
		for (ItemOrder io : itemOrders) {
			print("I need " + io.getAmount() + " " + io.getFood() + "s");
		}
		markets.get(index).market.msgHereIsOrder(itemOrders);
		markets.get(index).incrementOrderedFrom();
		itemOrders.clear();
	}
	
	/**
	 * Notifies the host that a food order has been received
	 * 
	 * @param f Reference to Food
	 */
	private void addFood(Food f) {
		for (Food food : foods.values()) {
			if (food == f) {
				food.setState(FoodState.Enough);
				host.msgReceivedOrder(food.type);
			}
		}
	}
	

	//Inner classes
	
	/**
	 * Contains all information about a Market relevant to the CookAgent
	 */
	private class MyMarket {
		MarketAgent market;
		int orderedFrom;
		
		MyMarket(MarketAgent m) {
			market = m;
			orderedFrom = 0;
		}
		
		public void incrementOrderedFrom() {
			orderedFrom++;
		}
	}

	/**
	 * Contains all information about a customer's order relevant to the CookAgent
	 */
	private class Order {
		WaiterAgent waiter;
		int table;
		private OrderState state;
		String choice;

		Order(WaiterAgent w, String c, int t, OrderState s) {
			waiter = w;
			choice = c;
			table = t;
			state = s;
		}

		WaiterAgent getWaiter() {
			return waiter;
		}
		
		public int getTable() {
			return table;
		}
		
		OrderState getState() {
			return state;
		}
		
		void setState(OrderState s) {
			state = s;
		}
		
		String getChoice() {
			return choice;
		}
	}
	
	/**
	 * Contains all information about food relevant to the CookAgent
	 */
	private class Food {
		String type;
		int cookingTime;
		int amount;
		int low;
		int capacity;
		FoodState state;
		
		Food(String t, int c, int cap, int a, int l) {
			type = t;
			cookingTime = c;
			amount = a;
			capacity = cap;
			low = l;
			state = FoodState.Enough;
		}
		
		int getCookingTime() {
			return cookingTime;
		}
		
		void setAmount(int a) {
			amount = a;
		}
		
		int getAmount() {
			return amount;
		}
		
		FoodState getState() {
			return state;
		}
		
		void setState(FoodState s) {
			state = s;
		}
	}
	
	/**
	 * TimerTask necessary for the order timer
	 */
	private class CookingTimerTask extends TimerTask {
		Order order;
		
		CookingTimerTask(Order o) {
			order = o;
		}
		
		public void run() {};
	}
}


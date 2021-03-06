package restaurant;

import agent.Agent;
import restaurant.interfaces.Market;

import java.util.*;


/**
 * Restaurant Market Agent
 */

public class MarketAgent extends Agent implements Market {
	public List<Order> orders = Collections.synchronizedList(new ArrayList<Order>());

	private String name;
	private CookAgent cook;
	private CashierAgent cashier;
	private Timer timer = new Timer();
	private int cash;
	
	Food steak;
	Food chicken;
	Food salad;
	Food pizza;
	
	Map<String, Food> foods = new HashMap<String, Food>();
	Map<String, Integer> foodPrices = new HashMap<String, Integer>();
	
	public enum OrderState
	{Received, ProducingOrder, Ready, Finished};
	
	/**
	 * Constructor
	 *
	 * @param name Agent name for messages
	 * @param c Reference to CookAgent
	 * @param ca Reference to CashierAgent
	 * @param stAmt Amount of steak in inventory
	 * @param cAmt Amount of chicken in inventory
	 * @param saAmt Amount of salad in inventory
	 * @param pAmt Amount of pizza in inventory
	 */
	public MarketAgent(String name, CookAgent c, CashierAgent ca, int stAmt, int cAmt, int saAmt, int pAmt) {
		super();
		this.name = name;
		cook = c;
		cashier = ca;
		cash = 1000;
		
		steak = new Food("steak", 10, stAmt);
		chicken = new Food("chicken", 10, cAmt);
		salad = new Food("salad", 10, saAmt);
		pizza = new Food("pizza", 10, pAmt);
		
		foods.put(steak.type, steak);
		foods.put(chicken.type, chicken);
		foods.put(salad.type, salad);
		foods.put(pizza.type, pizza);
		
		foodPrices.put("steak", 12);
		foodPrices.put("chicken", 9);
		foodPrices.put("salad", 4);
		foodPrices.put("pizza", 6);
	}
	
	/**
	 * Returns the Agent's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the integer total time to produce an order of one or more items
	 * 
	 * @param o Reference to Order
	 */
	public int timeToProduceOrder(Order o) {
		int time = 0;
		for (ItemOrder io : o.items) {
			time += foods.get(io.getFood()).timeToProduce*io.getAmount();
		}
		return time*400;
	}
	
	// Messages
	
	/**
	 * Adds an order to the market's list of orders (removing any parts it cannot fulfill)
	 * 
	 * @param io List of ItemOrders
	 */
	public void msgHereIsOrder(List<ItemOrder> io) {
		List<ItemOrder> temp = new ArrayList<ItemOrder>();
		for (ItemOrder o : io) {
			if (foods.get(o.getFood()).amount >= o.getAmount()) {
				temp.add(o);
			}
		}
		orders.add(new Order(temp, OrderState.Received));
		stateChanged();
	}
	
	/**
	 * Adds payment to the market's total cash
	 * 
	 * @param cash Integer amount of money paid to market
	 */
	public void msgPayment(int cash) {
		print("Received payment");
		this.cash += cash;
		stateChanged();
	}


	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		synchronized(orders) {
			for (Order order : orders) {
				if (order.getState() == OrderState.Ready) {
					deliverOrder(order);
					return true;
				}
			}
			for (Order order : orders) {
				if (order.getState() == OrderState.Received) {
					produceOrder(order);
					return true;
				}
			}
		}

		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	/**
	 * Tells the cook what it can fulfill and starts producing the order
	 * 
	 * @param o Reference to the Order
	 */
	private void produceOrder(Order o) {
		print("Here is what I can fulfill: ");
		for (ItemOrder io : o.items) {
			print(io.getAmount() + " " + io.getFood() + "s");
		}
		cook.msgHereIsWhatICanFulfill(o.items);
		if (!o.items.isEmpty()) {
			o.setState(OrderState.ProducingOrder);
			timer.schedule(new ProducingTimerTask(o) {
				@Override
				public void run() {
					order.setState(OrderState.Ready);
					stateChanged();
				}
			},
			timeToProduceOrder(o));
			for (ItemOrder io : o.items) {
				foods.get(io.getFood()).amount -= io.getAmount();
			}
		} else {
			print("I can't fulfill this order");
			o.setState(OrderState.Finished);
		}
	}
	
	/**
	 * Delivers an order to the cook and sends the cashier the bill
	 * 
	 * @param o Reference to the Order
	 */
	private void deliverOrder(Order o) {
		int bill = 0;
		print("Here is your order: ");
		for (ItemOrder io : o.items) {
			print(io.getAmount() + " " + io.getFood() + "s");
			bill += foodPrices.get(io.getFood())*io.getAmount();
		}
		cook.msgOrderDelivered(o.items);
		print("Here is the bill: $" + bill);
		cashier.msgHereIsBill(bill, this);
		o.setState(OrderState.Finished);
	}
	

	//Inner classes

	/**
	 * Contains all information about an order relevant to the MarketAgent
	 */
	private class Order {
		List<ItemOrder> items;
		OrderState state;

		Order(List<ItemOrder> io, OrderState s) {
			items = io;
			state = s;
		}
		
		OrderState getState() {
			return state;
		}
		
		void setState(OrderState s) {
			state = s;
		}
	}
	
	/**
	 * Contains all information about food relevant to the MarketAgent
	 */
	private class Food {
		String type;
		int timeToProduce;
		int amount;
		
		Food(String t, int p, int a) {
			type = t;
			timeToProduce = p;
			amount = a;
		}
	}
	
	/**
	 * TimerTask necessary for the order timer
	 */
	private class ProducingTimerTask extends TimerTask {
		Order order;
		
		ProducingTimerTask(Order o) {
			order = o;
		}
		
		public void run() {};
	}
}


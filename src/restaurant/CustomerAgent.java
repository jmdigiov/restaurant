package restaurant;

import restaurant.gui.CustomerGui;
import restaurant.interfaces.Customer;
import agent.Agent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

/**
 * Restaurant customer agent.
 */
public class CustomerAgent extends Agent implements Customer {
	private String name;
	private int hungerLevel = 1;
	private Timer timer = new Timer();
	private CustomerGui customerGui;
	private Menu menu;
	private String choice;
	private Semaphore doneOrdering = new Semaphore(0,true);
	private Semaphore atCashier = new Semaphore(0,true);
	
	private int tableNumber;
	private int cash;
	private int charge;
	
	// agent correspondents
	private HostAgent host;
	private WaiterAgent waiter;
	private CashierAgent cashier;

	public enum AgentState
	{DoingNothing, GoingToRestaurant, WaitingInRestaurant, BeingSeated, Seated, WantToLeave, ReadyToOrder, Ordered, Eating, WaitingForCheck, Paying, Leaving};
	private AgentState state = AgentState.DoingNothing;//The start state

	public enum AgentEvent 
	{none, gotHungry, arrivedAtRestaurant, gotImpatient, followWaiter, seated, looksAtMenuAndCries, toldWaiter, madeChoice, order, receivedFood, doneEating, receivedCheck, receivedChange, doneLeaving};
	AgentEvent event = AgentEvent.none;

	/**
	 * Constructor
	 *
	 * @param name Agent name for messages
	 */
	public CustomerAgent(String name){
		super();
		this.name = name;
		
		cash = 30;
		if (name.equals("cheapskate") || name.equals("poor")) {
			cash = 5;
		}
		if (name.equals("enoughforsalad")) {
			cash = 7;
		}
		charge = 0;
	}

	/**
	 * Hack to establish connection to HostAgent.
	 * 
	 * @param h Reference to HostAgent
	 */
	public void setHost(HostAgent host) {
		this.host = host;
	}
	
	/**
	 * Hack to establish connection to CashierAgent.
	 * 
	 * @param cashier Reference to CashierAgent
	 */
	public void setCashier(CashierAgent cashier) {
		this.cashier = cashier;
	}
	
	/**
	 * Returns the CustomerAgent's current food choice
	 */
	public String getChoice() {
		return choice;
	}
	
	/**
	 * Returns an integer of what the CustomerAgent currently owes
	 */
	public int getCharge() {
		return charge;
	}
	
	/**
	 * Returns whether the CustomerAgent is currently eating
	 * 
	 * @return true if the Customer's current state is Eating, false otherwise
	 */
	public boolean isEating() {
		if (state == AgentState.Eating) {
			return true;
		}
		return false;
	}

	/**
	 * Returns the Agent's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the CustomerAgent's hunger level
	 * 
	 * @param hungerLevel Integer hunger level to assign to CustomerAgent
	 */
	public void setHungerLevel(int hungerLevel) {
		this.hungerLevel = hungerLevel;
	}

	/**
	 * Returns a String that represents the Agent
	 */
	public String toString() {
		return "customer " + getName();
	}

	/**
	 * Assigns a GUI to Agent
	 * 
	 * @param g Reference to CustomerGui
	 */
	public void setGui(CustomerGui g) {
		customerGui = g;
	}

	/**
	 * Returns CustomerAgent's GUI
	 */
	public CustomerGui getGui() {
		return customerGui;
	}
	
	// Messages

	/**
	 * Makes the CustomerAgent's event "gotHungry"
	 */
	public void gotHungry() { //from animation
		print("I'm hungry");
		event = AgentEvent.gotHungry;
		stateChanged();
	}
	
	/**
	 * If CustomerAgent's name is "impatient" (hack), makes his event "gotImpatient". Otherwise he waits.
	 */
	public void msgRestaurantIsFull() {
		if (name.equals("impatient")) {
			event = AgentEvent.gotImpatient;
		} else {
			Do("I'll wait for a table to open");
		}
		stateChanged();
	}

	/**
	 * Tells customer to follow the waiter to the specified table
	 * 
	 * @param w Reference to WaiterAgent
	 * @param m Reference to restaurant menu
	 * @param tableNumber Number of the assigned table
	 */
	public void msgFollowMe(WaiterAgent w, Menu m, int tableNumber) {
		waiter = w;
		menu = m;
		this.tableNumber = tableNumber;
		print("Received msgFollowMe");
		event = AgentEvent.followWaiter;
		stateChanged();
	}

	/**
	 * Seats customer once he has reached his table
	 */
	public void msgAnimationFinishedGoToSeat() { //from animation
		if (!name.equals("cheapskate") && cash < menu.lowestPrice()) {
			event = AgentEvent.looksAtMenuAndCries;
		}
		else {
			event = AgentEvent.seated;
		}
		stateChanged();
	}
	
	/**
	 * Waiter asks customer what he would like to order and customer's event is set to "order"
	 */
	public void msgWhatWouldYouLike() {
		event = AgentEvent.order;
		stateChanged();
	}
	
	/**
	 * Waiter asks customer if he would like something different since his choice is not available
	 * 
	 * @param menu Reference to updated menu
	 */
	public void msgWantSomethingElse(Menu menu) {
		this.menu = menu;
		if (name != "cheapskate" && cash < menu.lowestPrice()) {
			state = AgentState.BeingSeated;
			event = AgentEvent.looksAtMenuAndCries;
		} else {
			state = AgentState.ReadyToOrder;
			event = AgentEvent.order;
		}
		stateChanged();
	}
	
	/**
	 * Notifies customer that his food has arrived and sets his event to "receivedFood"
	 * 
	 * @param choice Name of customer's choice
	 */
	public void msgHereIsFood(String choice) {
		if (this.choice == choice) {
			event = AgentEvent.receivedFood;
			stateChanged();
		}
	}
	
	/**
	 * Sets customer's event to "doneLeaving" since he has left the restaurant
	 */
	public void msgAnimationFinishedLeaveRestaurant() { //from animation
		event = AgentEvent.doneLeaving;
		stateChanged();
	}
	
	/**
	 * Sets customer's event to "arrivedAtRestaurant" since he has entered the restaurant
	 */
	public void msgAnimationFinishedEnterRestaurant() { //from animation
		event = AgentEvent.arrivedAtRestaurant;
		stateChanged();
	}
	
	/**
	 * Tells customer what he owes
	 * 
	 * @param c Integer amount of money that the customer owes
	 */
	public void  msgHereIsCheck(int c) {
		charge += c;
		event = AgentEvent.receivedCheck;
		stateChanged();
	}
	
	/**
	 * Gives the customer his change. If he underpaid, the change will be negative and he will owe this amount next time.
	 * 
	 * @param change Integer amount of change the customer receives
	 */
	public void msgChange(int change) {
		if (change < 0) {
			charge = -change;
			cash += 30;
		}
		else {
			cash += change;
			charge = 0;
		}
		event = AgentEvent.receivedChange;
		stateChanged();
	}
	
	/**
	 * Once the ordering animation has finished, this is called so the customer knows that he has finished ordering
	 */
	public void msgDoneOrdering() { //from animation
		doneOrdering.release();
		stateChanged();
	}
	
	/**
	 * Lets the CustomerAgent know that he has reached the cashier's location
	 */
	public void msgAtCashier() { //from animation
		atCashier.release();
		stateChanged();
	}

	/**
	 * Scheduler.  Determine what action is called for, and do it.
	 */
	public boolean pickAndExecuteAnAction() {
		//	CustomerAgent is a finite state machine

		if (state == AgentState.DoingNothing && event == AgentEvent.gotHungry){
			state = AgentState.GoingToRestaurant;
			return true;
		}
		if (state == AgentState.GoingToRestaurant && event == AgentEvent.arrivedAtRestaurant) {
			state = AgentState.WaitingInRestaurant;
			requestSeat();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.followWaiter){
			state = AgentState.BeingSeated;
			SitDown();
			return true;
		}
		if (state == AgentState.WaitingInRestaurant && event == AgentEvent.gotImpatient){
			state = AgentState.Leaving;
			leaveAndNotifyHost();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.looksAtMenuAndCries){
			state = AgentState.WantToLeave;
			tellWaiter();
			return true;
		}
		if (state == AgentState.WantToLeave && event == AgentEvent.toldWaiter){
			state = AgentState.Leaving;
			leaveRestaurant();
			return true;
		}
		if (state == AgentState.BeingSeated && event == AgentEvent.seated){
			state = AgentState.Seated;
			timer.schedule(new TimerTask() {
				public void run() {
					event = AgentEvent.madeChoice;
					stateChanged();
				}
			},
			hungerLevel * 500);//how long to wait before running task
			return true;
		}
		if (state == AgentState.Seated && event == AgentEvent.madeChoice){
			state = AgentState.ReadyToOrder;
			callWaiter();
			return true;
		}
		if (state == AgentState.ReadyToOrder && event == AgentEvent.order){
			state = AgentState.Ordered;
			giveOrder();
			return true;
		}
		if (state == AgentState.Ordered && event == AgentEvent.receivedFood){
			state = AgentState.Eating;
			EatFood();
			return true;
		}
		if (state == AgentState.Eating && event == AgentEvent.doneEating){
			state = AgentState.WaitingForCheck;
			askForCheck();
			return true;
		}
		if (state == AgentState.WaitingForCheck && event == AgentEvent.receivedCheck){
			state = AgentState.Paying;
			leaveTable();
			return true;
		}
		if (state == AgentState.Paying && event == AgentEvent.receivedChange){
			state = AgentState.Leaving;
			leaveRestaurant();
			return true;
		}
		if (state == AgentState.Leaving && event == AgentEvent.doneLeaving){
			state = AgentState.DoingNothing;
			//no action
			return true;
		}
		return false;
		//we have tried all our rules and found
		//nothing to do. So return false to main loop of abstract agent
		//and wait.
	}

	// Actions

	/**
	 * Tells the host that he wants to be seated
	 */
	private void requestSeat() {
		Do("Table for 1");
		host.msgIWantFood(this);
	}
	
	/**
	 * Tells the host that he is leaving the restaurant
	 */
	private void leaveAndNotifyHost() {
		Do("I don't want to wait. Leaving restaurant");
		customerGui.DoExitRestaurant();
		host.msgImLeaving(this);
	}

	/**
	 * Tells the customer's GUI to go to the table
	 */
	private void SitDown() {
		Do("Being seated. Going to table");
		customerGui.DoGoToSeat(tableNumber);
	}
	
	/**
	 * Tells the waiter that he wants to leave the restaurant
	 */
	private void tellWaiter() {
		Do("This food is too expensive. I'm leaving.");
		waiter.msgIWantToLeave(this);
		event = AgentEvent.toldWaiter;
	}
	
	/**
	 * Tells the customer's GUI to leave the restaurant
	 */
	private void leaveRestaurant() {
		Do("Leaving restaurant");
		customerGui.DoExitRestaurant();
	}
	
	/**
	 * Tells the waiter that he is ready to order
	 */
	private void callWaiter() {
		Do("I'm ready to order.");
		waiter.msgReadyToOrder(this);
	}
	
	/**
	 * Tells the waiter what his order is
	 */
	private void giveOrder() {
		if (menu.checkItem(name)) {
			choice = name;
		} else {
			do {
				choice = menu.randomItem();
			} while (menu.getPrice(choice) > cash && !name.equals("cheapskate"));
		}
		print("I would like to order " + choice);
		customerGui.order();
		try {
			doneOrdering.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		waiter.msgHereIsChoice(this, choice);
	}

	/**
	 * Start eating and run a timer to determine how long the customer eats
	 */
	private void EatFood() {
		Do("Eating Food");
		timer.schedule(new TimerTask() {
			public void run() {
				print("Done eating " + choice);
				event = AgentEvent.doneEating;
				stateChanged();
			}
		},
		hungerLevel * 1000);//how long to wait before running task
	}
	
	/**
	 * Tells the waiter that he is done eating and would like his check
	 */
	private void askForCheck() {
		Do("Check please.");
		waiter.msgDoneEating(this);
	}

	/**
	 * Tells the customer's GUI to go to the cashier and pays what he owes
	 */
	private void leaveTable() {
		Do("Going to cashier");
		customerGui.DoGoToCashier();
		try {
			atCashier.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int payment = charge + 10 - charge % 10;
		if (cash < payment) {
			payment = cash;
		}
		Do("Paying $" + payment);
		cashier.msgPayment(this, payment);
		cash -= payment;
	}
}


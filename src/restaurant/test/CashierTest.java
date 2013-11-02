package restaurant.test;

import restaurant.CashierAgent;
import restaurant.CashierAgent.CheckState;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockWaiter;
import restaurant.test.mock.EventLog;
import junit.framework.*;

public class CashierTest extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockWaiter waiter;
	MockCustomer customer;
	
	public void setUp() throws Exception{
		super.setUp();		
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");		
		waiter = new MockWaiter("mockwaiter");
	}	
	
	public void testOneNormalCustomerScenario()
	{
		customer.cashier = cashier;		
		
		//check preconditions
		assertEquals("Cashier should have 0 checks in it. It doesn't.", 0, cashier.checks.size());		
		
		assertEquals("Cashier should have $200. It doesn't.", 200, cashier.getCash());	
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgProducecheck is called. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
		
		//step 1: Receive message to produce check
		cashier.msgProduceCheck(waiter, customer, "steak");

		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgProduceCheck\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgProduceCheck"));
		
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals("Cashier should have 1 check in it. It doesn't.", cashier.checks.size(), 1);
		
		assertTrue("First Check in checks should have state == Created. It doesn't.", cashier.checks.get(0).getState() == CheckState.Created);
		
		assertTrue("First Check in checks should have charge == $16. Instead, the charge is: $" + cashier.checks.get(0).getCharge(), cashier.checks.get(0).getCharge() == 16);
		
		assertTrue("First Check in checks should have the right customer. It doesn't.", cashier.checks.get(0).getCust() == customer);
		
		assertTrue("First Check in checks should have the right waiter. It doesn't.", cashier.checks.get(0).getWaiter() == waiter);
		
		//step 2: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call giveToWaiter), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 2 and preconditions for step 3
		assertTrue("First Check in checks should have state == GivenToWaiter. It doesn't.", cashier.checks.get(0).getState() == CheckState.GivenToWaiter);
		
		assertTrue("MockWaiter should have logged an event for receiving \"msgHereIsCheck\" with the correct customer and charge. His last event logged reads instead: "
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received msgHereIsCheck from cashier. Customer = mockcustomer. Charge = $16"));
		
		assertEquals("MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		
		//step 3: Receive message Payment from customer
		cashier.msgPayment(customer, 16);
		
		//check postconditions for step 3 and preconditions for step 4
		assertTrue("Cashier should have logged \"Received msgPayment\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgPayment"));
		
		assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
				+ customer.log.toString(), 0, customer.log.size());
		
		assertTrue("First Check in checks should have state == Paid. It doesn't.", cashier.checks.get(0).getState() == CheckState.Paid);
		
		assertTrue("First Check in checks should have payment == $16. Instead, the payment is: $" + cashier.checks.get(0).getPayment(), cashier.checks.get(0).getPayment() == 16);
		
		//step 4: Run the scheduler again
		assertTrue("Cashier's scheduler should have returned true (it should call giveCustomerChange), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 4
		assertTrue("First Check in checks should have state == Done. It doesn't.", cashier.checks.get(0).getState() == CheckState.Done);
		
		assertEquals("Cashier should have $216. It doesn't.", 216, cashier.getCash());	
		
		assertTrue("MockCustomer should have logged an event for receiving \"msgChange\" with the correct change. His last event logged reads instead: "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgChange from cashier. Change = $0"));
		
		assertFalse("Cashier's scheduler should have returned false (nothing left to do), but didn't.", cashier.pickAndExecuteAnAction());
		
	}
	
	public void testOneCheapskateCustomerScenario()
	{
		customer.cashier = cashier;		
		
		//check preconditions
		assertEquals("Cashier should have 0 checks in it. It doesn't.", 0, cashier.checks.size());		
		
		assertEquals("Cashier should have $200. It doesn't.", 200, cashier.getCash());	
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgProducecheck is called. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
		
		//step 1: Receive message to produce check
		cashier.msgProduceCheck(waiter, customer, "steak");

		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgProduceCheck\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgProduceCheck"));
		
		assertEquals("MockWaiter should have an empty event log before the Cashier's scheduler is called. Instead, the MockWaiter's event log reads: "
						+ waiter.log.toString(), 0, waiter.log.size());
		
		assertEquals("Cashier should have 1 check in it. It doesn't.", cashier.checks.size(), 1);
		
		assertTrue("First Check in checks should have state == Created. It doesn't.", cashier.checks.get(0).getState() == CheckState.Created);
		
		assertTrue("First Check in checks should have charge == $16. Instead, the charge is: $" + cashier.checks.get(0).getCharge(), cashier.checks.get(0).getCharge() == 16);
		
		assertTrue("First Check in checks should have the right customer. It doesn't.", cashier.checks.get(0).getCust() == customer);
		
		assertTrue("First Check in checks should have the right waiter. It doesn't.", cashier.checks.get(0).getWaiter() == waiter);
		
		//step 2: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call giveToWaiter), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 2 and preconditions for step 3
		assertTrue("First Check in checks should have state == GivenToWaiter. It doesn't.", cashier.checks.get(0).getState() == CheckState.GivenToWaiter);
		
		assertTrue("MockWaiter should have logged an event for receiving \"msgHereIsCheck\" with the correct customer and charge. His last event logged reads instead: "
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received msgHereIsCheck from cashier. Customer = mockcustomer. Charge = $16"));
		
		assertEquals("MockCustomer should have an empty event log after the Cashier's scheduler is called for the first time. Instead, the MockCustomer's event log reads: "
						+ customer.log.toString(), 0, customer.log.size());
		
		//step 3: Receive message Payment from customer
		cashier.msgPayment(customer, 6);
		
		//check postconditions for step 3 and preconditions for step 4
		assertTrue("Cashier should have logged \"Received msgPayment\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgPayment"));
		
		assertEquals("MockCustomer should have an empty event log before the Cashier's scheduler is called. Instead, the MockCustomer's event log reads: "
				+ customer.log.toString(), 0, customer.log.size());
		
		assertTrue("First Check in checks should have state == Paid. It doesn't.", cashier.checks.get(0).getState() == CheckState.Paid);
		
		assertTrue("First Check in checks should have payment == $6. Instead, the payment is: $" + cashier.checks.get(0).getPayment(), cashier.checks.get(0).getPayment() == 6);
		
		//step 4: Run the scheduler again
		assertTrue("Cashier's scheduler should have returned true (it should call giveCustomerChange), but didn't.", cashier.pickAndExecuteAnAction());
		
		customer.setCharge(10);
		
		//check postconditions for step 4 and preconditions for step 5
		assertTrue("First Check in checks should have state == Done. It doesn't.", cashier.checks.get(0).getState() == CheckState.Done);
		
		assertEquals("Cashier should have $206. It doesn't.", 206, cashier.getCash());	
		
		assertTrue("MockCustomer should have logged an event for receiving \"msgChange\" with the correct change (debt in this case). His last event logged reads instead: "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgChange from cashier. Change = $-10"));
		
		assertFalse("Cashier's scheduler should have returned false (nothing left to do), but didn't.", cashier.pickAndExecuteAnAction());
		
		//step 5: Receive message to produce check
		cashier.msgProduceCheck(waiter, customer, "chicken");

		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgProduceCheck\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgProduceCheck"));
		
		assertEquals("Cashier should have 2 checks in it. It doesn't.", cashier.checks.size(), 2);
		
		assertTrue("Second Check in checks should have state == Created. It doesn't.", cashier.checks.get(1).getState() == CheckState.Created);
		
		assertTrue("Second Check in checks should have charge == $21. Instead, the charge is: $" + cashier.checks.get(1).getCharge(), cashier.checks.get(1).getCharge() == 21);
		
		assertTrue("Second Check in checks should have the right customer. It doesn't.", cashier.checks.get(1).getCust() == customer);
		
		assertTrue("Second Check in checks should have the right waiter. It doesn't.", cashier.checks.get(1).getWaiter() == waiter);
		
		//step 2: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call giveToWaiter), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 2 and preconditions for step 3
		assertTrue("Second Check in checks should have state == GivenToWaiter. It doesn't.", cashier.checks.get(1).getState() == CheckState.GivenToWaiter);
		
		assertTrue("MockWaiter should have logged an event for receiving \"msgHereIsCheck\" with the correct customer and charge. His last event logged reads instead: "
				+ waiter.log.getLastLoggedEvent().toString(), waiter.log.containsString("Received msgHereIsCheck from cashier. Customer = mockcustomer. Charge = $21"));
		
		//step 3: Receive message Payment from customer
		cashier.msgPayment(customer, 21);
		
		//check postconditions for step 3 and preconditions for step 4
		assertTrue("Cashier should have logged \"Received msgPayment\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgPayment"));
		
		assertTrue("Second Check in checks should have state == Paid. It doesn't.", cashier.checks.get(1).getState() == CheckState.Paid);
		
		assertTrue("Second Check in checks should have payment == $21. Instead, the payment is: $" + cashier.checks.get(1).getPayment(), cashier.checks.get(1).getPayment() == 21);
		
		//step 4: Run the scheduler again
		assertTrue("Cashier's scheduler should have returned true (it should call giveCustomerChange), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 4 and preconditions for step 5
		assertTrue("Second Check in checks should have state == Done. It doesn't.", cashier.checks.get(1).getState() == CheckState.Done);
		
		assertEquals("Cashier should have $227. It doesn't.", 227, cashier.getCash());	
		
		assertTrue("MockCustomer should have logged an event for receiving \"msgChange\" with the correct change. His last event logged reads instead: "
				+ customer.log.getLastLoggedEvent().toString(), customer.log.containsString("Received msgChange from cashier. Change = $0"));
		
		assertFalse("Cashier's scheduler should have returned false (nothing left to do), but didn't.", cashier.pickAndExecuteAnAction());
		
	}
}

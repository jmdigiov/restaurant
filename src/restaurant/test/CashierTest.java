package restaurant.test;

import restaurant.CashierAgent;
import restaurant.CashierAgent.CheckState;
import restaurant.test.mock.MockCustomer;
import restaurant.test.mock.MockWaiter;
import restaurant.test.mock.MockMarket;
import junit.framework.*;

public class CashierTest extends TestCase
{
	//these are instantiated for each test separately via the setUp() method.
	CashierAgent cashier;
	MockWaiter waiter;
	MockCustomer customer;
	MockMarket market;
	MockMarket market2;
	
	public void setUp() throws Exception{
		super.setUp();		
		cashier = new CashierAgent("cashier");		
		customer = new MockCustomer("mockcustomer");		
		waiter = new MockWaiter("mockwaiter");
		market = new MockMarket("mockmarket");
		market2 = new MockMarket("mockmarket2");
	}	
	
	public void testOneNormalCustomerScenario()
	{
		customer.cashier = cashier;		
		
		//check preconditions
		assertEquals("Cashier should have 0 checks in it. It doesn't.", 0, cashier.checks.size());		
		
		assertEquals("Cashier should have $200. It doesn't.", 200, cashier.getCash());	
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgProduceCheck is called. Instead, the Cashier's event log reads: "
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
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgProduceCheck is called. Instead, the Cashier's event log reads: "
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
	
	public void testOneMarketBillScenario()
	{
		market.cashier = cashier;		
		
		//check preconditions
		assertEquals("Cashier should have 0 bills in it. It doesn't.", 0, cashier.bills.size());		
		
		assertEquals("Cashier should have $200. It doesn't.", 200, cashier.getCash());	
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgHereIsBill is called. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
		
		//step 1: Receive msgHereIsBill
		cashier.msgHereIsBill(30, market);

		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));
		
		assertEquals("MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket's event log reads: "
						+ market.log.toString(), 0, market.log.size());
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", 1, cashier.bills.size());
		
		assertTrue("First Bill in bills should have charge == $30. Instead, the charge is: $" + cashier.bills.get(0).getCharge(), cashier.bills.get(0).getCharge() == 30);
		
		assertTrue("First Bill in bills should have the right market. It doesn't.", cashier.bills.get(0).getMarket() == market);
		
		//step 2: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call payBill), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 2
		assertEquals("Cashier should have 0 bills in it. It doesn't.", 0, cashier.bills.size());
		
		assertEquals("Cashier should have $170. It doesn't.", 170, cashier.getCash());	
		
		assertTrue("MockMarket should have logged an event for receiving \"msgPayment\" with the correct cash. His last event logged reads instead: "
				+ market.log.getLastLoggedEvent().toString(), market.log.containsString("Received msgPayment from cashier. Cash = $30"));
		
		assertFalse("Cashier's scheduler should have returned false (nothing left to do), but didn't.", cashier.pickAndExecuteAnAction());
		
	}
	
	public void testTwoMarketBillsScenario()
	{
		market.cashier = cashier;
		market2.cashier = cashier;
		
		//check preconditions
		assertEquals("Cashier should have 0 bills in it. It doesn't.", 0, cashier.bills.size());		
		
		assertEquals("Cashier should have $200. It doesn't.", 200, cashier.getCash());	
		
		assertEquals("Cashier should have an empty event log before the Cashier's msgHereIsBill is called. Instead, the Cashier's event log reads: "
						+ cashier.log.toString(), 0, cashier.log.size());
		
		//step 1: Receive msgHereIsBill from first market
		cashier.msgHereIsBill(18, market);

		//check postconditions for step 1 and preconditions for step 2
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));
		
		assertEquals("First MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket's event log reads: "
						+ market.log.toString(), 0, market.log.size());
		
		assertEquals("Cashier should have 1 bill in it. It doesn't.", 1, cashier.bills.size());
		
		assertTrue("First Bill in bills should have charge == $18. Instead, the charge is: $" + cashier.bills.get(0).getCharge(), cashier.bills.get(0).getCharge() == 18);
		
		assertTrue("First Bill in bills should have the right market. It doesn't.", cashier.bills.get(0).getMarket() == market);
		
		//step 2: Receive msgHereIsBill from second market
		cashier.msgHereIsBill(12, market2);
		
		//check postconditions for step 2 and preconditions for step 3
		assertTrue("Cashier should have logged \"Received msgHereIsBill\" but didn't. His last event logged reads instead: " 
				+ cashier.log.getLastLoggedEvent().toString(), cashier.log.containsString("Received msgHereIsBill"));
		
		assertEquals("Second MockMarket should have an empty event log before the Cashier's scheduler is called. Instead, the MockMarket's event log reads: "
						+ market2.log.toString(), 0, market2.log.size());
		
		assertEquals("Cashier should have 2 bills in it. It doesn't.", 2, cashier.bills.size());
		
		assertTrue("Second Bill in bills should have charge == $12. Instead, the charge is: $" + cashier.bills.get(1).getCharge(), cashier.bills.get(1).getCharge() == 12);
		
		assertTrue("Second Bill in bills should have the right market. It doesn't.", cashier.bills.get(1).getMarket() == market2);
				
		//step 3: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call payBill), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 3 and preconditions for step 4
		assertEquals("Cashier should have 1 bill in it. It doesn't.", 1, cashier.bills.size());
		
		assertEquals("Cashier should have $182. It doesn't.", 182, cashier.getCash());	
		
		assertTrue("MockMarket should have logged an event for receiving \"msgPayment\" with the correct cash. His last event logged reads instead: "
				+ market.log.getLastLoggedEvent().toString(), market.log.containsString("Received msgPayment from cashier. Cash = $18"));
		
		//step 4: Run the scheduler
		assertTrue("Cashier's scheduler should have returned true (it should call payBill), but didn't.", cashier.pickAndExecuteAnAction());
		
		//check postconditions for step 3 and preconditions for step 4
		assertEquals("Cashier should have 0 bills in it. It doesn't.", 0, cashier.bills.size());
		
		assertEquals("Cashier should have $170. It doesn't.", 170, cashier.getCash());	
		
		assertTrue("MockMarket should have logged an event for receiving \"msgPayment\" with the correct cash. His last event logged reads instead: "
				+ market2.log.getLastLoggedEvent().toString(), market2.log.containsString("Received msgPayment from cashier. Cash = $12"));
		
		assertFalse("Cashier's scheduler should have returned false (nothing left to do), but didn't.", cashier.pickAndExecuteAnAction());
		
	}
}

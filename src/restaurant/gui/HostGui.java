package restaurant.gui;


import restaurant.CustomerAgent;
import restaurant.HostAgent;

import java.awt.*;

public class HostGui implements Gui {

    private HostAgent agent = null;

    private int xPos = -20, yPos = -20;//default waiter position
    private int xDestination = -20, yDestination = -20;//default start position

    //Get rid of the "magic numbers"
    static final int HOSTWIDTH = 20;
	static final int HOSTHEIGHT = 20;
	
    public static final int xTable = 150;
    public static final int yTable = 250;
    public static final int tableWidth = 50;
    public static final int tableHeight = 50;

    public HostGui(HostAgent agent) {
        this.agent = agent;
    }

    public void updatePosition() {
        if (xPos < xDestination)
            xPos++;
        else if (xPos > xDestination)
            xPos--;

        if (yPos < yDestination)
            yPos++;
        else if (yPos > yDestination)
            yPos--;

        if (xPos == xDestination && yPos == yDestination) {
        	if ((xDestination >= xTable + 20) && (yDestination == yTable - 20)) {
        		agent.msgAtTable();
        	}
        	if (xDestination == -20 && yDestination == -20) {
        		agent.msgReadyToSeat();
        	}
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.MAGENTA);
        g.fillRect(xPos, yPos, HOSTWIDTH, HOSTHEIGHT);
    }

    public boolean isPresent() {
        return true;
    }

    public void DoBringToTable(CustomerAgent customer, int tableNumber) {
        xDestination = xTable + (tableNumber-1)*2*tableWidth + 20;
        yDestination = yTable - 20;
    }

    public void DoLeaveCustomer() {
        xDestination = -20;
        yDestination = -20;
    }

    public int getXPos() {
        return xPos;
    }

    public int getYPos() {
        return yPos;
    }
}

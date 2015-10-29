package com.ly.fn.inf.sso.server.ticket;

import java.util.Collection;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;

public class RedisTicketRegistry extends AbstractDistributedTicketRegistry {

	@Override
	public void addTicket(Ticket ticket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Ticket getTicket(String ticketId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteTicket(String ticketId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Ticket> getTickets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void updateTicket(Ticket ticket) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean needsCallback() {
		// TODO Auto-generated method stub
		return false;
	}

}

package com.ly.fn.inf.sso.client.demo;

import java.net.URL;

import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpsCasProtocolUrlBasedTicketValidator extends AbstractUrlBasedTicketValidator{

	private static final Logger LOG = LoggerFactory.getLogger(AbstractHttpsCasProtocolUrlBasedTicketValidator.class);
	
	protected AbstractHttpsCasProtocolUrlBasedTicketValidator(final String casServerUrlPrefix) {
        super(casServerUrlPrefix);
    }

    /**
     * Retrieves the response from the server by opening a connection and merely reading the response.
     */
    protected final String retrieveResponseFromServer(final URL validationUrl, final String ticket) {
        try {
			return NativeHttpUtils.doGet(validationUrl.toString());
		} catch (Exception e) {
			LOG.error("error happened during service ticket validation: validationUrl={}", validationUrl);
			LOG.error("error happened during service ticket validation", e);
		}
        return null;
    }

}

package com.ly.fn.inf.sso.client.demo;

import org.apache.shiro.cas.CasRealm;
import org.jasig.cas.client.validation.TicketValidator;

public class CustomCasRealm extends CasRealm {

	protected TicketValidator createTicketValidator() {
		String urlPrefix = getCasServerUrlPrefix();
        return new HttpsCas20ServiceTicketValidator(urlPrefix);
    }
}

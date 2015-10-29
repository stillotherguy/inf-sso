/*package com.ly.fn.inf.sso.server.authentication;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;

import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.inspektr.common.web.ClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class PassportAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	protected HandlerResult authenticateUsernamePasswordInternal(UsernamePasswordCredential credential) throws GeneralSecurityException,
			PreventedException {
		final String username = credential.getUsername();
		final String password = credential.getPassword();

		if ((username == null) || "".equals(username.trim())) {
			logger.warn("Username can not be empty.");
			throw new AccountNotFoundException("Username can not be empty.");
		}
		if ((password == null) || "".equals(password)) {
			logger.warn("Password can not be empty.");
			throw new AccountNotFoundException("Password can not be empty.");
		}
		UserInfo userInfo = userService.findUser(username);
		if (userInfo == null) {
			logger.warn("UserInfo was not found.{}", username);
			throw new AccountNotFoundException(username + "was not found.");
		}

		final boolean isPasswordValid = passwordEncoder.matches(password, userInfo.getPassword());
		if (!isPasswordValid) {
			logger.warn("Invalid credentials.{}", username);
			throw new FailedLoginException();
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userInfo.getId());
		map.put("account", username);
		map.put("username", EncodeUtils.hexEncode(userInfo.getUsername().getBytes()));
		HttpServletRequest request = ((ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes())
				.getRequest();
		ClientInfo client = ClientInfo.obtainClient(request);
		map.put("version", client.getVersion());
		map.put("client", client.getClient().name());

		if ((userInfo.getTenantKey() != null) && !"".equals(userInfo.getTenantKey())) {
			Tenant tenant = tenantRemotingService.loadTenant(userInfo.getTenantKey());
			logger.debug("tenant:{}", tenant);
			if (tenant != null) {
				map.put("tenantKey", userInfo.getTenantKey());
				map.put("employeeId", userInfo.getEmployeeId());
			}
		}
		HttpSession session = request.getSession();
		session.setAttribute("userInfo", userInfo);
		session.setAttribute("firsttime", true);
		session.setAttribute("rawPassword", password);// 供直接登录使用
		return createHandlerResult(credential, new SimplePrincipal(username, map), null);
	}

}
*/
package com.ly.fn.inf.sso.server.controller;

import static com.google.common.collect.Maps.newHashMap;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("deprecation")
@RestController
@RequestMapping(value = "/rest")
public class RestLoginController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	@Qualifier("ticketGrantingTicketCookieGenerator")
	private CookieRetrievingCookieGenerator tgtCookieGenerator;
	@Autowired
	private TicketRegistry ticketRegistry;
	@Autowired
	private CentralAuthenticationService centralAuthenticationService;
	@Value("applicationProperties['cas.client.callback']")
	private String DEFAULT_SERVICE;
	
	/**
	 * jsonp跨域登录接口
	 * @param request
	 * @param response
	 * @param username
	 * @param password
	 * @param service
	 * @return
	 */
	@RequestMapping(value="/login", method = RequestMethod.GET, params = {"username", "password", "service"}, produces = "text/plain; charset=UTF-8")
	public String login(HttpServletRequest request, HttpServletResponse response, final String username, final String password, String service) {
		String tgtId = tgtCookieGenerator.retrieveCookieValue(request);
		final Map<String,Object> result = newHashMap();
		Ticket ticket = null;
		if(hasText(tgtId)) {
			ticket = ticketRegistry.getTicket(tgtId);
		}
		try {
			//TODO !ticket.isExpired()
			if(ticket != null && ticket instanceof TicketGrantingTicket) {
				result.put("result", true);
				result.put("st", centralAuthenticationService.grantServiceTicket(tgtId, getService(service)));
				result.put("message", "登录成功");
				tgtCookieGenerator.addCookie(request, response, tgtId);
			}else if(hasText(username) && hasText(password)) {
				TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(new UsernamePasswordCredential(username, password));
				result.put("result", true);
				result.put("st", centralAuthenticationService.grantServiceTicket(tgt.getId(), getService(service)));
				result.put("message", "登录成功");
				tgtCookieGenerator.addCookie(request, response, tgtId);
			}else {
				//验证出现异常，用户名密码错误
				result.put("result", false);
				result.put("message", "TGT已失效");
			}
		} catch (TicketException e) {
			logger.error("tgt {} user {} authenticate falied exception is {}", tgtId, username, e.getMessage());
			result.put("result", false);
			result.put("message", "分配ST失败，TGT已失效");
		} catch (AuthenticationException e) {
			logger.error("user {} authenticate failed, wrong username or password", username);
			result.put("result", false);
			result.put("message", "用户名或密码错误，请重新登录");
		}
		
		return constructResult(result);
	}
	
	/**
	 * REST登录接口
	 * @param username
	 * @param password
	 * @param service
	 * @return
	 */
	@RequestMapping(value="/login", method = RequestMethod.POST, params = {"username", "password", "service"}, produces = "application/json; charset=UTF-8")
	public Map<String,String> login(final String username, final String password, String service) {
		//TODO:根据service参数和不同的profile来验证service是否合法
		final Map<String,String> result = newHashMap();
		
		if(!hasText(username) || !hasText(password) || !hasText(service)) {
			//请求参数缺失，验证失败
			result.put("success", "false");
			return result;
		}
		
		try {
			final TicketGrantingTicket tgt = centralAuthenticationService.createTicketGrantingTicket(new UsernamePasswordCredential(username, password));
			final ServiceTicket st = centralAuthenticationService.grantServiceTicket(tgt.getId(), getService(service));
			result.put("success", "true");
			result.put("serviceTicketId", st.getId());
			result.put("tgtId", tgt.getId());
			result.put("message", "登录成功");
		} catch (final TicketException e) {
			logger.error("rest login failed message is {}", e.getMessage());
			result.put("success", "false");
			result.put("message", "创建ticket失败，请重新登录");
		} catch (final AuthenticationException e) {
			logger.error("rest login failed message is {}", e.getMessage());
			result.put("success", "false");
			result.put("message", "错误的用户名或密码，请重新登录");
		}
		
		return result;
	}
	
	@RequestMapping(value="/login/service", method = RequestMethod.POST, produces = "text/plain; charset=UTF-8")
	public HttpEntity<String> login(@RequestParam String tgtId, @RequestParam String service) {
		ServiceTicket st = null;
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.TEXT_PLAIN);
		try {
			st = centralAuthenticationService.grantServiceTicket(tgtId, getService(service));
			return new HttpEntity<String>(st.getId(), header);
		} catch (TicketException e) {
			e.printStackTrace();
		}
		
		return new HttpEntity<String>(header);
	}
	
	@RequestMapping(value="/logout", method = RequestMethod.POST, params = "tgtId")
	@ResponseStatus(HttpStatus.OK)
	public void logout(final String tgtId) {
		this.centralAuthenticationService.destroyTicketGrantingTicket(tgtId);
	}
	
	private static String constructResult(Map<String, Object> result) {
		//"success({'result':'failure'})"
		StringBuilder sb = new StringBuilder("success({");
		int i = 0;
		for(Entry<String,Object> e:result.entrySet()) {
			sb.append("'").append(e.getKey()).append("'").append(":").append("'").append(e.getValue()).append("'");
			i++;
			if(i != result.size()) {
				sb.append(",");
			}
		}
		sb.append("})");
		
		return sb.toString();
	}
	
	public static void main(String[] s) {
		final Map<String,Object> result = newHashMap();
		result.put("result", true);
		result.put("message", "TGT还未失效，授权service成功");
		System.out.println(constructResult(result));
	}
	
	private Service getService(String service) {
		//TODO: pattern检测类似  https://www.eteams.cn/eteamslogin的url
		if(isEmpty(service)) {
			service = DEFAULT_SERVICE;
		}
		return new SimpleWebApplicationServiceImpl(service);
	}
}


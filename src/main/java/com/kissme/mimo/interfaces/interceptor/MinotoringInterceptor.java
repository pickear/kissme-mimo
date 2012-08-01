package com.kissme.mimo.interfaces.interceptor;

import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.Maps;
import com.kissme.core.domain.monitor.MonitoringContext;
import com.kissme.core.web.Webs;

/**
 * 
 * @author loudyn
 * 
 */
public class MinotoringInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// get method means just read,ingore it
		if (!equalsIgnoreCase("GET", request.getMethod())) {

			MonitoringContext context = MonitoringContext.get();
			if (null == context) {
				Map<String, Object> delegate = Maps.newHashMap();
				context = new MonitoringContext(delegate);
				MonitoringContext.set(context);
			}

			String username = SecurityUtils.getSubject().getPrincipal().toString();
			String source = getRequestIp(request);
			String target = request.getRequestURI();
			context.setSource(source).setTarget(target).setActor(username);
		}

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		MonitoringContext.set(null);
	}

	private String getRequestIp(HttpServletRequest request) {
		return Webs.requestIP(request);
	}
}

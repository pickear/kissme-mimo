package com.kissme.mimo.interfaces.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.kissme.core.orm.datasource.DynamicDataSourceRouter;
import com.kissme.lang.Preconditions;

/**
 * 
 * @author loudyn
 * 
 */
public class ReadWriteControllingDataSourceInterceptor extends HandlerInterceptorAdapter {

	private String readRoute = "read-datasource";
	private String writeRoute = "write-datasource";

	public void setReadRoute(String readRoute) {
		Preconditions.hasText(readRoute);
		this.readRoute = readRoute;
	}

	public void setWriteRoute(String writeRoute) {
		Preconditions.hasText(writeRoute);
		this.writeRoute = writeRoute;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		if (isReadOnlyRequest(request)) {
			DynamicDataSourceRouter.specifyRoute(readRoute);
			return true;
		}

		DynamicDataSourceRouter.specifyRoute(writeRoute);
		return true;
	}

	private boolean isReadOnlyRequest(HttpServletRequest request) {
		return StringUtils.equalsIgnoreCase("get", request.getMethod());
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		DynamicDataSourceRouter.clearSpecifiedRoute();
	}
}

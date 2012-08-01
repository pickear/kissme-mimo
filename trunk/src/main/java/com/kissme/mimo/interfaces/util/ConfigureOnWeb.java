package com.kissme.mimo.interfaces.util;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import com.kissme.lang.Files;
import com.kissme.lang.Preconditions;
import com.kissme.mimo.domain.Conf;

/**
 * 
 * @author loudyn
 * 
 */
@Component
public final class ConfigureOnWeb implements ServletContextAware {
	private ServletContext context;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public final void setServletContext(ServletContext servletContext) {
		this.context = servletContext;
	}

	/**
	 * 
	 * @param conf
	 * @return
	 */
	public final Conf wrap(Conf conf) {
		String templatePath = Files.join(getServletContextPath(), conf.getTemplatePath());
		String resourcePath = Files.join(getServletContextPath(), conf.getResourcePath());
		String securityResourcePath = Files.join(getServletContextPath(), conf.getSecurityResourcePath());
		String recycleResourcePath = Files.join(getServletContextPath(), conf.getRecycleResourcePath());
		String attachmentPath = Files.join(getServletContextPath(), conf.getAttachmentPath());
		String photoPath = Files.join(getServletContextPath(), conf.getPhotoPath());

		conf.setAttachmentPath(attachmentPath).setPhotoPath(photoPath);
		conf.setRecycleResourcePath(recycleResourcePath).setSecurityResourcePath(securityResourcePath);
		return conf.setTemplatePath(templatePath).setResourcePath(resourcePath).setRootPath(getServletContextPath());
	}

	private String getServletContextPath() {
		Preconditions.notNull(this.context);
		return context.getRealPath("/");
	}

}

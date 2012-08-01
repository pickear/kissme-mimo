package com.kissme.mimo.domain.template;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author loudyn
 * 
 */
public final class TemplateHelper {

	/**
	 * 
	 * @param template
	 * @return
	 */
	public static String pathWithoutSuffix(Template template) {
		String path = template.getPath();
		int dot = StringUtils.lastIndexOf(path, ".");
		return dot == -1 ? path : StringUtils.substring(path, 0, dot);
	}

	private TemplateHelper() {}
}

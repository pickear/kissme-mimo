package com.kissme.mimo.domain.channel;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.kissme.core.domain.event.AbstractLifecycleAwareObject;
import com.kissme.mimo.domain.template.Template;
import com.kissme.mimo.domain.template.TemplateHelper;
import com.kissme.mimo.infrastructure.RichHtmlPopulator;

/**
 * 
 * @author loudyn
 * 
 */
public class Channel extends AbstractLifecycleAwareObject<Channel> {

	private static final long serialVersionUID = 1L;

	private Channel father;
	private List<Channel> children = Lists.newArrayList();

	private Template selfTemplate;
	private Template articleTemplate;

	private String name;
	private String path;
	private String about;

	private String title;
	private String metaKeyword;
	private String metaTitle;
	private String metaDescr;

	private int priority;

	public Channel getFather() {
		return father;
	}

	public Channel setFather(Channel father) {
		this.father = father;
		return this;
	}

	public boolean hasFather() {
		return null != getFather();
	}

	public int getLevel() {
		if (!hasFather()) {
			return 1;
		}
		return getFather().getLevel() + 1;
	}

	public List<Channel> getChildren() {
		return children;
	}

	public Channel setChildren(List<Channel> children) {
		this.children = children;
		return this;
	}

	public boolean hasChildren() {
		if (null == getChildren()) {
			return false;
		}

		return !getChildren().isEmpty();
	}

	public Template getSelfTemplate() {
		return selfTemplate;
	}

	public Channel setSelfTemplate(Template selfTemplate) {
		this.selfTemplate = selfTemplate;
		return this;
	}

	public String getSelfTemplatePath() {
		if (null != getSelfTemplate()) {
			return TemplateHelper.pathWithoutSuffix(getSelfTemplate());
		}

		return null;
	}

	public Template getArticleTemplate() {
		return articleTemplate;
	}

	public Channel setArticleTemplate(Template articleTemplate) {
		this.articleTemplate = articleTemplate;
		return this;
	}
	
	public String getActualArticleTemplatePath() {
		Template actual = Optional.fromNullable(articleTemplate).or(selfTemplate);
		return TemplateHelper.pathWithoutSuffix(actual);
	}

	public String getName() {
		return name;
	}

	public Channel setName(String name) {
		this.name = name;
		return this;
	}

	public String getPath() {
		return path;
	}

	public Channel setPath(String path) {
		this.path = path;
		return this;
	}

	public String getAbout() {
		return about;
	}

	public Channel setAbout(String about) {
		this.about = about;
		return this;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getPhotos() {
		return RichHtmlPopulator.populatePhoto(getAbout());
	}

	/**
	 * 
	 * @return
	 */
	public String getFirstPhoto() {
		if (isNotEmptyPhotos()) {
			return getPhotos().get(0);
		}

		return "";
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNotEmptyPhotos() {
		return !getPhotos().isEmpty();
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getFlashes() {
		return RichHtmlPopulator.populateFlash(getAbout());
	}

	/**
	 * 
	 * @return
	 */
	public String getFirstFlash() {
		if (isNotEmptyFlashes()) {
			return getFlashes().get(0);
		}

		return "";
	}

	/**
	 * 
	 * @return
	 */
	public boolean isNotEmptyFlashes() {
		return !getFlashes().isEmpty();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMetaKeyword() {
		return metaKeyword;
	}

	public Channel setMetaKeyword(String metaKeyword) {
		this.metaKeyword = metaKeyword;
		return this;
	}

	public String getMetaTitle() {
		return metaTitle;
	}

	public Channel setMetaTitle(String metaTitle) {
		this.metaTitle = metaTitle;
		return this;
	}

	public String getMetaDescr() {
		return metaDescr;
	}

	public Channel setMetaDescr(String metaDescr) {
		this.metaDescr = metaDescr;
		return this;
	}

	public int getPriority() {
		return priority;
	}

	public Channel setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	@Override
	protected boolean beforeCreate() {
		selfCheck();
		return true;
	}

	private Channel selfCheck() {

		if (null == getSelfTemplate()) {
			throw new IllegalStateException("SelfTemplate must not null!");
		}

		if (StringUtils.isBlank(getSelfTemplate().getId())) {
			throw new IllegalStateException("SelfTemplate must avaliable!");
		}

		if (null != getArticleTemplate() && StringUtils.isBlank(getArticleTemplate().getId())) {
			setArticleTemplate(null);
		}

		if (hasFather() && StringUtils.isBlank(getFather().getId())) {
			setFather(null);
		}

		return this;
	}

	@Override
	protected boolean beforeModify() {
		selfCheck();
		return true;
	}

	@Override
	protected final Channel getCaller() {
		return this;
	}
}

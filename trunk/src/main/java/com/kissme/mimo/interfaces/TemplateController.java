package com.kissme.mimo.interfaces;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kissme.core.orm.Page;
import com.kissme.core.web.controller.CrudControllerSupport;
import com.kissme.lang.Lang;
import com.kissme.mimo.application.template.TemplateService;
import com.kissme.mimo.domain.Conf;
import com.kissme.mimo.domain.ConfsRepository;
import com.kissme.mimo.domain.template.Template;
import com.kissme.mimo.interfaces.util.ConfigureOnWeb;
import com.kissme.mimo.interfaces.util.JsonMessage;

/**
 * 
 * @author loudyn
 * 
 */
@Controller
@RequestMapping("/template")
public class TemplateController extends CrudControllerSupport<String, Template> {

	private static final String REDIRECT_LIST = "redirect:/template/list/";

	@Autowired
	private ConfigureOnWeb confOnWeb;

	@Autowired
	private TemplateService templateService;

	@Autowired
	private ConfsRepository confsRepository;

	/**
	 * 
	 * @param page
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/list/", method = GET)
	public String list(Page<Template> page, Model model) {
		page = templateService.queryPage(page);
		model.addAttribute(page);
		return listView();
	}

	@Override
	@RequestMapping(value = "/create/", method = GET)
	public String create(Model model) {
		model.addAttribute(new Template());
		return formView();
	}

	@Override
	@RequestMapping(value = "/create/", method = POST)
	public String create(@Valid Template entity, BindingResult result) {
		if (result.hasErrors()) {
			error("创建模版失败，请核对数据后重试");
			return REDIRECT_LIST;
		}

		Conf conf = confOnWeb.wrap(confsRepository.getConf());
		entity.selfAdjusting(conf).create();
		success("模板创建成功");
		return REDIRECT_LIST;
	}

	@Override
	@RequestMapping(value = "/{id}/edit/", method = GET)
	public String edit(@PathVariable("id") String id, Model model) {
		Template entity = templateService.get(id);
		model.addAttribute(entity).addAttribute("_method", "PUT");
		return formView();
	}

	@Override
	@RequestMapping(value = "/{id}/edit/", method = PUT)
	public String edit(@PathVariable("id") String id, HttpServletRequest request) {
		try {

			Template entity = templateService.get(id);
			bind(request, entity);
			checkIdNotModified(id, entity.getId());

			Conf conf = confOnWeb.wrap(confsRepository.getConf());
			entity.selfAdjusting(conf).modify();
			success("模板修改成功");
		} catch (Exception e) {
			error("修改模版失败，请核对数据后重试");
		}

		return REDIRECT_LIST;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/generate/", method = PUT)
	@ResponseBody
	public JsonMessage generate() {

		try {
			List<Template> entites = templateService.query(new Object());
			Conf conf = confOnWeb.wrap(confsRepository.getConf());

			for (Template entity : entites) {
				entity.selfAdjusting(conf).generate();
			}

			return JsonMessage.one().success();
		} catch (Exception e) {
			return JsonMessage.one().error().message(e.getMessage());
		}
	}

	@Override
	@RequestMapping(value = "/{id}/delete/", method = DELETE)
	public String delete(@PathVariable("id") String id) {
		Conf conf = confOnWeb.wrap(confsRepository.getConf());
		templateService.get(id).selfAdjusting(conf).delete();
		return REDIRECT_LIST;
	}

	@Override
	@RequestMapping(value = "/delete/", method = DELETE)
	public String delete(HttpServletRequest request) {
		for (String item : Lang.nullSafe(request.getParameterValues("items"), new String[] {})) {
			delete(item);
		}

		return REDIRECT_LIST;
	}

	@Override
	protected String getViewPackage() {
		return "template";
	}
}

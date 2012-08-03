package com.kissme.mimo.interfaces;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.kissme.core.filecommand.UnzipFileCommand;
import com.kissme.core.orm.Page;
import com.kissme.core.web.controller.CrudControllerSupport;
import com.kissme.lang.Each;
import com.kissme.lang.Files;
import com.kissme.lang.Files.FileType;
import com.kissme.lang.Lang;
import com.kissme.lang.Preconditions;
import com.kissme.lang.file.FileCommandInvoker;
import com.kissme.lang.file.WriteBytesToFileCommand;
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

	@RequestMapping(value = "/upload/", method = GET)
	public String upload() {
		return getViewPackage().concat("/upload");
	}

	@RequestMapping(value = "/upload/", method = POST)
	public String upload(@RequestParam("file") MultipartFile file, @RequestParam("encoding") final String encoding,
							@RequestParam("fileencoding") final String fileEncoding, @RequestParam("suffixes") final String[] supportSuffixes) {

		try {

			byte[] content = file.getBytes();
			checkZipFileType(content);

			File temp = File.createTempFile("template", ".zip");
			final Conf conf = confOnWeb.wrap(confsRepository.getConf());
			final File templatedir = new File(conf.getTemplatePath());

			long current = System.currentTimeMillis();
			new FileCommandInvoker().command(new WriteBytesToFileCommand(temp, content))
									.command(new UnzipFileCommand(temp, templatedir, encoding))
									.invoke();

			File[] templatefiles = listUploadTemplateFiles(templatedir, supportSuffixes, current);
			Lang.each(templatefiles, new Each<File>() {

				@Override
				public void invoke(int index, File file) {

					Template template = convertToTemplate(templatedir, file, fileEncoding);
					Template existTemplate = templateService.lazyGetByName(template.getName());
					if (null == existTemplate) {
						template.selfAdjusting(conf).create();
						return;
					}

					existTemplate.setEncode(template.getEncode())
								 .setContent(template.getContent())
								 .selfAdjusting(conf).modify();
				}

			});

			success("上传模版成功");
		} catch (Exception e) {
			error("上传模版失败，请核对数据（只支持zip压缩文件）重试");
		}

		return REDIRECT_LIST;

	}

	private void checkZipFileType(byte[] data) {
		Preconditions.isTrue(FileType.ZIP == Files.guessType(data));
	}

	private File[] listUploadTemplateFiles(final File templatedir, final String[] supportSuffixes, final long current) {

		return Files.list(templatedir, new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}

				if (pathname.lastModified() < current) {
					return false;
				}

				String suffix = Files.suffix(pathname.getName());
				for (String support : supportSuffixes) {
					if (StringUtils.equalsIgnoreCase(support, suffix)) {
						return true;
					}
				}
				return false;
			}
		});
	}

	protected Template convertToTemplate(File templatedir, File file, String encoding) {
		String filepath = Files.canonical(file);
		String templatepath = Files.canonical(templatedir);
		int prefixIndex = StringUtils.indexOf(filepath, templatepath);
		if (prefixIndex == -1) {
			throw Lang.impossiable();
		}

		filepath = StringUtils.substringAfter(filepath, templatepath);
		filepath = Files.asUnix(filepath);
		if (filepath.startsWith(Files.UNIX_SEPERATOR)) {
			filepath = filepath.substring(1);
		}

		int dot = StringUtils.lastIndexOf(filepath, ".");
		if (dot == -1) {
			throw Lang.impossiable();
		}

		String templatename = filepath.substring(0, dot);
		String templatecontent = Files.read(file, encoding);
		return new Template().setName(templatename).setContent(templatecontent).setEncode(encoding);
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

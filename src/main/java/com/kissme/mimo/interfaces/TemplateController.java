package com.kissme.mimo.interfaces;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Set;

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

import com.google.common.collect.ImmutableList;
import com.kissme.core.filecommand.UnzipFileCommand;
import com.kissme.core.helper.RichHtmlHelper;
import com.kissme.core.orm.Page;
import com.kissme.core.web.controller.CrudControllerSupport;
import com.kissme.lang.Each;
import com.kissme.lang.Files;
import com.kissme.lang.Files.FileType;
import com.kissme.lang.Lang;
import com.kissme.lang.Preconditions;
import com.kissme.lang.file.DeleteFileCommand;
import com.kissme.lang.file.FileCommandInvoker;
import com.kissme.lang.file.WriteBytesToFileCommand;
import com.kissme.mimo.application.template.TemplateService;
import com.kissme.mimo.domain.Conf;
import com.kissme.mimo.domain.ConfsRepository;
import com.kissme.mimo.domain.template.Template;
import com.kissme.mimo.domain.template.TemplateHelper;
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
	private static final List<String> SUPPORT_SUFFIXS = ImmutableList.of("ftl", "html", "htm", "txt");

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
							@RequestParam("suffixs") final String[] suffixs) {

		try {

			byte[] content = file.getBytes();
			checkZipFileType(content);
			checkSuffixs(suffixs);

			File temp = File.createTempFile("template", ".zip");
			final Conf conf = confOnWeb.wrap(confsRepository.getConf());
			final File templatedir = new File(conf.getTemplatePath());

			long current = System.currentTimeMillis();
			new FileCommandInvoker().command(new WriteBytesToFileCommand(temp, content))
									.command(new UnzipFileCommand(temp, templatedir, encoding))
									.invoke();

			final File[] templateFiles = listUploadFiles(templatedir, suffixs, current);
			final File[] resourceFiles = listUploadFiles(templatedir, new String[] { "css", "js", "jpg", "jpeg", "ico", "png", "gif", "bmp" }, current);

			Lang.each(templateFiles, new Each<File>() {

				@Override
				public void invoke(int index, File file) {

					Template template = convertToTemplate(templatedir, file);
					replaceTemplateContent(conf, template, resourceFiles);

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

	private void checkSuffixs(String[] suffixs) {
		if (!SUPPORT_SUFFIXS.containsAll(ImmutableList.copyOf(suffixs))) {
			throw new IllegalArgumentException("not supported template suffix");
		}
	}

	private File[] listUploadFiles(final File templatedir, final String[] suffixs, final long current) {

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
				for (String support : suffixs) {
					if (StringUtils.equalsIgnoreCase(support, suffix)) {
						return true;
					}
				}
				return false;
			}
		});
	}

	protected Template convertToTemplate(File templatedir, File file) {
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

		String templateName = filepath.substring(0, dot);
		// only accept utf-8
		String templateContent = Files.read(file, "UTF-8");
		new DeleteFileCommand(file).execute();
		return new Template().setName(templateName).setContent(templateContent).setEncode("UTF-8");
	}

	protected void replaceTemplateContent(Conf conf, Template template, File[] files) {

		String templateContent = template.getContent();
		Set<String> resources = RichHtmlHelper.populateStylesheets(templateContent);
		Set<String> javascripts = RichHtmlHelper.populateJavascripts(templateContent);
		Set<String> photos = RichHtmlHelper.populatePhotos(templateContent);

		resources.addAll(javascripts);
		resources.addAll(photos);

		replaceResourcePaths(conf, template, files, resources);

	}

	private void replaceResourcePaths(final Conf conf, final Template template, final File[] files, final Set<String> resources) {

		Lang.each(files, new Each<File>() {

			@Override
			public void invoke(int index, File which) {
				String filepath = Files.asUnix(Files.canonical(which));

				for (String url : resources) {

					if (!filepath.endsWith(Files.asUnix(url))) {
						continue;
					}

					String relativePath = StringUtils.substringAfter(filepath, Files.asUnix(conf.getRootPath()));
					relativePath = Files.asUnix(Files.join("/", conf.getContext(), relativePath));

					String content = TemplateHelper.replaceResources(template.getContent(), url, relativePath);
					template.setContent(content);
				}
			}

		});
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

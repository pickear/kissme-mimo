package com.kissme.mimo.application.resource.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.kissme.lang.Files;
import com.kissme.lang.Preconditions;
import com.kissme.lang.file.filter.AbstractCompoFileFilter;
import com.kissme.lang.file.filter.CompoFileFilter;
import com.kissme.mimo.domain.Conf;
import com.kissme.mimo.domain.resource.ResourceObject;
import com.kissme.mimo.domain.resource.ResourceObjectFactory;

/**
 * 
 * @author loudyn
 * 
 */
@Service("photo-resource-service")
public class PhotoResourceObjectServiceImpl extends ResourceServiceImpl {

	@Override
	protected CompoFileFilter createExtensionFileFilter(final Conf conf) {
		return new AbstractCompoFileFilter() {

			@Override
			public boolean accept(File testFile) {
				if (testFile.isDirectory()) {
					return true;
				}

				String extension = Files.suffix(testFile.getName());
				return conf.isAllowedPhotoTypes(extension);
			}
		};
	}

	@Override
	protected ResourceObject createSingleResourceBean(File file, Conf conf) throws IOException {
		String fileCanonicalPath = file.getCanonicalPath();
		String relativePath = StringUtils.substringAfter(fileCanonicalPath, getResourcePath(conf));
		String fullRelativePath = StringUtils.substringAfter(fileCanonicalPath, conf.getRootPath());

		boolean precondition = relativePath.length() < fileCanonicalPath.length();
		Preconditions.isTrue(precondition, new RuntimeException("Can't get the ResourceBean path!"));

		return ResourceObjectFactory.newPhotoResourceObject(file).setFullRelativePath(Files.asUnix(fullRelativePath)).setPath(Files.asUnix(relativePath));
	}

	@Override
	protected String getResourcePath(Conf conf) {
		return conf.getPhotoPath();
	}

}
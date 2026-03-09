package io.github.jokeray.mediatr4j.spring;

import io.github.jokeray.mediatr4j.NotificationHandler;
import io.github.jokeray.mediatr4j.PipelineBehavior;
import io.github.jokeray.mediatr4j.RequestExceptionAction;
import io.github.jokeray.mediatr4j.RequestExceptionHandler;
import io.github.jokeray.mediatr4j.RequestHandler;
import io.github.jokeray.mediatr4j.RequestPostProcessor;
import io.github.jokeray.mediatr4j.RequestPreProcessor;
import io.github.jokeray.mediatr4j.StreamPipelineBehavior;
import io.github.jokeray.mediatr4j.StreamRequestHandler;
import io.github.jokeray.mediatr4j.VoidRequestHandler;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class Mediatr4jComponentRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private static final Class<?>[] SUPPORTED_COMPONENT_TYPES = {
        RequestHandler.class,
        VoidRequestHandler.class,
        NotificationHandler.class,
        StreamRequestHandler.class,
        PipelineBehavior.class,
        StreamPipelineBehavior.class,
        RequestPreProcessor.class,
        RequestPostProcessor.class,
        RequestExceptionAction.class,
        RequestExceptionHandler.class
    };

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Set<String> basePackages = resolveBasePackages(importingClassMetadata);
        if (basePackages.isEmpty()) {
            return;
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        for (Class<?> supportedType : SUPPORTED_COMPONENT_TYPES) {
            scanner.addIncludeFilter(new AssignableTypeFilter(supportedType));
        }

        for (String basePackage : basePackages) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                String className = candidate.getBeanClassName();
                if (!StringUtils.hasText(className) || registry.containsBeanDefinition(className)) {
                    continue;
                }
                registry.registerBeanDefinition(className, candidate);
            }
        }
    }

    private Set<String> resolveBasePackages(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableMediatr4j.class.getName(), false);
        Set<String> packages = new LinkedHashSet<>();
        if (attributes == null) {
            packages.add(ClassUtils.getPackageName(metadata.getClassName()));
            return packages;
        }

        for (String basePackage : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(basePackage)) {
                packages.add(basePackage);
            }
        }

        for (Class<?> basePackageClass : (Class<?>[]) attributes.get("basePackageClasses")) {
            packages.add(ClassUtils.getPackageName(basePackageClass));
        }

        if (packages.isEmpty()) {
            packages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }
        return packages;
    }
}

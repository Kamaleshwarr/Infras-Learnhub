package com.company.learninghub.communication.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

/**
 * Dedicated Thymeleaf engines for communication email templates only.
 * Does not configure Spring MVC view resolution.
 */
@Configuration
public class CommunicationEmailTemplateConfiguration {

    private static final String TEMPLATE_PREFIX = "templates/communication/";

    @Bean(name = "communicationHtmlTemplateEngine")
    public SpringTemplateEngine communicationHtmlTemplateEngine() {
        ClassLoaderTemplateResolver resolver = baseResolver(TemplateMode.HTML, ".html");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean(name = "communicationTextTemplateEngine")
    public SpringTemplateEngine communicationTextTemplateEngine() {
        ClassLoaderTemplateResolver resolver = baseResolver(TemplateMode.TEXT, ".txt");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    private ClassLoaderTemplateResolver baseResolver(TemplateMode mode, String suffix) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATE_PREFIX);
        resolver.setSuffix(suffix);
        resolver.setTemplateMode(mode);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCheckExistence(true);
        resolver.setCacheable(true);
        return resolver;
    }
}

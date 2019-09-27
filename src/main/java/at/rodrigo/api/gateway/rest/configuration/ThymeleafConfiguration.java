package at.rodrigo.api.gateway.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;

import java.util.Collection;


@Configuration
public class ThymeleafConfiguration {

    public static final String TEMPLATES_BASE = "classpath:/templates/";

    /* Parameter keys for all the message templates. */
    public static final String USER = "user";
    public static final String DESCRIPTION = "description";
    public static final String MAIN_PANEL_TITLE = "mainPanelTitle";
    public static final String DASHBOARD_TITLE = "dashboardTitle";

    //Panel
    public static final String PANEL_ID = "id";
    public static final String PANEL_GRID_POSITION = "gridPostX";
    public static final String TARGET_EXPRESSION = "targetExpression";
    public static final String INSTANCE = "instance";
    public static final String PANEL_TITLE = "panelTitle";

    @Bean
    public SpringResourceTemplateResolver jsonMessageTemplateResolver() {
        SpringResourceTemplateResolver theResourceTemplateResolver = new SpringResourceTemplateResolver();
        theResourceTemplateResolver.setPrefix(TEMPLATES_BASE);
        theResourceTemplateResolver.setSuffix(".json");
        theResourceTemplateResolver.setTemplateMode("json");
        theResourceTemplateResolver.setCharacterEncoding("UTF-8");
        theResourceTemplateResolver.setCacheable(false);
        theResourceTemplateResolver.setOrder(2);
        return theResourceTemplateResolver;
    }

    @Bean
    public SpringTemplateEngine messageTemplateEngine(final Collection<SpringResourceTemplateResolver> inTemplateResolvers) {
        final SpringTemplateEngine theTemplateEngine = new SpringTemplateEngine();
        for (SpringResourceTemplateResolver theTemplateResolver : inTemplateResolvers) {
            theTemplateEngine.addTemplateResolver(theTemplateResolver);
        }
        return theTemplateEngine;
    }
}
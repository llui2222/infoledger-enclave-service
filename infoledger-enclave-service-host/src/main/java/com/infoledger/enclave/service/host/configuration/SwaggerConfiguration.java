package com.infoledger.enclave.service.host.configuration;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.schema.AlternateTypeRules.newRule;

/**
 * Configuration for swagger
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration implements WebMvcConfigurer {

    private final TypeResolver typeResolver;

    public SwaggerConfiguration(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Bean
    public Docket docket() {
        AlternateTypeRule rule = newRule(
                typeResolver.resolve(
                        DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)
                ),
                typeResolver.resolve(WildcardType.class)
        );
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("/")
                .genericModelSubstitutes(ResponseEntity.class)
                .alternateTypeRules(rule)
                .useDefaultResponseMessages(false)
                .apiInfo(new ApiInfoBuilder()
                        .title("InfoLedger Enclave Service Host Documentation Swagger API")
                        .description("This is a InfoLedger Enclave Service Host server endpoints. "
                                + "You can find here which kinds of requests they require "
                                + "and which responses they returns. Enjoy)))")
                        .version("v1")
                        .build()
                );
    }

    /**
     * Swagger view configuration
     *
     * @param registry view controller registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/swagger", "/swagger-ui.html");
    }

    /**
     * Resources for swagger
     *
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (!registry.hasMappingForPattern("/**")) {
            registry.addResourceHandler("/**").addResourceLocations(
                    "classpath:/META-INF/resources/");
        }
    }
}

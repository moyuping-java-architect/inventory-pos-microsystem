package com.psi.common.doc.config;

import com.psi.common.doc.properties.DocProperties;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiAutoConfig {

    private final DocProperties docProperties;

    public OpenApiAutoConfig(DocProperties docProperties) {
        this.docProperties = docProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(docProperties.getTitle() + " - " + (docProperties.getServiceName() != null ? docProperties.getServiceName() : "Service"))
                        .version(docProperties.getVersion())
                        .description(docProperties.getDescription())
                        .contact(new Contact()
                                .name(docProperties.getContactName())
                                .email(docProperties.getContactEmail()))
                        .license(new License()
                                .name("PSI License")
                                .url("https://psi-retail.com/license")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Gateway"),
                        new Server().url("http://localhost:" + getPort()).description("Direct")));
    }

    private String getPort() {
        String port = System.getProperty("server.port");
        return port != null ? port : "8080";
    }
}

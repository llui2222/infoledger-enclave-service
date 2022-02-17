package com.infoledger.enclave.service.host.configuration.factory;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Properties;

/**
 * Yaml properties resolver
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {
    @Override
    @Nonnull
    public PropertySource<?> createPropertySource(@Nullable String name, @NonNull EncodedResource encodedResource) {
        Objects.requireNonNull(encodedResource, "EncodedResource must not be null.");

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource(Objects.requireNonNull(encodedResource.getResource().getFilename()),
                Objects.requireNonNull(properties));
    }
}
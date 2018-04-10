/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.context.env;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.Toggleable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * An abstract implementation of the {@link PropertySourceLoader} interface
 *
 * @author Graeme Rocher
 * @since 1.0
 */
public abstract class AbstractPropertySourceLoader implements PropertySourceLoader, Toggleable, Ordered {

    /**
     * Default position for the property source loader
     */
    public static final int DEFAULT_POSITION = EnvironmentPropertySource.POSITION - 100;

    @Override
    public int getOrder() {
        return DEFAULT_POSITION;
    }

    @Override
    public Optional<PropertySource> load(String resourceName, ResourceLoader resourceLoader, String environmentName) {
        if(isEnabled()) {
            Set<String> extensions = getExtensions();
            for (String ext : extensions) {
                String fileName = resourceName;
                if (environmentName != null) {
                    fileName += "-" + environmentName;
                }
                String qualifiedName = fileName;
                fileName += "." + ext;
                Map<String,Object> finalMap = loadProperties(resourceLoader, qualifiedName, fileName);

                int order = this.getOrder();
                if (environmentName != null) {
                    order++; // higher precedence than the default
                }
                if (!finalMap.isEmpty()) {
                    int finalOrder = order;
                    MapPropertySource newPropertySource = new MapPropertySource(qualifiedName, finalMap) {
                        @Override
                        public int getOrder() {

                            return finalOrder;
                        }
                    };
                    return Optional.of(newPropertySource);
                }
            }
        }

        return Optional.empty();
    }

    private Map<String, Object> loadProperties(ResourceLoader resourceLoader, String qualifiedName, String fileName) {
        Optional<InputStream> config = readInput(resourceLoader, fileName);
        if(config.isPresent()) {
            try(InputStream input = config.get()) {
                return read(qualifiedName, input);
            } catch (IOException e) {
                throw new ConfigurationException("I/O exception occurred reading [" + fileName + "]: " + e.getMessage(), e);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> read(String name, InputStream input) throws IOException {
        Map<String, Object> finalMap = new LinkedHashMap<>();
        processInput(name, input, finalMap);
        return finalMap;
    }

    protected Optional<InputStream> readInput(ResourceLoader resourceLoader, String fileName) {
        return resourceLoader.getResourceAsStream(fileName);
    }

    protected abstract void processInput(String name, InputStream input, Map<String, Object> finalMap) throws IOException;

    protected void processMap(Map<String, Object> finalMap, Map map, String prefix) {
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (value instanceof Map) {
                processMap(finalMap, (Map) value, prefix + key + '.');
            } else {
                finalMap.put(prefix + key, value);
            }
        }
    }
}

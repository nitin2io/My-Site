package com.mysite.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Weather Service Configurations",
        description = "Configuration service for weather component")
public @interface WeatherConfig {

    @AttributeDefinition(name = "Weather API Key", description = "Weather API Key")
    String getApiKey() default "eded0545a3af9ff5fe4812bce33305ea";

    @AttributeDefinition(name = "Weather API Key", description = "Weather API Key")
    String getApiEndPoint() default "https://api.openweathermap.org/data/2.5/weather";
}

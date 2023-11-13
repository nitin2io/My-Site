package com.mysite.core.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysite.core.beans.weather.WeatherList;
import com.mysite.core.config.WeatherConfig;
import com.mysite.core.services.WeatherService;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.http.util.EntityUtils;
import org.apache.sling.api.resource.*;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component(service = WeatherService.class, immediate = true)
@Designate(ocd = WeatherConfig.class)
public class WeatherServiceImpl implements WeatherService {

    private static final int TIMEOUT = 5000;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private WeatherConfig weatherConfig;
    @Reference
    private transient HttpClientBuilderFactory clientBuilderFactory;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Activate
    @Modified
    protected final void activate(WeatherConfig weatherConfig) {
        this.weatherConfig = weatherConfig;
    }

    @Override
    public double getTemperature(String city) throws IOException {
        double temperature = 0;
        StringBuilder builder = new StringBuilder();
        builder.append(weatherConfig.getApiEndPoint()).append("?")
                .append("q=").append(city)
                .append("&appid=").append(weatherConfig.getApiKey())
                .append("&units=metric");
        try (CloseableHttpClient client = clientBuilderFactory.newBuilder()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(TIMEOUT)
                        .setConnectTimeout(TIMEOUT)
                        .build())
                .build()) {
            HttpGet httpGet = new HttpGet(builder.toString());

            try (CloseableHttpResponse httpClientResponse = client.execute(httpGet)) {
                String responseBody = EntityUtils.toString(httpClientResponse.getEntity(), StandardCharsets.UTF_8);

                if (httpClientResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try (JsonReader jsonReader = Json.createReader(new StringReader(responseBody))) {
                        JsonObject jsonObject = jsonReader.readObject();
                        ObjectMapper objectMapper = new ObjectMapper();
                        WeatherList weatherList = objectMapper.readValue(jsonObject.toString(), WeatherList.class);
                        temperature = weatherList.getMain().getTemp();
                    }
                } else {
                    logger.error("Error in fetching weather data from API status code = {}",
                            httpClientResponse.getStatusLine().getStatusCode());
                }
            } catch (ConnectTimeoutException e) {
                logger.error("Connection to the server timed out.");
            }
        }
        return temperature;
    }

    @Override
    public double getTemperatureStored(String city) throws LoginException {
        double temperature = 0;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(ResourceResolverFactory.SUBSERVICE, "weather-service-user");
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(userMap);
            Resource weatherResource = resourceResolver.getResource("/content/weather/jcr:content");
            if (null != weatherResource) {
                ValueMap vMap = weatherResource.getValueMap();
                if (null != city && vMap.containsKey(city)) {
                    temperature = (double) vMap.get(city);
                }
            }
            return temperature;
        } finally {
            if (null != resourceResolver && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }
}

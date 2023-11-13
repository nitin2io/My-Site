package com.mysite.core.jobs;

import com.mysite.core.services.WeatherService;
import org.apache.sling.api.resource.*;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component(service = JobConsumer.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "= Weather Data Update Job",
                JobConsumer.PROPERTY_TOPICS + "=weather/job/update"
        })
public class WeatherUpdateJob implements JobConsumer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Reference
    WeatherService weatherService;
    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public JobResult process(Job job) {
        try {
            logger.info("Weather Data Update Job Called *******");

            updateWeatherData((String) job.getProperty("weatherResourcePath"));

            return JobConsumer.JobResult.OK;
        } catch (Exception e) {
            logger.error("Exception in Weather Data Update Job", e);
            return JobResult.FAILED;
        }
    }

    private void updateWeatherData(String weatherResourcePath) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put(ResourceResolverFactory.SUBSERVICE, "weather-service-user");
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getServiceResourceResolver(userMap);
            Resource weatherResource = resourceResolver.getResource(weatherResourcePath);
            assert weatherResource != null;
            ModifiableValueMap modifiableValueMap = weatherResource.adaptTo(ModifiableValueMap.class);
            if (null != modifiableValueMap) {
                List<String> cityList = getCityList(resourceResolver, "/content/dam/mysite/weather/cityDropdown.json");
                for (String city : cityList) {
                    modifiableValueMap.put(city, weatherService.getTemperature(city));
                }
            }
            resourceResolver.commit();
        } catch (LoginException e) {
            logger.error("LoginException in updating weather data", e);
        } catch (PersistenceException | RepositoryException e) {
            logger.error("PersistenceException in updating weather data", e);
            throw new RuntimeException(e);
        } catch (IOException | JSONException e) {
            logger.error("IOException in updating weather data", e);
            throw new RuntimeException(e);
        } finally {
            if (null != resourceResolver && resourceResolver.isLive()) {
                resourceResolver.close();
            }
        }
    }

    private List<String> getCityList(ResourceResolver resourceResolver, String path) throws RepositoryException, IOException, JSONException {
        Resource jsonResource = resourceResolver.getResource(path + "/jcr:content/renditions/original/jcr:content");
        List<String> cityList = new ArrayList<>();
        if (null != jsonResource) {
            Node jsonNode = jsonResource.adaptTo(Node.class);
            assert jsonNode != null;
            InputStream inputStream = jsonNode.getProperty("jcr:data").getBinary().getStream();

            StringBuilder stringBuilder = new StringBuilder();
            String eachLine;
            assert inputStream != null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            while ((eachLine = bufferedReader.readLine()) != null) {
                stringBuilder.append(eachLine);
            }

            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            Iterator<String> jsonKeys = jsonObject.keys();
            while (jsonKeys.hasNext()) {
                String jsonKey = jsonKeys.next();
                String jsonValue = jsonObject.getString(jsonKey);
                cityList.add(jsonValue);
            }
        }
        return cityList;
    }
}

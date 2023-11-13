/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mysite.core.models;

import com.mysite.core.services.WeatherService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Model(adaptables = {Resource.class, SlingHttpServletRequest.class},
        resourceType = WeatherModel.RESOURCE_TYPE,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class WeatherModel {

    static final String RESOURCE_TYPE = "mysite/components/weather";

    @SlingObject
    private Resource currentResource;

    @ValueMapValue
    private String city;

    private double temperature;
    private double temperatureF;

    private List<String> images = null;

    private int imageListSize;

    @OSGiService
    private WeatherService weatherService;

    @PostConstruct
    protected void init() throws LoginException {
        temperature = weatherService.getTemperatureStored(city);
        temperatureF = temperature * 9 / 5 + 32;
        Resource listResource = currentResource.getChild("images");
        if (null != listResource) {
            images = new ArrayList<>();
            Iterator<Resource> itr = listResource.listChildren();
            while (itr.hasNext()) {
                Resource resource = itr.next();
                ValueMap map = resource.getValueMap();
                if (map.containsKey("image")) {
                    images.add((String) map.get("image"));
                }
            }
            imageListSize = images.size();
        }
    }

    public String getCity() {
        return city;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getTemperatureF() {
        return temperatureF;
    }

    public List<String> getImages() {
        if (null == images) {
            return Collections.emptyList();
        }
        return new ArrayList<>(images);
    }

    public int getImageListSize() {
        return imageListSize;
    }
}

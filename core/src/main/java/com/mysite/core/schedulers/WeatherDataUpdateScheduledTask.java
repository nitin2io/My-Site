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
package com.mysite.core.schedulers;

import com.mysite.core.services.WeatherService;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Designate(ocd = WeatherDataUpdateScheduledTask.Config.class)
@Component(service = Runnable.class)
public class WeatherDataUpdateScheduledTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String weatherResource;

    @Reference
    private WeatherService weatherService;

    @Reference
    private JobManager jobManager;

    @Override
    public void run() {
        logger.info("SimpleScheduledTask is now running, weatherResource='{}'", weatherResource);
        final Map<String, Object> props = new HashMap<>();
        props.put("weatherResourcePath", weatherResource);
        jobManager.addJob("weather/job/update", props);
        logger.info("Weather Update Job was added at {}", System.currentTimeMillis());
    }

    @Activate
    protected void activate(final Config config) {
        weatherResource = config.weatherResource();
    }

    @ObjectClassDefinition(name = "A scheduled task to update weather data",
            description = "Scheduler for cron-job with properties")
    public static @interface Config {

        @AttributeDefinition(name = "Cron-job expression")
        String scheduler_expression() default "0 0 0/1 1/1 * ? *";

        @AttributeDefinition(name = "Concurrent task",
                description = "Whether or not to schedule this task concurrently")
        boolean scheduler_concurrent() default false;

        @AttributeDefinition(name = "Weather Resource",
                description = "Can be configured in /system/console/configMgr")
        String weatherResource() default "/content/weather/jcr:content";
    }

}

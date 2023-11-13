package com.mysite.core.models;

import com.day.cq.wcm.api.Page;
import com.google.common.collect.ImmutableMap;
import com.mysite.core.services.WeatherService;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class WeatherModelTest {

    private final AemContext context = new AemContext();

    private WeatherModel weatherModel;

    private Page page;
    private Resource resource;

    @Mock
    private WeatherService weatherService;

    @BeforeEach
    void setup() throws Exception {
        context.addModelsForClasses(WeatherModel.class);
        context.registerService(WeatherService.class, weatherService);

     //    context.load().json("/com/mysite/core/images.json", "/content/dam/mysite/weather/cologne");
     //    context.load().json("/com/mysite/core/city.json", "/content/mysite/us/en/cologne/jcr:content/root/container/weather");

        Resource resource = context.create().resource("/content/mysite/us/en/cologne/jcr:content/root/container/weather", new ValueMapDecorator(ImmutableMap.<String, Object>of(
                "city", "Cologne",
                "sling:resourceType", "mysite/components/weather")));

        weatherModel = resource.adaptTo(WeatherModel.class);
    }

    @Test
    void testModel() throws Exception {
        assertNotNull(weatherModel);
        assertEquals(0.0, weatherModel.getTemperature());
        assertEquals(32.0, weatherModel.getTemperatureF());
    }

}
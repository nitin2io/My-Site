package com.mysite.core.services;

import org.apache.sling.api.resource.LoginException;

import java.io.IOException;

public interface WeatherService {
    double getTemperature(String city) throws IOException;

    double getTemperatureStored(String city) throws LoginException;
}

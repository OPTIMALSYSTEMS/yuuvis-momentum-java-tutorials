package com.os.services.webhook.services;

import com.os.services.webhook.config.HookException;
import com.os.services.webhook.util.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Service
public class PropertyValidationService {
    @Autowired
    private RestTemplate restTemplate;

    private final static Logger LOGGER = LoggerFactory.getLogger(PropertyValidationService.class);

    final String propertyId = "Name";
    final String catalogueName = "germancities";

    public void validateObject(Map<String, Object> dmsApiObjectList, String authorization) throws HookException
    {
        String propertyValue = null;

        //Get object properties and send for validation
        List<Map<String, Object>> newObjectList = (List<Map<String, Object>>) dmsApiObjectList.get("objects");

        for (Map<String, Object> newObject : newObjectList) {
            propertyValue = (String) PropertyUtils.getPropertyValue(newObject, propertyId);
            validateProperty(propertyValue, authorization);
        }
    }

    private void validateProperty (String propertyValue, String authorization) throws HookException {
        //Validate property with catalogue entry
        if (propertyValue != null) {
            if (!propertyValue.isBlank()) {
                String url = "http://catalog/api/catalogs/" + catalogueName + "/" + propertyValue;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", authorization);

                HttpEntity requestEntity = new HttpEntity<>(null, headers);

                try {
                    ResponseEntity<Void> response =
                            this.restTemplate.exchange(url, HttpMethod.HEAD, requestEntity, Void.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        LOGGER.debug("Property [" + propertyValue + "] validation was successful");
                    } else {
                        throw new HookException("Catalogue [" + catalogueName + "] does not exist or property [" + propertyValue + "] does not match any catalogue entry.");
                    }
                } catch (HttpStatusCodeException e) {
                    if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                        throw new HookException("Catalogue [" + catalogueName + "] does not exist or property [" + propertyValue + "] does not match any catalogue entry.");
                    } else {
                        //Error handling
                        throw new HookException("Something went wrong.");
                    }
                }
            } else {
                throw new HookException("The property [" + propertyId + "] is empty.");
            }
        } else {
            LOGGER.debug("This object does not have the property: " + propertyId);
        }
    }
}

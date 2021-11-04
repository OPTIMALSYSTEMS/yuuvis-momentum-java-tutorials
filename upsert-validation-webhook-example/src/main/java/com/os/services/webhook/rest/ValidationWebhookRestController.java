package com.os.services.webhook.rest;
import java.util.Map;

import com.os.services.webhook.config.HookException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;

import com.os.services.webhook.services.PropertyValidationService;

@RestController
@RequestMapping("/api/dms/request")
public class ValidationWebhookRestController
{
    @Autowired
    private PropertyValidationService propertyValidationService;

    @PostMapping(value = "/upsert", produces = {"application/json"})
    public Map<String, Object> validateObjectProperty (@RequestBody Map<String, Object> dmsApiObjectList,
                                                @RequestHeader(value = "Authorization", required = true) String authorization) throws HookException {
        this.propertyValidationService.validateObject(dmsApiObjectList, authorization);
        return dmsApiObjectList;
    }
}
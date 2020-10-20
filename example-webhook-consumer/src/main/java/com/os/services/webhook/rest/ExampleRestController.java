package com.os.services.webhook.rest;
import java.util.List;
import java.util.Map;
 
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api/dms/request")
public class ExampleRestController
{
    @PostMapping(value = "/update/metadata", produces = {"application/json"})
    public Map<String, Object> updateDmsObjectMetadata(@RequestBody Map<String, Object> dmsApiObjectList, 
                                                       @RequestHeader(value = "Authorization", required = true) String authorization)
    {
        doSomething(dmsApiObjectList, authorization);
        return dmsApiObjectList;
    }

    @PostMapping(value = "/retrieve/filter", produces = {"application/json"})
    public Map<String, Object> filterSearchResult(@RequestBody Map<String, Object> dmsApiObjectList,
                                                  @RequestHeader(value = "Authorization", required = true) String authorization)
    {
        Map<String, Object> filteredDmsApiObjectList = filterSomething(dmsApiObjectList, authorization);
        return filteredDmsApiObjectList;
    }
     
    private void doSomething(Map<String, Object> dmsApiObjectList, String authorization)
    {
        Object nameValue = getProperty(dmsApiObjectList);
        System.out.println(nameValue.toString());
    }

    private Map<String, Object> filterSomething(Map<String, Object> dmsApiObjectList, String authorization)
    {
        Map<String, Object> filteredDmsApiObjectList = filterExcludedProperty(dmsApiObjectList);
        System.out.println(filteredDmsApiObjectList);
        return filteredDmsApiObjectList;
    }

    public Object getProperty(Map<String, Object> dmsApiObjectList)
    {
        String propertyId = "Name";

        List<Map<String, Object>> newObjectList = (List<Map<String, Object>>) dmsApiObjectList.get("objects");
        Map<String, Object> newObject = newObjectList.get(0);
        Map<String, Object> properties = (Map<String, Object>)newObject.get("properties");

        Object value = getValue(properties, propertyId);
        if(value != null)
        {
            return value;
        }

        for(String currentPropertyId : properties.keySet())
        {
            if(propertyId.equalsIgnoreCase(currentPropertyId))
            {
                value = getValue(properties, currentPropertyId);
                if(value != null)
                {
                    return value;
                }
            }

            if(propertyId.contains(":"))
            {
                String propertyIdWithoutPrefix = propertyId.substring(propertyId.indexOf(':') + 1);
                if(propertyIdWithoutPrefix.equalsIgnoreCase(currentPropertyId))
                {
                    value = getValue(properties, currentPropertyId);
                    if(value != null)
                    {
                        return value;
                    }
                }
            }
        }
        return null;
    }

    private Object getValue(Map<String, Object> properties, String propertyId)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> property = (Map<String, Object>)properties.get(propertyId);
        if(property != null)
        {
            return property.get("value");
        }
        return null;
    }

    public Map<String, Object> filterExcludedProperty(Map<String, Object> dmsApiSearchResult)
    {
        List<Map<String, Object>> objectsList = (List<Map<String, Object>>)dmsApiSearchResult.get("objects");

        for(Map<String, Object> dmsApiObject : objectsList)
        {
            Object properties = dmsApiObject.get("properties");

            if(properties != null)
            {
                ((Map<String, Object>)properties).remove("appEmail:bcc");
            }
        }

        Map<String, Object> filteredSearchResult = dmsApiSearchResult;
        filteredSearchResult.replace("objects", objectsList);
        return filteredSearchResult;
    }
}
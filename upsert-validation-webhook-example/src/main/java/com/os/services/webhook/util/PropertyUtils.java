package com.os.services.webhook.util;

import java.util.Map;

public class PropertyUtils
{
    public static Object getPropertyValue(Map<String, Object> object, String propertyId)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>)object.get("properties");

        return getValue(properties, propertyId);
    }

    private static Object getValue(Map<String, Object> properties, String propertyId)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> property = (Map<String, Object>)properties.get(propertyId);
        if(property != null)
        {
            return property.get("value");
        }
        return null;
    }
}

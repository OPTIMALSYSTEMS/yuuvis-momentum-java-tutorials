package com.os.services.webhook.rest;
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
     
    private void doSomething(Map<String, Object> dmsApiObjectList, String authorization)
    {
        //...
    }
}
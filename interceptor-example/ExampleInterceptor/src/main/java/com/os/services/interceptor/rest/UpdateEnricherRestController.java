package com.os.services.interceptor.rest;

import com.os.services.interceptor.updateenricher.UpdateEnricherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UpdateEnricherRestController {
    @Autowired
    private UpdateEnricherService updateEnricherService;

    @PostMapping(value = "/dms/objects/{objectId}/update", headers = "content-type=application/json")
    public void enrichedUpdate(@RequestBody Map<String, Object> dmsApiObjectList,
                               @PathVariable("objectId") String objectId,
                               @RequestHeader(value = "Authorization", required = false) String auth,
                               HttpServletRequest servletRequest,
                               HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        updateEnricherService.enrichMetadata(servletResponse.getOutputStream(), dmsApiObjectList, objectId, auth);
    }
}

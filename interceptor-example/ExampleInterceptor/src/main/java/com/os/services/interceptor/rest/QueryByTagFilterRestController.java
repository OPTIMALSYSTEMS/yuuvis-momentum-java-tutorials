package com.os.services.interceptor.rest;

import com.os.services.interceptor.queryByTagFilterer.QueryFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QueryByTagFilterRestController {
    @Autowired
    private QueryFilterService queryFilterService;

    @PostMapping(value = "/dms/objects/search", headers = "content-type=application/json")
    public void searchByPostedQuery(@RequestBody Map<String, Object> incomingQuery,
                                    @RequestHeader(value = "Authorization", required = false) String auth,
                                    HttpServletRequest servletRequest,
                                    HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        queryFilterService.filterQueryByTag(servletResponse.getOutputStream(), incomingQuery, auth);

    }
}

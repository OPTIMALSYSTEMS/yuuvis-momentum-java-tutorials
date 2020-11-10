
package com.os.services.interceptor.rest;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.os.services.interceptor.pdfextractor.PdfPageSelectorService;

@RestController
@RequestMapping("/api")
public class PdfPageSelectorRestController
{
    @Autowired
    private PdfPageSelectorService pdfPageSelectorService;

   
    @PostMapping(value = "/dms/objects/{objectId}", headers = "content-type=application/json")
    public void getContentByPostRequest(@RequestBody Map<String, Object> dmsApiObjectList,
                                        @PathVariable("objectId") String objectId,
                                        @RequestHeader(value = "Authorization", required = false) String authorization,
                                        HttpServletRequest servletRequest,
                                        HttpServletResponse servletResponse) throws IOException
    {
        
        servletResponse.setContentType(MediaType.APPLICATION_PDF_VALUE);
        pdfPageSelectorService.extract(servletResponse.getOutputStream(), dmsApiObjectList, objectId, authorization);

    }

}

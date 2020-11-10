
package com.os.services.interceptor.pdfextractor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.os.services.interceptor.domain.Pages;

@Service
public class PdfPageSelectorService
{

    @Value("${repository.url}")
    private String repositoryUrl;
    
    @Value("${repository.useDiscovery}")
    private boolean useDiscovery;
  
    @Autowired
    RestTemplate restTemplate;

    private RestTemplate restTemplateStatic = new RestTemplate();
    
    private ObjectMapper objectMapper = new ObjectMapper();

    public void extract(OutputStream outputStream, Map<String, Object> requestObjects, String objectId, String authorization)
    {
        Pages pages = this.getPageBoundaries(requestObjects);

        RestTemplate restTemplate = useDiscovery ? this.restTemplate : this.restTemplateStatic;
        
        // @formatter:off
        restTemplate.execute(repositoryUrl + "/" + objectId,                                   
                                  HttpMethod.POST, 
                                  (ClientHttpRequest requestCallback) -> {
                                      if (StringUtils.hasLength(authorization))
                                      {
                                          // delegate auth header
                                          requestCallback.getHeaders().add(HttpHeaders.AUTHORIZATION, authorization);
                                      }
                                      requestCallback.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_PDF));
                                      requestCallback.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                                      requestCallback.getBody().write(this.objectMapper.writeValueAsString(requestObjects).getBytes("UTF-8"));
                                  }, 
                                  new StreamResponseExtractor(outputStream, pages.getStartPage(), pages.getEndPage()));
        // @formatter:on
    }

    @SuppressWarnings("unchecked")
    private Pages getPageBoundaries(Map<String, Object> requestObjects)
    {
        List<Map<String, Object>> list = (List<Map<String, Object>>)requestObjects.get("objects");
        Map<String, Object> dmsApiObject = list.get(0);
        List<Object> contentStreamObject = (List<Object>)dmsApiObject.get("contentStreams");
        String range = (String)((Map<String, Object>)contentStreamObject.get(0)).get("range");

        // "range" : "page:fromPage-endPage"

        if (range.startsWith("page:"))
        {
            ((Map<String, Object>)contentStreamObject.get(0)).remove("range");
            String[] bounds = range.substring("page:".length()).split("-");
            int startPage = Integer.parseInt(bounds[0]);
            int endPage = Integer.parseInt(bounds[1]);

            return new Pages(startPage, endPage);

        }

        else
        {
            throw new IllegalArgumentException("no pages found in contentstream-range attribute");
        }

    }

    public class StreamResponseExtractor
        implements ResponseExtractor<Object>
    {

        private OutputStream outputStream;
        private int startPage;
        private int endPage;

        public StreamResponseExtractor(OutputStream outputStream, int startPage, int endPage)
        {
            this.outputStream = outputStream;
            this.startPage = startPage;
            this.endPage = endPage;
        }

        @Override
        public Object extractData(ClientHttpResponse clientHttpResponse) throws IOException
        {
            if (clientHttpResponse.getStatusCode().is2xxSuccessful())
            {
                PdfTools.extractPageFromStream(clientHttpResponse.getBody(), startPage, endPage, outputStream);
            }
            else
            {
                throw new IllegalStateException(clientHttpResponse.getStatusCode() + " " + clientHttpResponse.getStatusText());
            }
            return null;
        }
    }
}

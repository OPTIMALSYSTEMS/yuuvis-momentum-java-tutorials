package com.os.services.interceptor.queryByTagFilterer;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

@Service
public class QueryFilterService {
    private String searchUrl;

    @Value("${repository.useDiscovery}")
    private boolean useDiscovery;

    @Autowired
    RestTemplate restTemplate;

    private RestTemplate restTemplateStatic = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    public void filterQueryByTag(OutputStream outputStream, Map<String, Object> incomingQuery, String authorization) {
        RestTemplate restTemplate = useDiscovery ? this.restTemplate : this.restTemplateStatic;
        Map<String, Object> filteredQuery = enrichQueryByTagFilter(incomingQuery);
        restTemplate.execute(searchUrl,
                HttpMethod.POST,
                (ClientHttpRequest requestCallback) -> {
                    if (StringUtils.hasLength(authorization)) {
                        requestCallback.getHeaders().add(HttpHeaders.AUTHORIZATION, authorization);
                    }
                    requestCallback.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                    requestCallback.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    requestCallback.getBody().write(this.objectMapper.writeValueAsString(filteredQuery).getBytes("UTF-8"));
                },
                new SearchResponseExtractor(outputStream));
    }

    public Map<String, Object> enrichQueryByTagFilter(Map<String, Object> incomingQuery){
        String statement = String.valueOf(incomingQuery.get("statement"));
        String filteredStatement = statement + "AND system:tags[test] > 2";

        Map<String, Object> filteredQuery = incomingQuery;
        filteredQuery.replace("statement", (Object)filteredStatement);

        return filteredQuery;
    }

    /**
     * This class can be used for manipulating the search result.
     */
    public class SearchResponseExtractor implements ResponseExtractor<Object>{
        private OutputStream outputStream;

        public SearchResponseExtractor(OutputStream outputStream){
            this.outputStream = outputStream;
        }

        @Override
        public Object extractData(ClientHttpResponse clientHttpResponse) throws IOException {
            if(clientHttpResponse.getStatusCode().is2xxSuccessful())
            {
                outputStream.write(clientHttpResponse.getBody().readAllBytes());
            }
            else
            {
                throw new IllegalStateException(clientHttpResponse.getStatusCode() + " " + clientHttpResponse.getStatusText());
            }
            return null;
        }
    }
}

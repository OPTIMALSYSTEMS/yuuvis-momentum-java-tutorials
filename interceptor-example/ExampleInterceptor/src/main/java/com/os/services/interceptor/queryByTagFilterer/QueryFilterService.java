package com.os.services.interceptor.queryByTagFilterer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Map;

@Service
public class QueryFilterService {
    @Value("${search.url}")
    private String searchUrl;

    @Value("${search.useDiscovery}")
    private boolean useDiscovery;

    @Autowired
    RestTemplate restTemplate;

    private RestTemplate restTemplateStatic = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();



    public void filterQueryByTag(OutputStream outputStream, Map<String, Object> incomingQuery, String authorization) throws IOException {
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> enrichQueryByTagFilter(Map<String, Object> incomingQuery){
        Map<String, Object> queryMap = (Map<String, Object>)incomingQuery.get("query");
        String statement = String.valueOf(queryMap.get("statement"));
        String filteredStatement = "";
        if (statement.contains("WHERE")){
            filteredStatement = statement + " AND WHERE system:tags[\"test\"].state > 1";
        } else {
            filteredStatement = statement + " WHERE system:tags[\"test\"].state > 1";
        }
        queryMap.replace("statement", filteredStatement);

        Map<String, Object> filteredQuery = incomingQuery;
        filteredQuery.replace("query", queryMap);

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

package com.os.services.interceptor.updateenricher;

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
import java.util.List;
import java.util.Map;

@Service
public class UpdateEnricherService {
    @Value("${repository.url}")
    private String repositoryUrl;

    @Value("${repository.useDiscovery}")
    private boolean useDiscovery;

    @Autowired
    RestTemplate restTemplate;

    private RestTemplate restTemplateStatic = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    public void enrichMetadata(OutputStream outputStream, Map<String, Object> incomingMetadata, String objectId, String authorization) {
        RestTemplate restTemplate = useDiscovery ? this.restTemplate : this.restTemplateStatic;
        Map<String, Object> enrichedMetadata = enrichMetadataTag(incomingMetadata);
        restTemplate.execute(repositoryUrl + "/" + objectId,
                HttpMethod.POST,
                (ClientHttpRequest requestCallback) -> {
                    if (StringUtils.hasLength(authorization))
                    {
                        requestCallback.getHeaders().add(HttpHeaders.AUTHORIZATION, authorization);
                    }
                    requestCallback.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
                    requestCallback.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    requestCallback.getBody().write(this.objectMapper.writeValueAsString(enrichedMetadata).getBytes("UTF-8"));
                },
                new MetadataResponseExtractor(outputStream));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> enrichMetadataTag(Map<String, Object> incomingMetadata){
        List<Map<String, Object>> list = (List<Map<String, Object>>)incomingMetadata.get("objects");
        Map<String, Object> dmsApiObject = list.get(0);
        Map<String, Object> propertyMap = (Map<String, Object>)dmsApiObject.get("properties");
        Map<String, Object> testStringMap = (Map<String, Object>)propertyMap.get("appJmeter:testString1");
        String oldValue = testStringMap.get("value").toString();
        testStringMap.replace("value", (oldValue+ " (enriched value)"));
        return incomingMetadata;
    }

    public class MetadataResponseExtractor implements ResponseExtractor<Object>{
        private OutputStream outputStream;

        public MetadataResponseExtractor(OutputStream outputStream) {
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

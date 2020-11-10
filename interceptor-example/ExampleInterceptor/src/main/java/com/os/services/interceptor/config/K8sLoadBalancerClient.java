package com.os.services.interceptor.config;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/*
 * In Kubernetes, kubernetes load balances you
 * 
 * This is just a facade.
 * No loadbalancing or logic is done here.
 * 
 * Just return the requested url. 
 */

@Component
@Profile("kubernetes")
public class K8sLoadBalancerClient implements LoadBalancerClient {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(K8sLoadBalancerClient.class);
	
	@Override
	public ServiceInstance choose(String serviceId) {
		return new DefaultServiceInstance(serviceId,serviceId,80,false);
	}

	@Override
	public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
		ServiceInstance serviceInstance = new DefaultServiceInstance(serviceId,serviceId,80,false);
		try {
			return request.apply(serviceInstance);
		} 
		catch (IOException io) {
			LOGGER.info("request failed for " + serviceId + " " + io.getMessage());
			throw io;
		}
		catch (Exception e) {
			LOGGER.info("request failed for " + serviceId + " " + e.getMessage());
		} 
		return null;
	}

	@Override
	public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request)
			throws IOException {
		
		try {
			return request.apply(serviceInstance);
		}
		catch (IOException io) {
			LOGGER.info("request failed for " + serviceId + " " + io.getMessage());
			throw io;
		} 
		catch (Exception e) {
			LOGGER.info("request failed for " + serviceId + " " + e.getMessage());
		}
		return null;
	}

	@Override
	public URI reconstructURI(ServiceInstance instance, URI original) {
		return original;
	}

}
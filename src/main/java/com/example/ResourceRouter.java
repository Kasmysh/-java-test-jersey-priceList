package com.example;

import java.lang.IllegalStateException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Provider
@PreMatching
public class ResourceRouter implements ContainerRequestFilter {
 
    @Override
    public void filter(ContainerRequestContext requestContext) throws IllegalStateException {
        UriInfo uriInfo = requestContext.getUriInfo();
        UriBuilder reqUriBuilder = uriInfo.getRequestUriBuilder();
        MultivaluedMap<String, String> queries = uriInfo.getQueryParameters();
        URI newUri = null;
        
        if( queries.getFirst("productName") != null && queries.getFirst("date") != null ){
            newUri = reqUriBuilder.path( "getPrice" ).build();
        }else if( queries.getFirst("productName") != null && queries.getFirst("price") != null ){
            newUri = reqUriBuilder.path( "setPrice" ).build();
        }

        if( newUri != null ){
            requestContext.setRequestUri( newUri );
        }
    }
}
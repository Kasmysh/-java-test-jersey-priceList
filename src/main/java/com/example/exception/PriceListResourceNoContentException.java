package com.example.exception;

import  javax.ws.rs.WebApplicationException;
import  javax.ws.rs.core.Response;

public class PriceListResourceNoContentException extends WebApplicationException {
 
  public PriceListResourceNoContentException() {
    super( Response.status( Response.Status.NO_CONTENT ).entity("").build() );
  }
}
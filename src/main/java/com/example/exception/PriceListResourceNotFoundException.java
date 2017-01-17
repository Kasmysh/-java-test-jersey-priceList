package com.example.exception;

import  javax.ws.rs.WebApplicationException;
import  javax.ws.rs.core.Response;

public class PriceListResourceNotFoundException extends WebApplicationException {
 
  public PriceListResourceNotFoundException() {
    super( Response.status( Response.Status.NOT_FOUND ).entity("").build() );
  }
}
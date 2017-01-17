package com.example.exception;

import  javax.ws.rs.WebApplicationException;
import  javax.ws.rs.core.Response;

public class PriceListResourceInternalServerErrorException extends WebApplicationException {
 
  public PriceListResourceInternalServerErrorException() {
    super( Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity("").build() );
  }
}
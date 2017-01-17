package com.example.intrf;

import com.example.ProductPrice;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PriceList {

    @GET @Path("/getPrice")
    BigDecimal getPrice(@NotNull @QueryParam("productName") String productName, @NotNull @QueryParam("date") LocalDate date);

    @GET
    List<ProductPrice> getPrices(@NotNull @QueryParam("productName") String productName);

    @POST @Path("/setPrice")
    void setPrice(
            @NotNull @QueryParam("productName") String productName,
            @NotNull @QueryParam("price") @DecimalMin("0") BigDecimal price,
            @QueryParam("fromDate") LocalDate fromDate,
            @QueryParam("toDate") LocalDate toDate
        );

    @POST
    void stopSelling(
            @NotNull @QueryParam("productName") String productName,
            @QueryParam("fromDate") LocalDate fromDate,
            @QueryParam("toDate") LocalDate toDate
        );

} 
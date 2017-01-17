package com.example;

import com.example.intrf.*;
import com.example.exception.*;
import com.example.storage.ProductPriceDAO;

import javax.ws.rs.Path;

import javax.servlet.http.HttpServletRequest;

import java.sql.*;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

import javax.ws.rs.core.Context;

@Path("/priceList")
public class MyResource implements PriceList {

    private ProductPriceDAO price_dao;

    public MyResource(@Context HttpServletRequest request) {
        Connection  conn = (Connection) request.getSession().getServletContext().getAttribute("connection");
        price_dao = new ProductPriceDAO(conn);
    }

    // Получает цену товара на указанную дату. Если товар не
    // продается в указанную дату, то возвращает пустой ответ с HTTP-кодом No Content.
    //
    // Если товар с переданным именем еще не был внесен в прайс-лист,
    // возвращает пустой ответ с HTTP-кодом Not Found.
    @Override
    public BigDecimal getPrice(String productName, LocalDate date){
        BigDecimal price;

        if( !price_dao.hasProduct(productName) ){
            throw new PriceListResourceNotFoundException();
        }else {
            price = price_dao.getPrice(productName, date);
            if(price == null){
                throw new PriceListResourceNoContentException();
            }
        }

        return price;
    }

    // Получает цены товара с промежутками действия цены.
    // Результат отсортирован в порядке возрастания даты действия
    // цены.
    //
    // Если товар с переданным именем еще не был внесен в прайс-лист,
    // возвращает пустой ответ с HTTP-кодом Not Found.
    @Override
    public List<ProductPrice> getPrices(String productName){
        List<ProductPrice> prices;

        if( !price_dao.hasProduct(productName) ){
            throw new PriceListResourceNotFoundException();
        }else {
            prices = price_dao.getPrices(productName);
        }

        return prices;
    }

    // Устанавливает цену товара в указанном интервале дат, цена
    // действует на протяжении всего интервала, включая его границы.
    // Если fromDate или toDate не установлены, то левая (правая) граница
    // интервала считается равной минус (плюс) бесконечности.
    //
    // Если для товара уже была ранее установлена другая цена в
    // какие-то из дней указанного периода, она будет изменена на
    // переданную в этом методе. При этом цены, установленные на
    // даты, не попадающие в заданный интервал, остаются неизменными.
    //
    // Если параметр fromDate больше toDate, возвращает HTTP-код Internal Server Error.
    @Override
    public void setPrice(String productName, BigDecimal price, LocalDate fromDate, LocalDate toDate ){
        if( fromDate != null && toDate != null ){
            if( fromDate.isAfter(toDate) ){
                throw new PriceListResourceInternalServerErrorException();
            }
        }
        price_dao.setPrice(productName, price, fromDate, toDate);
    }

    // Прекращает продажу товара в указанном интервале дат, товар
    // не доступен к продаже на протяжении всего интервала, включая
    // его границы. Если fromDate или toDate не установлены, то левая
    // (правая) граница интервала считается равной минус (плюс)
    // бесконечности.
    //
    // Если параметр fromDate больше toDate, возвращает HTTP-код Internal Server Error.
    //
    // Если товар с переданным именем еще не был внесен в прайс-лист,
    // возвращает пустой ответ с HTTP-кодом Not Found.
    @Override
    public void stopSelling( String productName, LocalDate fromDate, LocalDate toDate ){
        if( fromDate != null && toDate != null ){
            if( fromDate.isAfter(toDate) ){
                throw new PriceListResourceInternalServerErrorException();
            }
        }
        price_dao.stopSelling(productName, fromDate, toDate);
    }

    
}
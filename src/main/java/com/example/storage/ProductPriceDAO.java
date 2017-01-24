package com.example.storage;

import java.sql.*;

import java.util.Locale;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import com.example.ProductPrice;

public class ProductPriceDAO {
    private Connection dbconn;

    private final String productsTableName = "PRODUCTS";
    private final String pricesTableName   = "PRICES";
    private final int tableRowEnd   = 30;

    private final String productIdByProductNameTemplate;

    private Boolean isProductExist;
    private BigDecimal price = null;
    private List<ProductPrice> prices = null;
    private LocalDate _toDate;
    private LocalDate _fromDate;

    public ProductPriceDAO(Connection  conn){
        dbconn = conn;
        productIdByProductNameTemplate = "(select s.id from "+productsTableName+" s where s.name = '%s')";
        try{
            if ( !( conn.getMetaData().getTables(null, null, productsTableName, null) ).next() )
            {
                initProductsTable();
                initPricesTable();
            }
        }catch (SQLException e){
           System.out.println( "Getting of productsTable fail.  " + e.getMessage() );
            // e.printStackTrace();
        }
    }

    private void initProductsTable(){
        String initTableRequest = "create table "+productsTableName
            + "  (id    int primary key auto_increment not null,"
            + "   name  varchar(255))";

        String insertionRow = "insert  into "+productsTableName+"(name) values";
        String values = "('1_product')";
        int i = 2;
        while( i < tableRowEnd )
        {
            values+=String.format(",('%d_product')", i);
            i+=1;
        }

        makeRequest(initTableRequest);
        makeRequest(insertionRow+values);
    }

    private void initPricesTable(){
        String initTableRequest = "create table "+pricesTableName
            + "  (id    int primary key auto_increment not null,"
            + "   product_id  int,"
            + "   price decimal(8,2),"
            + "   validFrom date,"
            + "   validTo date,"
            + "   foreign key(product_id) references "+productsTableName+"(id))";

        String insertionRow = "insert  into "+pricesTableName+"(product_id, price, validFrom, validTo) values";
        String values = "('1', '92.52', '2005-11-15', null)";

        String validTime = null;
        long minDay;
        long maxDay = LocalDate.of(2018, 1, 1).toEpochDay();

        int i = 2;
        while( i < tableRowEnd )
        {
            minDay = LocalDate.of(2005, 1, 1).toEpochDay();

            if( ThreadLocalRandom.current().nextDouble(0, 1) > 0.5 ){
                validTime = "null";
            }else{
                minDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
                validTime = "'"+LocalDate.ofEpochDay( minDay ).toString()+"'";
            }
            validTime+=", ";
            if( ThreadLocalRandom.current().nextDouble(0, 1) > 0.5 ){
                validTime+= "null";
            }else{
                validTime+= "'"+LocalDate.ofEpochDay( ThreadLocalRandom.current().nextLong(minDay, maxDay) ).toString()+"'";
            }

            values+=String.format( Locale.ROOT, ",('%d', '%.2f', %s)",
                i,
                new BigDecimal( ThreadLocalRandom.current().nextDouble(0.0, 10000.0) ).divide(new BigDecimal("1"), BigDecimal.ROUND_DOWN),
                validTime
            );
            i+=1;
        }

        makeRequest(initTableRequest);
        makeRequest(insertionRow+values);
    }

    private Boolean switchAutocommit( Boolean autoCommitFlag ){
        Boolean res = false;

        try{
            dbconn.setAutoCommit( autoCommitFlag );
            res = true;
        }catch(SQLException e){
           System.out.println( "switchAutocommit is fail.  " + e.getMessage() );
        }

        return res;
    }

    private Boolean stopAutocommit(){
        return switchAutocommit(false);
    }

    private Boolean startAutocommit(){
        // System.out.println( "startAutocommit!!!" );
        return switchAutocommit(true);
    }

    private Boolean startTransaction(){
        return stopAutocommit();
    }

    private Boolean endTransaction(Boolean sucFlag){
        Boolean res = false;

        try{
            if(sucFlag) dbconn.commit(); 
            else dbconn.rollback();

            // System.out.println( "commit!!!");
            res = true;
        }catch(SQLException e){
           System.out.println( "endTransaction is fail.  " 
                    + e.getMessage() );
        }finally{
            startAutocommit();
        }

        return res;
    }

    private Boolean makeRequest(String request){
        Boolean reqStatus = false;
        try (Statement dataQuery = dbconn.createStatement()) {
            dataQuery.execute(request);
            reqStatus = true;
        }catch (SQLException ex) {
            System.out.println("Simple makeRequest fail: "
                    + ex.getMessage());
        }

        return reqStatus;
    }

    private Boolean makeRequest(String request, RequestResultHandler handler){
        Boolean reqStatus = false;
        try (Statement dataQuery = dbconn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            reqStatus = true;
            handler.handle( dataQuery.executeQuery(request) );
        }catch (SQLException ex) {
            System.out.println("MakeRequest with RequestResultHandler failure: "
                    + ex.getMessage());
        }

        return reqStatus;
    }

    public Boolean hasProduct(String productName) {
        String request = "select id from "+productsTableName+" where name='"+productName+"'";
        RequestResultHandler queryResHandler = new RequestResultHandler(){
            public void handle(ResultSet queryResult){
                try{
                    queryResult.next();
                    isProductExist = queryResult.getBoolean(1);
                }catch(SQLException e){
                    isProductExist = false;

                    System.out.println("hasProduct request handler failure. Does product not exist?  "
                            + e.getMessage());
                }
            }
        };

        makeRequest(request, queryResHandler);

        return isProductExist;
    }

    public BigDecimal getPrice(String productName, LocalDate date){
        String request = 
            "select m.price from "+pricesTableName+" m "
                +"join "+productsTableName+" s "
                    +"on m.product_id = s.id "
            +"where s.name = '"+productName+"' "
                +"and (m.validfrom  IS NULL or m.validfrom <= '"+date+"') "
                +"and (m.validto IS NULL or m.validto >= '"+date+"')";

        RequestResultHandler queryResHandler = new RequestResultHandler(){
            public void handle(ResultSet queryResult){
                try{
                    queryResult.next();
                    price = queryResult.getBigDecimal(1);
                }catch(SQLException e){
                    price = null;

                    System.out.println("getPrice request handler failure. Does price not exist?  "
                            + e.getMessage());
                }
            }
        };

        makeRequest(request, queryResHandler);

        return price;
    }

    public List<ProductPrice> getPrices(String productName){
        String request = 
            "select s.name, m.price, m.validfrom, m.validto from "+pricesTableName+" m "
                +"join "+productsTableName+" s "
                    +"on m.product_id = s.id "
            +"where s.name = '"+productName+"' "
            +"order by m.validfrom";

        RequestResultHandler queryResHandler = new RequestResultHandler(){
            public void handle(ResultSet queryResult){
                String date_str;
                LocalDate validfrom;
                LocalDate validto;

                try{
                    prices = new ArrayList<>();

                    while (queryResult.next()) {

                        date_str = queryResult.getString(3);
                        if( date_str != null ){
                            validfrom = LocalDate.parse( date_str );
                        }else{
                            validfrom = null;
                        }

                        date_str = queryResult.getString(4);
                        if( date_str != null ){
                            validto = LocalDate.parse( date_str );
                        }else{
                            validto = null;
                        }

                        prices.add( new ProductPrice(queryResult.getString(1), queryResult.getBigDecimal(2), validfrom, validto) );
                    }
                }catch(SQLException e){
                    prices = null;

                    System.out.println("getPrices request handler failure.  "
                            + e.getMessage());
                }
            }
        };

        makeRequest(request, queryResHandler);

        return prices;
    }

    public Boolean setPrice(String productName, BigDecimal price, LocalDate fromDate, LocalDate toDate){
        String setNewPriceSql = 
            "insert into "+pricesTableName+"(product_id, price, validfrom, validto) values";

        String productIdByProductName = String.format(productIdByProductNameTemplate, productName);
        String newPriceDateBordersSql;

        if(fromDate != null || toDate != null){
            if(fromDate == null){
                newPriceDateBordersSql  = "null, '"+toDate+"'";
            }else{
                newPriceDateBordersSql = "'"+fromDate+"', ";
                if(toDate == null){
                    newPriceDateBordersSql+="null";
                }else{
                    newPriceDateBordersSql+="'"+toDate+"'";
                }
            }
        }else{
            newPriceDateBordersSql = "null, null";
        }

        setNewPriceSql+=
            String.format( Locale.ROOT, "(%s, %.2f, %s)", productIdByProductName, price, newPriceDateBordersSql);

        Boolean res = false;

        if( stopAutocommit() ){
            res = stopSellingWhitoutTransaction(productName, fromDate, toDate);
            if(res) res = makeRequest(setNewPriceSql);
            // if( res && !commitTransaction() ){
            //     rollbackTransaction();
            //     res = false;
            // }
            if( res && !endTransaction(true) ){
                endTransaction(false);
                res = false;
            }
        }

        return res;

    }

    private Boolean stopSellingWhitoutTransaction(String productName, LocalDate fromDate, LocalDate toDate){
        _fromDate = fromDate;
        _toDate = toDate;

        RequestResultHandler queryResHandler = null;

        String productIdByProductName = String.format(productIdByProductNameTemplate, productName);

        String delPricesSql =
            "delete from "+pricesTableName+" m " 
            +"where m.product_id = "+productIdByProductName+" ";

        String fixOldPricesSql = 
            "select id, product_id, price, validfrom, validto from "+pricesTableName+" "
            +"where product_id = "+productIdByProductName+" ";

        String delPricesConstraintsSql = "";
        String fixOldPricesConstraintsSql;

        if(fromDate != null || toDate != null){
            if(fromDate == null){
                delPricesConstraintsSql = "and m.validto <= '"+toDate+"'";
                fixOldPricesConstraintsSql = "and (validfrom is null or validfrom <= '"+toDate+"')";
            }else{
                delPricesConstraintsSql = "and m.validfrom >= '"+fromDate+"' ";
                fixOldPricesConstraintsSql = "and (validto is null or validto >= '"+fromDate+"') ";
                if(toDate != null){
                    delPricesConstraintsSql+= "and m.validto <= '"+toDate+"'";
                    fixOldPricesConstraintsSql+= "and (validfrom is null or validfrom <= '"+toDate+"')";
                }
            }

            fixOldPricesSql+=
                fixOldPricesConstraintsSql;

            queryResHandler = new RequestResultHandler(){
                public void handle(ResultSet queryResult){
                    String date_str;
                    ProductPrice outerPriceDump = null;
                    LocalDate validfrom;
                    LocalDate validto;

                    try{
                        while(queryResult.next()){
                            date_str = queryResult.getString(4);
                            if( date_str != null ){
                                validfrom = LocalDate.parse( date_str );
                            }else{
                                validfrom = null;
                            }

                            date_str = queryResult.getString(5);
                            if( date_str != null ){
                                validto = LocalDate.parse( date_str );
                            }else{
                                validto = null;
                            }


                            if(_fromDate == null){
                                //2nd case
                                queryResult.updateString("validfrom", _toDate.plusDays(1).toString());
                            }else{
                                if(_toDate == null){
                                    //1st case
                                    queryResult.updateString("validto", _fromDate.minusDays(1).toString());
                                }else{
                                    if( validfrom == null || validfrom.isBefore(_fromDate) ){
                                        if( validto == null || validto.isAfter(_toDate) ){
                                            //4th case
                                            outerPriceDump = new ProductPrice(queryResult.getString("product_id"), queryResult.getBigDecimal("price"), validfrom, validto);
                                        }
                                        //1st case and part of 4th case
                                        queryResult.updateString("validto", _fromDate.minusDays(1).toString());
                                    }else if(validto == null || validto.isAfter(_toDate)){
                                        //2nd case
                                        queryResult.updateString("validfrom", _toDate.plusDays(1).toString());
                                    }
                                }
                            }

                            queryResult.updateRow();

                            if(outerPriceDump != null){
                                queryResult.moveToInsertRow();
                                queryResult.updateInt( "product_id", Integer.parseInt(outerPriceDump.getProductName()) );
                                queryResult.updateBigDecimal("price", outerPriceDump.getProductPrice());
                                queryResult.updateString("validfrom", _toDate.plusDays(1).toString());
                                if(outerPriceDump.getValidTo() != null){
                                    queryResult.updateString("validto", outerPriceDump.getValidTo().toString());
                                }
                                queryResult.insertRow();
                                queryResult.moveToCurrentRow();
                            }
                        }
                    }catch( SQLException e ){
                       System.out.println("StopSelling requestHandler failure.  " + e.getMessage());
                    }
                }
            };

        }

        delPricesSql+=
            delPricesConstraintsSql;

        Boolean res = false;
        res = makeRequest(delPricesSql);
        if(res && queryResHandler != null){
            res = makeRequest(fixOldPricesSql, queryResHandler);
        }

        return res;
    }

    public Boolean stopSelling(String productName, LocalDate fromDate, LocalDate toDate){
        Boolean res = false;
        if( stopAutocommit() ){
            res = stopSellingWhitoutTransaction(productName, fromDate, toDate);
            if( res && !endTransaction(true) ){
                endTransaction(false);
                res = false;
            }
        }

        return res;
    }

    interface RequestResultHandler{
        void handle(ResultSet queryResult);
    }
}
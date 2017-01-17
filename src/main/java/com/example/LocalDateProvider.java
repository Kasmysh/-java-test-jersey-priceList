package com.example;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Provider
public class LocalDateProvider implements ParamConverterProvider {

    private LocalDate String2LocalDate(String str_date){
        LocalDate date;

        if( str_date != null ){
            try{
               date = LocalDate.parse(str_date);
            }catch (DateTimeParseException e) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                date = LocalDate.parse(str_date, formatter);
            }
        }else{
            date = null;
        }

        return date;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> clazz, Type type, Annotation[] annotations) {
        if (clazz.getName().equals(LocalDate.class.getName())) {

            return new ParamConverter<T>() {

                @Override
                public T fromString(String value) {
                    LocalDate date = String2LocalDate(value);
                    
                    return (T) date;
                }

                @Override
                public String toString(T localDate) {
                    return ( (LocalDate) localDate ).toString();
                }

            };
        }
        return null;
    }
}

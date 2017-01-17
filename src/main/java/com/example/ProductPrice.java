package com.example;

import java.time.LocalDate;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement
public class ProductPrice {
    private String productName;
    private BigDecimal productPrice;
    private LocalDate validFrom;
    private LocalDate validTo;

    public ProductPrice(){}
    public ProductPrice(String productName, BigDecimal productPrice, LocalDate validFrom, LocalDate validTo) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    @XmlElement
    public String getProductName() { return productName; }

    @XmlElement
    public BigDecimal getProductPrice() { return productPrice; }

    @XmlElement
    public LocalDate getValidFrom() { return validFrom; }
    
    @XmlElement
    public LocalDate getValidTo() { return validTo; }
}
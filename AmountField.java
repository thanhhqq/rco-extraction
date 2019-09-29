package com.saigonbpo.entity.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class AmountField {

    @XmlAttribute
    private String name;
    @XmlAttribute(name = "shipping_rate")
    private String shippingRate;
    @XmlValue
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShippingRate() {
        if (shippingRate == null || shippingRate.isEmpty()) {
            return "0";
        }
        return shippingRate;
    }

    public void setShippingRate(String shippingRate) {
        this.shippingRate = shippingRate;
    }

    public String getValue() {
        if (value.isEmpty()) {
            return "0";
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AmountField [name=" + name + ", value=" + value + "]";
    }
}

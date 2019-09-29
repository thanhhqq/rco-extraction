/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.saigonbpo.entity.config;

import com.saigonbpo.extractions.DataExtractionAbs;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author Quoc Thanh
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WordSpace {
    @XmlValue
    private String value;
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getTop() {
        try {
            return Integer.parseInt(value.split(DataExtractionAbs.SEP_COMMA)[0].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getRight() {
        try {
            return Integer.parseInt(value.split(DataExtractionAbs.SEP_COMMA)[1].trim());
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    public int getBottom() {
        try {
            return Integer.parseInt(value.split(DataExtractionAbs.SEP_COMMA)[2].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public int getLeft() {
        try {
            return Integer.parseInt(value.split(DataExtractionAbs.SEP_COMMA)[3].trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

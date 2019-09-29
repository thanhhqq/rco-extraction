/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.saigonbpo.entity.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author Quoc Thanh
 */
@XmlAccessorType(XmlAccessType.FIELD)
    public class MaxLine {
        @XmlValue
        private int value;
        @XmlAttribute(name = "max_line_space")
        private int maxLineSpace;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public int getMaxLineSpace() {
            return maxLineSpace;
        }

        public void setMaxLineSpace(int maxLineSpace) {
            this.maxLineSpace = maxLineSpace;
        }

    }

package com.saigonbpo.entity.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Config {

	@XmlElement(name = "field")
	private List<Field> listField;

	@XmlElementWrapper(name = "amount")
	@XmlElement(name = "field")
	private List<AmountField> listAmtField;

	public List<Field> getListField() {
            if(listField == null)
                listField = new ArrayList<>();
            return listField;
	}

	public void setListField(List<Field> listField) {
		this.listField = listField;
	}

	public List<AmountField> getListAmtField() {
		return listAmtField;
	}

	public void setListAmtField(List<AmountField> listAmtField) {
		this.listAmtField = listAmtField;
	}
}

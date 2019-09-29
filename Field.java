package com.saigonbpo.entity.result;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Field {
	
	@XmlAttribute
	private String name;
	@XmlElement(name = "value")
	private List<Value> listValue;

	public List<Value> getListValue() {
		return listValue;
	}

	public void setListValue(List<Value> listValue) {
		this.listValue = listValue;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}

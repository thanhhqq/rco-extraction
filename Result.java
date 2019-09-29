package com.saigonbpo.entity.result;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("restriction")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Result {
	@XmlElement(name = "field")
	private List<ResultField> listField;

	public List<ResultField> getListField() {
		return listField;
	}

	public void setListField(List<ResultField> listField) {
		this.listField = listField;
	}
}

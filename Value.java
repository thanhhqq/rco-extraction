package com.saigonbpo.entity.result;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

@SuppressWarnings("restriction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Value {
	@XmlAttribute(name = "page_nr")
	private int pageNr;
	@XmlAttribute(name = "kw_box")
	private String kwBox;
	@XmlAttribute(name = "kw_val")
	private String kwVal;
	@XmlAttribute(name = "val_box")
	private String valBox;
	@XmlValue
	private String value;
	
	public int getPageNr() {
		return pageNr;
	}
	public void setPageNr(int pageNr) {
		this.pageNr = pageNr;
	}
	public String getKwBox() {
		return kwBox;
	}
	public void setKwBox(String kwBox) {
		this.kwBox = kwBox;
	}
	public String getKwVal() {
		return kwVal;
	}
	public void setKwVal(String kwVal) {
		this.kwVal = kwVal;
	}
	public String getValBox() {
		return valBox;
	}
	public void setValBox(String valBox) {
		this.valBox = valBox;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}

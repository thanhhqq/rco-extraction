package com.saigonbpo.entity.result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.saigonbpo.extractingbot_ocr.App;
@SuppressWarnings("restriction")
public class AppTest {

	public static void main(String[] args) throws JAXBException {
		Result result = new Result();
		List<ResultField> listField = new ArrayList<ResultField>();
		List<Value> listValue = new ArrayList<Value>();

		// ----
		Value v1 = new Value();
		v1.setKwBox("asdasdasd");
		v1.setKwVal("1111");
		v1.setValBox("asdasdasd");
		v1.setValue("aaaaaaaaa");
		v1.setPageNr(1);
		listValue.add(v1);
		// ----
		Value v2 = new Value();
		v2.setKwBox("asdasdasd");
		v2.setKwVal("1111");
		v2.setValBox("asdasdasd");
		v2.setValue("aaaaaaaaa");
		listValue.add(v2);

		ResultField f = new ResultField();
		f.setName("field_1");
		f.setListValue(listValue);
		listField.add(f);

		result.setListField(listField);

		
		JAXBContext context = JAXBContext.newInstance(Result.class);
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		// Write to System.out
		m.marshal(result, System.out);
		// Write to File
		// System.out.println(DataExtraction.RESULT);
		m.marshal(result, new File(App.RESULT));

	}

}

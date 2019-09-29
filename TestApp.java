package com.saigonbpo.extractingbot_ocr;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;

import com.saigonbpo.entity.config.Config;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.ocr.EntWord;
import com.saigonbpo.entity.result.Result;
import com.saigonbpo.entity.result.ResultField;
import com.saigonbpo.extractions.DataExtraction;
import com.saigonbpo.extractions.TesseractConversion;
import java.util.ArrayList;

public class TestApp {
	
	public static final String CONFIG_FILE = DataExtraction.class.getClassLoader().getResource("temp_config.xml")
			.getFile();
	public static final String INV_FILE = DataExtraction.class.getClassLoader().getResource("temp_inv.jpg").getFile();
	public static final String INV_HOCR = DataExtraction.class.getClassLoader().getResource("temp_inv_hocr_1.txt")
			.getFile();
	public static final String RESULT = DataExtraction.class.getClassLoader().getResource("result.xml").getFile();

	public static void main(String[] args) throws Exception {
		try {
			String ruleConfig = CONFIG_FILE;
			//String hocrPaths = TestApp.DOC_HOCR +","+ TestApp.DOC_HOCR+","+ TestApp.DOC_HOCR;
			String hocrPaths = TestApp.INV_HOCR;
			if(args.length > 0){
				ruleConfig = args[0];
				hocrPaths = args[1];
			}
		
			// Load OCR Config (*.xml)
			JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Config config = (Config) jaxbUnmarshaller.unmarshal(new File(ruleConfig));
//			for (AmountField amtField : config.getListAmtField()) {
//				System.out.println(amtField.getValue());
//			}
//			System.out.println("---------------------");
			// Load Hocr (*.txt)
			TesseractConversion tex = new TesseractConversion();
			Map<Integer, EntPage> pageMap = tex.unmarshalHocrToEntity(hocrPaths.split(","));
			//htmlContent = "There are more than £2,9.00 and less than -12 numbers here";
			Pattern p = Pattern.compile("\\d+[,]?\\d*[.]\\d+");
			Matcher m = p.matcher(pageMap.get(1).getTextOriginal());
			Map<Double, EntWord> mapCost = new HashMap<>();
			List<Double> cost = new ArrayList<>();
			
			while (m.find()) {
			  //System.out.println(m.group());
			  cost.add(Double.parseDouble(m.group().replaceAll(",+", StringUtils.EMPTY)));
			  mapCost.putIfAbsent(Double.parseDouble(m.group().replaceAll(",+", StringUtils.EMPTY)), 
					  pageMap.get(1).findbyKeyword(m.group()));
			  //System.out.println(pageMap.get(1).findbyKeyword(m.group()));
			  
			}
			Collections.sort(cost);
			String[] vatRates = {"10","5","0"};
			String vatRate = "";
			EntWord netEnt = null;
			EntWord vatAmountEnt = null;
			EntWord totalEnt = null;
			boolean isFindOut = false;
			for (String rate : vatRates) {
				vatRate = rate;
				for (int i = cost.size()-1; i >= 0; i--) {
					double net = cost.get(i);
					netEnt = mapCost.get(net);
//					System.out.println(cost.get(i));
//					System.out.println(mapCost.get(cost.get(i)));
					double vatAmount = net*10/100;
					System.out.println("vatAmount: "+net*10/100);
				 	vatAmountEnt = mapCost.get(vatAmount);
				 	totalEnt = mapCost.get(vatAmount + net);
				 	System.out.println("total: "+vatAmount + net);
				 	if(vatAmountEnt != null || totalEnt !=null){
				 		isFindOut = true;
				 		break;
				 	}
				}
				if(isFindOut)
					break;
			}
			System.out.println("-----------------");
			System.out.println(netEnt);
			System.out.println(vatRate+"%");
			System.out.println(vatAmountEnt);
			System.out.println(totalEnt);
			DataExtraction data = new DataExtraction();
			List<List<EntWord>> listList1 = new ArrayList<>();
			List<EntWord> list1 = new ArrayList<>();
			list1.add(netEnt);
			listList1.add(list1);
			ResultField rf = new ResultField();
			rf.setListValue(data.parseEntWord(listList1, netEnt));
			List<ResultField> listrrr = new ArrayList<>();
			listrrr.add(rf);
			Result result = new Result();
			result.setListField(listrrr);
//			// Extract & Mapping data
//			DataExtraction data = new DataExtraction();
//			Result result = data.extract(config, pageMap);
//
			// TESTING (Parse result object to text file)
			JAXBContext context = JAXBContext.newInstance(Result.class);
			Marshaller m1 = context.createMarshaller();
			m1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// Write to System.out
			m1.marshal(result, System.out);
////			// Write to File
////			m.marshal(result, new File(App.RESULT));
			
		} catch (Exception e) {
			throw e;
		}
	}
}

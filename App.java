package com.saigonbpo.extractingbot_ocr;

import com.saigonbpo.entity.config.Config;
import com.saigonbpo.entity.ocr.EntPage;
import com.saigonbpo.entity.result.Result;
import com.saigonbpo.extractions.DataExtraction;
import com.saigonbpo.extractions.TesseractConversion;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


@SuppressWarnings("restriction")
public class App {
	public static final String CONFIG_FILE = "";//DataExtraction.class.getClassLoader().getResource("temp_config.xml").getFile();
	public static final String DOC_FILE = "";//DataExtraction.class.getClassLoader().getResource("temp_doc.jpg").getFile();
	public static final String DOC_HOCR = "";//DataExtraction.class.getClassLoader().getResource("temp_doc_hocr_1.txt").getFile();
	public static final String RESULT = "";//DataExtraction.class.getClassLoader().getResource("result.xml").getFile();

	public static void main(String[] args) throws Exception {
		try {
			String ruleConfig = CONFIG_FILE;
			String hocrPaths = App.DOC_HOCR +","+ App.DOC_HOCR+","+ App.DOC_HOCR;
			if(args.length > 0){
				ruleConfig = args[0];
				hocrPaths = args[1];
			}
			
			// Load OCR Config (*.xml)
			JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			Config config = (Config) jaxbUnmarshaller.unmarshal(new File(ruleConfig));
			
			// Load Hocr (*.txt)
			TesseractConversion tex = new TesseractConversion();
			Map<Integer, EntPage> pageMap = tex.unmarshalHocrToEntity(hocrPaths.split(","));
			
			// Extract & Mapping data
			DataExtraction data = new DataExtraction();
			Result result = data.extract(config, pageMap);

			// TESTING (Parse result object to text file)
			JAXBContext context = JAXBContext.newInstance(Result.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// Write to System.out
			m.marshal(result, System.out);
//			// Write to File
//			m.marshal(result, new File(App.RESULT));
			
		} catch (Exception e) {
			throw e;
		}
	}
        
        public String doExtract(List<String> args) throws Exception {
		try {
			String ruleConfig = args.get(0);
			List<String> hocrPaths = args.subList(1, args.size());
						
			// Load OCR Config (*.xml)
			JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			//Config config = (Config) jaxbUnmarshaller.unmarshal(new File(ruleConfig));
                        Config config = (Config) jaxbUnmarshaller.unmarshal(new StringReader(ruleConfig));
			
			// Load Hocr (*.txt)
			TesseractConversion tex = new TesseractConversion();
			Map<Integer, EntPage> pageMap = tex.unmarshalHocrToEntity(hocrPaths.toArray(new String[hocrPaths.size()]));
			
			// Extract & Mapping data
			DataExtraction data = new DataExtraction();
			Result result = data.extract(config, pageMap);

			// TESTING (Parse result object to text file)
			JAXBContext context = JAXBContext.newInstance(Result.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			// Write to System.out
			//m.marshal(result, System.out);
//			// Write to File
//			m.marshal(result, new File(App.RESULT));
                        //write to string
                        StringWriter sw = new StringWriter();
                        m.marshal(result, sw);

                        return sw.toString();
			
		} catch (Exception e) {
			throw e;
		}
	}
}

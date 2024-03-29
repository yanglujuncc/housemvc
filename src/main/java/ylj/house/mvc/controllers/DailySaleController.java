package ylj.house.mvc.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;






import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import java.io.InputStreamReader;

import ylj.house.tmsf.dailysigned.sync.HouseDailySigned;
import ylj.house.tmsf.dailysigned.sync.HouseDailySignedDBUtil;
import ylj.utils.ConnectionUtil;


@Controller("DailySaleControl")

public class DailySaleController {

	static Logger logger=LoggerFactory.getLogger(DailySaleController.class);

	TimeZone zone =null;
	SimpleDateFormat ISO_time_format=null;
	public DailySaleController() throws Exception{
		
		System.out.println("DailySaleController created .");
		
		zone = TimeZone.getTimeZone("GMT+8");  //时区

		ISO_time_format = new SimpleDateFormat("yyyy-MM-dd");
		ISO_time_format.setTimeZone(zone);
	
	}
	
	@SuppressWarnings("restriction")
	@PostConstruct
	public void init() {
		System.out.println("DailySaleController init ...");

	//	DOMConfigurator.configure(HouseController.class.getResource("/conf/log4j.xml"));
		
	}
	
	@SuppressWarnings("restriction")
	@PreDestroy
	void destroy() {

	}

	@RequestMapping(value = "/daily", method = RequestMethod.GET)
	public void handleDaily(@RequestParam(value="date",required=false) String date,HttpServletResponse response) {
		//0model.addAttribute("propertyId",propertyId);
	    //model.addAttribute("date", date);
	
		System.out.println("      date:"+date);
		

		if(date==null){
			date=ISO_time_format.format(new Date(System.currentTimeMillis()));
		}
		
		StringBuffer aStringBuffer=new StringBuffer();
	
		List<HouseDailySigned> dailySigneds= HouseDailySignedDBUtil.queryRecords(date);
		System.out.println(dailySigneds.size());
		aStringBuffer.append(dailySigneds.size()+"\n");
		
		Map<String,List<HouseDailySigned>> propertySaleRecords=new TreeMap<String,List<HouseDailySigned>>();
		for(HouseDailySigned dailySigned:dailySigneds){
			
			
			//http://localhost:8080/housemvc/house?propertyId=64897079&date=2015-07-13
			
			
			List<HouseDailySigned> saleRecords=propertySaleRecords.get(dailySigned.propertyName);
			if(saleRecords==null){
				saleRecords=new LinkedList<HouseDailySigned>();
				propertySaleRecords.put(dailySigned.propertyName, saleRecords);
			}
			
			saleRecords.add(dailySigned);
		}
		
		//http://localhost:8080/housemvc/house?propertyId=64897079&date=2015-07-13
		
		for(Entry<String,List<HouseDailySigned>> entry:propertySaleRecords.entrySet()){
			aStringBuffer.append("--------------- "+entry.getKey()+" -------------\n");
			String url=null;
			for(HouseDailySigned dailySigned:entry.getValue()){
				
				 url="http://localhost:8080/housemvc/house?propertyId="+dailySigned.propertyId+"&dateFrom="+date+"&dateTo="+date;
				
				aStringBuffer.append(" S:"+dailySigned.signedNumber+" R:"+dailySigned.reservedNumber+" "+dailySigned.signedArea+"m "+dailySigned.signedAvgPrice+"/m "+dailySigned.signedTime+"\n");			
			}
			aStringBuffer.append("\n");
			aStringBuffer.append(url);
			aStringBuffer.append("\n");
		}
		
		
		try {
			 ServletOutputStream  sos=response.getOutputStream();
			 OutputStreamWriter osw=new OutputStreamWriter(sos,"utf-8");
			
			osw.append(" date:"+date+"\n");
			osw.append(aStringBuffer.toString());
			osw.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return ;
	}
	
	
	//http://localhost:8080/housemvc/daily?date=2015-07-10
	    public static class SignedRecord{
	    	
	    	
	    	
	    	HouseDailySigned houseDailySigned;
	    	
	    	
	    	public SignedRecord(HouseDailySigned houseDailySigned){
	    		this.houseDailySigned=houseDailySigned;
	    	}
			
			public String getSignedNumber(){
				return houseDailySigned.signedNumber;
			}
			public String getReservedNumber(){
				return houseDailySigned.reservedNumber;
			}
			public String getSignedArea(){
				return houseDailySigned.signedArea;
			}
			public String getSignedAvgPrice(){
				return houseDailySigned.signedAvgPrice;
			}
			public String getSignedTime(){
				return houseDailySigned.signedTime;
			}
			
	    }
		public static class PropertySignedRecord {
			
			public String propertyTypeCode;
			public String propertyId;			
			public String propertyName;		
			public String district;		
			public String signedDate;
			
			public String url;
			
			public List<SignedRecord> signedRecords;

			
			public String getPropertyTypeCode(){
				return propertyTypeCode;
			}
			public String getPropertyId(){
				return propertyId;
			}
			public String getPropertyName(){
				return propertyName;
			}
			public String getDistrict(){
				return district;
			}
			public String getSignedDate(){
				return signedDate;
			}
			public String getUrl(){
				return url;
			}
			public List<SignedRecord> getSignedRecords(){
				return signedRecords;
			}
			
		}
	
		
		
		
		//http://localhost:8080/housemvc/daily_jstl?date=2015-08-17
		@RequestMapping(value = "/daily_jstl", method = RequestMethod.GET)
		public String handleDaily_JSTL(@RequestParam(value="date",required=false) String date,Model model) {
			//0model.addAttribute("propertyId",propertyId);
		    //model.addAttribute("date", date);
		
			System.out.println("      date:"+date);	
			if(date==null){
				date=ISO_time_format.format(new Date(System.currentTimeMillis()));			
				String redictUrl="daily_jstl?date="+date;
				logger.info("redirect=> "+redictUrl);
				
				return "redirect:/"+redictUrl;
			}
			
		//	StringBuffer aStringBuffer=new StringBuffer();
		
			List<HouseDailySigned> dailySigneds= HouseDailySignedDBUtil.queryRecords(date);
			System.out.println(dailySigneds.size());
		//	aStringBuffer.append(dailySigneds.size()+"\n");
			logger.info("total dailySigned record: "+dailySigneds.size());
			
			
			Map<String,List<HouseDailySigned>> propertySaleRecords=new TreeMap<String,List<HouseDailySigned>>();
			for(HouseDailySigned dailySigned:dailySigneds){
	
				List<HouseDailySigned> saleRecords=propertySaleRecords.get(dailySigned.propertyName);
				if(saleRecords==null){
					saleRecords=new LinkedList<HouseDailySigned>();
					propertySaleRecords.put(dailySigned.propertyName, saleRecords);
				}
				
				saleRecords.add(dailySigned);
			}
			
			//http://localhost:8080/housemvc/house?propertyId=64897079&date=2015-07-13
			
			List<PropertySignedRecord> propertyRecords=new LinkedList<PropertySignedRecord>();
			
			for(Entry<String,List<HouseDailySigned>> entry:propertySaleRecords.entrySet()){
		//		aStringBuffer.append("--------------- "+entry.getKey()+" -------------\n");
				String propertyName=entry.getKey();
				String url=null;
				String propertyId=null;
				String propertyTypeCode=null;
				String district=null;
				String signedDate=null;
				
				List<SignedRecord> signedRecords=new LinkedList<SignedRecord>();
				
				
				for(HouseDailySigned dailySigned:entry.getValue()){
					
					 url="/housemvc/house_jstl?propertyId="+dailySigned.propertyId+"&dateFrom="+date+"&dateTo="+date;
					 propertyId=dailySigned.propertyId;
					 propertyTypeCode=dailySigned.propertyTypeCode;
					 district=dailySigned.district;
					 signedDate=dailySigned.signedDate;
					 
					 SignedRecord signedRecord=new SignedRecord(dailySigned);
					 signedRecords.add(signedRecord);
				}
				
			//	propertySignedRecord.district
				
				PropertySignedRecord newPropertySignedRecord=new PropertySignedRecord();
				newPropertySignedRecord.district=district;
				newPropertySignedRecord.propertyId=propertyId;
				newPropertySignedRecord.propertyName=propertyName;
				newPropertySignedRecord.propertyTypeCode=propertyTypeCode;
				newPropertySignedRecord.signedDate=signedDate;
				newPropertySignedRecord.url=url;
				newPropertySignedRecord.signedRecords=signedRecords;
				
				
				propertyRecords.add(newPropertySignedRecord);
					
			}
			
			
			model.addAttribute("propertyRecords", propertyRecords);
		
		   
			return "daily_jstl";
		}
		@RequestMapping(value = "/daily_json", method = RequestMethod.GET,produces = "application/json; charset=utf-8")
		@ResponseBody
		public String handleDaily_JSON(@RequestParam("date") String date) {
			//0model.addAttribute("propertyId",propertyId);
		    //model.addAttribute("date", date);
		
			System.out.println("      date:"+date);
			
		//	StringBuffer aStringBuffer=new StringBuffer();
		
			List<HouseDailySigned> dailySigneds= HouseDailySignedDBUtil.queryRecords(date);
			System.out.println(dailySigneds.size());
		//	aStringBuffer.append(dailySigneds.size()+"\n");
			
			Map<String,List<HouseDailySigned>> propertySaleRecords=new TreeMap<String,List<HouseDailySigned>>();
			for(HouseDailySigned dailySigned:dailySigneds){
	
				List<HouseDailySigned> saleRecords=propertySaleRecords.get(dailySigned.propertyName);
				if(saleRecords==null){
					saleRecords=new LinkedList<HouseDailySigned>();
					propertySaleRecords.put(dailySigned.propertyName, saleRecords);
				}
				
				saleRecords.add(dailySigned);
			}
			
			//http://localhost:8080/housemvc/house?propertyId=64897079&date=2015-07-13
			
			List<PropertySignedRecord> propertyRecords=new LinkedList<PropertySignedRecord>();
			
			for(Entry<String,List<HouseDailySigned>> entry:propertySaleRecords.entrySet()){
		//		aStringBuffer.append("--------------- "+entry.getKey()+" -------------\n");
				String propertyName=entry.getKey();
				String url=null;
				String propertyId=null;
				String propertyTypeCode=null;
				String district=null;
				String signedDate=null;
				
				List<SignedRecord> signedRecords=new LinkedList<SignedRecord>();
				
				
				for(HouseDailySigned dailySigned:entry.getValue()){
					
					 url="/housemvc/house_jstl?propertyId="+dailySigned.propertyId+"&dateFrom="+date+"&dateTo="+date;
					 propertyId=dailySigned.propertyId;
					 propertyTypeCode=dailySigned.propertyTypeCode;
					 district=dailySigned.district;
					 signedDate=dailySigned.signedDate;
					 
					 SignedRecord signedRecord=new SignedRecord(dailySigned);
					 signedRecords.add(signedRecord);
				}
				
			//	propertySignedRecord.district
				
				PropertySignedRecord newPropertySignedRecord=new PropertySignedRecord();
				newPropertySignedRecord.district=district;
				newPropertySignedRecord.propertyId=propertyId;
				newPropertySignedRecord.propertyName=propertyName;
				newPropertySignedRecord.propertyTypeCode=propertyTypeCode;
				newPropertySignedRecord.signedDate=signedDate;
				newPropertySignedRecord.url=url;
				newPropertySignedRecord.signedRecords=signedRecords;
				
				
				propertyRecords.add(newPropertySignedRecord);
					
			}
			
		
		   
			return JSON.toJSONString(propertyRecords);
		}
}

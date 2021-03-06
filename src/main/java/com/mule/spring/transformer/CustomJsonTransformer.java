package com.mule.spring.transformer;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.module.json.transformers.AbstractJsonTransformer;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import com.mule.spring.entity.Student;
import com.mule.spring.service.StudentService;
import com.mule.spring.util.ExceptionUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomJsonTransformer extends AbstractJsonTransformer {

	@Autowired
    protected StudentService studentService;
	
	protected static Logger logger = LogManager.getLogger(CustomJsonTransformer.class);
	
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		String transformJsonStr = null;
		
		 try {
	            String jsonMessage = message.getPayloadAsString();	   
	            //添加信息
	            JSONObject jsonMap = updateStudentInfos(jsonMessage);	            
	            transformJsonStr = jsonMap.toJSONString();
	            if(!Strings.isNullOrEmpty(transformJsonStr))
	            {
	            	logger.info("The json message after transformation is:" + transformJsonStr);
	            }
	        } catch (Exception e) {
	        	 logger.error("exception message is:" + ExceptionUtil.getExceptionMessage(e));
	        }		
		 return null;
		 //return transformJsonStr;
	}
	
	/**
	 * 从请求中的json报文中读取student的id列表，再从数据库中读取Student信息，
	 * 追加到json报文中。
	 * @param originalJsonStr
	 * 请求的json报文
	 * @return
	 * 追加了student信息的JSON对象
	 */
	private JSONObject updateStudentInfos(String originalJsonStr)
	{
			JSONObject studentsJsonObj = null;
			try
			{
				studentsJsonObj = JSONObject.parseObject(originalJsonStr);
				JSONArray studentMapArray =
							studentsJsonObj.getJSONArray("students");
				
				//从请求json报文中读取student的id列表
				List<String> idList = new ArrayList<String>();
		    	int length = studentMapArray.size();		    	
		    	for(int i=0;i<length;i++)
		    	{
		    		JSONObject studentMap = studentMapArray.getJSONObject(i);
		    		String id = studentMap.getString("id");
		    		idList.add(id);
		    	}
		    	//根据student的id列表从数据库中读取student信息
				List<Student> studentList = 
						studentService.getStudentsByIds(idList);
				
				//将student信息填充到json报文中。
				for(int j=0;j<length;j++)
		    	{
		    		JSONObject studentMap = studentMapArray.getJSONObject(j);
		    		Student student = studentList.get(j);
		    		String name = student.getName();
		    		String className = student.getClassName();
		    		studentMap.put("name", name);
		    		studentMap.put("class", className);
		    	}
				studentsJsonObj.put("students", studentMapArray);				
			}
			catch(Exception ex)
			{
				logger.info("updateStudentInfos method "
						+ "exception message is:" + ExceptionUtil.getExceptionMessage(ex));
			}	
			return studentsJsonObj;
	}

}

package com.amazonaws.lambda.demo;

import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
        JSONParser parser=new JSONParser();

		context.getLogger().log("Input: " + input);
		try {
			JSONObject jsonObject=(JSONObject) parser.parse(new FileReader("src/main/java/com/amazonaws/lambda/demo/json/types.json"));
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "Hello from Lambda!";
	}

	private CallTipoAPI getpublic(String type,String applno) {
		CallTipoAPI res = null;
		try {
			res = new CallTipoAPI(type, applno);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO: implement your handler
		System.out.print(res.patent_no + "\n");
		System.out.print(res.case_type + "\n");
		System.out.print(res.public_date + "\n");
		System.out.print(res.inventors + "\n");
		System.out.print(res.agents + "\n");
		System.out.print(res.applicants + "\n");

		return res;
	}
}

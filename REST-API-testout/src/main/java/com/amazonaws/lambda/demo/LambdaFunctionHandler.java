package com.amazonaws.lambda.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.InputStream;
import java.io.OutputStream;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class LambdaFunctionHandler implements RequestStreamHandler
{
JSONParser parser = new JSONParser();

   @Override
   public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
   {
    LambdaLogger logger = context.getLogger();
       logger.log("Loading Java Lambda handler of ProxyWithStream");
       
       String proxy = null;
String param1 = null;
String param2 = null;
       
BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
       JSONObject responseJson = new JSONObject();
       String responseCode = "200";
       JSONObject event = null;
      
       try {
           event = (JSONObject)parser.parse(reader);
           if (event.get("pathParameters") != null) {
               JSONObject pps = (JSONObject)event.get("pathParameters");
               if ( pps.get("proxy") != null) {
                   proxy = (String)pps.get("proxy");
               }
           }
           if (event.get("queryStringParameters") != null)
           {
               JSONObject qps = (JSONObject)event.get("queryStringParameters");
               if ( qps.get("param1") != null)
               {
                   param1 = (String)qps.get("param1");
               }
           }
           if (event.get("queryStringParameters") != null)
           {
               JSONObject qps = (JSONObject)event.get("queryStringParameters");
               if ( qps.get("param2") != null)
               {
                   param2 = (String)qps.get("param2");
               }
           }
           
       }
       catch(Exception pex)
       {
        responseJson.put("statusCode", "400");
        responseJson.put("exception", pex);
       }
         // Implement your logic here
       int output = 0;
       if (proxy.equals("sum"))
       {
        output = sum(Integer.parseInt(param1), Integer.parseInt(param2));
       }
       else if (proxy.equals("subtract"))
       {
        output = subtract(Integer.parseInt(param1), Integer.parseInt(param2));
       }
       
        JSONObject responseBody = new JSONObject();
           responseBody.put("input", event.toJSONString());
           responseBody.put("message", "Output is" + output);

           JSONObject headerJson = new JSONObject();
           headerJson.put("x-custom-header", "my custom header value");
           headerJson.put("Access-Control-Allow-Origin", "*");

           responseJson.put("isBase64Encoded", false);
           responseJson.put("statusCode", responseCode);
           responseJson.put("headers", headerJson);
           responseJson.put("body", responseBody.toString());  

           OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
           writer.write(responseJson.toJSONString());  
           writer.close();
   }
   public int sum(int a, int b)
   {
    return a+b;
   }
   public int subtract(int a, int b)
   {
    return a-b;
   }
}

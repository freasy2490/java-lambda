package com.amazonaws.lambda.demo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.mysql.cj.xdevapi.JsonArray;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	public String handleRequest(Object input, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");
		JSONArray Input = (JSONArray) input;
		String currentTime = "unavailable";

		// Get time from DB server
		try {
			String url = "jdbc:mysql://" + System.getenv("RDS_HOSTNAME") + "/" + System.getenv("RDS_DB_NAME");
			String username = System.getenv("RDS_USERNAME");
			String password = System.getenv("RDS_PASSWORD");

			Connection conn = DriverManager.getConnection(url, username, password);
			Statement stmt = conn.createStatement();
			ArrayList<ResultSet> resultSet=new ArrayList<ResultSet>();
			for(int i=0;i<Input.length();i++) {
				JSONObject this_object=Input.getJSONObject(i);
				//
				String applno=this_object.getString("applno");
				
				resultSet.add(i,stmt.executeQuery("SELECT NOW()"));
				if (resultSet.get(i).next()) {
					currentTime = resultSet.get(i).getObject(1).toString();
				}
			}

			logger.log("Successfully executed query.  Result: " + currentTime);

		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());
		}

		return currentTime;
	}

}
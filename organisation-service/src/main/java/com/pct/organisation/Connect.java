package com.pct.organisation;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
public class Connect {

	
	public static void main (String aa[])
	{
		try {
		HttpClientBuilder builder;
		 RequestConfig requestConfig = RequestConfig.custom()
	                .setConnectionRequestTimeout(500)
	                .setConnectTimeout(500)
	                .setSocketTimeout(500)
	                .build();
	       builder = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig);
        HttpResponse response = null;
        HttpPost post = null;
        String url = "";
        HttpClient httpClient = builder.build(); 
        post = new HttpPost("https://geotab-intermediate-forwarder.phillips-connect.net/geotab/api/eventhandler/report");
        StringEntity postingString = new StringEntity("{}");
        post.setEntity(postingString);
        post.setHeader("Content-type", "application/json");
        //post.setHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE4NjIzNTQ5MTgsImlhdCI6MTU0Njk5NDkxOCwibmJmIjoxNTQ2OTk0OTE4LCJzdWIiOnsiYXBpX3VzZXIiOiJjb25uZWN0ZWQtaG9sZGluZ3MuY29tIiwiYXBpX3VzZXJfcm9sZSI6IlJPTEVfQVBJX1VTRVIiLCJjbGllbnQiOiJjb25uZWN0ZWQtaG9sZGluZ3MuY29tIn19.cujZu0OK41AR3jh0XSi1ltVGhQtEAelNPWB5l_l0mjk");
        response = httpClient.execute(post);
        if (response.getStatusLine().getStatusCode() != 200) {
	}
	}
		catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
}

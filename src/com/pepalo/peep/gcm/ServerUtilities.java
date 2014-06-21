/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pepalo.peep.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.util.Base64;
import android.util.Log;

import com.pepalo.peep.Common;


/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {
	
	private static final String TAG = "ServerUtilities";

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     */
    public static String register(final String email, final String regId) {
        //Log.i(TAG, "registering device (regId = " + regId + ")");
        String serverUrl = Common.getServerUrl() + "/register";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.FROM, email);
        params.put(Common.REG_ID, regId);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        try {
        	return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final String email) {
        //Log.i(TAG, "unregistering device (email = " + email + ")");
        String serverUrl = Common.getServerUrl() + "/unregister";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.FROM, email);
        try {
            post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
            // At this point the device is unregistered from GCM, but still
            // registered in the server.
            // We could try to unregister again, but it is not necessary:
            // if the server tries to send a message to the device, it will get
            // a "NotRegistered" error message and should unregister the device.
        }
    }
    
    /**
     * Send a message.
     */
    public static String send(String msg, String to) throws IOException {
        //Log.i(TAG, "sending message (msg = " + msg + ")");
    	byte[] data = msg.getBytes("UTF-8");
    	msg = Base64.encodeToString(data, Base64.DEFAULT);
    	msg=URLEncoder.encode(msg,"UTF-8");
    	
    	
        String serverUrl = Common.getServerUrl() + "/chat";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.MSG,  msg);
        params.put(Common.FROM, Common.getChatId());
        params.put(Common.TO, to);        
        
        return post(serverUrl, params, MAX_ATTEMPTS);
    }
    
    /**
     * Create a group.
     */
    public static String create() {
        //Log.i(TAG, "creating group");
        String serverUrl = Common.getServerUrl() + "/group";
        Map<String, String> params = new HashMap<String, String>();
        
        try {
        	return post(serverUrl, params, MAX_ATTEMPTS);
        } catch (IOException e) {
        }
        return null;
    }
    
    /**
     * Join a group.
     */
    public static void join(String to) throws IOException {
    	//Log.i(TAG, "joining group");
        String serverUrl = Common.getServerUrl() + "/join";
        Map<String, String> params = new HashMap<String, String>();
        params.put(Common.FROM, Common.getChatId());
        params.put(Common.TO, to);        
        
        post(serverUrl, params, MAX_ATTEMPTS);
    }    

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params request parameters.
     * @return response
     * @throws IOException propagated from POST.
     */
    private static String executePost(String endpoint, Map<String, String> params) throws IOException {
        URL url;
        StringBuffer response = new StringBuffer();
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=').append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();
        //Log.v(TAG, "Posting '" + body + "' to " + url);
        byte[] bytes = body.getBytes();
        
        String s = new String(bytes);
        //Log.d("message", s);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
              throw new IOException("Post failed with error code " + status);
              
            } else {
            	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        		String inputLine;
        		while ((inputLine = in.readLine()) != null) {
        			response.append(inputLine);
        		}
        		in.close();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response.toString();
      }
    
    /** Issue a POST with exponential backoff */
    private static String post(String endpoint, Map<String, String> params, int maxAttempts) throws IOException {
    	long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
    	for (int i = 1; i <= maxAttempts; i++) {
    		Log.d(TAG, "Attempt #" + i + " to connect");
    		try {
    			return executePost(endpoint, params);
    		} catch (IOException e) {
    			Log.e(TAG, "Failed on attempt " + i + ":" + e);
    			if (i == maxAttempts) {
    				throw e;
                }
                try {
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                backoff *= 2;    			
    		} catch (IllegalArgumentException e) {
    			throw new IOException(e.getMessage(), e);
    		}
    	}
    	return null;
    }
}

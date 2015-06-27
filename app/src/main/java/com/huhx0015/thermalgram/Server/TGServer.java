package com.huhx0015.thermalgram.Server;

import android.os.Handler;

import com.google.gson.Gson;

/**
 * -----------------------------------------------------------------------------------------------
 * [TGServer] CLASS
 * DESCRIPTION: Holds data returned from the server.
 * -----------------------------------------------------------------------------------------------
 */

public class TGServer {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // SERVER VARIABLES
    private static final String URL = ""; // Server URL
    private static final Gson gsonServer = new Gson(); // Gson parser object
    public static final Handler uiHandler = new Handler(); // UI handler

    // CLIENT VARIABLES
    private static final String GETURL = "";
    private static final Gson gsonClient = new Gson(); // Gson parser object

    // LOGGING VARIABLES
    private static final String TAG = TGServer.class.getSimpleName();

    /** SERVER FUNCTIONALITY ___________________________________________________________________ **/

    /*
    // updateServer(): Updates the server with the StepBOT data.
    public static void updateServer(
            final String name,
            final int steps, // CHANGE PARAMETER
            final int distance,
            final int energy,
            final int level,
            final int type,
            final List<String> jsonList, // JSON LIST
            final HONFragment fragment) {
        Log.d(TAG, "updateServer");

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                updateServerOnSecondThread(name, steps, distance, energy, level, type, jsonList, fragment);
            }
        });
        thread.start();
    }

    private static void updateServerOnSecondThread(
            final String name,
            final int steps,
            final int distance,
            final int energy,
            final int level,
            final int type,
            final List<String> jsonList, // JSON LIST
            final HONFragment fragment) {
        Log.d(TAG, "updateServerOnSecondThread");
        Log.d("SERVERRESPONDER", "NAME: " + name);

        HttpClient httpclient = new DefaultHttpClient();

        // NEW POST METHOD
        HttpPost httpMethod = new HttpPost(URL
                + "?q=" + encodeValue(Double.valueOf(steps))
        );

        HttpResponse response;
        try {

            final String json = gsonServer.toJson(jsonList);
            final StringEntity jsonEntity = new StringEntity(json);
            httpMethod.setEntity(jsonEntity);
            httpMethod.setHeader("Content-Type", "application/json");

            Log.d(TAG, "json = " + json);


            response = httpclient.execute(httpMethod);
            Log.d(TAG, "response status: " + response.getStatusLine().toString());
            HttpEntity entity = response.getEntity();

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragment.onServerUpdated(); // USED FOR LOGGING IN SERVER. NOT NEEDED.
                }
            });


        }

        catch (Exception e) {
            Log.e(TAG, "Error downloading items", e);

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragment.onServerUpdated(); // USED FOR LOGGING IN SERVER. NOT NEEDED.
                }
            });
        }
    }

    public static String encodeValue(final Double unescaped) {
        if (null == unescaped) {
            return "";
        }
        return encodeValue(Double.toString(unescaped));
    }

    public static String encodeValue(final String unescaped) {
        if (null == unescaped) {
            return "";
        }

        try { return URLEncoder.encode(unescaped, "UTF-8"); }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Couldn't encode query value", e);
        }
    }
    */

    /** GET METHODS _______________________________________________________________________ **/

    /*
    // updateClient(): Updates the client.
    public static void updateClient(
            final String name,
            final int steps,
            final int distance,
            final int energy,
            final int level,
            final int type,
            final List<Integer> jsonList, // JSON LIST
            final HONFragment fragment) {

        Log.d(TAG, "updateServer");

        final Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                updateClientOnSecondThread(name, steps, distance, energy, level, type, jsonList, fragment);
            }
        });
        thread.start();
    }

    private static void updateClientOnSecondThread(
            final String name,
            final int steps,
            final int distance,
            final int energy,
            final int level,
            final int type,
            final List<Integer> jsonList, // JSON LIST
            final HONFragment fragment) {
        Log.d(TAG, "updateServerOnSecondThread");

        // NEW GET METHOD
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpMethod = new HttpGet(GETURL); // Creates new HttpGet object

        StringBuilder builder = new StringBuilder(); // NEW
        try {

            HttpResponse response = httpclient.execute(httpMethod);
            StatusLine statusLine = response.getStatusLine(); // Used for getting the HTTP status response code.
            int statusCode = statusLine.getStatusCode(); // Used for getting the HTTP status response code.

            // If HTTP Status code is 200, read the content.
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragment.onServerUpdated(); // USED FOR LOGGING IN SERVER. NOT NEEDED.
                }
            });

        }

        catch (Exception e) {
            Log.e(TAG, "Error downloading items", e);

            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    fragment.onServerUpdated(); // USED FOR LOGGING IN SERVER. NOT NEEDED.
                }
            });
        }

        // DEBUG LOG
        Log.d("SERVERRESPONDER", "RESPONSE (1): " + builder.toString());
    }
    */
}

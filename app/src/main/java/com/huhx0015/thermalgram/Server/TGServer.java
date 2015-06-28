package com.huhx0015.thermalgram.Server;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * -----------------------------------------------------------------------------------------------
 * [TGServer] CLASS
 * DESCRIPTION: Holds data returned from the server.
 * -----------------------------------------------------------------------------------------------
 */

public class TGServer {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // SERVER VARIABLES
    private static final String POSTURL = "http://50.62.57.6/~ibrahimkabil7/thermalgram/endpoint.php"; // Server URL

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGServer.class.getSimpleName();

    // NEW VARIABLES
    static int serverResponseCode = 0;
    ProgressDialog dialog = null;

    /** SERVER FUNCTIONALITY ___________________________________________________________________ **/

    // imageUploadFile(): Uploads the image to the web server.
    public static int imageUploadFile(String fileName) {

        // References the directory path where the image is stored.
        final String uploadFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/";
        String fullFilePath = uploadFilePath + "" + fileName; // Sets the full file path.

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(fullFilePath);

        if (!sourceFile.isFile()) {

            Log.e("uploadFile", "Source File not exist :" +uploadFilePath + "" + fileName);

            return 0;
        }

        else {

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(POSTURL);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=uploaded_file; filename="
                        + fileName + "" + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    Log.d(LOG_TAG, "File Upload Complete.");
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            }

            catch (MalformedURLException ex) {

                ex.printStackTrace();

                Log.d(LOG_TAG, "MalformedURLException");
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            }

            catch (Exception e) {

                e.printStackTrace();

                Log.d(LOG_TAG, "Got Exception : see logcat ");
                Log.e(LOG_TAG, "Excpetion: " + e);

            }

            return serverResponseCode;

        } // End else block
    }
}

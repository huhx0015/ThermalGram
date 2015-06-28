package com.huhx0015.thermalgram.Server;

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
 * DESCRIPTION: Contains methods that interact with the web server.
 * -----------------------------------------------------------------------------------------------
 */

public class TGServer {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGServer.class.getSimpleName();

    // SERVER VARIABLES
    private static final String POSTURL = "http://50.62.57.6/~ibrahimkabil7/thermalgram/endpoint.php"; // Server URL
    static int serverResponseCode = 0; // Response code.

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

        // If the file could not be found, an error is returned.
        if (!sourceFile.isFile()) {
            Log.e(LOG_TAG, "imageUploadFile(): Source file does not exist: " + uploadFilePath + "" + fileName);
            return 0;
        }

        else {

            try {

                // Opens a URL connection to the servlet.
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(POSTURL);

                // Opens an HTTP connection to the URL.
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allows for inputs.
                conn.setDoOutput(true); // Allows for outputs
                conn.setUseCaches(false); // Disables cache option.
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

                // Creates a buffer of maximum size.
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Reads file and writes it.
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                // Reads all the bytes of the file.
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // Sends the multipart form data after the file data.
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Code and message responses from the server.
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                // Indicates a successful web transaction.
                if (serverResponseCode == 200){
                    Log.d(LOG_TAG, "File Upload Complete.");
                }

                // Closes the streams.
                fileInputStream.close();
                dos.flush();
                dos.close();
            }

            // Malformed URL exception handler.
            catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.d(LOG_TAG, "MalformedURLException");
                Log.e(LOG_TAG, "Upload file to server error: " + ex.getMessage(), ex);
            }

            // Exception handler.
            catch (Exception e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Got Exception : see logcat ");
                Log.e(LOG_TAG, "Excpetion: " + e);
            }

            return serverResponseCode;
        }
    }
}

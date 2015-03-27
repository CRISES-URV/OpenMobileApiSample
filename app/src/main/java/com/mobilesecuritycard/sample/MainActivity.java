package com.mobilesecuritycard.sample;

import java.io.IOException;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mobilesecuritycard.openmobileapi.Channel;
import com.mobilesecuritycard.openmobileapi.Reader;
import com.mobilesecuritycard.openmobileapi.SEService;
import com.mobilesecuritycard.openmobileapi.SEService.CallBack;
import com.mobilesecuritycard.openmobileapi.Session;
import com.mobilesecuritycard.sample.R;

public class MainActivity extends Activity {

	final String LOG_TAG = "HelloSmartcard";
	
    /** Open Mobile API service. */
    SEService seService = null;

    /** GUI elements on the screen. */
    TextView _textview = null;
    ScrollView _scrollview = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        _textview = (TextView) findViewById(R.id.textView1);
        _scrollview = (ScrollView) findViewById(R.id.scrollView1);
        SEServiceCallback callback = new SEServiceCallback();
        new SEService(this, callback);
    }

    @Override
    protected void onDestroy() {
        if (seService != null) {
            seService.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Callback interface if informs that this SEService is connected to the
     * backend system and it's resources.
     */
    public class SEServiceCallback implements CallBack {
        
        /*
         * Run the sample only on the first connection.
         */
        public boolean firstConnection = true;

        /**
         * This method will be called if this SEService is connected
         */
        public void serviceConnected(SEService service) {
            seService = service;
            if(firstConnection){
                firstConnection = false;
                new Sample().execute();
            }
        }
    }
    
    private class Sample extends AsyncTask<Void, String, Void> {

        protected Void doInBackground(Void... voids) {

            Reader[] readers = seService.getReaders();
            if (readers.length != 1) {
                publishProgress("\nMobile Security Card not found.");
                return null;
            }
            Reader reader = readers[0];
            int a = readers.length;
            publishProgress("\nNumero de readers = "+ a);
            
            boolean isPresent = reader.isSecureElementPresent();
            if (!isPresent) {
                publishProgress("\nSecure element is not present.");
                return null;
            } else {
                publishProgress("\nSecure element is present.");
            }
            
            Session session = null;
            byte[] atr = null;
            try {
                session = reader.openSession();
                atr = session.getATR();
                publishProgress("\nConnected to secure element with ATR", bytesToString(atr));
            } catch (IOException e) {
                publishProgress("\nCould not connect to the secure element.");
                return null;
            }

            Channel basicChannel = null;
            Channel logicalChannel = null;
            try {
                // Open channel and select the card manager (ISD = issuer
                // security domain)
                byte[] aid_isd = new byte[] {(byte) 0xAB, (byte)0xCD, (byte)0xEF, (byte)0xFE, (byte)0xDC, 0x12, (byte)0xCA, (byte)0xCA, 0x00, 0x01};
                publishProgress("\nOpen basic channel to card manager with AID\n" + bytesToString(aid_isd));
                basicChannel = session.openBasicChannel(aid_isd);

                byte[] cmd = new byte[] { (byte) 0x80, (byte) 0x06, (byte) 0x00, (byte) 0x00};
                byte[] cmd1 = new byte[] { (byte) 0x80, (byte) 0x07, (byte) 0x00, (byte) 0x00};
                byte[] cmd2 = new byte[] { (byte) 0x80, (byte) 0x09, (byte) 0x00, (byte) 0x00};
                byte[] cmd3 = new byte[] { (byte) 0x80, (byte) 0x10, (byte) 0x00, (byte) 0x00};
                byte[] cmd4 = new byte[] { (byte) 0x80, (byte) 0x22, (byte) 0x00, (byte) 0x00};
                
                byte[] response;

                // Send a command that always works for the card manager
                publishProgress("\nSending 'GET CPLC DATA'", "CMD: " + bytesToString(cmd));
                response = basicChannel.transmit(cmd);
                publishProgress("RSP: " + bytesToString(response));
                
                // Send a command that always works for the card manager
                publishProgress("\nSending 'GET CPLC DATA'", "CMD: " + bytesToString(cmd1));
                response = basicChannel.transmit(cmd1);
                publishProgress("RSP: " + bytesToString(response));
                
                // Send a command that always works for the card manager
                publishProgress("\nSending 'GET CPLC DATA'", "CMD: " + bytesToString(cmd2));
                response = basicChannel.transmit(cmd2);
                publishProgress("RSP: " + bytesToString(response));
                
                // Send a command that always works for the card manager
                publishProgress("\nSending 'GET CPLC DATA'", "CMD: " + bytesToString(cmd3));
                response = basicChannel.transmit(cmd3);
                publishProgress("RSP: " + bytesToString(response));
                
                // Send a command that always works for the card manager
                publishProgress("\nSending 'GET CPLC DATA'", "CMD: " + bytesToString(cmd4));
                response = basicChannel.transmit(cmd4);
                publishProgress("RSP: " + bytesToString(response));
                
                seService.shutdown();
                publishProgress("\nShutdown service (closes all channels).");

            } catch (Exception e) {
                publishProgress("\nError:\n" + e.getMessage());
                return null;
                
            }
            return null;
        }

        protected void onProgressUpdate(String... messages) {
            for (String message : messages) {
                logText(message);
            }
        }

    }

    private static String bytesToString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString();
    }

    private void logText(String message) {
        _scrollview.post(new Runnable() {
            public void run() {
                _scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }

        });
        _textview.append(message + "\n");
    }
}
package com.abc.citizen;

import android.media.Image;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender extends AsyncTask<String,Void,Void> {
    private Socket s;
    DataOutputStream dos;
    private PrintWriter pw;
    //////// image send /////////

    ////////////////////////////
    protected Void doInBackground(String...voids){
        String message = voids[0];
        try{

            s = new Socket("31.192.57.86",7800);
            pw = new PrintWriter(s.getOutputStream());
            pw.write(message);
            pw.flush();
            pw.close();
            s.close();

        }catch(IOException e){
            e.printStackTrace();
        }




    return null;
    }


}


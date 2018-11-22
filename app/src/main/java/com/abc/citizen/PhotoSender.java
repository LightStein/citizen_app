//package com.abc.citizen;
//
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.media.Image;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.support.v4.app.FragmentActivity;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import java.io.*;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//import static android.app.Activity.RESULT_OK;
//import static android.support.v4.app.ActivityCompat.startActivityForResult;
//
//public class PhotoSender{
//
//    private ImageView imageView;
//    Handler handler = new Handler();
//
//    @Override
//    protected Void main(ImageView... imageViews) {
//    imageView = imageViews[0];
//            return null;
//    }
//
//    public void send(View v)
//    {
//        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("image/*");
//        startActivityForResult(Description,i,1);
//
//    }
//
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        if(requestCode == 1)
//        {
//            if(resultCode == RESULT_OK)
//            {
//                try {
//                    final Uri imageUri = data.getData();
//                     final InputStream imageStream = getContentResolver().openInputStream(imageUri);
//                     final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                     photo.setImageBitmap(selectedImage);
//
//                     ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                     selectedImage.compress(Bitmap.CompressFormat.PNG, 0 , bos);
//                     byte[] array =bos.toByteArray();
//
//                     SendImageClient sic = new SendImageClient();
//                     sic.execute(array);
//                }catch (FileNotFoundException e){
//
//                    e.printStackTrace();
//                    Toast.makeText(getApplicationContext(),"Something went wrong", Toast.LENGTH_LONG).show();
//                }
//            }else{
//                Toast.makeText(getApplicationContext(),"No Image Selected",Toast.LENGTH_LONG).show();
//            }
//        }
//
//    }
//    public class SendImageClient extends AsyncTask<byte[],Void,Void>{
//        protected Void doInBackground(byte... voids){
//
//            try{
//                Socket socket = new Socket("31.192.57.86",7800);
//
//                OutputStream out = socket.getOutputStream();
//                DataOutputStream dos = new DataOutputStream(out);
//                dos.writeInt(voids[0].length);
//                dos.write(voids[0], 0, voids[0].length);
//                handler.post(()->{
//                    Toast.makeText(getApplicationContext(),"image sent", Toast.LENGTH_LONG).show();
//                });
//                dos.close();
//                out.close();
//                socket.close();
//            }catch (IOException e)
//            {
//                e.printStackTrace();
//                handler.post(()->{
//                    Toast.makeText(getApplicationContext(), "i/o exception", Toast.LENGTH_SHORT).show();
//                });
//            }
//            return null;
//
//        }
//
//    }
//}
//
//
//

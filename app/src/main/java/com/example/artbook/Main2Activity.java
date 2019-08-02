package com.example.artbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.example.artbook.MainActivity.imageArray;
public class Main2Activity extends AppCompatActivity {
    SQLiteDatabase sqLiteDatabase;
    ImageView imageView;
    EditText nameEditText;
    EditText artistEditText;
    Button saveButton;
    Bitmap seceltedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView=findViewById(R.id.imageView);
        nameEditText=findViewById(R.id.nameEditText);
        artistEditText=findViewById(R.id.artistEditText);
        saveButton=findViewById(R.id.saveButton);
        Intent intent=getIntent();
        String info=intent.getStringExtra("info");
        if (info.matches("newArt")){
            nameEditText.setText("");
            artistEditText.setText("");
            seceltedImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.artbookimage);
            imageView.setImageBitmap(seceltedImage);
            saveButton.setVisibility(View.VISIBLE);
        }
        else {
            saveButton.setVisibility(View.INVISIBLE);
            String name=intent.getStringExtra("name");
            String artist=intent.getStringExtra("artist");
            Bitmap image=imageArray.get(intent.getIntExtra("position",0));
            nameEditText.setText(name);
            artistEditText.setText(artist);
            imageView.setImageBitmap(image);
        }
    }

    //SAVE butonuna tıklandığında çalışacak metod
    public void save(View view){
        String name=nameEditText.getText().toString();
        String artist=artistEditText.getText().toString();
        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        seceltedImage.compress(Bitmap.CompressFormat.PNG,25,outputStream);
        byte[] bytearray=outputStream.toByteArray();
        try {
            sqLiteDatabase=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS arts(name VARCHAR,artist VARCHAR,image BLOB)");
            String sqlString= "INSERT INTO arts(name,artist,image) VALUES(?,?,?)";
            //sqlString oluşturarak bunu compileStatement ederek tek tek ekleyeceğimiz verileri VALUES(?,?,?) değerlerine ekleriz.
            SQLiteStatement sqLiteStatement=sqLiteDatabase.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artist);
            sqLiteStatement.bindBlob(3,bytearray);
            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        /*nameArray.add(name);
        artistArray.add(artist);
        imageArray.add(seceltedImage);
        arrayAdapter.notifyDataSetChanged();*/
        //Yukarıdaki yöntemle mainactivityde kaydettiğimiz veriyi doğrudan görebiliriz fakat değişkenleri static olarak
        //tanımlayıp bu şekilde kullanmak pek tercih edilmez
        //intent kullanarak direk MainActivity'deki onCreate metoduna yönlendiririz fakat bu sefer de MainActivity birden
        //fazla defa çalışır.
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        //MainActivity'nin bir defa çalışıp kapanmasını istiyorsak aşağıdaki iki yöntemden birini kullanabiliriz.
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //finish();
        //finish metodu kaydetme işlemi bittikten sonra otomatik olarak Main2Activity sonlandırır ve MainActiviy yönlendirir.
    }
    public void selectImage(View view){
        //Kullanıcı verilerine erişilmesine izin vermezse yapacaklarımız
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},2);
        }
        else {
            //Aşağıdaki intent bize kullanıcının sanat kitaplarına erişmesini sağlar.
            Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //Aşağıdaki kod 1 numaralı request code kullanarak resime erişmemizi sağlar.
            startActivityForResult(intent,1);
        }
    }
    //Aşağıdaki hazır metot intentin hangi sonuca göre çaıştırılacağını yazacağımız metottur.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode==1&&resultCode==RESULT_OK&&data!=null){
            Uri imageData=data.getData();//Uri türündeki imageData değişkeni resimlerin adresini tutan değişkendir.
            try {
                //imageView'daki sdk versiyonunu kontrol edip kod dönüşümlerini yaptıktan sonra uygulamada kullanabilmek için
                //kullanıcı izinlerini almak gerekir.
                if (Build.VERSION.SDK_INT>=28){
                    ImageDecoder.Source source=ImageDecoder.createSource(this.getContentResolver(),imageData);
                    Bitmap bitmap=ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(bitmap);
                }
                else {
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    //getBitmap metoduna yazdığımız getContentResolver() metodunu kullanarak Uri çözümleyerek bitmap türüne dönüştürdük.
                    imageView.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //Aşağıdaki hazır metodun içine kullanıcı izin verdiğinde ne yapacağımızı yazarız.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==2){
            if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                //Aşağıdaki intent bize kullanıcının sanat kitaplarına erişmesini sağlar.
                Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //Aşağıdaki kod 1 numaralı request code kullanarak resime erişmemizi sağlar.
                startActivityForResult(intent,1);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

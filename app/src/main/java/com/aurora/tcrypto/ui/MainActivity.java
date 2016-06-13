package com.aurora.tcrypto.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.os.Bundle;
import android.os.Environment;

import android.content.Intent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.aurora.tcrypto.proto.CBC;
import com.aurora.tcrypto.proto.GenerateParam;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {
    private final int CHOOSE_FILE_REQUEST_CODE = 0xff;

    private class MenuItemClickListener implements Toolbar.OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.item_action_net_demo:
                    Intent netDemoLaunchIntent = new Intent(
                            MainActivity.this, NetLogActivity.class);
                    startActivity(netDemoLaunchIntent);
                    return true;
                default:
                    return false;
            }
        }
    }

    private class SetInitButtonClickListener implements Button.OnClickListener {
        public void onClick(View view) {
            try {
                MainActivity mainActivity = MainActivity.this;
                EditText modulusEditText = mainActivity.modulusEditText;
                int modulus = Integer.valueOf(modulusEditText.getText().toString());
                GenerateParam.generateInitParameters(modulus);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private class ChooseFileButtonListener implements Button.OnClickListener {
        public void onClick(View view) {
            try {
                Intent chooseFileIntent;
                PackageManager packageManager = getPackageManager();
                String mimeType = "*/*";

                do {
                    chooseFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    chooseFileIntent.setType(mimeType);
                    if (packageManager.resolveActivity(chooseFileIntent, 0) != null) {
                        break;
                    }

                    chooseFileIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
                    chooseFileIntent.putExtra("CONTENT_TYPE", mimeType);
                } while (false);

                Intent chooserIntent = Intent.createChooser(chooseFileIntent, "Select File");
                startActivityForResult(chooserIntent, CHOOSE_FILE_REQUEST_CODE);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(
                        getApplicationContext(),
                        "No file manager found", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setInitButton = (Button) findViewById(R.id.set_init_button);
        this.setInitButton.setOnClickListener(new SetInitButtonClickListener());

        this.chooseFileButton = (Button) findViewById(R.id.choose_file_button);
        this.chooseFileButton.setOnClickListener(new ChooseFileButtonListener());

        this.modulusEditText = (EditText) findViewById(R.id.numberN);

        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.toolbar.inflateMenu(R.menu.menu_main);
        this.toolbar.setOnMenuItemClickListener(new MenuItemClickListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case CHOOSE_FILE_REQUEST_CODE:
                Uri filepath = data.getData();
                File fileName = new File(getRealPathFromURI(filepath));
                onSelectedFile(fileName.toString());
                break;
            default:
                break;
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private void onSelectedFile(String fileName) {
        try {

            showDialog(fileName);
            ad.show();
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_LONG).show();
    }

    private void showDialog(final String fileName) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException {

        context = MainActivity.this;
        String title = "Вибір дії";
        String message = "Обрано файл ";
        String button1String = "Зашифрувати";
        String button2String = "Розшифрувати";

        final String fileContent = openFile(fileName);

        ad = new AlertDialog.Builder(context);
        ad.setTitle(title);
        ad.setMessage(message);
        ad.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(context, "файл буде шифруватись", Toast.LENGTH_LONG).show();
                String encrypted = null;

                try {
                    String secretKey = GenerateParam.generateSymKey();
                    CBC cbc = new CBC(secretKey);
                    encrypted = cbc.encrypt(fileContent);
                    writeFile(fileName, encrypted);
                    saveSymKey(fileName, secretKey);
                } catch (InvalidAlgorithmParameterException | InvalidKeyException
                        | BadPaddingException | IllegalBlockSizeException
                        | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }

            }
        });
        ad.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(context, "файл буде розшифровуватись", Toast.LENGTH_LONG).show();
                String decrypted = null;

                try {
                    String secretKey = loadSymKey(fileName);
                    CBC cbc = new CBC(secretKey);
                    decrypted = cbc.decrypt(fileContent);
                    writeFile(fileName, decrypted);
                } catch (InvalidAlgorithmParameterException | InvalidKeyException
                        | BadPaddingException | IllegalBlockSizeException
                        | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        });
        ad.setCancelable(true);
        ad.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(context, "Вы ничего не выбрали",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String openFile(String filepath) throws IOException {
        FileInputStream inputStream;
        inputStream = new FileInputStream(filepath);
        int data = inputStream.read();
        char content;
        String finalString = "";
        while (data != -1) {
            content = (char) data;
            data = inputStream.read();
            finalString += content;
        }
        inputStream.close();
        return finalString;
    }

    private void writeFile(String filePath, String fileContent) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Log.d("errorSD", "SD card is unavailable " + Environment.getExternalStorageState());
            return;
        }
        File sdFile = new File(filePath);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(sdFile));
            bw.write(fileContent);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSymKey(String fileName, String secretKey) {
        sPref = getSharedPreferences("MySymKeys", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(fileName, secretKey);
        ed.apply();
    }

    private String loadSymKey(String fileName) {
        sPref = getSharedPreferences("MySymKeys", MODE_PRIVATE);
        String savedSymKey = sPref.getString(fileName, "");
        return savedSymKey;
    }

    private SharedPreferences sPref;
    private AlertDialog.Builder ad;
    private Context context;

    private Button setInitButton;
    private Button chooseFileButton;
    private EditText modulusEditText;
    private Toolbar toolbar;
}

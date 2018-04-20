package lc.btl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by THHNt on 2/10/2018.
 */

public class BaseActivity extends AppCompatActivity {

    public String baseURL = "https://todolistv1.000webhostapp.com";
    public String getAllDataURL = baseURL + "/getAllData.php?email=";
    private GoogleSignInClient mGoogleSignInClient;
    LocalDatabse database;
    Dialog dialogInstance;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        sp = getSharedPreferences("currentUser", MODE_PRIVATE);
        editor = sp.edit();
        getAllDataURL += sp.getString("email","");

        database = new LocalDatabse(this, "todolist.sql", null, 1);
        database.queryData("CREATE TABLE IF NOT EXISTS boards( id INTEGER PRIMARY KEY, name VARCHAR(255), is_owner INTEGER)");
        database.queryData("CREATE TABLE IF NOT EXISTS cardslist( id INTEGER PRIMARY KEY, name VARCHAR(255), idBoard INTEGER)");
        database.queryData("CREATE TABLE IF NOT EXISTS card( id INTEGER PRIMARY KEY, name VARCHAR(255), description VARCHAR(255), date VARCHAR(255), time VARCHAR(255), location VARCHAR(255), lat VARCHAR(255), lng VARCHAR(255), idList INTEGER)");
        database.queryData("CREATE TABLE IF NOT EXISTS board_users( idBoard INTEGER , idUser INTEGER, name VARCHAR(255), email VARCHAR(255))");
        database.queryData("CREATE TABLE IF NOT EXISTS card_users( idCard INTEGER , idUser INTEGER, name VARCHAR(255), email VARCHAR(255))");

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuYourInfo:
                infoDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialogInstance != null) {
            dialogInstance.dismiss();
            dialogInstance = null;
        }
    }

    public void setDialogWidth(Dialog dialog, Float width) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        int dialogWindowWidth = (int) (displayWidth * width);
        layoutParams.width = dialogWindowWidth;
        dialog.getWindow().setAttributes(layoutParams);
    }

    public void infoDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_information);

        ImageView imageProfile = (ImageView) dialog.findViewById(R.id.imageProfile);
        TextView tvUserName = (TextView) dialog.findViewById(R.id.tvUserName);
        TextView tvUserEmail = (TextView) dialog.findViewById(R.id.tvUserEmail);
        Button btCloseInfo = (Button) dialog.findViewById(R.id.btCloseInfo);
        Button btLogOut = (Button) dialog.findViewById(R.id.btLogOut);

        SharedPreferences sp = getSharedPreferences("currentUser", MODE_PRIVATE);
        String name = sp.getString("name","");
        String email = sp.getString("email","");
        String image = sp.getString("image","");

        tvUserName.setText(getString(R.string.user_name) + ": " + name);
        tvUserEmail.setText(getString(R.string.email) + ": " + email);
        if(!image.equals("")) {
            Picasso.with(this).load(image).into(imageProfile);
        }

        btCloseInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOutDialog();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialogInstance = dialog;

        dialog.show();
    }

    public void logOutDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.confirm_logout));

        dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                signOut();
            }
        });

        dialog.show();
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        clearLocalData();
                        clearUserInfo();
                        showSignInScreen();
                    }
                });
    }

    private void showSignInScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void clearUserInfo() {
        editor.putString("first_load", "1");
        editor.putString("name", "");
        editor.putString("email", "");
        editor.putString("image", "");
        editor.commit();
    }

    public void insertBoardLocal(int id, String name, int is_owner) {
        name = name.replace("'","''");
        database.queryData("INSERT INTO boards VALUES(" + id + ",'" + name + "'," + is_owner + ")");
        Log.e("DATABASE", "add board " + name + "-" + id + "-" + is_owner);
    }

    public Cursor getBoardsLocal() {
        return database.getData("SELECT * FROM boards");
    }

    public Cursor getListsLocal(int idBoard) {
        return database.getData("SELECT * FROM cardslist WHERE idBoard = " + idBoard);
    }

    public Cursor getCardsLocal(int idList) {
        return database.getData("SELECT * FROM card WHERE idList = " +idList);
    }

    public Cursor getCardDetailsLocal(int id) {
        return database.getData("SELECT * FROM card WHERE id = " + id);
    }

    public void editBoardLocal(int id, String name) {
        name = name.replace("'","''");
        database.queryData("UPDATE boards SET name = '" + name + "' WHERE id = " + id);
        Log.e("DATABASE", "rename board " + id + " to " + name);
    }

    public void editListLocal(int id, String name) {
        name = name.replace("'","''");
        database.queryData("UPDATE cardslist SET name = '" + name + "' WHERE id = " + id);
        Log.e("DATABASE", "rename list " + id + " to " + name);
    }

    public void editCardLocal(int id, String name, String description) {
        name = name.replace("'","''");
        description = description.replace("'","''");
        database.queryData("UPDATE card SET name = '" + name + "', description = '" + description + "' WHERE id = " + id);
        Log.e("DATABASE", "rename card " + id + " to " + name + "-" + description);
    }

    public void setTimeLocal(int id, String date, String time) {
        date = date.replace("'","''");
        time = time.replace("'","''");
        database.queryData("UPDATE card SET date = '" + date + "', time = '" + time + "' WHERE id = " + id);
        Log.e("DATABASE", "set time card " + id + " to " + date + "-" + time);
    }

    public void setLocationLocal(int id, String location, String lat, String lng) {
        location = location.replace("'","''");
        lat = lat.replace("'","''");
        lng = lng.replace("'","''");
        database.queryData("UPDATE card SET location = '" + location + "', lat = '" + lat + "', lng = '" + lng + "' WHERE id = " + id);
        Log.e("DATABASE", "set location card " + id + " to " + location + "-" + lat + "-" + lng);
    }

    public void deleteBoardLocal(int id) {
        database.queryData("DELETE FROM boards WHERE id = " + id);
        Cursor cursor = getListsLocal(id);
        while(cursor.moveToNext()) {
            deleteListLocal(cursor.getInt(0));
        }
        Log.e("DATABASE", "delete board " + id);
    }

    public void deleteListLocal(int id) {
        database.queryData("DELETE FROM cardslist WHERE id = " + id);
        Cursor cursor = getCardsLocal(id);
        while(cursor.moveToNext()) {
            deleteCardLocal(cursor.getInt(0));
        }
        Log.e("DATABASE", "delete list " + id);
    }

    public void deleteCardLocal(int id) {
        database.queryData("DELETE FROM card WHERE id = " + id);
        Log.e("DATABASE", "delete card " + id);
    }

    public void removeCardMemberLocal(int idCard, int idUser) {
        database.queryData("DELETE FROM card_users WHERE idCard = " + idCard + " AND idUser = " + idUser);
    }

    public void insertListLocal(int id, String name, int idBoard) {
        name = name.replace("'","''");
        database.queryData("INSERT INTO cardslist VALUES(" + id + ",'" + name + "'," + idBoard + ")");
        Log.e("DATABASE", "add list " + name + "-" + id);
    }

    public void insertCardLocal(int id, String name, String description, String date, String time, String location, String lat, String lng, int idList) {
        name = name.replace("'","''");
        description = description.replace("'","''");
        date = date.replace("'","''");
        time = time.replace("'","''");
        location = location.replace("'","''");
        lat = lat.replace("'","''");
        lng = lng.replace("'","''");
        database.queryData("INSERT INTO card VALUES(" + id + ",'" + name + "','" + description + "','" + date + "','" + time + "','" + location + "','" + lat + "','" + lng + "'," + idList + ")");
        Log.e("DATABASE", "add card " + name  + "-" + id);
    }

    public void addBoardMemberLocal(int idBoard, int idUser, String name, String email) {
        if(!database.getData("SELECT * FROM board_users WHERE idBoard = " + idBoard + " AND idUser = " + idUser).moveToFirst()) {
            database.queryData("INSERT INTO board_users VALUES (" + idBoard + "," + idUser + ",'" + name + "','" + email + "')");
            Log.e("DATABASE", "add member " + name + "-" + idBoard);
        }
    }

    public void assignCardLocal(int idCard, int idUser, String name, String email) {
        if(!database.getData("SELECT * FROM card_users WHERE idCard = " + idCard + " AND idUser = " + idUser).moveToFirst()) {
            database.queryData("INSERT INTO card_users VALUES (" + idCard + "," + idUser + ",'" + name + "','" + email + "')");
            Log.e("DATABASE", "assign " + name + "-" + idCard);
        }
    }

    public Cursor getBoardMemberLocal(int idBoard) {
        return database.getData("SELECT * FROM board_users WHERE idBoard = " + idBoard);
    }

    public Cursor getCardMemberLocal(int idCard) {
        return database.getData("SELECT * FROM card_users WHERE idCard = " + idCard);
    }

    public Cursor getMemberInfo(int idBoard, int idUser) {
        return database.getData("SELECT * FROM board_users WHERE idBoard = " + idBoard + " AND idUser = " + idUser);
    }

    public Cursor getUser(String email) {
        return database.getData("SELECT * FROM card_users WHERE email = '" + email + "' LIMIT 1");
    }

    public boolean isAssigned(int idCard, int idUser) {
        return database.getData("SELECT * FROM card_users WHERE idCard = " + idCard + " AND idUser = " + idUser).moveToFirst();
    }

    public void moveCardLocal(String idCard, String oldList, String newList) {
        database.queryData("UPDATE card SET idList = " + newList + " WHERE id = " + idCard + " AND idList = " + oldList);
    }

    public Cursor getList(int idList) {
        return database.getData("SELECT * FROM cardslist WHERE id = " + idList);
    }

    public void clearLocalData() {
        database.queryData("DELETE FROM boards");
        database.queryData("DELETE FROM cardslist");
        database.queryData("DELETE FROM card");
        database.queryData("DELETE FROM board_users");
        database.queryData("DELETE FROM card_users");
    }
}
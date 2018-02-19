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
//    public String syncDataURL = baseURL + "/syncData.php";
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
//        database.queryData("CREATE TABLE IF NOT EXISTS local_actions( id INTEGER PRIMARY KEY AUTOINCREMENT, target VARCHAR(255), targetId INTEGER, boardId INTEGER, listId INTEGER, type VARCHAR(255), name VARCHAR(255), description VARCHAR(255), date VARCHAR(255), time VARCHAR(255), location VARCHAR(255), lat VARCHAR(255), lng VARCHAR(255))");
//        database.queryData("CREATE TABLE IF NOT EXISTS unsynced_items(type VARCHAR(255), id INTEGER)");

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

    /*public void refreshData(String url) {
        clearLocalData();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                insertBoardLocal(obj.getInt("id"), obj.getString("name"), obj.getInt("is_owner"));
                                JSONArray lists = obj.getJSONArray("lists");
                                for(int j = 0; j < lists.length(); j++) {
                                    JSONObject obj1 = lists.getJSONObject(j);
                                    insertListLocal(obj1.getInt("id"), obj1.getString("name"), obj1.getInt("idBoard"));
                                    JSONArray cards = obj1.getJSONArray("cards");
                                    for(int k = 0; k < cards.length(); k++) {
                                        JSONObject obj2 = cards.getJSONObject(k);
                                        insertCardLocal(obj2.getInt("id"), obj2.getString("name"), obj2.getString("description"), obj2.getString("date"), obj2.getString("time"), obj2.getString("location"), obj2.getString("lat"), obj2.getString("lng"), obj2.getInt("idList"));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BaseActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }*/

    public void infoDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_information);

        ImageView imageProfile = (ImageView) dialog.findViewById(R.id.imageProfile);
        TextView tvUserName = (TextView) dialog.findViewById(R.id.tvUserName);
        TextView tvUserEmail = (TextView) dialog.findViewById(R.id.tvUserEmail);
        Button btCloseInfo = (Button) dialog.findViewById(R.id.btCloseInfo);
        Button btLogOut = (Button) dialog.findViewById(R.id.btLogOut);
        Button btSync = (Button) dialog.findViewById(R.id.btSync);

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

        btSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setDialogWidth(dialog, 0.9f);

        dialogInstance = dialog;

        dialog.show();
    }

    /*private void syncData(String url, String target, int targetId, int boardId, int listId, String type, String name, String description, String date, String time, String location, String lat, String lng) {

    }*/

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
//        database.queryData("DELETE FROM unsynced_items WHERE type = 'board' AND id = " + id);
        Cursor cursor = getListsLocal(id);
        while(cursor.moveToNext()) {
            deleteListLocal(cursor.getInt(0));
        }
        Log.e("DATABASE", "delete board " + id);
    }

    public void deleteListLocal(int id) {
        database.queryData("DELETE FROM cardslist WHERE id = " + id);
//        database.queryData("DELETE FROM unsynced_items WHERE type = 'list' AND id = " + id);
        Cursor cursor = getCardsLocal(id);
        while(cursor.moveToNext()) {
            deleteCardLocal(cursor.getInt(0));
        }
        Log.e("DATABASE", "delete list " + id);
    }

    public void deleteCardLocal(int id) {
        database.queryData("DELETE FROM card WHERE id = " + id);
//        database.queryData("DELETE FROM unsynced_items WHERE type = 'card' AND id = " + id);
        Log.e("DATABASE", "delete card " + id);
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

    /*public void saveLocalAction(String target, int targetId, int boardId, int listId, String type, String name, String description, String date, String time, String location, String lat, String lng) {
        name = name.replace("'","''");
        description = description.replace("'","''");
        date = date.replace("'","''");
        time = time.replace("'","''");
        location = location.replace("'","''");
        lat = lat.replace("'","''");
        lng = lng.replace("'","''");
        database.queryData("INSERT INTO local_actions VALUES(null,'" + target + "'," + targetId + "," + boardId + "," + listId + ",'" + type + "','" + name + "','" + description + "','" + date + "','" + time + "','" + location + "','" + lat + "','" + lng + "')");
        Log.e("DATABASE", "local action " + target  + "-" + targetId + "-" + type);
    }*/

    /*public void editLocalAction(int id, String target, int targetId, String type, String name, String description, String date, String time, String location, String lat, String lng) {
        name = name.replace("'","''");
        description = description.replace("'","''");
        date = date.replace("'","''");
        time = time.replace("'","''");
        location = location.replace("'","''");
        lat = lat.replace("'","''");
        lng = lng.replace("'","''");
    }*/

    /*public void insertBoardLocalUnsync(String name) {
        Cursor cursor = database.getData("SELECT MIN(id) AS id FROM boards");
        int id;
        if(cursor.moveToFirst()) {
            id = Math.min(cursor.getInt(0) - 1, -1);
        } else {
            id = -1;
        }
        insertBoardLocal(id, name, 1);
        database.queryData("INSERT INTO unsynced_items VALUES('board'," + id + ")");
    }*/

    /*public void editBoardLocalUnsync(int id, String name) {
        saveLocalAction("board", id, 0, 0, "edit", name, "", "", "", "", "", "");
        Cursor cursor = database.getData("SELECT * FROM unsynced_items WHERE type = 'board' AND id = " + id);
        if(!cursor.moveToFirst()) {
            database.queryData("INSERT INTO unsynced_items VALUES('board'," + id + ")");
        }
        editBoardLocal(id, name);
    }*/

    /*public void deleteBoardLocalUnsync(int id) {
        deleteBoardLocal(id);
        database.queryData("DELETE FROM local_actions WHERE target = 'board' AND targetId = " + id);
        database.queryData("DELETE FROM local_actions WHERE boardId = " + id);
        saveLocalAction("board", id, 0, 0, "delete", sp.getString("email", ""), "", "", "", "", "", "");
    }*/

    public void clearLocalData() {
        database.queryData("DELETE FROM boards");
        database.queryData("DELETE FROM cardslist");
        database.queryData("DELETE FROM card");
    }

    /*public void clearSyncedData() {
        database.queryData("DELETE FROM boards WHERE id > 0");
        database.queryData("DELETE FROM cardslist WHERE id > 0");
        database.queryData("DELETE FROM card WHERE id > 0");
    }*/

    /*public boolean checkUnsync(String type, int id) {
        Cursor cursor = database.getData("SELECT * FROM unsynced_items WHERE type = '" + type + "' AND id = " + id);
        if(cursor.moveToFirst()) {
            return true;
        }
        return false;
    }*/
}
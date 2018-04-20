package lc.btl;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BoardsListActivity extends BaseActivity {

    private String addBoardURL = baseURL + "/insertBoard.php";
    private String editBoardURL = baseURL + "/editBoard.php";
    private String deleteBoardURL = baseURL + "/deleteBoard.php";
    private String createUserURL = baseURL + "/createUser.php";
    ListView lvBoards;
    ImageButton btAddBoard, btRefresh;
    ArrayList<Board> arrayBoard;
    BoardAdapter boardAdapter;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    Intent intentReciever;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boards_list);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        intentReciever = new Intent(BoardsListActivity.this, AlarmReciever.class);
        sharedPreferences = getSharedPreferences("alarmsID", MODE_PRIVATE);

        sync();

        arrayBoard = new ArrayList<>();
        boardAdapter = new BoardAdapter(this, R.layout.item_board, arrayBoard);
        lvBoards.setAdapter(boardAdapter);
        Log.e("URL", getAllDataURL);

        if(sp.getString("first_load", "1").equals("1")) {
            createUser(createUserURL,sp.getString("name",""),sp.getString("email",""));
        } else {
            getAllData(getAllDataURL);
        }

        btAddBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getString("first_load", "1").equals("1")) {
                    Toast.makeText(BoardsListActivity.this, getString(R.string.please_refresh), Toast.LENGTH_SHORT).show();
                } else {
                    addBoardDialog();
                }
            }
        });

        btRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sp.getString("first_load", "1").equals("1")) {
                    createUser(createUserURL,sp.getString("name",""),sp.getString("email",""));
                } else {
                    getAllData(getAllDataURL);
                }
            }
        });
    }

    private void sync() {
        lvBoards = (ListView) findViewById(R.id.lvBoards);
        btAddBoard = (ImageButton) findViewById(R.id.btAddBoard);
        btRefresh = (ImageButton) findViewById(R.id.btRefresh);
    }

    private void createUser(String url, final String name, final String email) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] parts = response.trim().split("-");
                        if (parts[0].equals("id")) {
                            if (parts[1].equals("1")) {
                                Toast.makeText(BoardsListActivity.this, getString(R.string.registed), Toast.LENGTH_SHORT).show();
                            } else if(parts[1].equals("99")){
                                Toast.makeText(BoardsListActivity.this, getString(R.string.welcome_back) + " " + name + "!", Toast.LENGTH_LONG).show();
                            }
                            editor.putString("first_load", "0");
                            editor.putString("idUser", parts[2]);
                            editor.commit();
                            getAllData(getAllDataURL);
                        } else {
                            Toast.makeText(BoardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_LONG).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BoardsListActivity.this, getString(R.string.please_refresh), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void showBoardsLocal() {
        arrayBoard.clear();
        Cursor cursor = getBoardsLocal();
        while(cursor.moveToNext()) {
            arrayBoard.add(new Board(cursor.getInt(0), cursor.getString(1), cursor.getInt(2)));
        }
        boardAdapter.notifyDataSetChanged();
    }

    private void getAllData(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        clearLocalData();
                        arrayBoard.clear();
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
                                        Card currentCard = new Card(obj2.getInt("id"),obj2.getString("name"),obj2.getString("date"),obj2.getString("time"));
                                        String id = sharedPreferences.getString("ids", "");
                                        String[] ids = id.split(",");
                                        if (checkAlarm(ids, currentCard.getId())) {
                                            refreshAlarm(currentCard, obj.getString("name"), obj.getInt("id"), obj.getInt("is_owner"));
                                        }
                                        JSONArray users = obj2.getJSONArray("users");
                                        for(int l = 0; l < users.length(); l++) {
                                            JSONObject obj3 = users.getJSONObject(l);
                                            assignCardLocal(obj2.getInt("id"),obj3.getInt("id"),obj3.getString("name"),obj3.getString("email"));
                                        }
                                    }
                                }
                                JSONArray members = obj.getJSONArray("users");
                                for(int j = 0; j < members.length(); j++) {
                                    JSONObject obj1 = members.getJSONObject(j);
                                    addBoardMemberLocal(obj.getInt("id"), obj1.getInt("id"), obj1.getString("name"), obj1.getString("email"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        showBoardsLocal();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BoardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                        showBoardsLocal();
                    }
                });
        requestQueue.add(jsonArrayRequest);
    }

    private void refreshAlarm(Card currentCard, String boardName, int boardId, int is_owner) {
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        try {
            Date date = df.parse(currentCard.getDate() + " " + currentCard.getTime());
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SharedPreferences sp = getSharedPreferences("currentUser", MODE_PRIVATE);
        Bundle extras = new Bundle();
        extras.putString("userEmail", sp.getString("email",""));
        extras.putString("cardName", currentCard.getName());
        extras.putString("cardId", String.valueOf(currentCard.getId()));
        extras.putString("boardId", String.valueOf(boardId));
        extras.putString("boardName", boardName);
        extras.putInt("is_owner", is_owner);
        extras.putString("status", "on");
        intentReciever.putExtras(extras);
        pendingIntent = PendingIntent.getBroadcast(
                    BoardsListActivity.this, currentCard.getId(), intentReciever, PendingIntent.FLAG_UPDATE_CURRENT
        );
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private boolean checkAlarm(String[] ids, int currentId) {
        String id = String.valueOf(currentId);
        for(int i = 0; i < ids.length; i++) {
            if (ids[i].equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void addBoardDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_board);

        Button btAddBoardOk = (Button) dialog.findViewById(R.id.btAddBoardOk);
        Button btAddBoardCancel = (Button) dialog.findViewById(R.id.btAddBoardCancel);
        final EditText edtNewBoard = (EditText) dialog.findViewById(R.id.edtNewBoard);

        btAddBoardOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtNewBoard.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(BoardsListActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    // add board
                    dialog.dismiss();
                    addBoard(addBoardURL, sp.getString("email",""), name);
                }
            }
        });

        btAddBoardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    public void editBoardDialog(final Board board) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_board);

        Button btEditBoardOk = (Button) dialog.findViewById(R.id.btEditBoardOk);
        Button btEditBoardCancel = (Button) dialog.findViewById(R.id.btEditBoardCancel);
        final EditText edtEditBoard = (EditText) dialog.findViewById(R.id.edtEditBoard);

        edtEditBoard.setText(board.getName());
        edtEditBoard.setSelection(edtEditBoard.getText().length());

        btEditBoardOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtEditBoard.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(BoardsListActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    // edit board
                    dialog.dismiss();
                    editBoard(editBoardURL, name, board.getId());
                }
            }
        });

        btEditBoardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    public void deleteBoardDialog(final Board board) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.confirmDelete) + " " + getString(R.string.board) + " " + board.getName() + "?");

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
                deleteBoard(deleteBoardURL, sp.getString("email",""), board.getId());
            }
        });

        dialog.show();
    }

    private void addBoard(String url, final String email, final String name) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] parts = response.trim().split(":");
                        if (parts[0].equals("id")) {
                            int idBoard = Integer.parseInt(parts[1].trim());
                            insertBoardLocal(idBoard, name, 1);
                            addBoardMemberLocal(idBoard, Integer.parseInt(sp.getString("idUser", "0")),
                                    sp.getString("email", ""), sp.getString("name", ""));
                            showBoardsLocal();
                            Toast.makeText(BoardsListActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BoardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BoardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("nameBoard", name);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void editBoard(String url, final String name, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            editBoardLocal(id, name);
                            showBoardsLocal();
                            Toast.makeText(BoardsListActivity.this, getString(R.string.edit_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BoardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        editBoardLocalUnsync(id, name);
//                        showBoardsLocal();
                        Toast.makeText(BoardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idBoard", String.valueOf(id));
                params.put("nameBoard", name);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void deleteBoard(String url, final String email, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            deleteBoardLocal(id);
                            showBoardsLocal();
                            Toast.makeText(BoardsListActivity.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BoardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BoardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("idBoard", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void showCardsList(Board board) {
        Intent intent = new Intent(BoardsListActivity.this, CardsListActivity.class);
        intent.putExtra("board", board);
        startActivity(intent);
    }
}

package lc.btl;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lc.btl.Object.Card;
import lc.btl.Object.User;

public class CardDetailsActivity extends BaseActivity {

    private String editCardURL = baseURL + "/editCard.php";
    private String assignURL = baseURL + "/assign.php";
    private String deleteCardURL = baseURL + "/deleteCard.php";
    private String removeCardMemberURL = baseURL + "/removeCardMember.php";
    private String setTimeURL = baseURL + "/setTime.php";
    private String setLocationURL = baseURL + "/setLocation.php";
    public String currentId;
    private int is_owner;
    private String soundStatus;
    private Card currentCard;
    private String boardName, boardId;
    int PLACE_PICKER_REQUEST = 1;
    LinearLayout layoutTime, layoutLocation, layoutAssign;
    TextView tvCardName, tvCardPosition, tvDescription, tvTime, tvLocation, tvAssign;
    ImageButton btCardOption, btDirection;
    ListView lvCardMember;
    EditText savedEditText;
    TextView savedTvLat, savedTvLng;
    Switch swNotification;
    ArrayList<String> checkedUser;
    ArrayList<User> arrayUser;
    AssignAdapter assignAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_details);

        Intent intent = getIntent();
        boardName = intent.getExtras().getString("boardName");
        boardId = intent.getExtras().getString("boardId");
        currentId = intent.getExtras().getString("cardId");
        is_owner = intent.getExtras().getInt("is_owner");
        soundStatus = intent.getExtras().getString("status");
        currentCard = new Card();
        if(soundStatus != null && soundStatus.equals("off")) {
            stopSound();
            cancelAlarm(currentCard);
        }

        sync();

        tvCardName.setText(currentCard.getName());
        arrayUser = new ArrayList<>();
        checkedUser = new ArrayList<>();
        assignAdapter = new AssignAdapter(this, R.layout.item_member_card, arrayUser);
        lvCardMember.setAdapter(assignAdapter);

        showCardDetailsLocal();

        btCardOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCardOptions(btCardOption, currentCard);
            }
        });

        swNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(timeNotSet(currentCard)) {
                    Toast.makeText(CardDetailsActivity.this, getString(R.string.please_set_time), Toast.LENGTH_SHORT).show();
                    swNotification.setChecked(false);
                } else {
                    if(isChecked) {
                        setAlarm();
                    } else {
                        cancelAlarm(currentCard);
                        String idC = sharedPreferences.getString("idsCancel", "");
                        String[] idsC = idC.split(",");
                        if (!checkAlarm(idsC,currentCard.getId())) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < idsC.length; i++) {
                                sb.append(idsC[i]).append(",");
                            }
                            sb.append(String.valueOf(currentCard.getId())).append(",");
                            editor.putString("idsCancel", sb.toString());
                            editor.apply();
                        }
                    }
                }
            }
        });

        btDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locationNotSet()) {
                    showDirection();
                } else {
                    Toast.makeText(CardDetailsActivity.this, getString(R.string.no_location), Toast.LENGTH_SHORT).show();
                }
            }
        });

        layoutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_owner == 1) {
                    setTimeDialog(currentCard);
                }
            }
        });

        layoutLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_owner == 1) {
                    setLocationDialog(currentCard);
                }
            }
        });

        layoutAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_owner == 1) {
                    chooseMemberDialog(currentCard,Integer.parseInt(boardId));
                }
            }
        });
    }

    private void sync() {
        tvCardName = (TextView) findViewById(R.id.tvCardName);
        tvCardPosition = (TextView) findViewById(R.id.tvCardPosition);
        tvDescription = (TextView) findViewById(R.id.tvDescription);
        tvTime = (TextView) findViewById(R.id.tvTime);
        btCardOption = (ImageButton) findViewById(R.id.btCardOption);
        swNotification = (Switch) findViewById(R.id.swNotification);
        btDirection = (ImageButton) findViewById(R.id.btDirection);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        lvCardMember = (ListView) findViewById(R.id.lvCardMembers);
        tvAssign = (TextView) findViewById(R.id.tvAssign);
        layoutTime = (LinearLayout) findViewById(R.id.layoutTime);
        layoutLocation = (LinearLayout) findViewById(R.id.layoutLocation);
        layoutAssign = (LinearLayout) findViewById(R.id.layoutAssign);
    }

    public void showCardDetailsLocal() {
        Cursor cursor = getCardDetailsLocal(Integer.parseInt(currentId));
        if(cursor != null)
        {
            if (cursor.moveToFirst()) {
                currentCard.setId(cursor.getInt(0));
                currentCard.setName(cursor.getString(1));
                currentCard.setDescription(cursor.getString(2));
                currentCard.setDate(cursor.getString(3));
                currentCard.setTime(cursor.getString(4));
                currentCard.setLocation(cursor.getString(5));
                currentCard.setLat(cursor.getString(6));
                currentCard.setLng(cursor.getString(7));
                Cursor cursor2 = getList(cursor.getInt(9));
                if (cursor2 != null && cursor2.moveToFirst()) {
                    String listName = cursor2.getString(1);
                    tvCardPosition.setText(getString(R.string.list) + " " + listName + " ~ " + getString(R.string.board) + " " + boardName);
                }
                refresh();
            }
        }
        Cursor cursor1 = getCardMemberLocal(Integer.parseInt(currentId));
        arrayUser.clear();
        while (cursor1.moveToNext()) {
            arrayUser.add(new User(cursor1.getInt(1),cursor1.getString(2),cursor1.getString(3)));
        }
        if(arrayUser.size() > 0) {
            tvAssign.setVisibility(View.GONE);
        } else {
            tvAssign.setVisibility(View.VISIBLE);
        }
        assignAdapter.notifyDataSetChanged();
    }

    public void refresh() {
        tvCardName.setText(currentCard.getName());
        if(currentCard.getDescription().equals("")) {
            tvDescription.setText(getString(R.string.no_description));
        } else {
            tvDescription.setText(currentCard.getDescription());
        }
        if(timeNotSet(currentCard)) {
            tvTime.setText(getString(R.string.no_time));
        } else {
            tvTime.setText(currentCard.getDate() + " " + getString(R.string.at) + " " + currentCard.getTime());
        }
        if(currentCard.getLocation().equals("")) {
            tvLocation.setText(getString(R.string.not_set));
        } else {
            tvLocation.setText(currentCard.getLocation());
        }
        switchAlarmCheck();
    }

    public void showCardOptions(ImageButton button, final Card card) {
        PopupMenu menu = new PopupMenu(this, button);
        menu.getMenuInflater().inflate(R.menu.menu_card_option, menu.getMenu());

        if(is_owner == 0) {
            menu.getMenu().findItem(R.id.menuEditCard).setEnabled(false);
            menu.getMenu().findItem(R.id.menuSetTime).setEnabled(false);
            menu.getMenu().findItem(R.id.menuSetLocation).setEnabled(false);
            menu.getMenu().findItem(R.id.menuAssign).setEnabled(false);
            menu.getMenu().findItem(R.id.menuDeleteCard).setEnabled(false);
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menuEditCard:
                        editCardDialog(card);
                        break;
                    case R.id.menuSetTime:
                        setTimeDialog(card);
                        break;
                    case R.id.menuSetLocation:
                        setLocationDialog(card);
                        break;
                    case R.id.menuAssign:
                        chooseMemberDialog(card, Integer.parseInt(boardId));
                        break;
                    case R.id.menuDeleteCard:
                        deleteCardDialog(card);
                        break;
                }

                return false;
            }
        });
        menu.show();
    }

    public void editCardDialog(final Card card) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_card);

        Button btEditCardOk = (Button) dialog.findViewById(R.id.btEditCardOk);
        Button btEditCardCancel = (Button) dialog.findViewById(R.id.btEditCardCancel);
        final EditText edtEditCardName = (EditText) dialog.findViewById(R.id.edtEditCardName);
        final EditText edtEditCardDescription = (EditText) dialog.findViewById(R.id.edtEditCardDescription);

        edtEditCardDescription.setText(card.getDescription());
        edtEditCardName.setText(card.getName());
        edtEditCardName.setSelection(edtEditCardName.getText().length());

        btEditCardOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtEditCardName.getText().toString();
                String description = edtEditCardDescription.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(CardDetailsActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    editCard(editCardURL, name, description, card.getId());
                }
            }
        });

        btEditCardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    public void setLocationDialog(final Card card) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_set_location);

        Button btSetLocationOk = (Button) dialog.findViewById(R.id.btSetLocationOk);
        Button btSetLocationCancel = (Button) dialog.findViewById(R.id.btSetLocationCancel);
        ImageButton btClearLocation = (ImageButton) dialog.findViewById(R.id.btClearLocation);
        final EditText edtSetLocation = (EditText) dialog.findViewById(R.id.edtSetLocation);
        final TextView tvLocationLat = (TextView) dialog.findViewById(R.id.tvLocationLat);
        final TextView tvLocationLng = (TextView) dialog.findViewById(R.id.tvLocationLng);
        savedEditText = edtSetLocation;
        savedTvLat = tvLocationLat;
        savedTvLng = tvLocationLng;

        if(!currentCard.getLocation().equals("") && !currentCard.getLat().equals("") && !currentCard.getLng().equals("")) {
            edtSetLocation.setText(currentCard.getLocation());
            tvLocationLat.setText(currentCard.getLat());
            tvLocationLng.setText(currentCard.getLng());
        }

        btSetLocationOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String location = edtSetLocation.getText().toString();
                String lat = tvLocationLat.getText().toString();
                String lng = tvLocationLng.getText().toString();
                dialog.dismiss();
                setLocation(setLocationURL, location, lat, lng, card.getId());
            }
        });

        btSetLocationCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        edtSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent = builder.build(CardDetailsActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btClearLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSetLocation.setText("");
                tvLocationLat.setText(getString(R.string.no_location));
                tvLocationLng.setText(getString(R.string.no_location));
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void setLocation(String url, final String location, final String lat, final String lng, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            setLocationLocal(id, location, lat, lng);
                            showCardDetailsLocal();
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.set_location_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("location", location);
                params.put("lat", lat);
                params.put("lng", lng);
                params.put("idCard", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PLACE_PICKER_REQUEST) {
            if(resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                savedEditText.setText(place.getName().toString() + " ~ " + place.getAddress().toString());
                savedTvLat.setText(String.valueOf(place.getLatLng().latitude));
                savedTvLng.setText(String.valueOf(place.getLatLng().longitude));
            }
        }
    }

    private void editCard(String url, final String name, final String description, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            editCardLocal(id, name, description);
                            showCardDetailsLocal();
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.edit_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idCard", String.valueOf(id));
                params.put("nameCard", name);
                params.put("description", description);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void chooseMemberDialog(Card card, int boardId) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_member_card);

        ListView lvBoardMembers = (ListView) dialog.findViewById(R.id.lvBoardMembers);
        Button btAssignOk = (Button) dialog.findViewById(R.id.btAssignOk);
        Button btAssignCancel = (Button) dialog.findViewById(R.id.btAssignCancel);
        TextView tvNoOne = (TextView) dialog.findViewById(R.id.tvNoOne);

        ArrayList<User> arrayUser = new ArrayList<>();
        Cursor cursor = getBoardMemberLocal(boardId);
        while (cursor.moveToNext()) {
            if(!isAssigned(card.getId(), cursor.getInt(1))) {
                arrayUser.add(new User(cursor.getInt(1), cursor.getString(2), cursor.getString(3)));
            }
        }
        if(arrayUser.size() > 0) {
            tvNoOne.setVisibility(View.GONE);
        }
        MemberAdapter memberAdapter = new MemberAdapter(CardDetailsActivity.this, R.layout.item_member_board, arrayUser);
        lvBoardMembers.setAdapter(memberAdapter);
        memberAdapter.notifyDataSetChanged();

        btAssignCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btAssignOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assign(assignURL);
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    public void deleteCardDialog(final Card card) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.confirmDelete) + " " + getString(R.string.card) + " " + card.getName() + "?");

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
                deleteCard(deleteCardURL, card.getId());
            }
        });

        dialog.show();
    }

    public void removeCardMemberDialog(final String email, String name) {
        if (is_owner == 0) {
            Toast.makeText(this, getString(R.string.not_owner), Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Are you sure want to remove " + name + " from card " + currentCard.getName() + "?");

            dialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Cursor cursor = getUser(email);
                    int idUser = 0;
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            idUser = cursor.getInt(1);
                        }
                    }
                    dialog.dismiss();
                    removeCardMember(removeCardMemberURL, currentCard.getId(), idUser);
                }
            });

            dialog.show();
        }
    }

    public void removeCardMember(String url, final int idCard, final int idUser) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            removeCardMemberLocal(idCard, idUser);
                            showCardDetailsLocal();
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.removed), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idCard", String.valueOf(idCard));
                params.put("idUser", String.valueOf(idUser));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void deleteCard(String url, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            deleteCardLocal(id);
                            finish();
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idCard", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void assign(String url) {
        if(checkedUser.size() != 0) {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.trim().equals("1")) {
                                for (int i = 0; i < checkedUser.size(); i++) {
                                    Cursor cursor = getMemberInfo(Integer.parseInt(boardId), Integer.parseInt(checkedUser.get(i)));
                                    if(cursor.moveToFirst()) {
                                        assignCardLocal(currentCard.getId(), cursor.getInt(1), cursor.getString(2), cursor.getString(3));
                                    }
                                }
                                showCardDetailsLocal();
                                checkedUser.clear();
                                Toast.makeText(CardDetailsActivity.this, getString(R.string.assign_success), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                                Log.e("ERROR", response);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                        }
                    }
            ){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("idCard", String.valueOf(currentCard.getId()));
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < checkedUser.size(); i++) {
                        sb.append(checkedUser.get(i)).append(",");
                    }
                    Log.e("ids", sb.toString());
                    params.put("idUsers", sb.toString());
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }
    }

    private void setTimeDialog(final Card card) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_set_time);

        Button btSetTimeOk = (Button) dialog.findViewById(R.id.btSetTimeOk);
        Button btSetTimeCancel = (Button) dialog.findViewById(R.id.btSetTimeCancel);
        ImageButton btClearTime = (ImageButton) dialog.findViewById(R.id.btClearTime);
        final CheckBox cbNoticeMember = (CheckBox) dialog.findViewById(R.id.cbNoticeMember);
        final EditText edtSetDate = (EditText) dialog.findViewById(R.id.edtSetDate);
        final EditText edtSetTime = (EditText) dialog.findViewById(R.id.edtSetTime);

        if(!timeNotSet(currentCard)) {
            edtSetDate.setText(card.getDate());
            edtSetTime.setText(card.getTime());
        }

        btClearTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtSetDate.setText("");
                edtSetTime.setText("");
            }
        });

        edtSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(edtSetDate, card);
            }
        });

        cbNoticeMember.setChecked(true);

        edtSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickTime(edtSetTime, card);
            }
        });

        btSetTimeOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = edtSetDate.getText().toString();
                String time = edtSetTime.getText().toString();
                if((date.equals("") && time.equals("")) || (!date.equals("") && !time.equals(""))) {
                    dialog.dismiss();
                    if (cbNoticeMember.isChecked()) {
                        setTime(setTimeURL, date, time, card.getId(), 1);
                    } else {
                        setTime(setTimeURL, date, time, card.getId(), 0);
                    }
                } else {
                    Toast.makeText(CardDetailsActivity.this, getString(R.string.complete_time), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btSetTimeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void pickDate(final EditText edt, Card card) {
        final Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        if(!card.getDate().equals("")) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            try {
                Date date = df.parse(card.getDate());
                calendar.setTime(date);
                day = calendar.get(Calendar.DATE);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                edt.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    private void pickTime(final EditText edt, Card card) {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if(!card.getTime().equals("")) {
            DateFormat df = new SimpleDateFormat("HH:mm", Locale.US);
            try {
                Date date = df.parse(card.getTime());
                calendar.setTime(date);
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(0,0,0,hourOfDay,minute);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
                edt.setText(simpleDateFormat.format(calendar.getTime()));
            }
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setTime(String url, final String date, final String time, final int id, final int nf) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            setTimeLocal(id, date, time, nf);
                            showCardDetailsLocal();
                            if(!timeNotSet(currentCard) && !expired(currentCard)) {
                                swNotification.setChecked(true);
                            }
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.set_time_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardDetailsActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardDetailsActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("date", date);
                params.put("time", time);
                params.put("notice", String.valueOf(nf));
                params.put("idCard", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void setAlarm() {
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
        if(!expired(currentCard)) {
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
                    CardDetailsActivity.this, currentCard.getId(), intentReciever, PendingIntent.FLAG_UPDATE_CURRENT
            );
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String id = sharedPreferences.getString("ids", "");
            String[] ids = id.split(",");
            if (!checkAlarm(ids,currentCard.getId())) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < ids.length; i++) {
                    sb.append(ids[i]).append(",");
                }
                sb.append(String.valueOf(currentCard.getId())).append(",");
                editor.putString("ids", sb.toString());
                editor.apply();
            }
            String idC = sharedPreferences.getString("idsCancel", "");
            String[] idsC = idC.split(",");
            if (checkAlarm(idsC, currentCard.getId())) {
                String currentId = String.valueOf(currentCard.getId());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < idsC.length; i++) {
                    if(!idsC[i].equals(currentId)) {
                        sb.append(idsC[i]).append(",");
                    }
                }
                editor.putString("idsCancel", sb.toString());
                editor.apply();
            }
        } else {
            swNotification.setChecked(false);
            Toast.makeText(this, getString(R.string.expired), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSound() {
        cancelAlarm(currentCard);
        intentReciever.putExtra("status", "off");
        sendBroadcast(intentReciever);
    }

    private void switchAlarmCheck() {
        if(expired(currentCard) || timeNotSet(currentCard)) {
            swNotification.setChecked(false);
            cancelAlarm(currentCard);
        } else {
            String id = sharedPreferences.getString("ids", "");
            String[] ids = id.split(",");
            if (checkAlarm(ids, currentCard.getId())) {
                swNotification.setChecked(true);
                setAlarm();
            } else {
                swNotification.setChecked(false);
            }
        }
    }

    private boolean locationNotSet() {
        if(!currentCard.getLocation().equals("") && !currentCard.getLat().equals("") && !currentCard.getLng().equals("")) {
            return false;
        }
        return true;
    }

    private void showDirection() {
        Intent intent = new Intent(CardDetailsActivity.this, MapsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("lat", currentCard.getLat());
        extras.putString("lng", currentCard.getLng());
        intent.putExtras(extras);
        startActivity(intent);
    }
}

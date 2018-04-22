package lc.btl;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CardsListActivity extends BaseActivity {

    private String addListURL = baseURL + "/insertList.php";
    private String deleteListURL = baseURL + "/deleteList.php";
    private String editListURL = baseURL + "/editList.php";
    private String addCardURL = baseURL + "/insertCard.php";
    private String addUserURL = baseURL + "/addMember.php";
    final public String moveCardURL = baseURL + "/moveCard.php";

    boolean shouldExecuteOnResume;
    TextView tvBoardName;
    ImageButton btAddList, btAddUser;
    ArrayList<CardList> listList;
    Board currentBoard;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    CardListAdapter RecyclerViewHorizontalAdapter;
    LinearLayoutManager HorizontalLayout ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards_list);

        sync();

        Intent intent = getIntent();
        currentBoard = (Board) intent.getSerializableExtra("board");
        tvBoardName.setText(currentBoard.getName());

        shouldExecuteOnResume = false;
        listList = new ArrayList<>();

        if(currentBoard.getIs_owner() == 0) {
            btAddUser.setEnabled(false);
            btAddUser.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
            btAddList.setEnabled(false);
            btAddList.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        } else {
            btAddUser.setEnabled(true);
            btAddUser.setColorFilter(null);
            btAddList.setEnabled(true);
            btAddList.setColorFilter(null);
        }

        btAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListDialog();
            }
        });
        btAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUserDialog();
            }
        });


        recyclerView = (RecyclerView)findViewById(R.id.recyclerview1);

        RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());

        recyclerView.setLayoutManager(RecyclerViewLayoutManager);

        RecyclerViewHorizontalAdapter = new CardListAdapter(this,listList);

        HorizontalLayout = new LinearLayoutManager(CardsListActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);

        recyclerView.setAdapter(RecyclerViewHorizontalAdapter);

        showListsLocal();
        handleScrollOnDrag();
    }

    private void handleScrollOnDrag() {
        recyclerView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        if (event.getX() < v.getWidth()*0.2) {
                            recyclerView.smoothScrollBy(-30,0);
                        } else if (event.getX() > v.getWidth()*0.8) {
                            recyclerView.smoothScrollBy(+30,0);
                        }
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        break;
                    case DragEvent.ACTION_DROP:
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(shouldExecuteOnResume){
            showListsLocal();
        } else {
            shouldExecuteOnResume = true;
        }
    }

    private void sync() {
        tvBoardName = (TextView) findViewById(R.id.tvBoardName);
        btAddList = (ImageButton) findViewById(R.id.btAddList);
        btAddUser = (ImageButton) findViewById(R.id.btAddUser);
    }

    private void showListsLocal() {
        listList.clear();
        Cursor cursor = getListsLocal(currentBoard.getId());
        while(cursor.moveToNext()) {
            CardList current = new CardList(cursor.getInt(0), cursor.getString(1));
            Cursor cursor1 = getCardsLocal(cursor.getInt(0));
            ArrayList<Card> cards = new ArrayList<>();
            while (cursor1.moveToNext()) {
                cards.add(new Card(cursor1.getInt(0), cursor1.getString(1),cursor1.getString(3),cursor1.getString(4), cursor1.getInt(8)));
            }
            current.setCards(cards);
            listList.add(current);
        }
        RecyclerViewHorizontalAdapter.notifyDataSetChanged();
    }

    private void addListDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_list);

        Button btAddListOk = (Button) dialog.findViewById(R.id.btAddListOk);
        Button btAddListCancel = (Button) dialog.findViewById(R.id.btAddListCancel);
        TextView tvBoardToAdd = (TextView) dialog.findViewById(R.id.tvBoardToAdd);
        final EditText edtNewList = (EditText) dialog.findViewById(R.id.edtNewList);

        tvBoardToAdd.setText(getString(R.string.to_board) + " " + currentBoard.getName());

        btAddListOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtNewList.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(CardsListActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    // add board
                    dialog.dismiss();
                    addList(addListURL, name, currentBoard.getId());
                }
            }
        });

        btAddListCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void addUserDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_user);

        Button btAddUserOk = (Button) dialog.findViewById(R.id.btAddUserOk);
        Button btAddUserCancel = (Button) dialog.findViewById(R.id.btAddUserCancel);
        TextView tvBoardToAddUser = (TextView) dialog.findViewById(R.id.tvBoardToAddUser);
        final EditText edtAddUser = (EditText) dialog.findViewById(R.id.edtAddUser);

        tvBoardToAddUser.setText(getString(R.string.to_board) + " " + currentBoard.getName());

        btAddUserOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtAddUser.getText().toString();
                if(email.equals("")) {
                    Toast.makeText(CardsListActivity.this, getString(R.string.empty_email), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    addUser(addUserURL, email, currentBoard.getId());
                }
            }
        });

        btAddUserCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void addList(String url, final String name, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] parts = response.trim().split(":");
                        if (parts[0].equals("id")) {
                            int idList = Integer.parseInt(parts[1].trim());
                            insertListLocal(idList, name, id);
                            showListsLocal();
                            Toast.makeText(CardsListActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nameList", name);
                params.put("idBoard", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void addUser(String url, final String email, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("99")) {
                            Toast.makeText(CardsListActivity.this, getString(R.string.not_found_email) + " " + email, Toast.LENGTH_SHORT).show();
                        } else if (response.trim().equals("100")) {
                            Toast.makeText(CardsListActivity.this, getString(R.string.member_existed), Toast.LENGTH_SHORT).show();
                        } else {
                            String[] parts = response.trim().split("-");
                            if(parts[0].equals("user")) {
                                addBoardMemberLocal(id,Integer.parseInt(parts[1]),parts[2],parts[3]);
                                Toast.makeText(CardsListActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                                Log.e("ERROR", response);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
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

    public void moveCard(String url, final String idCard, final String oldList, final String newList) {
        if (currentBoard.getIs_owner() == 0) {
            Toast.makeText(this, getString(R.string.not_owner), Toast.LENGTH_SHORT).show();
        } else {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.trim().equals("1")) {
                                moveCardLocal(idCard, oldList, newList);
                                showListsLocal();
                                Toast.makeText(CardsListActivity.this, getString(R.string.moved), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("idCard", idCard);
                    params.put("oldList", oldList);
                    params.put("newList", newList);
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        }
    }

    public void showListOptions(ImageButton button, final CardList list) {
        PopupMenu menu = new PopupMenu(this, button);
        menu.getMenuInflater().inflate(R.menu.menu_list_option, menu.getMenu());

        if(currentBoard.getIs_owner() == 0) {
            menu.getMenu().findItem(R.id.menuAddCard).setEnabled(false);
            menu.getMenu().findItem(R.id.menuRenameList).setEnabled(false);
            menu.getMenu().findItem(R.id.menuDeleteList).setEnabled(false);
        }

        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menuAddCard:
                        addCardDialog(list);
                        break;
                    case R.id.menuRenameList:
                        editListDialog(list);
                        break;
                    case R.id.menuDeleteList:
                        deleteListDialog(list);
                        break;
                }

                return false;
            }
        });
        menu.show();
    }

    public void deleteListDialog(final CardList list) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(getString(R.string.confirmDelete) + " " + getString(R.string.list) + " " + list.getName() + "?");

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
                deleteList(deleteListURL, list.getId());
            }
        });

        dialog.show();
    }

    private void deleteList(String url, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            deleteListLocal(id);
                            showListsLocal();
                            Toast.makeText(CardsListActivity.this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idList", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void editListDialog(final CardList list) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_edit_list);

        Button btEditListOk = (Button) dialog.findViewById(R.id.btEditListOk);
        Button btEditListCancel = (Button) dialog.findViewById(R.id.btEditListCancel);
        final EditText edtEditList = (EditText) dialog.findViewById(R.id.edtEditList);

        edtEditList.setText(list.getName());
        edtEditList.setSelection(edtEditList.getText().length());

        btEditListOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtEditList.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(CardsListActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    editList(editListURL, name, list.getId());
                }
            }
        });

        btEditListCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void editList(String url, final String name, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.trim().equals("1")) {
                            editListLocal(id, name);
                            showListsLocal();
                            Toast.makeText(CardsListActivity.this, getString(R.string.edit_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("idList", String.valueOf(id));
                params.put("nameList", name);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void addCardDialog(final CardList list) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_card);

        TextView tvListToAdd = (TextView) dialog.findViewById(R.id.tvListToAdd);
        Button btAddCardOk = (Button) dialog.findViewById(R.id.btAddCardOk);
        Button btAddCardCancel = (Button) dialog.findViewById(R.id.btAddCardCancel);
        final EditText edtNewCard = (EditText) dialog.findViewById(R.id.edtNewCard);
        final EditText edtNewCardDescription = (EditText) dialog.findViewById(R.id.edtNewCardDescription);

        tvListToAdd.setText(getString(R.string.to_list) + " " + list.getName());

        btAddCardOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtNewCard.getText().toString();
                String description = edtNewCardDescription.getText().toString();
                if(name.equals("")) {
                    Toast.makeText(CardsListActivity.this, getString(R.string.empty_field), Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    addCard(addCardURL, name, description, list.getId());
                }
            }
        });

        btAddCardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        setDialogWidth(dialog, 0.9f);

        dialog.show();
    }

    private void addCard(String url, final String name, final String description, final int id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] parts = response.trim().split(":");
                        if (parts[0].equals("id")) {
                            int idCard = Integer.parseInt(parts[1].trim());
                            insertCardLocal(idCard, name, description, "", "", "", "", "", id);
                            showListsLocal();
                            Toast.makeText(CardsListActivity.this, getString(R.string.add_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CardsListActivity.this, getString(R.string.errorPOST), Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CardsListActivity.this, getString(R.string.errorServe), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("nameCard", name);
                params.put("description", description);
                params.put("idList", String.valueOf(id));
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void showCardDetails(Card card, String boardName, int is_owner) {
        Intent intent = new Intent(CardsListActivity.this, CardDetailsActivity.class);
        Bundle extras = new Bundle();
        extras.putString("cardId", String.valueOf(card.getId()));
        extras.putString("boardId", String.valueOf(currentBoard.getId()));
        extras.putString("boardName", boardName);
        extras.putInt("is_owner", is_owner);
        intent.putExtras(extras);
        startActivity(intent);
    }
}

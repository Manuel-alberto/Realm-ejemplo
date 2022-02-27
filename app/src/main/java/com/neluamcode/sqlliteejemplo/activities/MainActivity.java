package com.neluamcode.sqlliteejemplo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.neluamcode.sqlliteejemplo.R;
import com.neluamcode.sqlliteejemplo.adapters.boardAdapter;
import com.neluamcode.sqlliteejemplo.models.Board;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements RealmChangeListener<RealmResults<Board>>, AdapterView.OnItemClickListener {

    private Realm realm;

    private ListView listView;
    private boardAdapter adapter;
    private RealmResults<Board> boards;

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //BD realm
        realm = Realm.getDefaultInstance();
        boards = realm.where(Board.class).findAll();
        boards.addChangeListener(this);
        this.setTitle("Realm database");

        listView = (ListView) findViewById(R.id.listViewBoard);
        adapter =new boardAdapter(this, boards, R.layout.list_view_board_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        fab = (FloatingActionButton) findViewById(R.id.addBoard);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertforCreatingBoard("Añade nuevo board", "Escribe el nombre de titulo");
            }
        });

        registerForContextMenu(listView);

    }

    private void createNewBoard(String boardName){
        realm.beginTransaction();
        Board board=new Board(boardName);
        realm.copyToRealm(board);
        realm.commitTransaction();
    }

    private void editBoard(String newName, Board board){
        realm.beginTransaction();
        board.setTitle(newName);
        realm.copyToRealmOrUpdate(board);
        realm.commitTransaction();
    }

    private void deleteAll(){
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    private void deleteBoard(Board board){
        realm.beginTransaction();
        board.deleteFromRealm();
        realm.commitTransaction();
    }

    private void showAlertforCreatingBoard(String title, String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.layout_create_board, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.edt_tittle);

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String boardName= input.getText().toString().trim();
                if (boardName.length() > 0){
                    createNewBoard(boardName);
                }else{
                    Toast.makeText(MainActivity.this, "Es requerido el nombre para crear una nota", Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void showAlertforEditingBoard(String title, String message, final Board board){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.layout_create_board, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.edt_tittle);
        input.setText(board.getTitle());

        builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String boardName= input.getText().toString().trim();
                if (boardName.length() == 0){
                    Toast.makeText(MainActivity.this, "Es requerido el nombre para editarlo", Toast.LENGTH_SHORT).show();
                }else if(boardName.equals(board.getTitle())){
                    Toast.makeText(MainActivity.this, "El nombre debe de ser diferente", Toast.LENGTH_SHORT).show();
                }else{
                    editBoard(boardName, board);
                }
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_board_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_all:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(boards.get(info.position).getTitle());
        getMenuInflater().inflate(R.menu.context_menu_board_activity, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()){
            case R.id.deleted_board:
                    deleteBoard(boards.get(info.position));
                return true;
            case R.id.edit_board:
                    showAlertforEditingBoard("Editar boar", "Cambia el nombre del board", boards.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onChange(RealmResults<Board> boards) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent=new Intent(MainActivity.this, NoteActivity.class);
        intent.putExtra("id", boards.get(position).getId());
        startActivity(intent);
    }
}
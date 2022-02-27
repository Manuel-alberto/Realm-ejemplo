package com.neluamcode.sqlliteejemplo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.FloatArrayEvaluator;
import android.content.DialogInterface;
import android.os.Bundle;
import android.service.controls.actions.FloatAction;
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
import com.neluamcode.sqlliteejemplo.adapters.NoteAdapter;
import com.neluamcode.sqlliteejemplo.models.Board;
import com.neluamcode.sqlliteejemplo.models.Note;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class NoteActivity extends AppCompatActivity implements RealmChangeListener<Board> {

    private ListView listView;
    private FloatingActionButton fab;
    private NoteAdapter adapter;
    private RealmList<Note> notes;
    private Realm realm;
    private int boardId;
    private Board board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        realm = Realm.getDefaultInstance();

        if(getIntent().getExtras() != null){
            boardId = getIntent().getExtras().getInt("id");
        }

        board = realm.where(Board.class).equalTo("id", boardId).findFirst();
        notes = board.getNotes();

        board.addChangeListener(this);

        this.setTitle(board.getTitle());

        fab = (FloatingActionButton) findViewById(R.id.addNote);
        listView = (ListView) findViewById(R.id.listViewNote);
        adapter = new NoteAdapter(this, notes, R.layout.list_view_note_item);
        listView.setAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertforCreatingNote("Añadir nota", "Escribe una nota para "+board.getTitle()+".");
            }
        });

        registerForContextMenu(listView);

    }

    private void createNewNote(String note){
        realm.beginTransaction();
        Note _note= new Note(note);
        realm.copyToRealm(_note);
        board.getNotes().add(_note);
        realm.commitTransaction();
    }

    private void editNote(String newNoteDescription, Note note){
        realm.beginTransaction();
        note.setDescripcion(newNoteDescription);
        realm.copyToRealmOrUpdate(note);
        realm.commitTransaction();
    }

    private void deleteNote(Note note){
        realm.beginTransaction();
        note.deleteFromRealm();
        realm.commitTransaction();
    }

    private void deleteAll(){
        realm.beginTransaction();
        board.getNotes().deleteAllFromRealm();
        realm.commitTransaction();
    }

    private void showAlertforCreatingNote(String title, String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String note= input.getText().toString().trim();
                if (note.length() > 0){
                    createNewNote(note);
                }else{
                    Toast.makeText(NoteActivity.this, "No puede estar en blanco", Toast.LENGTH_LONG).show();
                }
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
    }

    private void showAlertforEditingNote(String title, String message, final Note note) {

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_note, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewNote);
        input.setText(note.getDescripcion());

        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteDescription= input.getText().toString().trim();
                if (noteDescription.length() == 0){
                    Toast.makeText(NoteActivity.this, "Es requerido el texto ha editar", Toast.LENGTH_SHORT).show();
                }else if(noteDescription.equals(note.getDescripcion())){
                    Toast.makeText(NoteActivity.this, "No puedes guardar lo mismo", Toast.LENGTH_LONG).show();
                }else{
                    editNote(noteDescription, note);
                }
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_all_notes:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu_note_activity, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.deleted_note:
                deleteNote(notes.get(info.position));
                return true;
            case R.id.edit_description:
                showAlertforEditingNote("Edit Note", "Change note", notes.get(info.position));
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onChange(Board board) {
        adapter.notifyDataSetChanged();
    }
}
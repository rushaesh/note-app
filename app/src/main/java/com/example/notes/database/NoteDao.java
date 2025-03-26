package com.example.notes.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    Note getNoteById(int noteId);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("DELETE FROM notes")
    void deleteAllNotes();

    @Query("UPDATE notes SET isTrashed = 1 WHERE id = :noteId")
    void moveToTrash(int noteId);


    @Query("SELECT * FROM notes WHERE isTrashed = 0 AND isArchived = 0 ORDER BY isPinned DESC, id DESC")
    LiveData<List<Note>> getAllNote(); // ✅ Show pinned notes first

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    void updatePinStatus(int noteId, boolean isPinned);
    @Query("SELECT * FROM notes WHERE isArchived = 1 ORDER BY id DESC")
    LiveData<List<Note>> getArchivedNotes(); // ✅ Separate method for archived notes
    @Query("SELECT * FROM notes WHERE isTrashed = 1 ORDER BY id DESC")
    LiveData<List<Note>> getTrashedNotes();
    @Query("SELECT * FROM notes  WHERE isTrashed = 0 AND isArchived = 0 ORDER BY noteText ASC")
    LiveData<List<Note>> getNotesAscending();

    @Query("SELECT * FROM notes  WHERE isTrashed = 0 AND isArchived = 0 ORDER BY noteText DESC")
    LiveData<List<Note>> getNotesDescending();
    @Query("SELECT * FROM notes  WHERE isTrashed = 0 AND isArchived = 0 ORDER BY date ASC")
    LiveData<List<Note>> getNotesByDateAsc();

    @Query("SELECT * FROM notes  WHERE isTrashed = 0 AND isArchived = 0 ORDER BY date DESC")
    LiveData<List<Note>> getNotesByDateDesc();
    @Query("SELECT * FROM notes WHERE :imagePath IN (imagePaths)")
    Note getNoteByImagePath(String imagePath);



}

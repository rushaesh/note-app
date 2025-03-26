package com.example.notes.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.notes.Converters;

import java.util.Date;
import java.util.List;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int color;
    public int color2;
    public boolean isTrashed;
    public boolean isArchived;
    public boolean isDeleted;
    @TypeConverters(Converters.class) // ✅ Add this line
    private Date date;
    @TypeConverters(Converters.class)
    public List<String> imagePaths;
    private String checklist; // Stores checklist items as text
    private boolean allChecked; // Stores whether all items are checked
    public String noteText;
    public String noteTttale;

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }


    public Date getDate() {
        return date != null ? date : new Date(); // ✅ Prevents null
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public boolean isTrashed() {
        return isTrashed;
    }

    public void setTrashed(boolean trashed) {
        isTrashed = trashed;
    }

    public boolean isPinned = false;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        this.color2 = color2;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }


    public String getNoteTttale() {
        return noteTttale;
    }

    public void setNoteTttale(String noteTttale) {
        this.noteTttale = noteTttale;
    }


    public String getChecklist() {
        return checklist;
    }

    public boolean isAllChecked() {
        return allChecked;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public void setAllChecked(boolean allChecked) {
        this.allChecked = allChecked;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }
}

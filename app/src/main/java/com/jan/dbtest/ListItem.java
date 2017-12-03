package com.jan.dbtest;



// Object class for the object of the list
public class ListItem {

    private String title;
    private int imageId;
    private String description;
    private boolean attended;

    public ListItem(){              // Constructor for the object without parameters

    }

    public ListItem(String title, int imageId, String description, boolean attended){
        this.title = title;
        this.imageId = imageId;
        this.description = description;
        this.attended = attended;
    }

    public String getTitle(){           // Getter for the title string
        return this.title;
    }

    public void setTitle(String newTitle){
        this.title = newTitle;
    }

    public int getImageId(){
        return this.imageId;
    }

    public void setImageId(int imageId){
        this.imageId = imageId;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String newDescription){
        this.description = newDescription;
    }

    public void setAttended(boolean attended){
        this.attended = attended;
    }

    public boolean getAttended(){
        return this.attended;
    }
}

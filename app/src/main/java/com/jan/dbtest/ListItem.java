package com.jan.dbtest;



// Object class for the object of the list
public class ListItem {

    private String title;
    private int imageId;
    private String description;

    public ListItem(){              // Constructor for the object without parameters

    }

    public ListItem(String title, int imageId, String description){
        this.title = title;
        this.imageId = imageId;
        this.description = description;
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

}

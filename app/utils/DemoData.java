package utils;

import models.*;
import play.Logger;

import java.util.ArrayList;

public class DemoData {

    public static User user1;

    public static Todo todo1;
    public static Todo todo2;
    
    public static void loadDemoData() {
        
        Logger.info("Loading Demo Data");

        user1 = new User("user1@demo.com", "password", "John Doe");
        //user1.todos.add(todo1);
        //user1.todos.add(todo2);
        user1.save();
        
        todo1 = new Todo(user1, "make it secure");
        todo1.save();

        todo2 = new Todo(user1, "make it neat");
        todo2.save();
        
        
        

    }

}

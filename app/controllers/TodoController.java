package controllers;

import models.Todo;
import play.Logger;
import play.data.Form;
import play.mvc.*;

import java.util.List;

import static play.libs.Json.toJson;

@With(SecurityController.class)
public class TodoController extends Controller {

    public static Result getAllTodos() {
        return ok(toJson(models.Todo.findByUser(SecurityController.getUser())));
    }

    public static Result createTodo() {
        Form<models.Todo> form = Form.form(models.Todo.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        else {
            models.Todo todo = form.get();
            todo.user = SecurityController.getUser();
            todo.save();
            return ok(toJson(todo));
        }
    }
    
}

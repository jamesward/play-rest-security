package controllers;

import models.Todo;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;

import javax.inject.Inject;

import static play.libs.Json.toJson;

@Security.Authenticated(Secured.class)
public class TodoController extends Controller {

    @Inject FormFactory formFactory;

    public Result getAllTodos() {
        return ok(toJson(Todo.findByUser(SecurityController.getUser())));
    }

    public Result createTodo() {
        Form<Todo> form = formFactory.form(Todo.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(form.errorsAsJson());
        }
        else {
            Todo todo = form.get();
            todo.user = SecurityController.getUser();
            todo.save();
            return ok(toJson(todo));
        }
    }
    
}

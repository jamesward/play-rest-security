package controllers;

import com.wordnik.swagger.annotations.*;
import models.Todo;
import play.Logger;
import play.data.Form;
import play.mvc.*;

import java.util.List;

import static play.libs.Json.toJson;

@Api(value = "/api/todos", description = "Operations with Todos")
@Security.Authenticated(Secured.class)
public class TodoController extends Controller {

    @ApiOperation(value = "get All Todos",
            notes = "Returns List of all Todos",
            response = Todo.class,
            httpMethod = "GET")
    public static Result getAllTodos() {
        return ok(toJson(models.Todo.findByUser(SecurityController.getUser())));
    }

    @ApiOperation(
            nickname = "createTodo",
            value = "Create Todo",
            notes = "Create Todo record",
            httpMethod = "POST",
            response = Todo.class
    )
    @ApiImplicitParams(
            {
                    @ApiImplicitParam(
                            name = "body",
                            dataType = "Todo",
                            required = true,
                            paramType = "body",
                            value = "Todo"
                    )
            }
    )
    @ApiResponses(
            value = {
                    @com.wordnik.swagger.annotations.ApiResponse(code = 400, message = "Json Processing Exception")
            }
    )
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

package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Todo;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;
import utils.DemoData;

import static org.junit.Assert.*;
import static play.test.Helpers.*;

public class TodoControllerTest extends WithApplication {

    @Test
    public void getAllTodosNoAuthToken() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        Result result = route(controllers.routes.TodoController.getAllTodos());
        assertEquals(UNAUTHORIZED, result.status());
        assertFalse(contentAsString(result).contains(demoData.todo1_1.value));
    }

    @Test
    public void getAllTodosInvalidAuthToken() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        Result result = route(fakeRequest(controllers.routes.TodoController.getAllTodos()).header(SecurityController.AUTH_TOKEN_HEADER, "wrong"));
        assertEquals(UNAUTHORIZED, result.status());
        assertFalse(contentAsString(result).contains(demoData.todo1_1.value));
    }
    

    @Test
    public void getAllTodosForUser1() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        String authToken = demoData.user1.createToken();

        Result result = route(fakeRequest(controllers.routes.TodoController.getAllTodos()).header(SecurityController.AUTH_TOKEN_HEADER, authToken));

        assertEquals(OK, result.status());
        assertTrue(contentAsString(result).contains(demoData.todo1_1.value));
    }

    @Test
    public void addTodo() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        String authToken = demoData.user1.createToken();

        ObjectNode todoJson = Json.newObject();
        todoJson.put("value", "make it work");

        Http.RequestBuilder fakeRequest = fakeRequest(controllers.routes.TodoController.createTodo())
                .header(SecurityController.AUTH_TOKEN_HEADER, authToken)
                .bodyJson(todoJson);

        Result result = route(fakeRequest);

        assertEquals(OK, result.status());

        Todo todo = Json.fromJson(Json.parse(contentAsString(result)), Todo.class);
        assertNotNull(todo.id);
        assertEquals("make it work", todo.value);
        assertNull(todo.user); // this should not be serialized
    }

    @Test
    public void addTodoNoValue() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        String authToken = demoData.user1.createToken();

        Http.RequestBuilder fakeRequest = fakeRequest(controllers.routes.TodoController.createTodo())
                .header(SecurityController.AUTH_TOKEN_HEADER, authToken);

        Result result = route(fakeRequest);

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void addTodoUnauthorized() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        Result result = route(fakeRequest(controllers.routes.TodoController.createTodo()));

        assertEquals(UNAUTHORIZED, result.status());
    }

}

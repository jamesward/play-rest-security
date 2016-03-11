package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;
import utils.DemoData;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

public class SecurityControllerTest extends WithApplication {

    @Test
    public void login() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        ObjectNode loginJson = Json.newObject();
        loginJson.put("emailAddress", demoData.user1.getEmailAddress());
        loginJson.put("password", demoData.user1.getPassword());

        Result result = route(fakeRequest(controllers.routes.SecurityController.login()).bodyJson(loginJson));

        assertEquals(OK, result.status());

        JsonNode json = Json.parse(contentAsString(result));
        assertNotNull(json.get("authToken"));
    }

    @Test
    public void loginWithBadPassword() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        ObjectNode loginJson = Json.newObject();
        loginJson.put("emailAddress", demoData.user1.getEmailAddress());
        loginJson.put("password", demoData.user1.getPassword().substring(1));

        Result result = route(fakeRequest(controllers.routes.SecurityController.login()).bodyJson(loginJson));

        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void loginWithBadUsername() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        ObjectNode loginJson = Json.newObject();
        loginJson.put("emailAddress", demoData.user1.getEmailAddress().substring(1));
        loginJson.put("password", demoData.user1.getPassword());

        Result result = route(fakeRequest(controllers.routes.SecurityController.login()).bodyJson(loginJson));

        assertEquals(UNAUTHORIZED, result.status());
    }

    @Test
    public void loginWithDifferentCaseUsername() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        ObjectNode loginJson = Json.newObject();
        loginJson.put("emailAddress", demoData.user1.getEmailAddress().toUpperCase());
        loginJson.put("password", demoData.user1.getPassword());

        Result result = route(fakeRequest(controllers.routes.SecurityController.login()).bodyJson(loginJson));

        assertEquals(OK, result.status());
    }

    @Test
    public void loginWithNullPassword() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        ObjectNode loginJson = Json.newObject();
        loginJson.put("emailAddress", demoData.user1.getEmailAddress());

        Result result = route(fakeRequest(controllers.routes.SecurityController.login()).bodyJson(loginJson));

        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void logout() {
        DemoData demoData = app.injector().instanceOf(DemoData.class);
        String authToken = demoData.user1.createToken();

        Result result = route(fakeRequest(controllers.routes.SecurityController.logout()).header(SecurityController.AUTH_TOKEN_HEADER, authToken));

        assertEquals(SEE_OTHER, result.status());
    }

}

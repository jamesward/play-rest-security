Securing Single Page Apps and REST Services (implement with Authenticator)
===========================================

The move towards Single Page Apps and RESTful services open the doors to a much better way of securing web applications.  Traditional web applications use browser cookies to identify a user when a request is made to the server.  This approach is fundamentally flawed and causes many applications to be vulnerable to Cross-Site Request Forgery (CSRF) attacks.  When used correctly, RESTful services can avoid this vulnerability altogether.  Before we go into the solution, lets recap the problem.

HTTP is a stateless protocol.  Make a request and get a response.  Make another request and get another response.  There is no correlation (i.e. "state") between these requests.  This poses a problem when you need to identify a user to the system because one request logs the user in and another request needs to tell the server who is making the request.

Web browsers have an automatic way to store some information (i.e. "state") on the user's machine and then add that information to every request.  This is called "cookies" and they provide a convenient way to create a correlation across HTTP requests.  Most web frameworks have a built-in concept called "session state" which uses a unique token for each user.  That token is stored in a cookie and automatically sent to the server on each request.  Now the server knows how to identify a user across requests.

This approach is simple and works great until you realize the dark truth of CSRF.  Usually a user is doing something that tells the browser to make a request to server and because the cookies are sent, everything is good.  But suppose the user gets an email that says "Check out these funny kittens!" with a link to a malicious website.  No one can avoid seeing funny kittens, so the user clicks the link.  It turns out that the funny kittens website is a malicious website which now makes some requests to an application that only uses cookies for authentication.  Perhaps the malicious request is to transfer money out of your bank account.  Or perhaps it posts something on a social network.  These requests will be identified AS THE USER because no matter what causes the request, the browser will send the cookies.  This is CSRF and many web apps are vulnerable to it.

The root of the problem is using cookies as the sole method of identifying a user since no matter how the request is initiated, the cookies which include the authentication token are always sent to the server.  One way to protect against this type of attack is to force each request to contain another token which is not automatically sent.  Most web frameworks provide a way to do this but they are error prone because it often requires developers to explicitly enable it and the approach doesn't always work well with Single Page Apps.


The Way Forward
---------------

The easiest way to do authentication without risking CSRF vulnerabilities is to simply avoid using cookies to identify the user.  However each request must still send a token to the server to identify the user.  This requires a token to be somehow "remembered" so that each request can manually send it.  Luckily Single Page Apps provide a way to keep a token in memory across requests because the page never reloads.

But what if the page does reload and the authentication token is lost because that in-memory state has been cleared?  Does the user have to log back in to get a new authentication token?  That would not be a very good user experience.  Browsers have a few ways to store data locally across requests.  The easiest is to simply use cookies.  Wait...  aren't cookies the root of the problem?  Cookies themselves are not the cause of CSRF vulnerabilities.  It's using the cookies on the server to validate a user that is the cause of CSRF.  Just putting an authentication token into a cookie doesn't mean it must be used as the mechanism to identify the user.

When a Single Page App loads it can read the cookies (via JavaScript), grab the authentication token, and then manually send that token on each request through a custom HTTP header.  This is safe because that malicious funny kitten site does not have access to the cookies.  If it did, every website would have a severe security issue.

The flow with this approach may go something like this:

1. The user navigates in their browser to the application
2. The server returns a basic web page and a JavaScript application
3. The JavaScript application can't find an authentication token in the web site's cookies
4. The JavaScript application displays a login form
5. The user enters correct login credentials and then submits the form
6. The server validates the login information and creates an authentication token for the user
7. The server sets the authentication token in a cookie and returns it to the JavaScript application
8. The JavaScript application makes a request for some protected data, sending the authentication token in a custom header
9. The server validates the token and then returns the data

At step 3 if the JavaScript application does find an authentication token in a cookie then it can skip ahead to step 8.

At step 9 the server may not be able to validate the token in which case it should return a 401 (Unauthorized) response which the JavaScript application can handle by going to step 4.

There are a variety of ways to implement this approach but the real key is that the server doesn't validate a user based on a cookie, it instead validates the user with a customer HTTP header.

This approach can be used over HTTP or HTTPS.  But it is highly recommended that authentication tokens are only passed over encrypted connections which means you should probably only be using this approach over HTTPS connections.  Whenever an application is not being used for local development it should automatically redirect HTTP connections to the corresponding HTTPS connection.  In this setup make sure that the cookie containing the authentication token can't be inadvertently transmitted over the HTTP connection by forcing the cookie to only be sent over HTTPS (an option which is typically available in cookie APIs).


Sample App
----------

To better explain this approach lets walk through an example application.  You can get the full source for the application from: [http://github.com/jamesward/play-rest-security](http://github.com/jamesward/play-rest-security)

This application is built using Play Framework, Java, jQuery, and CoffeeScript.

To run the application locally, [download latest Typesafe Activator](http://www.playframework.com/download), extract the zip and optionally add the extracted directory to your system's path.  Then using a command line, navigate into the `play-rest-security` directory and run the following (assuming the `activator` command is in your path):

    activator run

This will start the application which you can connect to in your browser at: [http://localhost:9000/](http://localhost:9000/)

You should see a login form which you can test out and once logged in, you will see the protected data and can add new data.

There are also a number of [functional and unit tests](https://github.com/jamesward/play-rest-security/tree/master/test) for the application which validate the security of the application.  You can run the tests locally by running:

    activator test

### RESTful JSON Back-End Services

Starting with [User.java](https://github.com/jamesward/play-rest-security/tree/master/app/models/User.java) you will see this is a typical database-backed entity using JPA.  The `User` class has a property `authToken` which will store a single authentication token.  In a real-world application you will probably want to allow a user to be logged in from multiple clients (e.g. different browsers).  To enable this you could simply turn this into a list.  You may also want to have some tracking on when authentication tokens are used, what IP address used them, and when they were created.  The tokens could also be encrypted in the database.

The [Todo.java](https://github.com/jamesward/play-rest-security/tree/master/app/models/Todo.java) file contains the `Todo` entity which stores a user's Todos.  Access to the `Todo` objects happen via the [TodoController.java](https://github.com/jamesward/play-rest-security/tree/master/app/controllers/TodoController.java) class.  In this case the `TodoController` only has two methods, `getAllTodos()` and `createTodo()`.  These methods are exposed via HTTP through the [routes](https://github.com/jamesward/play-rest-security/tree/master/conf/routes) file.  The `TodoController` has the `@Security.Authenticated` annotation which uses action composting to call the 'getUsername' method in the [Secured.java](https://github.com/jamesward/play-rest-security/tree/master/app/controllers/Secured.java) class.

The `getUsername` method in the `Secured.java` class uses the authentication token to look up the User. If it finds one, it returns the username, if not it returns null. If null is returned, the request is blocked, and the 'onUnathorized' method in `Secured.java` is called, returning a redirect.

Both `getAllTodos()` and `createTodo()` in the `TodoController` use the authenticated user that was stored in the HTTP Context to either fetch the user's todos or create a new todo.

The `SecurityController` class also has `login` and `logout` request handlers which are mapped to URLs in the `routes` file.  The `login` method tries to locate a user by the provided username and password.  If it succeeds then it creates a new authentication token for the user, then creates a cookie containing the token, and returns the token in a JSON response.  The `logout` method uses the `SecurityController` interceptor to validate the user and then deletes the cookie that stores the authentication token and set's the user's `authToken` to null.

That is the RESTful back-end of the example app.  Now lets explore the front-end.

### CoffeeScript + jQuery Front-End UI

In the [routes](https://github.com/jamesward/play-rest-security/tree/master/conf/routes) file you will see that requests to `/` are handled by returning [public/index.html](https://github.com/jamesward/play-rest-security/tree/master/public/index.html).  This file doesn't do much other than load jQuery and also load the `index.min.js` file which is compiled and minified by Play's asset compiler.  The source for that file is [index.coffee](https://github.com/jamesward/play-rest-security/tree/master/app/assets/javascripts/index.coffee) and it provides the whole UI for the application.  This example uses CoffeeScript because it provides a more concise and readable syntax for writing JavaScript applications.

When the page is ready the `init` function is called and the application attempts to find the authentication token in a cookie.  If it can't be found then a login form is displayed.  If the cookie can be found then the `displayTodos` function is called.  This function tries to fetch the user's list of `Todo` objects and then display them.  The request to fetch the `Todo` objects is a normal Ajax JSON request except that the user's authentication token is sent in a custom HTTP header.  If the server responds with a 401 error then the application calls `displayLoginForm` otherwise the user's `Todo` objects are displayed.  The `createTodo` function also sends the authentication token in custom HTTP header and the JSON data for the `Todo` within an Ajax request.

That is really all there is to the front-end UI.  Most of the code in the CoffeeScript is displaying data and forms in the HTML through jQuery DOM manipulation.  This DOM manipulation could also be done through one of the many client-side templating libraries.


Further Learning
----------------

The important point to remember is that using cookies for authentication opens up the possibility of CSRF attacks.  Custom HTTP headers provide a more secure method of identifying users than cookies alone do.  The combination of Single Page Apps and REST services provide the perfect opportunity to move away from cookie based authentication.  This simple application illustrates how to implement this approach.

Learn more:

- [CSRF](http://en.wikipedia.org/wiki/XSRF)
- [Play Framework](http://playframework.com)

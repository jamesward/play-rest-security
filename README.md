Securing REST Services in Play Framework
========================================

The move towards Single Page Apps and RESTful services open the doors to a much better way of securing web applications.  Traditional web applications use browser cookies to identify a user when a request is made to the server.  This approach is fundamentally flawed and causes many applications to be vulnerable to Cross-Site Request Forgery (CSRF) attacks.  When used correctly, RESTful services can avoid this vulnerability alltogether.  Before we go into the solution, lets recap the problem.

HTTP is a stateless protocol.  Make a request and get a response.  Make another request and get another response.  There is no correlation (e.g. "state") between these requests.  This poses a problem when you need to identify a user to the system because one request logs the user in and another request needs to tell the server who is making the request.

Web browsers have an automatic way to store some information (e.g. "state") on the user's machine and then add that information to every request.  This is called "cookies" and they provide a convienent way to create a corallation across HTTP requests.  Most web frameworks have a built-in concept called "session state" which creates a unique token for each user.  That token is stored in a cookie and automatically sent to the server on each request.  Now the server knows how to identify a user across requests.

This approach is simple and works great until you realize the dark truth of CSRF.  Usually a user is doing something that tells the browser to make a request to server and because the cookies are sent, everything is good.  But suppose the user gets an email that says "Check out these funny kittens!" with a link to a malicious website.  No one can avoid seeing funny kittens, so the user clicks the link.  It turns out that the funny kittens website is a malicious website which now makes some requests to an application that only uses cookies for authentication.  Perhaps the malicious request is to transfer money out of your bank account.  Or perhaps it posts something on a social network.  These requests will be identified AS THE USER because no matter what causes the request, the browser will send the cookies.  This is CSRF and most web apps are vulnerable to it.

The root of the problem is using cookies as the sole method of identifying a user since no matter how the request is initiated, the cookies which include the authentication token is always sent to the server.  One way to protect against this type of attack is to force each request to contain another token which is not automatically sent.  Most web frameworks provide a way to do this but they are error prone because it often requires developers to explicitly enable it and the approach doesn't always work well with Single Page Apps.

The Way Forward
---------------

The easiest way to do authentication without risking CSRF vulnerabilities is to simply avoid using cookies to identify the user.  However each request must still send a token to the server to identify the user.  This requires a token to be somehow "remembered" so that each request can manually send it.  Luckily Single Page Apps provide a way to keep a token in memory across requests because the page never reloads.

But what if the page does reload and the authentication token is lost because that in-memory state has been cleared?  Does the user have to log back in to get a new authentication token?  That would not be a very good user experience.  Browsers have a few ways to store data locally across requests.  The easiest is to simply use cookies.  Wait...  Weren't cookies were the whole problem?  Cookies themseleves are not the cause of CSRF vulnerabilities.  It's using the cookies on the server to validate a user that is the cause of CSRF.  Just putting an authentication token into a cookie doesn't mean it must be used as the mechanism to identify the user.

When a Single Page App loads it can read the cookies (via JavaScript), grab the authentication token, and then manually send that token on each request through a custom HTTP header.  This is safe because that malicious funny kitten site does not have access to the cookies.  If it did, every website would have a severe security issue.

A typical flow will go something like:
1) User tries to access a protected resource
2) Server looks for a custom authentication token (via a custom HTTP header)
3) 


Sample App



Side note on https.
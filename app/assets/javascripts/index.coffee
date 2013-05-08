$ -> init()


init = () ->
  # try to get an auth token
  document.cookie.split('; ').forEach (cookieString) ->
    cookie = cookieString.split("=")
    if ((cookie.length == 2) && (cookie[0] == "authToken"))
      window.authToken = cookie[1]
      
  if (window.authToken == undefined)
    displayLoginForm()
  else
    displayTodos()


displayLoginForm = () ->
  $("body").empty()
  loginForm = $("<form>").attr("action", "/login").attr("method", "post").attr("id", "loginForm")
  loginForm.append $("<input>").attr("id", "emailAddress").attr("name", "emailAddress").val("user1@demo.com")
  loginForm.append $("<input>").attr("id", "password").attr("name", "password").attr("type", "password").val("password")
  loginForm.append $("<input>").attr("type", "submit").val("Login")
  loginForm.submit (event) ->
    event.preventDefault()
    $.ajax
      url: event.currentTarget.action
      type: event.currentTarget.method
      dataType: 'json'
      contentType: 'application/json'
      data: JSON.stringify({emailAddress: $("#emailAddress").val(), password: $("#password").val()})
      error: (jqXHR, errorText, error) ->
        displayError("Login failed")
      success: doLogin
  $("body").append loginForm


doLogin = (data, textStatus, jqXHR) ->
  window.authToken = data.authToken # global state holder for the auth token
  $("#loginForm").remove()
  displayTodos()


displayTodos = () ->
  fetchTodos()
  $("body").empty()
  $("body").append $("<button>").text("Logout").click(doLogout)
  $("body").append $("<h3>").text "Your Todos"
  todoList = $("<ul>").attr("id", "todos")
  $("body").append todoList
  todoForm = $("<form>").attr("action", "/todos").attr("method", "post").attr("id", "todoForm")
  todoForm.append $("<input>").attr("id", "todoValue").attr("name", "value").attr("required", true)
  todoForm.append $("<input>").attr("type", "submit").val("Create Todo")
  todoForm.submit(createTodo)
  $("body").append todoForm


createTodo = (event) ->
  event.preventDefault()
  $.ajax
    url: event.currentTarget.action
    type: event.currentTarget.method
    dataType: 'json'
    contentType: 'application/json'
    headers: {"X-AUTH-TOKEN": window.authToken}
    data: JSON.stringify({value: $("#todoValue").val()})
    error: (jqXHR, errorText, error) ->
      if (jqXHR.status == 401)
        displayLoginForm()
      else if (JSON.parse(jqXHR.responseText).value[0] != undefined)
        displayError("A value must be specified for the Todo")
      else
        displayError("An uknown error occurred")
    success: fetchTodos


fetchTodos = () ->
  $.ajax
    url: "/todos"
    type: "get"
    dataType: 'json'
    headers: {"X-AUTH-TOKEN": window.authToken}
    error: (jqXHR, errorMessage, error) ->
      if (jqXHR.status == 401)
        displayLoginForm()
    success: (todos) ->
      $("#todoValue").val("")
      todoList = $("#todos")
      todoList.empty()
      $.each todos, (index, todo) ->
        todoList.append $("<li>").text(todo.value)


displayError = (error) ->
  $("body").prepend $("<span>").text(error).css("color", "red")


doLogout = (event) ->
  $.ajax
    url: "/logout"
    type: "post"
    dataType: 'json'
    headers: {"X-AUTH-TOKEN": window.authToken}
    success: displayLoginForm
    error: displayLoginForm
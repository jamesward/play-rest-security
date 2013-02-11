$ ->
  # try to get an auth token
  authToken = null
  
  if (authToken == null)
    displayLoginForm()
  else
    displayTodos()
  

displayLoginForm = () ->
  # todo change the url to /login
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
      error: (jqXHR, textStatus, errorThrown) ->
        console.log textStatus
      success: doLogin
  $("body").append loginForm

doLogin = (data, textStatus, jqXHR) ->
  window.authToken = data.authToken # global state holder for the auth token
  $("#loginForm").remove()
  displayTodos()

displayTodos = () ->
  $("body").append $("<h3>").text "Your Todos"
  todoList = $("<ul>").attr("id", "todos")
  $("body").append todoList
  fetchTodos()
  todoForm = $("<form>").attr("action", "/todos").attr("method", "post").attr("id", "todoForm")
  todoForm.append $("<input>").attr("id", "todoValue").attr("name", "value")
  todoForm.append $("<input>").attr("type", "submit").val("Create Todo")
  todoForm.submit (event) ->
    event.preventDefault()
    $.ajax
      url: event.currentTarget.action
      type: event.currentTarget.method
      dataType: 'json'
      contentType: 'application/json'
      headers: {"X-AUTH-TOKEN": window.authToken}
      data: JSON.stringify({value: $("#todoValue").val()})
      error: (jqXHR, textStatus, errorThrown) ->
        console.log textStatus
      success: fetchTodos
  $("body").append todoForm

fetchTodos = () ->
  $.ajax
    url: "/todos"
    type: "get"
    dataType: 'json'
    headers: {"X-AUTH-TOKEN": window.authToken}
    success: (todos) ->
      $("#todoValue").val("")
      todoList = $("#todos")
      todoList.empty()
      $.each todos, (index, todo) ->
        todoList.append $("<li>").text(todo.value)
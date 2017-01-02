defmodule ElixirTodo.PageController do
  use ElixirTodo.Web, :controller

  def index(conn, _params) do
    render conn, "index.html"
  end
end

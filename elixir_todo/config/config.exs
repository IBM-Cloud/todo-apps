# This file is responsible for configuring your application
# and its dependencies with the aid of the Mix.Config module.
#
# This configuration file is loaded before any dependency and
# is restricted to this project.
use Mix.Config

# General application configuration
config :elixir_todo,
  ecto_repos: [ElixirTodo.Repo]

# Configures the endpoint
config :elixir_todo, ElixirTodo.Endpoint,
  url: [host: "localhost"],
  secret_key_base: "096nyl9CtZYE8p5dgtvJSag3btfqz7hnLkTKleEC5NYfhf66055ngr7SZzBAKPGK",
  render_errors: [view: ElixirTodo.ErrorView, accepts: ~w(html json)],
  pubsub: [name: ElixirTodo.PubSub,
           adapter: Phoenix.PubSub.PG2]

# Configures Elixir's Logger
config :logger, :console,
  format: "$time $metadata[$level] $message\n",
  metadata: [:request_id]

# Import environment specific config. This must remain at the bottom
# of this file so it overrides the configuration defined above.
import_config "#{Mix.env}.exs"

class TodosController < ApplicationController

	def index
		todos = Todo.all
		render :json => todos.as_json(except: [:created_at, :updated_at]), :status => :ok
	end

	def create
		todo = Todo.create(params[:todo].permit(:order, :title, :completed))
		render :json => todo.as_json(except: [:created_at, :updated_at]), :status => :created
	end

	def update
		todo = Todo.update(params[:id], params[:todo].permit(:order, :title, :completed))
		render :json => todo.as_json(except: [:created_at, :updated_at]), :status => :ok
	end

	def destroy
		Todo.destroy(params[:id])
		head :no_content
	end

end
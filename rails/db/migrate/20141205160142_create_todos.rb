class CreateTodos < ActiveRecord::Migration
  def change
    create_table :todos do |t|
      t.boolean :completed
      t.integer :order
      t.string :title

      t.timestamps
    end
  end
end

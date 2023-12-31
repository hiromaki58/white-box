require 'swagger_helper'

RSpec.describe 'api/v1/todos', type: :request do

  let(:Authorization) { 'Barer abcdefg123456' }

  path '/api/v1/todos' do

    get 'get todo list' do
      consumes 'application/json'
      produces 'application/json'
      security [Bearer: {}]
      response 200, 'todo list' do
        schema type: :array, items: {
          type: :object,
          properties: {
            name: { type: :string },
            done: { type: :boolean },
          },
          required: [:name, :done]
        }
        run_test!
      end
    end
  end
end
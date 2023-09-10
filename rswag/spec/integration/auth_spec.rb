# require 'swagger_helper'

# describe 'Auth API' do
#   path '/auth/login' do
#     post 'Login user' do
#       tags 'Authentication'
#       consumes 'application/json'
#       parameter name: :user, in: :body, schema: {
#         type: :object,
#         properties: {
#           email: { type: :string },
#           password: { type: :string }
#         },
#         required: ['email', 'password']
#       }

#       response '200', 'user logged in' do
#         let(:user) { { email: 'user@example.com', password: 'password' } }
#         run_test!
#       end

#       response '401', 'invalid credentials' do
#         let(:user) { { email: 'user@example.com', password: 'wrong_password' } }
#         run_test!
#       end
#     end
#   end
# end

# class SessionsController < ApplicationController
#   def create
#     user = User.find_by(email: params[:email])
#     if user&.authenticate(params[:password])
#       # ここでトークンやセッションを生成・返す処理を記述
#       render json: { status: 'success' }, status: :ok
#     else
#       render json: { error: 'Invalid credentials' }, status: :unauthorized
#     end
#   end
# end

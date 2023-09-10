# class AuthController < ApplicationController
#   def login
#     user = User.find_by(email: params[:email])
    
#     # ユーザーが存在し、パスワードが正しい場合
#     if user && user.authenticate(params[:password])
#       # トークンの生成やセッション管理などの実装がここに必要です。
#       render json: { message: "user logged in" }, status: :ok
#     else
#       render json: { error: "invalid credentials" }, status: :unauthorized
#     end
#   end
# end

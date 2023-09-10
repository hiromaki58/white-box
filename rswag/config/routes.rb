Rails.application.routes.draw do
  # Rswag routes
  mount Rswag::Ui::Engine => '/api-docs'
  mount Rswag::Api::Engine => '/api-docs'

  # post '/auth/login', to: 'auth#login'

  namespace :api do
    namespace :v1 do
      resources :todos, only: [:index]
    end
  end
end

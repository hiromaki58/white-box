# RSpec, rswag and OpenAPI-TypeScript  
## Object　　
  

## Library  
Ruby 3.1.2  
Rails 7.0.7.2  

## Procedure   
1, Type "rails new ."  
2, Type "rails generate model User email:string password_digest:string"  
3, Type "rails db:migrate"  
4, Add "has_secure_password" to User model  
5, Add login logic to sessions_controller.rb  
6, Set the routing  
7, Type "rails generate rswag:install"  
8, Add test to spec/integration  
9, Type "rails generate rspec:install"  
10, Type "bundle exec rspec"
  
11, Type "typeprof foo.rb -o ./sig/foo.rbs"  
12, Type "steep check"  
13, Error msg will be shown.  

## FYI  
- Check version  
"ruby -v"  

- Change version  
"rbenv install 3.1.2"  
"rbenv global 3.1.2"  
"rbenv local 3.1.2"  

- Change path  
"nano ~/.bashrc"

export PATH="$HOME/.rbenv/bin:$PATH  
eval "$(rbenv init -)  

"source ~/.bashrc"  
"rbenv rehash"  

## Task  
- 
  
Now  
  
Should be  
  
  
## Reference  
  

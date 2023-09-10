# RSpec, rswag and OpenAPI-TypeScript  
## Object　　
To see the basic use of RSpec and rswag and show the api document on "http://localhost:3000/api-docs/index.html"  
1, Create Swagger by rswag  
2, Make spec test  
3, Execute the spec test by "bundle exec rspec" command  

## Library  
Ruby 3.1.2  
Rails 7.0.7.2  

## Procedure   
1, Type in "rails new ."  
2, Type in "bundle install"  
3, Type in "bundle exec rails g rswag:api:install"  
4, Type in "bundle exec rails g rswag:ui:install"  
5, Type in "bundle exec rails generate rspec:install"  
6, Type in "RAILS_ENV=test bundle exec rails g rswag:specs:install"  
7, Type in "rails generate rspec:ui:install"  
8, Type in "rails generate rspec:api:install"  
9, Type in "RAILS_ENV=test rake rswag:specs:swaggerize"  
10, Type in "rails server"  
11, Access to "http://localhost:3000/api-docs/index.html"  
12, Type in "bundle exec rspec"  
13, Modify swagger_helper.rb file  
14, Type i "RAILS_ENV=test rake rswag:specs:swaggerize"  
10, Type in "rails server"  
11, Access to "http://localhost:3000/api-docs/index.html"  

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
  
## Reference  
https://qiita.com/nakazawaken1/items/1cf12756a9e00f1a8fc4  

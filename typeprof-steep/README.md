# TypeProf and Steep Testing  
## Object　　
Try to make the funciton to check the data type with RBS, TypeProf and Steep  

## Library  
Ruby 3.1.2  
Rails 7.0.4  
RBS 3.2.1  
Steep 1.5.3  

## Procedure   
1, Type "rbenv install 3.1.2"  
2, Type "rbenv global 3.1.2"  
3, Type "mkdir xxx"  
4, Type "cd xxx"  
5, Create gemfile and add below.  
  
  source 'https://rubygems.org'  
  gem 'rbs'  
  gem 'steep'  

6, Type "gem install rbs"  
7, Type "bundle add typeprof"  
8, Type "bundle add steep"  
9, Type "bundle exec steep init"  
10, Create foo.rb file in root directory.  
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

change path  
"nano ~/.bashrc"

export PATH="$HOME/.rbenv/bin:$PATH  
eval "$(rbenv init -)  

"source ~/.bashrc"  
"rbenv rehash"  
  
## Reference  
https://semaphoreci.com/blog/ruby-rbs-typeprof-steep  
https://zenn.dev/yukyan/articles/4c62efd21ff4b2  

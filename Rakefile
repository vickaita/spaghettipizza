require 'rubygems'
require 'net/scp'
require 'edn'
require 's3_website'

namespace :build do
  file "resources/public/js/pizza.js"  do
    `lein with-profile prod cljsbuild once prod`
  end

  file "resources/public/css/main.css" do
    `compass compile`
  end

  task :pages do
    # TODO add a lein task to generate the html page(s)
    `lein`
  end

  task :api do
    `lein with-profile prod uberjar`
  end

  task :ui => ['build:pages', 'build:cljs', 'build:scss'] do
  end
end

namespace :deploy do
  task :api => ['build:api'] do
    Net::SCP.start('api.spaghettipizza.us') do |scp|
    end
  end
  task :ui => ['build:ui'] do
    # use s3_website to upload to S3
  end
end

task :default do
  puts "default!!"
end

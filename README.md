# Everybody loves Spaghetti Pizza!

Start a server with

    lein run

Compile ClojureScript with

    lein cljsbuild

or
    lein cljsbuild auto

for automatic compilation when changing files.

# TODO

- SHARING!!!
  - put uploaded image urls into simple db
  - parse the edn response
  - display the url
  - better ui for sharing
  - add share buttons
    - facebook
    - twitter
    - google plus
    - pinterest
  - admin interface to see what has been uploaded
  - view page: should just be the main page but with a query param
      http://spaghettipizza.us/?pizza=md5-hash
  - consider saving svg instead of png. it will probably be smaller in most
    cases
      - should test size to be sure
      - worst case it could be larger
        - could limit number of noodles to counteract this ...
      - still have to rasterize it to share on social networks?
- more toppings!!
- smooth noodles
- Migrate deployment to the Rakefile

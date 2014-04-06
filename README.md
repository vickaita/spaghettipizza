# Everybody loves Spaghetti Pizza!

High in carbohydrates and now you can draw your own! See this site running live
at [http://spaghettipizza.us].

## Running your own pizza shop!

You need to have [leiningen](https://github.com/technomancy/leiningen) and
[compass](https://github.com/chriseppstein/compass) installed to build and run
the project.

From within the root of the project directory, build the assets

    compass build  # Generates the CSS
    lein cljsbuild # Generates the JS

then starting a server by executing

    lein run

Make sure that you add your AWS credentials to `resources/credentials/aws.clj`
if you want to be able to save images to S3 and not get annoying errors.

TODO: Currently the S3 bucket name is hard coded, so that will also need to be
changed or, ideally, moved into a configuration file.

If you are working on changes to the CSS or ClojureScript you may find these
commands useful:

    compass watch

    lein cljsbuild auto

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

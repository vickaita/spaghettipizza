(defproject pizza "0.4.7"
  :description "Spaghetti Pizza -- the original pasta on pizza simulator!"
  :url "http://spaghettipizza.us"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/core.typed "0.2.34"]
                 [org.clojure/tools.logging "0.2.6"]
                 [commons-codec/commons-codec "1.4"]
                 [ring "1.2.2"]
                 [ring-cors "0.1.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [amazonica "0.1.32"]
                 [clj-aws-s3 "0.3.7"]
                 [com.cemerick/rummage "1.0.1"]
                 [digest "1.4.3"]
                 [prismatic/dommy "0.1.1"]
                 [om "0.5.3"]
                 [secretary "1.1.0"]
                 [sablono "0.2.15"]
                 [environ "0.4.0"]
                 [shodan "0.1.0"]]
  :source-paths ["src/clj" "src/cljs"]
  :main limn.handler
  :plugins [[lein-cljsbuild "1.0.3"]
            [com.cemerick/austin "0.1.4"]
            ;[com.cemerick/clojurescript.test "0.2.2"]
            [lein-environ "0.4.0"]
            [lein-typed "0.3.1"]]
  :injections [(require 'cemerick.austin.repls)
               (defn browser-repl-env []
                 (reset! cemerick.austin.repls/browser-repl-env
                         (cemerick.austin/repl-env)))
               (defn browser-repl []
                 (cemerick.austin.repls/cljs-repl
                   (browser-repl-env)))]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/debug/pizza.js"
                           :output-dir "resources/public/js/debug"
                           :optimizations :none
                           :pretty-print true
                           :source-map true}}
               ;{:id "test"
               ; :source-paths ["src/cljs" "test/cljs"]
               ; :compiler {:output-to "resources/public/js/test/pizza.js"
               ;            :output-dir "resources/public/js/test"
               ;            :preamble ["react/react.min.js"]
               ;            :externs ["react/externs/react.js"]
               ;            :optimizations :whitespace}}
               {:id "prod"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/pizza.js"
                           :output-dir "resources/public/js"
                           :preamble ["react/react.min.js"]
                           :externs ["react/externs/react.js"]
                           :source-map "resources/public/js/pizza.js.map"
                           :optimizations :advanced}}]
              ;:test-commands
              ;{"units" ["phantomjs" :runner
              ;          "this.literal_js_was_evaluated=true"
              ;          "resources/public/js/test/pizza.js"]}
              }
  :profiles {:dev {:env {:environment :dev}}
             :test {:env {:environment :test}
                    :aot [limn.handler]}
             :prod {:env {:environment :prod}
                    :aot [limn.handler]}}
  :aliases {"push-ui" ["run" "-m" "limn.tasks/push-ui"]
            "push-api" ["run" "-m" "limn.tasks/push-api"]})

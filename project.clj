(defproject pizza "0.1.0"
  :description "Spaghetti Pizza -- the original pasta on pizza simulator!"
  :url "http://spaghettipizza.us"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 ;[org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [org.clojure/core.typed "0.2.26"]
                 [org.clojure/tools.logging "0.2.6"]
                 [commons-codec/commons-codec "1.4"]
                 [ring "1.2.1"]
                 [ring-cors "0.1.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [amazonica "0.1.32"]
                 [clj-aws-s3 "0.3.7"]
                 [com.cemerick/rummage "1.0.1"]
                 [digest "1.4.3"]
                 [prismatic/dommy "0.1.1"]
                 [om "0.3.6"]
                 [com.facebook/react "0.8.0.1"]
                 [sablono "0.2.6"]
                 [environ "0.4.0"]
                 [com.cemerick/piggieback "0.1.2"]]
  :source-paths ["src/clj" "src/cljs"]
  :main pizza.handler
  :plugins [[lein-cljsbuild "1.0.2"]
            [lein-environ "0.4.0"]
            [lein-typed "0.3.1"]]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/debug/pizza.js"
                           :output-dir "resources/public/js/debug"
                           :optimizations :none
                           :pretty-print true
                           :source-map true}}
               {:id "prod"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/pizza.js"
                           :output-dir "resources/public/js"
                           :preamble ["react/react.min.js"]
                           :externs ["react/externs/react.js"]
                           :source-map "resources/public/js/pizza.js.map"
                           :optimizations :advanced}}]}
  :core.typed {:check [pizza.core]}
  :profiles {:dev {:env {:environment :dev}}
             :test {:env {:environment :test}
                    :aot [pizza.handler]}
             :prod {:env {:environment :prod}
                    :aot [pizza.handler]}}
  :aliases {"push-ui" ["run" "-m" "pizza.tasks/push-ui"]
            "push-api" ["run" "-m" "pizza.tasks/push-api"]})

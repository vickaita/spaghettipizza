(defproject pizza "0.1.0-SNAPSHOT"
  :description "Spaghetti Pizza -- the original pasta on pizza simulator!"
  :url "http://spaghettipizza.us"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2127"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.typed "0.2.19"]
                 [org.clojure/tools.logging "0.2.6"]
                 [commons-codec/commons-codec "1.4"]
                 [ring "1.2.1"]
                 [ring-cors "0.1.0"]
                 #_[http-kit "2.1.13"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]
                 [amazonica "0.1.32"]
                 [digest "1.4.3"]
                 [prismatic/dommy "0.1.1"]
                 [environ "0.4.0"]]
  :source-paths ["src/clj" "src/cljs"]
  :main pizza.handler
  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            [lein-environ "0.4.0"]
            #_[lein-typed "0.3.1"]]
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
                           :optimizations :advanced}}]}
  ;:core.typed {:check [pizza.types]}
  :profiles {:dev {:env {:foo 1}}
             :prod {:aot [pizza.handler]
                    :env {:foo 2}}}
  :aliases {"push-ui" ["run" "-m" "pizza.tasks/push-ui"]
            "push-api" ["run" "-m" "pizza.tasks/push-api"]})

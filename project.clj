(defproject pizza "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.typed "0.2.19"]
                 [ring "1.2.1"]
                 [http-kit "2.1.13"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.4"]]

  :main pizza.handler

  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            [lein-typed "0.3.1"]
            [lein-kibit "0.0.8"]
            #_[com.cemerick/austin "0.1.3"]]

  :source-paths ["src/clj" "src/cljs"]

  :cljsbuild { 
    :builds [{:id "pizza"
              :source-paths ["src/cljs"]
              :compiler {
                :output-to "resources/public/js/pizza.js"
                :output-dir "resources/public/js"
                :optimizations :none
                :source-map true}}]}

  :core.typed  {:check [pizza.types]})

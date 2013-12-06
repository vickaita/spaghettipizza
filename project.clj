(defproject pizza "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2080"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/core.typed "0.2.19"]]

  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            [lein-typed "0.3.1"]
            [lein-kibit "0.0.8"]
            [com.cemerick/austin "0.1.3"]]

  :source-paths ["src"]

  :cljsbuild { 
    :builds [{:id "pizza"
              :source-paths ["src"]
              :compiler {
                :output-to "pizza.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]}

  :core.typed  {:check [pizza.core pizza.pizza pizza.spaghetti pizza.svg]})

(ns pizza.pages
  (:require [hiccup.core :refer [h html]]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn home
  [_]
  (html5
    [:head
     [:title "Spaghetti Pizza"]
     (include-css "http://fonts.googleapis.com/css?family=Ribeye"
                  "css/main.css")]
    [:body
     [:header#masthead
      [:h1 "Spaghetti Pizza"]
      [:button#clean "Reset"]]
     [:div#toolbar
      [:a.tool.active {:data-tool "spaghetti"} "Spaghetti"]
      [:a.tool {:data-tool "ziti"} "Ziti"]
      [:a.tool {:data-tool "ricotta"} "Ricotta"]]
     [:div#counter-top
      [:svg#main-svg {:width 500
                      :height 500
                      :viewPort "0 0 500 500"
                      :version "1.1"
                      :xmlns "http://www.w3.org/2000/svg"}]]
     (include-js "js/goog/base.js"
                 "js/pizza.js")
     [:script {:type "text/javascript"} "goog.require(\"pizza.core\");"]]))

(ns pizza.pages
  (:require [hiccup.core :refer [h html]]
            [hiccup.form :as form]
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
      [:button#clean "Reset"]
      (form/form-to {:id "register"} [:post "/kitchens"]
                    [:div.row (form/text-field "kitchen")]
                    [:div.row (form/submit-button "Register")])]
     [:ul#users]
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

(defn error-404
  [_]
  (html5
    [:head
     [:title "404 - Page Not Found"]
     (include-css "http://fonts.googleapis.com/css?family=Ribeye"
                  "css/main.css")]
    [:body
     [:h1 "404 &mdash; Page Not Found"]]))

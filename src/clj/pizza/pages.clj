(ns pizza.pages
  (:require [hiccup.core :refer [h html]]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn home
  ([] (home nil false))
  ([_] (home nil false))
  ([_ dev]
   (html5
     [:head
      [:title "Spaghetti Pizza"]
      (include-css "//fonts.googleapis.com/css?family=Ribeye"
                   "css/main.css")
      [:script ;; Google Analytics
       "(function (i,s,o,g,r,a,m) {i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
       (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
       m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
       })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
       ga('create','UA-46522614-1','spaghettipizza.us');
       ga('send','pageview');"]]
     [:body
      [:header#masthead
       [:h1 "Spaghetti Pizza"]
       #_(form/form-to {:id "register"} [:post "/kitchens"]
                       [:div.row (form/text-field "kitchen")]
                       [:div.row (form/submit-button "Register")])]
      [:ul#users]
      [:div#toolbar
       [:button#clean.tool "Reset"]
       [:a.tool.active {:data-tool "spaghetti"} "Spaghetti"]
       [:a.tool {:data-tool "ziti"} "Ziti"]
       [:a.tool {:data-tool "ricotta"} "Ricotta"]]
      [:div#counter-top
       [:svg#main-svg {:width 500
                       :height 500
                       :viewPort "0 0 500 500"
                       :version "1.1"
                       :xmlns "http://www.w3.org/2000/svg"}]]
      (when dev (include-js "js/goog/base.js"))
      (include-js "js/pizza.js")
      (when dev [:script {:type "text/javascript"}
                 "goog.require(\"pizza.core\");"])])))

(defn error-404
  [_]
  (html5
    [:head
     [:title "404 - Page Not Found"]
     (include-css "http://fonts.googleapis.com/css?family=Ribeye"
                  "css/main.css")]
    [:body
     [:h1 "404 &mdash; Page Not Found"]]))

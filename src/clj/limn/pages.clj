(ns limn.pages
  (:require [hiccup.core :refer [h html]]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn head
  [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:title title]
   [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
   [:meta {:name "viewport" :content "width=device-width, user-scalable=no"}]
   (include-css ;"//fonts.googleapis.com/css?family=Ribeye"
                "//fonts.googleapis.com/css?family=Oregano"
                "css/main.css")
   [:script ;; Google Analytics
    "(function (i,s,o,g,r,a,m) {i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    ga('create','UA-46522614-1','spaghettipizza.us');
    ga('send','pageview');"]])

(defn home
  [dev?]
  (html5 {:lang "en-us"}
    (head "Spaghetti Pizza: The Adventure Begins!")
    [:body
     (if dev?
       (include-js "http://fb.me/react-0.8.0.js"
                   "js/debug/goog/base.js"
                   "js/debug/pizza.js")
       (include-js "js/pizza.js"))
     (when dev? [:script {:type "text/javascript"}
                 "goog.require(\"limn.core\");"])]))

(defn show
  [dev?]
  (html5 {:lang "en-us"}
    (head "Behold! A wonderous pizza created with spaghetti!")
    [:body
     [:div#site
      [:div#page
       [:p "This pizza was lovingly crafted with the finest locally sourced"
        [:span "SVG"]
        ". You may be wondering what makes Spaghetti Pizza so wonderful. Is it
        perhaps the delicious rich creamyness of the carbs? No it's something
        more; something special. Take this this pizza for instance:"]
       [:img.pizza {:src (str "//spaghettipizza.us/pizza/")
                    :alt "A glorious Spaghetti Pizza!"}]
       [:p "Does it not tantalize you?"]]]]))

(defn error-404
  []
  (html5
     (head "404 - Page Not Found")
    [:body
     [:h1 "404 &mdash; Page Not Found"]]))

(ns pizza.pages
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
   (include-css "//fonts.googleapis.com/css?family=Ribeye"
                "css/main.css")
   [:script ;; Google Analytics
    "(function (i,s,o,g,r,a,m) {i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
    (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
    m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
    })(window,document,'script','//www.google-analytics.com/analytics.js','ga');
    ga('create','UA-46522614-1','spaghettipizza.us');
    ga('send','pageview');"]])

(defn toolbar
  []
  [:div#toolbar.toolbar
   [:div.actions
    [:a#clean.action "Reset"]
    [:a#photo.action "Share"]]
   [:h2 "Toppings!"]
   [:dl.tools
    [:dt.group "Pasta"]
    [:dd [:a.tool.active {:data-tool "spaghetti"} "Spaghetti"]]
    [:dd [:a.tool {:data-tool "ziti"} "Ziti"]]
    [:dd [:a.tool {:data-tool "linguini"} "Linguini"]]
    [:dt.group "Cheese"]
    [:dd [:a.tool {:data-tool "ricotta"} "Ricotta"]]]])

(defn easel
  "Create a drawing surface."
  ([] (easel 500 500))
  ([width height]
   [:div#easel
    [:svg#main-svg {:width width
                    :height height
                    :viewPort (str "0 0 " width " " height)
                    :version "1.1"
                    :xmlns "http://www.w3.org/2000/svg"}]]))

(defn home
  [dev?]
  (html5 {:lang "en-us"}
    (head "Spaghetti Pizza: The Adventure Begins!")
    [:body
     (toolbar)
     [:div#page
      [:header#masthead
       [:a#menu-control]
       [:h1 "Spaghetti Pizza"]]
      (easel)]
     (when dev? (include-js "js/goog/base.js"))
     (include-js "js/pizza.js")
     (when dev? [:script {:type "text/javascript"}
                 "goog.require(\"pizza.core\");"])]))

(defn show
  [dev?]
  (html5 {:lang "en-us"}
    (head "Behold! A wonderous pizza created with spaghetti!")
    [:body
     [:div#page
      [:p "This pizza was lovingly crafted with the finest locally sourced"
       [:span "SVG"]
       ". You may be wondering what makes Spaghetti Pizza so wonderful. Is it
       perhaps the delicious rich creamyness of the carbs? No it's something
       more; something special. Take this this pizza for instance:"]
      [:img.pizza {:src (str "//spaghettipizza.us/pizza/")
                   :alt "A glorious Spaghetti Pizza!"}]
      [:p "Does it not tantalize you?"]]]))

(show "sadkjfasiodnvsoidnfaslf.jpg")

(defn error-404
  []
  (html5
     (head "404 - Page Not Found")
    [:body
     [:h1 "404 &mdash; Page Not Found"]]))

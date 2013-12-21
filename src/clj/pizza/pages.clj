(ns pizza.pages
  (:require [hiccup.core :refer [h html]]
            [hiccup.form :as form]
            [hiccup.page :refer [html5 include-css include-js]]))

(defn head
  [title]
  [:head
   [:title title]
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

(defn home
  [dev?]
  (html5
    (head "Spaghetti Pizza: The Adventure Begins!")
    [:body
     [:header#masthead
      [:h1 "Spaghetti Pizza"]
      [:button#photo "Take a picture (it will last longer)."]
      #_(form/form-to {:id "register"} [:post "/kitchens"]
                      [:div.row (form/text-field "kitchen")]
                      [:div.row (form/submit-button "Register")])]
     [:ul#users]
     [:div#toolbar
      [:button#clean.tool "Reset"]
      [:a.tool.active {:data-tool "spaghetti"} "Spaghetti"]
      [:a.tool {:data-tool "ziti"} "Ziti"]
      [:a.tool {:data-tool "ricotta"} "Ricotta"]
      [:a.tool {:data-tool "linguini"} "Linguini"]] 
     [:div#counter-top
      [:svg#main-svg {:width 500
                      :height 500
                      :viewPort "0 0 500 500"
                      :version "1.1"
                      :xmlns "http://www.w3.org/2000/svg"}]]
     (when dev? (include-js "js/goog/base.js"))
     (include-js "js/pizza.js")
     (when dev? [:script {:type "text/javascript"}
                "goog.require(\"pizza.core\");"])]))

(defn show
  [dev?]
  (html5
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

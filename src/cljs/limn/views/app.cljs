(ns limn.views.app
  (:require [clojure.string :refer [join]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.views.toolbar :refer [toolbar color-wheel]]
            [limn.views.menu :refer [menu-bar]]
            [limn.views.easel :refer [easel]]))

(defn- site-classes
  [app]
  (join " "
        [(when (:show-toolbar? app) "show-toolbar")
         (when (:show-color-wheel? app) "show-color-wheel")]))

(defn- masthead
  [app owner]
  (om/component
    (html
      [:header#masthead
       [:a#menu-control
        {:on-click (fn [e]
                     (doto e .preventDefault .stopPropagation)
                     (om/transact! app [:show-toolbar?] not))}]
       [:h1 "Spaghetti Pizza"]])))

(defn- footer
  [app owner]
  (om/component
    (html
      [:footer#site-footer
       [:p "Spaghetti pizza is a great new site that lets you take control of
           your own virtual pizza shop and create crazy combinations of pizza
           toppings including such whacky things as spaghetti and ziti!"]
       [:p (str "Vick Aita © " (.getFullYear (js/Date.)))]])))

(defn app-view
  [app owner]
  (om/component
    (html
      [:div#site {:class (site-classes app)}
       (om/build menu-bar (:menu-bar app))
       (om/build toolbar
                 (om/graft {:tool (:tool app)
                            :color (:color app)
                            :groups (:groups (:toolbar app))
                            :colors (:colors (:toolbar app))} app))
       [:div#page
        (om/build masthead app)
        (om/build easel app)
        (om/build footer app)]
       [:div.palettes
        (om/build color-wheel
                  (om/graft {:color (:color app)
                             :colors (:colors (:toolbar app))} app))]])))

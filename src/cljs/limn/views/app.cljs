(ns limn.views.app
  (:require [clojure.string :refer [join]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.toolbar :as toolbar]
            [limn.menu :refer [menu-bar]]
            [limn.easel :as easel]))

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

(defn app-view
  [app owner]
  (om/component
    (html
      [:div#site {:class (site-classes app)}
       (om/build toolbar/toolbar
                 (om/graft {:tool (:tool app)
                            :color (:color app)
                            :groups (:groups (:toolbar app))
                            :colors (:colors (:toolbar app))} app))
       [:div.palettes
        (om/build toolbar/color-wheel
                  (om/graft {:commands (:commands app)
                             :color (:color app)
                             :colors (:colors (:toolbar app))}
                            app))]
       [:div#page
        (om/build masthead app)
        (om/build menu-bar (:menu-bar app))
        (om/build easel/easel app)
        [:footer#site-footer
         [:p (str "Vick Aita © " (.getFullYear (js/Date.)))]]]])))
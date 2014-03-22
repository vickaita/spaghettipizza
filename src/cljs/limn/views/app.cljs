(ns limn.views.app
  (:require [clojure.string :refer [join]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.models.gallery :as gm]
            [limn.views.toolbar :as toolbar]
            [limn.views.menu :refer [menu-bar quick-links]]
            [limn.views.gallery :refer [gallery]]
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
       [:a.menu-control
        {:on-click (fn [e]
                     (doto e .preventDefault .stopPropagation)
                     (om/transact! app [:show-toolbar?] not))}]
       [:h1.logo "Spaghetti Pizza"]
       #_(om/build quick-links app)])))

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
       [:section.toolbar
        #_[:a.menu-control
         {:on-click (fn [e]
                      (doto e .preventDefault .stopPropagation)
                      (om/transact! app [:show-toolbar?] not))}]
        (om/build toolbar/actions (:actions app))
        (om/build toolbar/colors (:colors app) {:state {:color (:color app)}})
        (om/build toolbar/tools (:tools app) {:state {:tool (:tool app)}})]
       [:div#page
        (om/build masthead app)
        (if (gm/visible? (:gallery app))
          (om/build gallery (:gallery app))
          (om/build easel (:easel app) {:state {:skin (:id (:tool app))
                                                :color (:color app)}}))
        (om/build footer app)]])))

(ns pizza.toolbar
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

(defn- handler
  [world message]
  (fn [e]
    (doto e .preventDefault .stopPropagation)
    (put! (:commands world) message)))

(defn toolbar
  [menu owner]
  (om/component
    (html
      [:section#toolbar.toolbar
       [:section.actions
        [:a#clear.action {:on-click (handler menu [:clear])} "Clear"]
        [:a#save.action {:on-click (handler menu [:save])} "Save"]]
       [:section.toppings
        [:h1 "Toppings!"]
        [:section.tools
         (for [group (:groups menu)]
           [:section.group {:key (:name group)} (:name group)
            [:ul
             (for [tool (:tools group)]
               [:li [:a.tool {:class (when (= (:id tool) (:tool menu)) "active")
                              :key (:id tool)
                              :on-click (handler menu [:select-tool (:id tool)])}
                     (:name tool)]])]])]]])))

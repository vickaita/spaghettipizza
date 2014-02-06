(ns pizza.toolbar
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn- handler
  [world message]
  (fn [e]
    (doto e .preventDefault .stopPropagation)
    (put! (:commands world) message)))

(defn toolbar
  [menu owner]
  (om/component
    (html
      [:section.toolbar
       [:section.actions
        [:a.action {:on-click (handler menu [:clear])} "Clear"]
        [:a.action {:on-click (handler menu [:save])} "Save"]]
       [:section.toppings
        [:h1 "Toppings!"]
        [:section.tools
         (for [group (:groups menu)]
           [:section.group {:key (:name group)} (:name group)
            [:ul.tool-list
             (for [tool (:tools group)]
               [:li.tool-item {:key (str (:id tool))}
                [:a.tool {:class (when (= (:id tool) (:tool menu)) "active")
                          :on-click (handler menu [:select-tool (:id tool)])}
                 (:name tool)]])]])]]])))

(ns pizza.toolbar
  (:require-macros [cljs.core.async.macros :refer [go go-loop alt!]])
  (:require [cljs.core.async :refer [put! close! chan <! map<]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

(defn toolbar
  [menu owner]
  (om/component
    (html
      [:section#toolbar.toolbar
       [:section.actions
        [:a#clear.action {:on-click #(put! (:cmd @menu) [:clear])} "Clear"]
        [:a#save.action {:on-click #(put! (:cmd @menu) [:save])} "Save"]]
       [:section.toppings
        [:h1 "Toppings!"]
        [:dl.tools
         (for [group (:groups menu)]
           (cons [:dt.group {:key (:name group)} (:name group)]
                 (for [tool (:tools group)]
                   [:dd [:a.tool {:class (when (= (:id tool) (:tool menu)) "active")
                                  :data-tool (:id tool)
                                  :key (:id tool)}
                         (:name tool)]])))]]])))

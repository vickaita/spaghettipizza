(ns limn.views.menu
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn menu
  [app owner]
  (reify
    om/IInitState
    (init-state [_] {:open false})
    om/IRender
    (render [_]
      (html [:section.menu {:class (when (om/get-state owner :open) "open")}
             [:a.menu-title
              {:on-click (fn [e]
                           (doto e .preventDefault .stopPropagation)
                           (om/set-state!
                            owner :open
                            (not (om/get-state owner :open))))}
              (:name app)]
             [:ul.menu-list
              (for [item (:items app)]
                [:li.menu-item
                 {:on-click #(prn (:command @item))}
                 (:name item)])]]))))

(defn menu-bar
  [app owner]
  (om/component
    (html [:section.menu-bar
           (map #(om/build menu %) app)])))

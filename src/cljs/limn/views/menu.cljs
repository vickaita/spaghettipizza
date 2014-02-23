(ns limn.views.menu
  (:require [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn menu-bar
  [app owner]
  (reify
    om/IInitState
    (init-state [_] {:open nil})
    om/IWillMount
    (will-mount [_]
      (events/listen js/document "mouseup" #(om/set-state! owner :open nil)))
    om/IRender
    (render [_]
      (html [:section.menu-bar
             (for [menu app]
               [:section.menu {:key (:name menu)
                               :class (when (= (om/get-state owner :open) menu)
                                        "open")}
                [:a.menu-title
                 {:on-click (fn [e]
                              (doto e .preventDefault .stopPropagation)
                              (om/set-state! owner :open menu))}
                 (:name menu)]
                [:ul.menu-list
                 (for [item (:items menu)]
                   [:li.menu-item
                    [:a.menu-link
                     {:on-click #(prn (:command @item))}
                     [:span.name (:name item)]
                     (when (:shortcut item)
                       [:span.shortcut (:shortcut item)])]])]])]))))

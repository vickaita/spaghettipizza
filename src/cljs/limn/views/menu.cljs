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
    om/IRender
    (render [_]
      (html [:section.menu-bar
             (when (om/get-state owner :open)
               [:div.menu-close-overlay
                {:style {:height "1000px"}
                 :on-click (fn [e] (om/set-state! owner :open nil))}])
             (for [menu app]
               [:section.menu {:key (:name menu)
                               :class (when (= (om/get-state owner :open) menu)
                                        "open")}
                [:a.menu-title
                 {:on-click (fn [e]
                              (doto e .preventDefault .stopPropagation)
                              (if (= @menu (om/get-state owner :open))
                                (om/set-state! owner :open nil)
                                (om/set-state! owner :open @menu)))
                  :on-mouseover (fn [e]
                                   (doto e .preventDefault .stopPropagation)
                                   (prn "mouseover")
                                   (prn (om/get-state owner))
                                   (prn (om/get-state owner :open))
                                   (when (om/get-state owner :open)
                                     (om/set-state! owner :open @menu)))}
                 (:name menu)]
                [:ul.menu-list
                 (for [item (:items menu)]
                   [:li.menu-item
                    [:a.menu-link
                     {:on-click
                      (fn [e]
                        (when-let [command(:command @item)]
                          (put! (om/get-shared owner :commands) command)))}
                     [:span.name (:name item)]
                     (when (:shortcut item)
                       [:span.shortcut (:shortcut item)])]])]])]))))

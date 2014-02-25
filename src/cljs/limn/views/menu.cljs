(ns limn.views.menu
  (:require [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn menu-bar
  [app owner]
  (let [handle-close (fn [e] (om/set-state! owner :open nil))
        handle-toggle (fn [menu e]
                        (doto e .preventDefault .stopPropagation)
                        (if (= @menu (om/get-state owner :open))
                          (om/set-state! owner :open nil)
                          (om/set-state! owner :open @menu)))
        handle-hover (fn [menu e]
                       (doto e .preventDefault .stopPropagation)
                       (when (om/get-state owner :open)
                         (om/set-state! owner :open @menu)))
        handle-select (fn [item e]
                        (when-let [command (:command @item)]
                          (om/set-state! owner :open nil)
                          (put! (om/get-shared owner :commands) command)))]
    (reify
      om/IInitState
      (init-state [_] {:open nil})
      om/IRender
      (render [_]
        (html
          [:section.menu-bar
           (when (om/get-state owner :open)
             [:div.menu-close-overlay {:style {:height "1000px"} :on-click handle-close}])
           (for [menu app]
             [:section.menu {:key (:name menu)
                             :class (when (= (om/get-state owner :open) menu) "open")}
              [:a.menu-title {:on-click (partial handle-toggle menu)
                              :on-mouse-enter (partial handle-hover menu)}
               (:name menu)]
              [:ul.menu-list
               (for [item (:items menu)]
                 [:li.menu-item {:on-click (partial handle-select item)}
                  [:a.menu-link
                   [:span.name (:name item)]
                   (when (:shortcut item)
                     [:span.shortcut (:shortcut item)])]])]])])))))

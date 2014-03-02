(ns limn.views.toolbar
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn- handler
  [owner message]
  (fn [e]
    (doto e .preventDefault .stopPropagation)
    (put! (:commands (om/get-shared owner)) message)))

(defn actions
  [app owner]
  (om/component
    (html
      [:section.actions
           (for [action app]
             [:a.action {:id (:id action)
                         :on-click (handler owner (:command action))}
              (:text action)])])))

(defn colors
  [app owner]
  (om/component
    (html
      [:section.colors
       [:h1 "Colors"]
       (let [selected (om/get-state owner :color)]
         (for [palette (:palettes app)]
           [:ul.palette
            (for [color (:colors palette)]
              [:li.color {:key (str "color:" (:name color))
                          :class (when (= color selected) "active")
                          :style {:border-color (:stroke color)
                                  :background-color (:fill color)}
                          :on-click (handler owner [:select-color color])}
               (:name color)])]))])))

(defn tools
  [app owner]
  (om/component
    (html
      [:section.toppings
       [:h1 "Toppings!"]
       (for [group (:groups app)]
         [:section.group {:key (:name group)}
          [:h1 (:name group)]
          [:ul.tool-list
           (let [selected (om/get-state owner :tool)]
             (for [tool (:tools group)]
               [:li.tool-item {:key (:id tool)}
                [:a.tool {:class (when (= tool selected) "active")
                          :on-click (handler owner [:select-tool tool])}
                 (:name tool)]]))]])])))

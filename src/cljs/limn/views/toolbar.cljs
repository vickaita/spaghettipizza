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
             [:a.action {:key (:id action)
                         :id (:id action)
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
           [:ul.palette {:key (:name palette)}
            (for [color (:colors palette)]
              [:li.color {:key (str "color:" (:name color))
                          :class (when (= color selected) "active")
                          :style {:border-color (:stroke color)
                                  :background-color (:fill color)}
                          :on-click (handler owner [:select-color color])}
               (:name color)])]))])))

(defn- tool
  [tool owner]
  (om/component
    (html
      [:li.tool-item
       [:a.tool {:class (when (= tool (om/get-state owner :tool)) "active")
                 :on-click (handler owner [:select-tool tool])}
        (:name tool)]])))

(defn- tool-group
  [app owner]
  (om/component
    (html
      [:section.group
       [:h1 (:name app)]
       [:ul.tool-list
        (om/build-all tool (:tools app) {:key :id})]])))

(defn tools
  [app owner]
  (om/component
    (html
      [:section.toppings
       [:h1 "Toppings!"]
       (om/build-all tool-group (:groups app) {:key :name})])))

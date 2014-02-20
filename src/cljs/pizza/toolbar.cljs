(ns pizza.toolbar
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn- handler
  [world message]
  (fn [e]
    (doto e .preventDefault .stopPropagation)
    (put! (:commands world) message)))

(defn- color-circle
  [color owner]
  (om/component
    (html
      [:circle.color
       {:key (:fill color)
        :fill (:fill color)
        :stroke (:stroke color)
        :stroke-width 4
        :cx (:cx color)
        :cy (:cy color)
        :r (:r color)
        :on-click (fn [e]
                    ;(doto e .preventDefault .stopPropagation)
                    (doto (:commands @color)
                      (put! [:set-color @color])
                      (put! [:hide-color-wheel])))}
       ;; Not supported by React.js (yet)
       #_[:animate {:attribute-name "r" :from "25" :to "30" :dur "100ms"}]])))

(defn color-wheel
  [app owner]
  (om/component
    (html
      [:section.color-wheel
       [:h1 "Color Selector"]
       [:p.tooltip "Mouseover a color to select it."]
       [:svg {:width 500
              :height 500
              :viewBox (str "0 0 500 500")
              :version "1.1"
              :preserveAspectRatio "xMidYMid"
              :xmlns "http://www.w3.org/2000/svg"}
        (let [angle-step (/ Math/PI (inc (count (:color app))))
              wheel-radius 120]
          (map-indexed
            (fn [i color]
              (let [angle (* i angle-step)]
                (om/build
                  color-circle
                  (merge
                    color
                    {:cx (+ 250 (Math/floor (* wheel-radius (Math/cos angle))))
                     :cy (+ 250 (Math/floor (* wheel-radius (Math/sin angle))))
                     :r 25
                     :active? (= color (:color app))
                     :commands (:commands app)}))))
            (:colors app)))]])))

(defn toolbar
  [menu owner]
  (om/component
    (html
      [:section.toolbar
       [:section.actions
        [:a#clear.action {:on-click (handler menu [:clear])} "Clear"]
        [:a#save.action {:on-click (handler menu [:save])} "Save"]]
       [:section.colors
        [:h1 "Colors"]
        [:ul
         (for [color (:colors menu)]
           [:li.color {:class (when (= color (:color menu)) "active")
                       :style {:border-color (:stroke color)
                               :background-color (:fill color)}
                       :on-click (handler menu [:set-color color])}
            (:name color)])]]
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

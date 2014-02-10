(ns pizza.toolbar
  (:require [cljs.core.async :refer [put!]]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]))

(defn- handler
  [world message]
  (fn [e]
    (doto e .preventDefault .stopPropagation)
    (put! (:commands world) message)))

(defn color-wheel
  [app owner]
  (om/component
    (html
      [:section.color-wheel
       [:h1 "Color Selector"]
       [:p.tooltip "Mouseover a color to select it."]
       [:ul
        (let [angle-step (/ Math/PI (count (:color app)))
              radius 100]
          (map-indexed
          (fn [i color]
            (let [angle (* i angle-step)]
              [:li.color {:class (when (= color (:color app)) "active")
                        :style {:border-color (:stroke color)
                                :background-color (:fill color)
                                :left (* radius (Math/cos angle))
                                :top (* radius (Math/sin angle))}
                        :on-click
                        (fn [e]
                          (doto e .preventDefault .stopPropagation)
                          (doto (:commands app)
                            (put! [:set-color color])
                            (put! [:hide-color-wheel])))}
             [:span.name (:name color)]]))
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

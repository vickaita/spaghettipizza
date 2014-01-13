(ns pizza.easel
  (:require [goog.dom :as dom]
            [goog.events :as events]))

(defn adjust-size
  [easel]
  (let [size (.getBoundingClientRect (dom/getElement "easel"))
        side (min (.-width size) (.-height size))]
    (doto easel
      (.setAttribute "width" side)
      (.setAttribute "height" side))))

;(defn draw-pizza
;  "Draw the pizza."
;  [easel]
;  (let [origin [256 256]
;        crust (svg/create-irregular-circle origin 227 "#FAE265" "#DDAB0B" 3)
;        sauce (svg/create-irregular-circle origin 210 "#F86969" "#F04F4F" 3)]
;    (dom/append easel crust sauce)))
;
;(defn display-pizza
;  [easel pizza-hash]
;  (dom/append easel (node [:img {:src (str "/pizza/" pizza-hash ".png")}])))

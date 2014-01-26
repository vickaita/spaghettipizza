(ns pizza.easel
  (:require [goog.dom :as dom]
            [goog.dom.classlist :as cls]
            [goog.events :as events]
            [pizza.pizza :as pzz]))

(defn adjust-size!
  [easel]
  (let [size (.getBoundingClientRect (dom/getElement "easel"))
        side (min (.-width size) (.-height size))]
    (doto easel
      (.setAttribute "width" side)
      (.setAttribute "height" side))))

(defn update!
  ([easel] (update easel nil))
  ([easel pizza-hash]
  (let [img-wrapper (dom/getElement "img-wrapper")
        svg-wrapper (dom/getElement "svg-wrapper")
        svg-elem (dom/getElement "main-svg")]
    (if pizza-hash
      (do (cls/add svg-wrapper "hidden")
          (doto img-wrapper
            (cls/remove "hidden")
            (dom/append (pzz/pizza-img pizza-hash))))
      (do (cls/add img-wrapper "hidden")
          (cls/remove svg-wrapper "hidden")
          (dom/append svg-elem (pzz/fresh-pizza)))))))

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

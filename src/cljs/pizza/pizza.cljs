(ns pizza.pizza
  (:require [pizza.svg :as svg]
            [goog.dom :as dom]))

(defn draw-pizza
  "Draw the pizza."
  [parent-element]
  (let [origin [250 250]
        crust (svg/create-irregular-circle origin 227 "#FAE265" "#DDAB0B" 3)
        sauce (svg/create-irregular-circle origin 210 "#F86969" "#F04F4F" 3)]
    (dom/append parent-element crust sauce)))

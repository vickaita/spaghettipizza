(ns limn.pizza
  (:require-macros [dommy.macros :refer [node]])
  (:require [limn.svg :as svg]
            [goog.dom :as dom]))

(defn fresh-pizza
  "Draw the pizza."
  []
  (let [origin [256 256]
        group (svg/g)
        crust (svg/create-irregular-circle origin 227 "#FAE265" "#DDAB0B" 3)
        sauce (svg/create-irregular-circle origin 210 "#F86969" "#F04F4F" 3)]
    (doto group (dom/append crust sauce))))

(defn pizza-img
  [pizza-hash]
  (node [:img {:src (str "/pizza/" pizza-hash ".png")}]))

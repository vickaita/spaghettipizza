(ns pizza.easel
  (:require [goog.dom :as dom]))

(defn adjust-size
  [easel]
  (let [size (.getBoundingClientRect (dom/getElement "easel"))
        side (min (.-width size) (.-height size))]
    (doto easel
      (.setAttribute "width" side)
      (.setAttribute "height" side))))


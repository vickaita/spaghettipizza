(ns pizza.spaghetti
  (:require [pizza.svg :as svg]))

(def max-length 250)

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- points->str
  [pts]
  (apply str (interleave (flatten pts) (repeat " "))))

(defprotocol Noodle
  (length [this])
  (add-point! [this x y])
  (to-svg [this])
  (smooth [this]))

(defn create-noodle
  []
  (let [g (svg/create-svg-element "g")
        border (svg/create-svg-element "polyline")
        inner (svg/create-svg-element "polyline")
        points (atom [])]
    (doto g
      (.appendChild border)
      (.appendChild inner)) 
    (doto border
      (.setAttribute "fill" "transparent")
      (.setAttribute "stroke" "#9E9E22")
      (.setAttribute "stroke-linecap" "round")
      (.setAttribute "stroke-width" 6))  
    (doto inner
      (.setAttribute "fill" "transparent")
      (.setAttribute "stroke" "#F5F5AA")
      (.setAttribute "stroke-linecap" "round")
      (.setAttribute "stroke-width" 4))  
    (reify
      Noodle
      (length [_]
        (reduce + (map distance @points (drop 1 @points))))
      (add-point! [_ x y]
        (swap! points conj [x y])
        (.setAttribute inner "points" (points->str @points)) 
        (.setAttribute border "points" (points->str @points)))
      (to-svg [_] g)
      (smooth [_]
        (swap! points (fn [ps] (keep-indexed #(when (even? %1) %2) ps)))
        (.setAttribute inner "points" (points->str @points)) 
        (.setAttribute border "points" (points->str @points)) 
        ))))

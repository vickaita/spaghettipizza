(ns pizza.spaghetti
  (:require [pizza.svg :as svg]))

(defmulti create-topping identity)

(def max-length 250)

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- points->str
  [pts]
  (apply str (interleave (flatten pts) (repeat " "))))

#_(defprotocol Topping
  (length [this])
  (add-point! [this x y])
  (to-svg [this])
  (smooth [this]))

#_(defn create-noodle
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
      Topping
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

(defprotocol Topping
  (add-point! [this point] "Add a point to the topping."))

(extend-protocol Topping
  nil
  (add-point! [_ _] nil))

;;; Spaghetti

(defrecord Spaghetti [element border inner points length]
  Topping
  (add-point! [this point]
    (if (< length 250)
      (let [new-points (conj points point)]
        (.setAttribute inner "points" (points->str new-points)) 
        (.setAttribute border "points" (points->str new-points)) 
        (Spaghetti. element border inner new-points
                    (+ length (distance point (peek points)))))
      this)))

(defmethod create-topping :spaghetti
  [_ point]
  (let [group (svg/create-svg-element "g")
        border (svg/create-svg-element "polyline")
        inner (svg/create-svg-element "polyline")]
    (doto group
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
    (Spaghetti. group border inner [point] 0)))

;;; Ziti

(defn- clamp-line
  [p1 p2]
  [p1 p2])

(defrecord Ziti [element border inner points]
  Topping
  (add-point! [this point]
    (let [new-points (clamp-line (first points) point)]
      (.setAttribute inner "points" (points->str new-points))
      (.setAttribute border "points" (points->str new-points))
      (Ziti. element border inner new-points))))

(defmethod create-topping :ziti
  [_ point]
  (let [group (svg/create-svg-element "g")
        border (svg/create-svg-element "polyline")
        inner (svg/create-svg-element "polyline")]
    (doto group
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
    (Ziti. group border inner [point] 0)))

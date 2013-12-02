(ns pizza.spaghetti
  (:require [pizza.svg :as svg]))

;;; Utils

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- length
  [ps]
  (reduce + (map distance ps (drop 1 ps))))

(defn- points->str
  [pts]
  (apply str (interleave (flatten pts) (repeat " "))))

(defmulti create-topping
  "The create-topping method is a factory for creating the appropriate topping.
  It expects two arguments -- a keyword that corresponds to a legal topping and
  the point of initial creation."
  identity)

(defprotocol Topping
  "A protocol for toppings which can go on a pizza. Toppings are also expected
  to have an:element key with an SVG node so that they can be added to the DOM."
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

;;; Ricotta

(def ^:private max-ricotta-radius 50)
(def ^:private min-ricotta-radius 5)

(defn- create-ricotta-blob
  [origin radius]
  (svg/create-circle origin radius "#fefefd" "#eeeeee" 3))

(defrecord Ricotta [element last-point max-radius]
  Topping
  (add-point! [this point]
    (if (> (distance last-point point) (/ max-radius 2))
      (let [circle (create-ricotta-blob point (* max-radius (rand)))
            new-max-radius (* 0.9 max-radius)]
        (.appendChild element circle)
        (if (> new-max-radius min-ricotta-radius)
          (Ricotta. element point (* 0.9 max-radius))
          nil))
      this)))

(defmethod create-topping :ricotta
  [_ point]
  (let [group (svg/create-svg-element "g")
        circle (create-ricotta-blob point (* max-ricotta-radius (rand)))]
    (.appendChild group circle)
    (Ricotta. group point (* 0.9 max-ricotta-radius))))

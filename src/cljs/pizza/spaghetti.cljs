(ns pizza.spaghetti
  (:require [pizza.svg :as svg]))

;;; Utils

(defn- rand-range
  [min max]
  (+ min (* max (rand))))

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- length
  [ps]
  (reduce + (map distance ps (drop 1 ps))))

(defn- angle
  [p1 p2]
  (apply Math/atan2 (reverse (map - p2 p1))))

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
  (let [group (svg/create-element "g")
        border (svg/create-element "polyline")
        inner (svg/create-element "polyline")]
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
  [[x y :as p1] p2]
  (let [max-len 60]
    (if (< (distance p1 p2) max-len)
    [p1 p2]
    (let [a (angle p1 p2)]
      [p1 [(+ x (* max-len (Math/cos a)))
           (+ y (* max-len (Math/sin a)))]]))))

(defrecord Ziti [element border inner hole points]
  Topping
  (add-point! [this point]
    (let [[[cx1 cy1] [cx2 cy2] :as new-points] (clamp-line (first points) point)]
      (.setAttribute inner "points" (points->str new-points))
      (.setAttribute border "points" (points->str new-points))
      #_(doto hole
        (.setAttribute "cx" (if (> cy1 cy2) cx1 cx2))
        (.setAttribute "cy" (if (> cy1 cy2) cy1 cy2)))
      (doto hole
        (.setAttribute "cx" cx2)
        (.setAttribute "cy" cy2))
      (Ziti. element border inner hole new-points))))

(defmethod create-topping :ziti
  [_ point]
  (let [group (svg/create-element "g")
        border (svg/create-element "polyline")
        inner (svg/create-element "polyline")
        hole (svg/create-element "circle")]
    (doto group
      (.appendChild border)
      (.appendChild inner)
      (.appendChild hole))
    (doto border
      (.setAttribute "fill" "transparent")
      (.setAttribute "stroke" "#9E9E22")
      (.setAttribute "stroke-linecap" "round")
      (.setAttribute "stroke-width" 17))
    (doto inner
      (.setAttribute "fill" "transparent")
      (.setAttribute "stroke" "#F5F5AA")
      (.setAttribute "stroke-linecap" "round")
      (.setAttribute "stroke-width" 15))
    (doto hole
      (.setAttribute "fill" "#9E9E22")
      (.setAttribute "r" 6)
      (.setAttribute "cx" (first point))
      (.setAttribute "cy" (second point)))
    (Ziti. group border inner hole [point])))

;;; Ricotta

(def ^:private max-ricotta-radius 40)
(def ^:private min-ricotta-radius 10)

(defn- add-blob
  "Adds a circle of ricotta to the element. Creates a circle for the border and
  a slightly smaller circle for the fill. By using two circles on different
  layers we can give the illusion that it is one irregular shape instead of a
  bunch of circles."
  [this point]
  (let [radius (rand-range 3 (:max-radius this))]
    (doto (:border this)
      (.appendChild (svg/create-circle point radius "#dde" "#dde" 0)))
    (doto (:inner this)
      (.appendChild (svg/create-circle point (- radius 2) "#eed" "#eed" 0)))
    this))

(defrecord Ricotta [element border inner last-point max-radius]
  Topping
  (add-point! [this point]
    (if (> (distance last-point point) (/ max-radius 5))
      (do (add-blob this point)
          (if (> max-radius min-ricotta-radius)
            (Ricotta. element border inner point (* 0.9 max-radius))
            nil))
      this)))

(defmethod create-topping :ricotta
  [_ point]
  (let [group (svg/create-element "g")
        border (svg/create-element "g")
        inner (svg/create-element "g")]
    (doto group
      (.appendChild border)
      (.appendChild inner))
    (-> (Ricotta. group border inner point (* 0.9 max-ricotta-radius))
      (add-blob point))))

;;; Linguini

(defrecord Linguini [element spaghetti]
  Topping
  (add-point! [this point]
    (add-point! spaghetti point)))

(defmethod create-topping :linguini
  [_ point]
  (let [spaghetti (create-topping :spaghetti point)]
    (doto (:border spaghetti)
      (.setAttribute "stroke-linecap" "square")
      (.setAttribute "stroke-width" 12))
    (doto (:inner spaghetti)
      (.setAttribute "stroke-linecap" "square")
      (.setAttribute "stroke-width" 10))
    (Linguini. (:element spaghetti) spaghetti)))

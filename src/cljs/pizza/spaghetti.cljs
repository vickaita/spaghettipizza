(ns pizza.spaghetti
  (:require [pizza.svg :as svg]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

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

(defmulti render :skin)

(defmethod render nil [_] nil)

;;; Spaghetti

(defn format-points
  [points]
  (apply str (interleave (flatten points) (repeat " "))))

(defmethod render :spaghetti
  [stroke owner]
  (om/component
    (let [points (format-points (:points stroke))]
      (html [:g.spaghetti.noodle {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke "#9E9E22"
                                :stroke-linecap "round"
                                :stroke-width 6}]
             [:polyline.inner {:points points
                               :fill "transparent"
                               :stroke "#F5F5AA"
                               :stroke-linecap "round"
                               :stroke-width 4}]]))))

(defmethod render :linguini
  [stroke owner]
  (om/component
    (let [points (format-points (:points stroke))]
      (html [:g.linguini.noodle {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke "#9E9E22"
                                :stroke-linecap "square"
                                :stroke-width 12}]
             [:polyline.inner {:points points
                               :fill "transparent"
                               :stroke "#F5F5AA"
                               :stroke-linecap "square"
                               :stroke-width 10}]]))))

(defn- clamp
  [[x y :as p1] p2]
  (let [max-len 60]
    (if (< (distance p1 p2) max-len)
    [p1 p2]
    (let [a (angle p1 p2)]
      [p1 [(+ x (* max-len (Math/cos a)))
           (+ y (* max-len (Math/sin a)))]]))))

(defmethod render :ziti
  [stroke owner]
  (om/component
    (let [points (:points @stroke)
          begin (first points)
          end (peek points)
          clamped (clamp begin end)
          formated (format-points clamped)]
      (html [:g.ziti.noodle {:key (:id stroke)}
             [:polyline.border {:points formated
                                :fill "transparent"
                                :stroke "#9E9E22"
                                :stroke-linecap "round"
                                :stroke-width 17}]
             [:polyline.inner {:points formated
                               :fill "transparent"
                               :stroke "#F5F5AA"
                               :stroke-linecap "round"
                               :stroke-width 15}]
             [:circle.hole {:fill "#9E9E22"
                            :r 6
                            :cx (first (second clamped))
                            :cy (second (second clamped))}]]))))

;; ---

;(def ^:private max-ricotta-radius 40)
;(def ^:private min-ricotta-radius 10)
;
;(defn- add-blob
;  "Adds a circle of ricotta to the element. Creates a circle for the border and
;  a slightly smaller circle for the fill. By using two circles on different
;  layers we can give the illusion that it is one irregular shape instead of a
;  bunch of circles."
;  [this point]
;  (let [radius (rand-range 3 (:max-radius this))]
;    (doto (:border this)
;      (.appendChild (svg/create-circle point radius "#dde" "#dde" 0)))
;    (doto (:inner this)
;      (.appendChild (svg/create-circle point (- radius 2) "#eed" "#eed" 0)))
;    this))
;
;(defrecord Ricotta [element border inner last-point max-radius]
;  Topping
;  (add-point! [this point]
;    (if (> (distance last-point point) (/ max-radius 5))
;      (do (add-blob this point)
;          (if (> max-radius min-ricotta-radius)
;            (Ricotta. element border inner point (* 0.9 max-radius))
;            nil))
;      this)))
;
;(defmethod create-topping :ricotta
;  [_ point]
;  (let [group (svg/create-element "g")
;        border (svg/create-element "g")
;        inner (svg/create-element "g")]
;    (doto group
;      (.appendChild border)
;      (.appendChild inner))
;    (-> (Ricotta. group border inner point (* 0.9 max-ricotta-radius))
;      (add-blob point))))

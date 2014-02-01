(ns pizza.spaghetti
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]))

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

(defn- clamp
  [[x y :as p1] p2]
  (let [max-len 60]
    (if (< (distance p1 p2) max-len)
    [p1 p2]
    (let [a (angle p1 p2)]
      [p1 [(+ x (* max-len (Math/cos a)))
           (+ y (* max-len (Math/sin a)))]]))))

(defn- format-points*
  "A non-memoized version of format points."
  [points]
  (apply str (interleave (flatten points) (repeat " "))))

(declare format-points)

(def ^:private format-points
  (memoize
    (fn [points]
      (if (empty? points)
        ""
        (str (ffirst points) " " (second (first points)) " "
             (format-points (rest points)))))))
;;; Toppings

(defmulti render :skin)

(defmethod render nil [_] nil)

(defmethod render :spaghetti
  [stroke owner]
  (om/component
    (let [points (format-points (:points stroke))]
      (html [:g.topping.noodle.spaghetti {:key (str (:id stroke) "-topping")}
             [:polyline.border {:key (str (:id stroke) "-border")
                                :points points
                                :fill "transparent"
                                :stroke "#9E9E22"
                                :stroke-linecap "round"
                                :stroke-width 6}]
             [:polyline.inner {:key (str (:id stroke) "-inner")
                               :points points
                               :fill "transparent"
                               :stroke "#F5F5AA"
                               :stroke-linecap "round"
                               :stroke-width 4}]]))))

(defmethod render :linguini
  [stroke owner]
  (om/component
    (let [points (format-points (:points stroke))]
      (html [:g.topping.noodle.linguini {:key (:id stroke)}
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

(defmethod render :ziti
  [stroke owner]
  (om/component
    (let [points (:points @stroke)
          begin (last points)
          end (first points)
          clamped (clamp begin end)
          formated (format-points clamped)]
      (html [:g.topping.noodle.ziti {:key (:id stroke)}
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

(def ^:private max-ricotta-radius 40)
(def ^:private min-ricotta-radius 10)

(defn- thin
  "Takes a seq points and thins them out so that you have a more sparse
  distribution of points."
  [points]
  (loop [acc []
         prev (first points)
         [current & remaining] (rest points)]
    (if (not current)
      acc
      (if (> (distance prev current) 10)
        (recur (conj acc current) current remaining)
        (recur acc prev remaining)))))

(defn- cheese-blob
  [point]
  [point (rand-range min-ricotta-radius max-ricotta-radius)])

;(def test-points (take 20 (repeatedly #(vector (rand-int 100) (rand-int 100)))))
;(map cheese-blob (thin test-points))

(defmethod render :ricotta
  [stroke owner]
  (om/component
    (let [circles (map cheese-blob (:points stroke))]
      (html [:g.topping.cheese.ricotta
             ; By using two circles on different layers we can give the illusion
             ; that it is one irregular shape instead of a  bunch of circles.
             (list [:g.border
                    (for [[[x y] radius] circles]
                      [:circle {:cx x
                                :cy y
                                :r (+ radius 2)
                                :fill "#DDDDEE"}])]
                   [:g.inner
                    (for [[[x y] radius] circles]
                      [:circle {:cx x
                                :cy y
                                :r radius
                                :fill "#EEEEDD"}])])]))))

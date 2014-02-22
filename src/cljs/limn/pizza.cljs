(ns limn.pizza
  (:require [goog.dom :as dom]
            [clojure.string :refer [join]]
            [limn.svg :as svg :refer [M C S rotate-point median-point]]))

(defn create-irregular-circle
  [origin radius]
  (let [[x y] origin
        variance (* 0.008 radius)
        ;; A variety of radius lengths
        radii (repeatedly #(- (+ radius variance) (rand (* 2 variance))))
        ;; A seq of random angles that go once around a circle
        angles (take-while #(< % (* 2 Math/PI)) (iterate #(+ % 0.2 (rand 0.2)) 0))
        ;; Convert the radii and angles into a series of points randomly around
        ;; the circle
        points (map #(vector (+ x (* %1 (Math/cos %2))) (+ y (* %1 (Math/sin %2))))
                    radii angles)
        ;; Take the points and create smooth curves between them.
        curves (reduce (fn [acc [[x1 y1 :as p1] [x2 y2 :as p2]]]
                         (conj acc (S (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI)) p2)))
                       (let [[[x1 y1 :as p1] [x2 y2 :as p2] & _] points]
                         [(M p1)
                          (C (rotate-point (median-point p1 p2) p1 (* 0.01 Math/PI))
                             (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI))
                             p2)])
                       (partition 2 1 points))]
    (str (join " " curves) " Z")))

(defn fresh-pizza []
  (let [origin [256 256]]
    {:crust (create-irregular-circle origin 227)
     :sauce (create-irregular-circle origin 210)}))

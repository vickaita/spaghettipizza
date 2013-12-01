(ns pizza.svg
  (:require [clojure.string :refer [join]]))

(def svg-ns "http://www.w3.org/2000/svg")

(defn create-svg-element
  [tag-name]
  (.createElementNS js/document svg-ns tag-name))

(defn- create-circle
  [origin radius fill stroke]
  (doto (create-svg-element "circle")
    (.setAttribute "cx" (first origin))
    (.setAttribute "cy" (second origin))
    (.setAttribute "r" radius)
    (.setAttribute "fill" fill)
    (.setAttribute "stroke" stroke)
    (.setAttribute "stroke-width" 3)))

(defn- create-path
  []
  (doto (create-svg-element "polyline")
    (.setAttribute "fill" "transparent")
    (.setAttribute "stroke" "#F5F5AA")
    (.setAttribute "stroke-width" 6)))

(defn- avg
  [a b]
  (/ (+ a b ) 2))

(defn- blend
  [[x1 y1 :as p1] [x2 y2 :as p2]]
  (let [[mx my :as median] [(avg x1 x2) (avg y1 y2)]
        cp1x (+ mx 5)
        cp1y (+ my 5)
        cp2x (- mx 5)
        cp2y (- my 5)]
    (str "C " cp1x " " cp1y " " cp2x " " cp2y " " x2 " " y2)))

(defn create-irregular-circle
  [origin radius fill stroke stroke-width]
  (let [[x y] origin
        circ (create-svg-element "path")
        variance (* 0.008 radius)
        radii (map #(- % (rand (* 2 variance))) (repeat (+ radius variance)))
        points (map (fn [dist angle]
                      [(+ x (* dist (Math/sin angle)))
                       (+ y (* dist (Math/cos angle)))])
                    radii
                    (take-while #(< % (* 2 Math/PI))
                                (iterate #(+ % 0.2 (rand 0.3)) 0)))
        curves (reduce (fn [acc [p1 p2]]
                         (conj acc (blend p1 p2)))
                       (let [[[x y] & _] points]
                         (do
                           (.log js/console x y)
                           [(str "M" x " " y)]))
                       (partition 2 1 points))]
    (doto (create-svg-element "path")
      (.setAttribute "fill" fill)
      (.setAttribute "stroke" stroke)
      (.setAttribute "stroke-width" stroke-width)
      ;(.setAttribute "points" (join " " (map (fn [[x y]] (str x "," y)) points)))   
      (.setAttribute "d" (str (join " " curves) " Z")))))

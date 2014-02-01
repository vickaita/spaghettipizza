(ns pizza.stroke
  (:require [om.core :as om :include-macros true]))

(let [counter (atom 0)]
  (defn- gen-id
    []
    (swap! counter inc)
    (str "stroke_" @counter)))

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn stroke
  []
  {:id (gen-id) :points () :skin nil :granularity 10})

(defn append
  [stroke pt]
  (let [points (:points stroke)]
    (if (or (empty? points)
            (> (distance pt (first points)) (:granularity stroke)))
      (assoc stroke :points (cons pt points))
      stroke)))

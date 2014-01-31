(ns pizza.stroke
  (:require [om.core :as om :include-macros true]
            [goog.dom :as gdom]))

(let [counter (atom 0)]
  (defn gen-id
    []
    (swap! counter inc)
    (str "stroke_" @counter)))

(defn- distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- normalize-point
  "Convert an event into a point."
  [e]
  (doto e .preventDefault .stopPropagation)
  (let [; XXX: There is a bug in React.js that incorrectly reports #document as
        ; the current target. When this is fixed upstream then getElement call
        ; can be avoided.
        elem (gdom/getElement "align-svg") ;elem (.-currentTarget e)
        offset (.getBoundingClientRect elem)
        left (.-left offset)
        top (.-top offset)
        scale-factor (/ 512 (.-width offset))]
    (case (.-type e)
      ("touchstart" "touchmove")
      (let [t (-> e .-touches (aget 0))]
        [(* scale-factor (- (.-pageX t) left))
         (* scale-factor (- (.-pageY t) top))])
      "touchend"
      nil
      [(* scale-factor (- (.-pageX e) left))
       (* scale-factor (- (.-pageY e) top))])))

(defn start
  [skin e]
  {:id (gen-id)
   :skin skin
   :points (list (normalize-point e))})

(defn append
  [points e granularity]
  (let [pt (normalize-point e)]
    (if (> (distance pt (first points)) granularity)
      (cons pt points)
      points)))

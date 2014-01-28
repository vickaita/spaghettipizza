(ns pizza.stroke
  (:require [om.core :as om :include-macros true]))

(let [counter (atom 0)]
  (defn id
    []
    (swap! counter inc)
    (str "stroke_" @counter)))

(defn- normalize-point
  "Convert an event into a point."
  [e]
  (doto e .preventDefault .stopPropagation)
  (let [elem (.-target e)
        _ (.log js/console elem)
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

(defn start [skin e] {:id (id) :skin skin :points [(normalize-point e)]})

(defn append [e] (normalize-point e))

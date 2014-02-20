(ns pizza.spaghetti
  (:require [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [pizza.stroke :as s]
            [pizza.geometry :as g]))

(defmulti render :skin)

(defmethod render nil [_] nil)

(defmethod render :edit
  [stroke owner]
  (om/component
    (let [sparse (s/thin stroke 30)]
      (html [:g.topping.edit {:key (:id stroke)}
           [:polyline.path {:points (s/format-points sparse)
                            :fill "transparent"
                            :stroke "black"
                            :stroke-width 2}]
           (for [point (:points sparse)]
             [:circle.point {:cx (first point)
                             :cy (second point)
                             :r 3
                             :fill "red"
                             :stroke "blue"
                             :stroke-width 1}])]))))

(defmethod render :spaghetti
  [stroke owner]
  (om/component
    (let [points (s/format-points stroke)]
      (html [:g.topping.noodle.spaghetti {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke (:stroke (:color stroke))
                                :stroke-linecap "round"
                                :stroke-width 6}]
             [:polyline.inner {:key (str (:id stroke) "-inner")
                               :points points
                               :fill "transparent"
                               :stroke (:fill (:color stroke))
                               :stroke-linecap "round"
                               :stroke-width 4}]]))))

(defmethod render :linguini
  [stroke owner]
  (om/component
    (let [points (s/format-points stroke)]
      (html [:g.topping.noodle.linguini {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke (:stroke (:color stroke))
                                :stroke-linecap "square"
                                :stroke-width 12}]
             [:polyline.inner {:points points
                               :fill "transparent"
                               :stroke (:fill (:color stroke))
                               :stroke-linecap "square"
                               :stroke-width 10}]]))))

(defmethod render :ziti
  [stroke owner]
  (om/component
    (let [clamped (s/clamp stroke 60)
          points (s/format-points clamped)]
      (html [:g.topping.noodle.ziti {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke "#9E9E22"
                                :stroke-linecap "round"
                                :stroke-width 17}]
             [:polyline.inner {:points points
                               :fill "transparent"
                               :stroke "#F5F5AA"
                               :stroke-linecap "round"
                               :stroke-width 15}]
             [:circle.hole {:fill "#9E9E22"
                            :r 6
                            :cx (first (s/destin clamped))
                            :cy (second (s/destin clamped))}]]))))

(defmethod render :ricotta
  [stroke owner]
  (om/component
    (let [circles (map vector
                       (reverse (:points stroke))
                       (map #(+ 3 (* 20 %)) (s/rand-seq stroke)))]
      (html [:g.topping.cheese.ricotta {:key (:id stroke)}
             ; By using two circles on different layers we can give the illusion
             ; that it is one irregular shape instead of a bunch of circles.
             [:g.border
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
                          :fill "#EEEEDD"}])]]))))

(defmethod render :basil
  [stroke owner]
  (om/component
    (let [points (s/format-points stroke)]
      (html [:g.topping.herb.basil {:key (:id stroke)}
             [:polyline.border {:points points
                                :fill "transparent"
                                :stroke "#F5F5AA"
                                :stroke-linecap "round"
                                :stroke-width 6}]
             [:polyline.inner {:key (str (:id stroke) "-inner")
                               :points points
                               :fill "transparent"
                               :stroke "#9E9E22"
                               :stroke-linecap "round"
                               :stroke-width 4}]
             [:g.leaves
              (for [[[x y] sign] (map vector
                                      (reverse (:points (s/thin stroke 40)))
                                      (cycle [-1 1]))]
                (let [outline (apply str (interleave [x y,
                                                      (+ x (* 20 (Math/cos 90))) (+ y (* 20 (Math/sin 90))),
                                                      x y]
                                          (repeat " ")))]
                  [:g.leaf
                 [:polyline {:points outline}]]))]]))))

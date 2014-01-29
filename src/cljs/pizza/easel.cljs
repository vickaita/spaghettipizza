(ns pizza.easel
  (:require [clojure.string :refer [join]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer [html] :include-macros true]
            [pizza.stroke :as stroke]
            [pizza.spaghetti :refer [render]]
            [pizza.svg :refer [M C S rotate-point median-point]]))

;; TODO: this needs to be in a geometry or svg namesapce (don't forget to
;; pizza.svg :require on the ns)
(defn create-irregular-circle
  [origin radius]
  (let [[x y] origin
        variance (* 0.008 radius)
        radii (map #(- % (rand (* 2 variance))) (repeat (+ radius variance)))
        points (map (fn [dist angle]
                      [(+ x (* dist (Math/cos angle)))
                       (+ y (* dist (Math/sin angle)))])
                    radii
                    (take-while #(< % (* 2 Math/PI))
                                (iterate #(+ % 0.2 (rand 0.2)) 0)))
        curves (reduce (fn [acc [[x1 y1 :as p1] [x2 y2 :as p2]]]
                         (conj acc (S (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI)) p2)))
                       (let [[[x1 y1 :as p1] [x2 y2 :as p2] & _] points]
                         [(M p1)
                          (C (rotate-point (median-point p1 p2) p1 (* 0.01 Math/PI))
                             (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI))
                             p2)])
                       (partition 2 1 points))]
    (str (join " " curves) " Z")))

;; TODO: put this back into the pizza namespace
(defn pizza
  "Draw a pizza."
  [app owner]
  (let [origin [256 256]]
    (html [:g.pizza
           [:path.crust {:d (:crust app)
                         :fill "#FAE265"
                         :stroke "#DDAB0B"
                         :stroke-width 3}]
           [:path.sauce {:d (:sauce app)
                         :fill "#F86969"
                         :stroke "#F04F4F"
                         :stroke-width 3}]])))

(defn easel
  [{:keys [image-url strokes width height tool] :as app} owner]
  (reify
    om/IInitState
    (init-state [_] {:drawing? false})
    om/IWillMount
    (will-mount [_]
      (events/listen js/document "mouseup" #(om/set-state! owner :drawing? false)))
    om/IRender
    (render [_]
      (html [:section.easel
             {:width width
              :height height
              :on-mouse-down
              #(do (om/set-state! owner :drawing? true)
                   (om/transact! app [:strokes] conj (stroke/start tool %)))
              ;:on-touch-start #(stroke/start tool %)
              :on-mouse-move
              #(when (om/get-state owner :drawing?)
                 (om/transact! app [:strokes (dec (count (:strokes @app))) :points]
                               conj (stroke/append %)))
              ;:on-touch-move #(stroke/append %)
              }
             [:div#align-svg
              [:svg#main-svg {:width width
                              :height height
                              :viewBox "0 0 512 512"
                              :version "1.1"
                              :preserveAspectRatio "xMidYMid"
                              :xmlns "http://www.w3.org/2000/svg"}
               (if image-url
                 [:text "foo"]
                 ;[:g.raster.layer
                 ; [:image {:xlink:href image-url
                 ;          :x "0" :y "0"
                 ;          :height (str height "px") :width (str width "px")}]]
                 [:g.vector.layer
                  (pizza app owner)
                  (for [stroke (:strokes app)]
                    (om/build render stroke))
                  ;(map #(render % owner) (:strokes app))
                  ])]]]))))

(ns pizza.easel
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer [html] :include-macros true]
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
  [{:keys [crust sauce]} owner]
  (om/component
    (html [:g.pizza
           [:path.crust {:d crust
                         :fill "#FAE265"
                         :stroke "#DDAB0B"
                         :stroke-width 3}]
           [:path.sauce {:d sauce
                         :fill "#F86969"
                         :stroke "#F04F4F"
                         :stroke-width 3}]])))

(defn- normalize-point
  "Convert an event into a point."
  [e]
  (let [; XXX: There is a bug in React.js that incorrectly reports #document as
        ; the current target. When this is fixed upstream then getElement call
        ; can be avoided.
        elem (.getElementById js/document "align-svg") ;elem (.-currentTarget e)
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
              (fn [e]
                (doto e (.preventDefault) (.stopPropagation))
                (om/set-state! owner :drawing? true)
                (put! (:commands @app) [:new-stroke (normalize-point e)]))
              ;#(do (om/set-state! owner :drawing? true)
              ;     (om/transact! app [:strokes] conj (stroke/start tool %)))
              ;:on-touch-start #(stroke/start tool %)
              :on-mouse-move
              (fn [e]
                (doto e .preventDefault .stopPropagation)
                (when (om/get-state owner :drawing?)
                  (put! (:commands @app) [:extend-stroke (normalize-point e)])))
              ;#(when (om/get-state owner :drawing?)
              ;   (om/transact!
              ;     app
              ;     [:strokes (dec (count (:strokes @app))) :points]
              ;     stroke/append % (:granularity @app)))
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
                  (om/build pizza (:pizza app))
                  (for [stroke (:strokes app)]
                    (om/build render stroke))])]]]))))

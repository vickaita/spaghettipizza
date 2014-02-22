(ns limn.easel
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.stroke :as stroke]
            [limn.spaghetti :refer [render]]
            [limn.svg :refer [M C S rotate-point median-point]]))

;; TODO: this needs to be in a geometry or svg namesapce (don't forget to
;; pizza.svg :require on the ns)
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

;; TODO: put this back into the pizza namespace
(defn pizza
  "Draw a pizza."
  [{:keys [crust sauce]} owner]
  (om/component
    (html [:g.pizza {:key "pizza"}
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
  [scale-by e]
  (when-let [elem (.getElementById js/document "align-svg")]
    (let [; XXX: There is a bug in React.js that incorrectly reports #document as
          ; the current target. When this is fixed upstream then getElement call
          ; can be avoided. Fixed with this pull request:
          ; https://github.com/facebook/react/pull/747, but not in current
          ; release yet.
          ;elem (.getElementById js/document "align-svg") ;elem (.-currentTarget e)
          rect (.getBoundingClientRect elem)
          left (.-left rect)
          top (.-top rect)]
      (case (.-type e)
        ("touchstart" "touchmove")
        (let [t (-> e .-touches (aget 0))]
          [(-> t (.-pageX) (- left) (* scale-by) Math/floor)
           (-> t (.-pageY) (- top) (* scale-by) Math/floor)])
        "touchend"
        nil
        [(-> e (.-pageX) (- left) (* scale-by) Math/floor)
         (-> e (.-pageY) (- top) (* scale-by) Math/floor)]))))

(defn easel
  [{:keys [image-url image-loading? strokes tool] :as app} owner]
  (reify
    om/IInitState
    (init-state [_] {:drawing? false})
    om/IWillMount
    (will-mount [_]
      (events/listen js/document "mouseup" #(om/set-state! owner :drawing? false)))
    om/IRender
    (render [_]
      (let [side (min (:easel-width app) (:easel-height app))
            start-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (om/set-state! owner :drawing? true)
              (let [{:keys [scale-by]} @app]
                (put! (:commands (om/get-shared owner))
                      [:new-stroke (normalize-point scale-by e)])))
            extend-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (when (om/get-state owner :drawing?)
                (let [{:keys [scale-by]} @app]
                  (put! (:commands (om/get-shared owner))
                        [:extend-stroke (normalize-point scale-by e)]))))
            end-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (om/set-state! owner :drawing? false))]
        (html [:section.easel {:id "align-svg"
                               :width side
                               :height side
                               :on-mouse-down start-stroke
                               :on-touch-start start-stroke
                               :on-mouse-move extend-stroke
                               :on-touch-move extend-stroke
                               :on-touch-end end-stroke}
               (cond
                 (:image-url app)
                 [:div#image-wrapper
                  [:img {:src (:image-url app)}]]

               (:image-loading? app)
               [:div#image-wrapper
                [:p "Loading ..."]]

               :else
               [:svg {:id "main-svg"
                      :width side
                      :height side
                      :viewBox (str "0 0 " (:viewport-width app) " "
                                    (:viewport-height app))
                      :version "1.1"
                      :preserveAspectRatio "xMidYMid"
                      :xmlns "http://www.w3.org/2000/svg"}
                [:g.vector.layer
                 (om/build pizza (:pizza app))
                 (for [stroke (:strokes app)]
                   (om/build render stroke))]])])))))

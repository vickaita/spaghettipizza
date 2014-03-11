(ns limn.views.easel
  (:require [clojure.string]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.models.easel :as me]
            [limn.stroke :as s]
            [spaghetti-pizza.pizza :as pizza]))

(defn- translate
  "Maps a point from user-space (the document coordinate system) to view-space
  (the svg-coordiate system)."
  [app [x y]]
  (let [view-offset-x (:view-offset-x app)
        view-offset-y (:view-offset-y app)
        scale (:scale-by app)]
    [(-> x (* scale) (+ view-offset-x) Math/floor)
     (-> y (* scale) (+ view-offset-y) Math/floor)]))

(defn- normalize-points
  "Returns a vector of points from an event. Click events will always return a
  vector with a single point in it; touch events will return a point for each
  finger that is touching. Points will be offset so that they are relative to
  the svg element, but they are still in the user-space."
  [e]
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
        (for [i (range (alength (.-touches e)))]
          (let [t (aget (.-touches e) i)]
            [(- (.-pageX t) left) (- (.-pageY t) top)]))
        "touchend"
        nil
        [[(- (.-pageX e) left) (- (.-pageY e) top)]]))))

(defn easel
  [app owner]
  (let [commands (om/get-shared owner :commands)

        start-stroke
        (fn [e]
          (doto e .preventDefault .stopPropagation)
          (let [points (normalize-points e)]
            (case (count points)
              1 (let [state (om/get-state owner)
                      skin (:skin state)
                      color (:color state)]
                  (om/set-state! owner :drawing? true)
                  (om/transact! app #(me/start-stroke % (translate @app (first points)) skin color)))
              2 (om/set-state! owner :zoom (map translate points)))))

        extend-stroke
        (fn [e]
          (doto e .preventDefault .stopPropagation)
          (let [points (normalize-points e)
                state (om/get-state owner)]
            (case (count points)
              1 (when (:drawing? state)
                  (om/transact! app #(me/extend-stroke % (translate @app (first points)))))
              2 (when-let [zoom (:zoom state)]
                  (om/transact! app #(me/zoom % zoom points))))))

        end-stroke
        (fn [e]
          (doto e .preventDefault .stopPropagation)
          (let [state (om/get-state owner)]
            (when (:drawing? state)
              (om/set-state! owner :drawing? false)
              (om/transact! app #(me/end-stroke %)))
            (when (:zoom state)
              (om/set-state! owner :zoom nil))))

        ]
    (reify
      om/IWillMount
      (will-mount [_] (events/listen js/document "mouseup" end-stroke))
      om/IRender
      (render [_]
        (let [side (min (:width app) (:height app))]
          (html [:section.easel {:id "align-svg"
                                 :width side
                                 :height side
                                 :on-mouse-down start-stroke
                                 :on-touch-start start-stroke
                                 :on-mouse-move extend-stroke
                                 :on-touch-move extend-stroke
                                 :on-touch-end end-stroke}
                 [:svg {:id "main-svg"
                        :width side
                        :height side
                        :viewBox (clojure.string/join " " (:view-box app))
                        :version "1.1"
                        :preserveAspectRatio "xMidYMid"
                        :xmlns "http://www.w3.org/2000/svg"}
                  [:g.vector.layer
                   (om/build pizza/draw-pizza (:pizza app) {:react-key "base-pizza"})
                   (om/build-all s/render (:strokes app) {:key :id})]
                  [:g.vector.layer.current
                   (when-let [current (:current-stroke app)]
                     (om/build s/render current {:react-key "current-stroke"}))]]]))))))

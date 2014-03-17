(ns limn.views.easel
  (:require [clojure.string]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [shodan.console :as console]
            [limn.models.easel :as me]
            [limn.stroke :as s]
            [spaghetti-pizza.pizza :as pizza]))

(defn- transform-point
  "Returns an point mapped from the user space to the svg space."
  [svg ctm [x y]]
  ;; FIXME: `createSVGPoint`, `matrixTransform`, and `getCTM` are being munged
  ;; by the closure compiler. This can probably be fixed by adding some
  ;; externs rather than these inlined strings.
  (let [p (js* "~{}['createSVGPoint']();" svg)
        #_(.createSVGPoint svg)]
    (set! (.-x p) x)
    (set! (.-y p) y)
    (let [p2 (js* "~{}['matrixTransform'](~{})" p ctm)
          #_(.matrixTransform p (.inverse (.getCTM svg)))]
      [(Math/floor (.-x p2)) (Math/floor (.-y p2))])))

(defn- normalized-points
  "Returns a vector of points from an event. Click events will always return a
  vector with a single point in it; touch events will return a point for each
  finger that is touching. Points will be offset so that they are relative to
  element, but they are still in the user-space."
  [element e]
    (let [rect (.getBoundingClientRect element)
          left (.-left rect)
          top (.-top rect)]
      (case (.-type e)
        ("touchstart" "touchmove")
        (for [i (range (alength (.-touches e)))]
          (let [t (aget (.-touches e) i)]
            [(- (.-pageX t) left) (- (.-pageY t) top)]))
        "touchend"
        nil
        [[(- (.-pageX e) left) (- (.-pageY e) top)]])))

(defn- handle-start
  [app owner e]
  (doto e .preventDefault .stopPropagation)
  (when-let [elem (om/get-node owner)]
    (let [svg (.-firstChild elem)
          ctm (js* "~{}['getCTM']().inverse()" svg)
          points (normalized-points elem e)]
      (om/set-state! owner :ctm ctm)
      (case (count points)
        1 (let [state (om/get-state owner)
                skin (:skin state)
                color (:color state)]
            (om/set-state! owner :drawing? true)
            (om/transact! app #(me/start-stroke
                                 %
                                 (transform-point svg ctm (first points))
                                 skin color)))
        2 (om/set-state! owner :zoom (map (partial transform-point svg ctm)
                                          points))))))

(defn- handle-move
  [app owner e]
  (doto e .preventDefault .stopPropagation)
  (when-let [elem (om/get-node owner)]
    (let [svg (.-firstChild elem)
          points (normalized-points elem e)
          state (om/get-state owner)
          ctm (:ctm state)]
      (when ctm
        (case (count points)
          1 (when (:drawing? state)
              (om/transact! app #(me/extend-stroke
                                   %
                                   (transform-point svg ctm (first points)))))
          2 (when-let [zoom (:zoom state)]
              (om/transact! app #(me/zoom %
                                          zoom
                                          (map (partial transform-point
                                                        svg
                                                        ctm)
                                               points)))))))))

(defn- handle-end
  [app owner e]
  (doto e .preventDefault .stopPropagation)
  (let [state (om/get-state owner)]
    (when (:drawing? state)
      (om/set-state! owner :drawing? false)
      (om/transact! app #(me/end-stroke %)))
    (when (:zoom state)
      (om/set-state! owner :zoom nil))
    (when (:ctm state)
      (om/set-state! owner :ctm nil))))

(defn easel
  [app owner]
  (let [start-stroke (partial handle-start app owner)
        extend-stroke (partial handle-move app owner)
        end-stroke (partial handle-end app owner)]
    (reify
      om/IWillMount
      (will-mount [_] (events/listen js/document "mouseup" end-stroke))
      om/IRender
      (render [_]
        (let [side (min (:width app) (:height app))]
          (html [:section.easel {:width side
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
                     (om/build s/render current))]]]))))))

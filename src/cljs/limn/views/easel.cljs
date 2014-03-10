(ns limn.views.easel
  (:require [clojure.string]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.models.easel :as me]
            [limn.stroke :as s]))

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

(defn- translate
  "Maps a point from user-space (the document coordinate system) to view-space
  (the svg-coordiate system)."
  [x user-offset view-offset scale]
  (-> x (- user-offset) (* scale) (+ view-offset) Math/floor))

(defn- normalize-points
  "Convert an event into a point."
  [app e]
  (when-let [elem (.getElementById js/document "align-svg")]
    (let [; XXX: There is a bug in React.js that incorrectly reports #document as
          ; the current target. When this is fixed upstream then getElement call
          ; can be avoided. Fixed with this pull request:
          ; https://github.com/facebook/react/pull/747, but not in current
          ; release yet.
          ;elem (.getElementById js/document "align-svg") ;elem (.-currentTarget e)
          scale-x (:scale-by app)
          scale-y (:scale-by app)
          view-offset-x (:view-offset-x app)
          view-offset-y (:view-offset-y app)
          rect (.getBoundingClientRect elem)
          left (.-left rect)
          top (.-top rect)]
      (case (.-type e)
        ("touchstart" "touchmove")
        (for [i (range (alength (.-touches e)))]
          (let [t (aget (.-touches e) i)]
            [(translate (.-pageX t) left view-offset-x scale-x)
             (translate (.-pageY t) top view-offset-y scale-y)]))
        "touchend"
        nil
        [[(translate (.-pageX e) left view-offset-x scale-x)
          (translate (.-pageY e) top view-offset-y scale-y)]]))))

(defn easel
  [app owner]
  (let [commands (om/get-shared owner :commands)

        start-stroke
        (fn [e]
          (doto e .preventDefault .stopPropagation)
          (let [points (normalize-points @app e)]
            (case (count points)
              1 (let [state (om/get-state owner)
                      skin (:skin state)
                      color (:color state)]
                  (om/set-state! owner :drawing? true)
                  (om/transact! app #(me/start-stroke % (first points) skin color)))
              2 (om/set-state! owner :zoom points))))

        extend-stroke
        (fn [e]
          (doto e .preventDefault .stopPropagation)
          (let [points (normalize-points @app e)
                state (om/get-state owner)]
            (case (count points)
              1 (when (:drawing? state)
                  (om/transact! app #(me/extend-stroke % (first points))))
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
                   (om/build pizza (:pizza app) {:react-key "base-pizza"})
                   (om/build-all s/render (:strokes app) {:key :id})]
                  [:g.vector.layer.current
                   (when-let [current (:current-stroke app)]
                     (om/build s/render current {:react-key "current-stroke"}))]]]))))))

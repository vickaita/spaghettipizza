(ns limn.views.easel
  (:require [clojure.string]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
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

(defn- normalize-points
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
        (for [i (range (alength (.-touches e)))]
          (let [t (aget (.-touches e) i)]
            [(-> t (.-pageX) (- left) (* scale-by) Math/floor)
             (-> t (.-pageY) (- top) (* scale-by) Math/floor)]))
        "touchend"
        nil
        [[(-> e (.-pageX) (- left) (* scale-by) Math/floor)
          (-> e (.-pageY) (- top) (* scale-by) Math/floor)]]))))

(defn easel
  [app owner]
  (let [commands (om/get-shared owner :commands)
        start-stroke (fn [e]
                       (doto e .preventDefault .stopPropagation)
                       (om/set-state! owner :drawing? true)
                       (prn (normalize-points (:scale-by @app) e))
                       (put! commands [:new-stroke (first (normalize-points (:scale-by @app) e))]))
        extend-stroke (fn [e]
                        (doto e .preventDefault .stopPropagation)
                        (prn (normalize-points (:scale-by @app) e))
                        (when (om/get-state owner :drawing?)
                          (put! commands [:extend-stroke (first (normalize-points (:scale-by @app) e))])))
        end-stroke (fn [e]
                     (doto e .preventDefault .stopPropagation)
                     (om/set-state! owner :drawing? false))]
    (reify
      om/IInitState
      (init-state [_] {:drawing? false})
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
                   (om/build pizza (:pizza app))
                   (om/build-all s/render (:strokes app))]]]))))))

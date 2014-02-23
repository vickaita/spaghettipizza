(ns limn.views.easel
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put!]]
            [goog.events :as events]
            [om.core :as om :include-macros true]
            [sablono.core :refer-macros [html]]
            [limn.stroke :refer [stroke render]]))

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
    (init-state [_] {:draw-target nil
                     :edit-target nil})
    om/IWillMount
    (will-mount [_]
      (events/listen js/document "mouseup" #(om/set-state! owner :draw-target nil)))
    om/IRender
    (render [_]
      (let [side (min (:easel-width app) (:easel-height app))
            start-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (let [{:keys [scale-by]} @app]
                (om/set-state! owner :draw-target
                               (stroke (normalize-point scale-by e)))))
            extend-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (when-let [target (om/get-state owner :draw-target)]
                (let [{:keys [scale-by]} @app]
                  (put! (:commands (om/get-shared owner))
                        [:extend-stroke (normalize-point scale-by e)]))))
            end-stroke
            (fn [e]
              (doto e .preventDefault .stopPropagation)
              (om/set-state! owner :draw-target nil))]
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

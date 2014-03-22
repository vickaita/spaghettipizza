(ns limn.models.easel
  (:require [limn.stroke :as s]
            [limn.geometry :refer [distance median-point]]
            [shodan.console :as console]))

(defn loggit
  [msg]
  (doto (.getElementById js/document "loggit")
    (.appendChild (.createTextNode js/document msg))))

(def ^:private VBX 512)

(def default-easel
  {:current-stroke nil
   :strokes []
   :width 0
   :height 0
   :scale 1
   :offset-x 0
   :offset-y 0
   :view-box [0 0 VBX VBX]})

(defn start-stroke
  [easel pt skin color]
  (assoc easel :current-stroke (s/stroke {:skin skin
                                          :color color
                                          :points (list pt)})))

(defn extend-stroke
  [easel pt]
  (assoc easel :current-stroke (s/append (:current-stroke easel) pt)))

(defn end-stroke
  [easel]
  (if (:current-stroke easel)
    (-> easel
        (assoc :current-stroke nil)
        (assoc :strokes (conj (:strokes easel) (:current-stroke easel))))
    easel))

(defn zoom
  [easel from to]
  (let [scale (/ (apply distance to) (apply distance from))
        start (apply median-point from)
        end (apply median-point to)
        offset-x (- (first end) (first start))
        offset-y (- (second end) (second start))]
    (conj easel {:scale (max scale 1)
                 :offset-x offset-x
                 :offset-y offset-y})))

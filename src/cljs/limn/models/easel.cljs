(ns limn.models.easel
  (:require [limn.stroke :as s]
            [limn.geometry :refer [distance]]))

(def ^:private VBX 512)

(def default-easel
  {:current-stroke nil
   :strokes []
   :width 0
   :height 0
   :scale-by 1
   :view-offset-x 0
   :view-offset-y 0
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
  "Zooms the easel by adjusting the view-box and scale-by so that the segment
  `from` maps to `to`."
  [easel from to]
  (let [scale (/ (apply distance to) (apply distance from))
        offset-x (first (first from))
        offset-y (second (first from))
        view-box [offset-x
                  offset-y
                  (+ offset-x ((* scale VBX)))
                  (+ offset-y (* scale VBX))]]
    (conj easel {:scale-by scale
                 :view-offset-x offset-x
                 :view-offset-y offset-y
                 :view-box view-box})))

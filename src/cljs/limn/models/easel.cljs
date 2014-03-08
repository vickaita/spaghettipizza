(ns limn.models.easel
  (:require [limn.stroke :as s]))

(def default-easel
  {:scale-by 1
   :current-stroke nil
   :strokes []
   :width 0
   :height 0
   :view-box [0 0 512 512]})

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

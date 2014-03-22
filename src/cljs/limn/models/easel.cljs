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

;(defn- left-most
;  [points]
;  (first (first (sort-by first points))))
;
;(defn- top-most
;  [points]
;  (second (first (sort-by second points))))
;
;(defn- left-edge   [[[x1 _] [x2 _]]] (min x1 x2))
;(defn- right-edge  [[[x1 _] [x2 _]]] (max x1 x2))
;(defn- top-edge    [[[_ y1] [_ y2]]] (min y1 y2))
;(defn- bottom-edge [[[_ y1] [_ y2]]] (max y1 y2))

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

;(defn zoom
;  [easel zoom-box from to]
;  (let [[[xa1 ya1] [xa2 ya2]] from
;        [[xb1 yb1] [xb2 yb2]] to
;        delta-x-a (- xa2 xa1)
;        delta-x-b (- xb2 xb1)
;        mx (/ delta-x-b delta-x-a)
;        bx (- xb1 (* mx xa1))
;        delta-y-a (- ya2 ya1)
;        delta-y-b (- yb2 yb1)
;        my (/ delta-y-b delta-y-a)
;        by (- yb1 (* my ya1))
;        [a b c d] zoom-box]
;    (conj easel {:view-box [(+ (* mx a) bx)
;                            (+ (* my b) by)
;                            (+ (* mx c) bx)
;                            (+ (* my d) by)]})))

;;; Shift down
;(= [0 -100 512 412]
;   (:view-box (zoom default-easel
;                    [0 0 512 512]
;                    [[0 0] [100 0]]
;                    [[0 100] [100 100]])))
;
;;; Shift right
;(= [-100 0 412 512]
;   (:view-box (zoom default-easel
;                    [0 0 512 512]
;                    [[0 0] [0 100]]
;                    [[100 0] [100 100]])))
;
;;; Shift down and right
;(= [-100 -100 412 412]
;   (:view-box (zoom default-easel
;                    [0 0 512 512]
;                    [[0 0] [100 100]]
;                    [[100 100] [200 200]])))
;
;;; Scale x
;(= [0 0 256 256]
;   (:view-box (zoom default-easel
;                    [0 0 512 512]
;                    [[0 0] [256 0]]
;                    [[0 0] [512 0]])))
;
;(= [-256 -256 512 512]
;   (:view-box (zoom default-easel
;                    [0 0 512 512]
;                    [[256 0] [512 0]]
;                    [[0 0] [512 0]])))

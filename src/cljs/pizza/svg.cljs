(ns pizza.svg
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put! chan]]
            [goog.dom :as dom]
            [goog.events :as events]))

(def svg-ns "http://www.w3.org/2000/svg")

(defn create-element
  [tag-name]
  (.createElementNS js/document svg-ns tag-name))

(def g (partial create-element "g"))

(defn- create-circle
  [origin radius fill stroke stroke-width]
  (doto (create-element "circle")
    (.setAttribute "cx" (first origin))
    (.setAttribute "cy" (second origin))
    (.setAttribute "r" radius)
    (.setAttribute "fill" fill)
    (.setAttribute "stroke" stroke)
    (.setAttribute "stroke-width" stroke-width)))

(defn- create-path
  []
  (doto (create-element "polyline")
    (.setAttribute "fill" "transparent")
    (.setAttribute "stroke" "#F5F5AA")
    (.setAttribute "stroke-width" 6)))

(defn- median-point
  "Given two points returns the point which lies halfway between them."
  [[x1 y1] [x2 y2]]
  [(/ (+ x1 x2) 2) (/ (+ y1 y2) 2)])

(defn- rotate-point
  [[px py] [ox oy] theta]
  (let [c (Math/cos theta)
        s (Math/sin theta)]
    [(+ ox (- (* c (- px ox)) (* s (- py oy))))
     (+ oy (* s (- px ox)) (* c (- py oy)))]))

(defn- M
  [[x y]]
  (str "M" x " " y))

(defn- C
  [[x1 y1] [x2 y2] [x y]]
  (str "C" x1 " " y1 " " x2 " " y2 " " x " " y))

(defn- S
  [[x2 y2] [x y]]
  (str "S" x2 " " y2 " " x " " y))

;(defn create-irregular-circle
;  [origin radius fill stroke stroke-width]
;  (let [[x y] origin
;        circ (create-element "path")
;        variance (* 0.008 radius)
;        radii (map #(- % (rand (* 2 variance))) (repeat (+ radius variance)))
;        points (map (fn [dist angle]
;                      [(+ x (* dist (Math/cos angle)))
;                       (+ y (* dist (Math/sin angle)))])
;                    radii
;                    (take-while #(< % (* 2 Math/PI))
;                                (iterate #(+ % 0.2 (rand 0.2)) 0)))
;        curves (reduce (fn [acc [[x1 y1 :as p1] [x2 y2 :as p2]]]
;                         (conj acc (S (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI)) p2)))
;                       (let [[[x1 y1 :as p1] [x2 y2 :as p2] & _] points]
;                         [(M p1)
;                          (C (rotate-point (median-point p1 p2) p1 (* 0.01 Math/PI))
;                             (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI))
;                             p2)])
;                       (partition 2 1 points))]
;    (doto (create-element "path")
;      (.setAttribute "fill" fill)
;      (.setAttribute "stroke" stroke)
;      (.setAttribute "stroke-width" stroke-width)
;      (.setAttribute "d" (str (join " " curves) " Z")))))

(defn svg->img-chan
  [svg-elem w h]
  (let [img (doto (js/Image.)
              (.setAttribute "width" w)
              (.setAttribute "height" h))
        canvas (doto (dom/createElement "canvas")
                 (.setAttribute "width" w)
                 (.setAttribute "height" h))
        context (.getContext canvas "2d")
        svg-data (.serializeToString (js/XMLSerializer.) svg-elem)
        svg-blob (js/Blob. #js [svg-data]
                           #js {:type "image/svg+xml;base64"})
        url (.createObjectURL js/URL svg-blob)
        ;url (str "data:image/svg+xml;base64," (js/btoa svg-data))
        out (chan)]
    (prn :listen)
    (prn url)
    (set! (.-onload img)
          (fn [e]
            (prn :load)
            (.drawImage context img 0 0 w h)
            (.revokeObjectURL js/URL url)
            (let [url (.toDataURL canvas "image/png")
                  binary (js/atob (aget (.split url ",") 1))
                  arr #js []]
              (dotimes [i (alength binary)]
                (aset arr i (.charCodeAt binary i)))
              (put! out (js/Blob. #js [(js/Uint8Array. arr)]
                                  #js {:type "image/png"})))))
    (set! (.-onerror img) #(.log js/console "There was an error:" %))
    (set! (.-src img) url)
    out))

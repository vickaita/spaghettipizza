(ns pizza.svg
  (:require [clojure.string :refer [join]]
            [cljs.core.async :refer [put! chan]]))

(def svg-ns "http://www.w3.org/2000/svg")

(defn create-svg-element
  [tag-name]
  (.createElementNS js/document svg-ns tag-name))

(defn- create-circle
  [origin radius fill stroke stroke-width]
  (doto (create-svg-element "circle")
    (.setAttribute "cx" (first origin))
    (.setAttribute "cy" (second origin))
    (.setAttribute "r" radius)
    (.setAttribute "fill" fill)
    (.setAttribute "stroke" stroke)
    (.setAttribute "stroke-width" stroke-width)))

(defn- create-path
  []
  (doto (create-svg-element "polyline")
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

(defn create-irregular-circle
  [origin radius fill stroke stroke-width]
  (let [[x y] origin
        circ (create-svg-element "path")
        variance (* 0.008 radius)
        radii (map #(- % (rand (* 2 variance))) (repeat (+ radius variance)))
        points (map (fn [dist angle]
                      [(+ x (* dist (Math/cos angle)))
                       (+ y (* dist (Math/sin angle)))])
                    radii
                    (take-while #(< % (* 2 Math/PI))
                                (iterate #(+ % 0.2 (rand 0.2)) 0)))
        curves (reduce (fn [acc [[x1 y1 :as p1] [x2 y2 :as p2]]]
                         (conj acc (S (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI)) p2)))
                       (let [[[x1 y1 :as p1] [x2 y2 :as p2] & _] points]
                         [(M p1)
                          (C (rotate-point (median-point p1 p2) p1 (* 0.01 Math/PI))
                             (rotate-point (median-point p1 p2) p2 (* -0.01 Math/PI))
                             p2)])
                       (partition 2 1 points))]
    (doto (create-svg-element "path")
      (.setAttribute "fill" fill)
      (.setAttribute "stroke" stroke)
      (.setAttribute "stroke-width" stroke-width)
      (.setAttribute "d" (str (join " " curves) " Z")))))

(defn svg->png-chan
  [svg-elem]
  (let [canvas (.createElement js/document "canvas")
        context (.getContext canvas "2d")
        data (.serializeToString (js/XMLSerializer.) svg-elem)
        img (js/Image.)
        svg-blob (js/Blob. (array data) (js-obj "type" "image/svg+xml;charset=utf-8"))
        url (.createObjectURL js/URL svg-blob)
        out (chan)]
    (.setAttribute canvas "width" (.getAttribute svg-elem "width"))
    (.setAttribute canvas "height" (.getAttribute svg-elem "height"))
    (set! (.-onload img) (fn []
                           (.drawImage context img 0 0)
                           (.revokeObjectURL js/URL url)
                           (put! out (.toDataURL canvas "image/png"))))
    (set! (.-src img) url)
    out))

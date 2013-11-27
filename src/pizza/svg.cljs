(ns pizza.svg)

(def svg-ns "http://www.w3.org/2000/svg")

(defn create-svg-element
  [tag-name]
  (.createElementNS js/document svg-ns tag-name))

(defn- create-circle
  [origin radius fill stroke]
  (doto (create-svg-element "circle")
    (.setAttribute "cx" (first origin))
    (.setAttribute "cy" (second origin))
    (.setAttribute "r" radius)
    (.setAttribute "fill" fill)
    (.setAttribute "stroke" stroke)
    (.setAttribute "stroke-width" 3)))

(defn- create-path
  []
  (doto (create-svg-element "polyline")
    (.setAttribute "fill" "transparent")
    (.setAttribute "stroke" "#F5F5AA")
    (.setAttribute "stroke-width" 6)))

(defn create-irregular-circle
  [origin radius fill stroke stroke-width]
  (let [[x y] origin
        circ (create-svg-element "path")
        variance (* 0.008 radius)
        radii (map #(- % (rand (* 2 variance))) (repeat (+ radius variance)))
        points (map (fn [dist angle] [(+ x (* dist (Math/cos angle)))
                                      (+ y (* dist (Math/sin angle)))])
                    radii
                    (take-while #(< % (* 2 Math/PI)) (iterate #(+ % 0.2 (rand 0.3)) 0)))
        curves nil]; #_(reduce (fn [acc [p1 p2]] (conj ()) ) [] (partition 2 1 points))]
    (doto (create-svg-element "polygon")
      (.setAttribute "fill" fill)
      (.setAttribute "stroke" stroke)
      (.setAttribute "stroke-width" stroke-width)
      (.setAttribute "points" (apply str
                                     (interleave
                                       (map (fn [[x y]] (str x "," y)) points)
                                       (repeat " ")))))))

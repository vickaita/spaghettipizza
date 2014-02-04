(ns pizza.geometry)

(defn distance
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn length
  [ps]
  (reduce + (map distance ps (drop 1 ps))))

(defn angle
  [p1 p2]
  (apply Math/atan2 (reverse (map - p2 p1))))

(defn median-point
  "Given two points returns the point which lies halfway between them."
  [[x1 y1] [x2 y2]]
  [(/ (+ x1 x2) 2) (/ (+ y1 y2) 2)])

(defn rotate-point
  "Rotates point around origin by theta radians."
  [point origin theta]
  (let [[px py] point
        [ox oy] origin
        c (Math/cos theta)
        s (Math/sin theta)]
    [(+ ox (- (* c (- px ox)) (* s (- py oy))))
     (+ oy (* s (- px ox)) (* c (- py oy)))]))


(ns pizza.stroke
  (:require [goog.testing.PseudoRandom]))

(let [counter (atom 0)]
  (defn- gen-id
    []
    (swap! counter inc)
    (str "stroke_" @counter)))

(defn- distance
  "The distance between two points."
  [p1 p2]
  (Math/sqrt (reduce + (map #(* % %) (map - p1 p2)))))

(defn- angle
  "The angle between two points."
  [p1 p2]
  (apply Math/atan2 (reverse (map - p2 p1))))

(defn stroke
  [& points]
  {:id (gen-id)
   :seed (rand)
   :points (seq points)
   :skin nil
   :granularity 3})

(defn append
  [stroke pt]
  (let [points (:points stroke)]
    (if (or (empty? points)
            (> (distance pt (first points)) (:granularity stroke)))
      (assoc stroke :points (cons pt points))
      stroke)))

(defn length
  "The length of the stroke."
  [stroke]
  (let [points (:points stroke)]
    (reduce + (map distance points (drop 1 points)))))

(defn origin
  "Returns the originating point of the stroke."
  [stroke]
  (last (:points stroke)))

(defn destin
  "Returns the final point of the stroke."
  [stroke]
  (first (:points stroke)))

(defn rand-seq
  [stroke]
  (let [rng (goog.testing.PseudoRandom. (:seed stroke))]
    (repeatedly #(.random rng))))

(defn- clamp-point
  [max-len [x y :as p1] p2]
  (if (< (distance p1 p2) max-len)
    p2
    (let [a (angle p1 p2)]
      [(+ x (* max-len (Math/cos a)))
       (+ y (* max-len (Math/sin a)))])))

(defn clamp
  "Takes a stroke and returns a stroke with only two points that are a maximum
  of max-len apart."
  [stroke max-len]
  (let [cnt (count (:points stroke))
        p1 (origin stroke)
        p2 (destin stroke)]
    (if (< cnt 2)
      stroke
      (assoc stroke :points (list (clamp-point max-len p1 p2) p1)))))

(defn- thin
  "Takes a seq points and thins them out so that you have a more sparse
  distribution of points."
  [stroke spacing]
  (let [points (reverse (:points stroke))]
    (loop [acc ()
           prev (first points)
           [current & remaining] (rest points)]
      (if (not current)
        (assoc stroke :points acc)
        (if (> (distance prev current) spacing)
          (recur (conj acc current) current remaining)
          (recur acc prev remaining))))))

(defn- format-points
  "A non-memoized version of format points."
  [stroke]
  (apply str (interleave (flatten (:points stroke)) (repeat " "))))

;(declare format-points)
;
;(def ^:private format-points
;  (memoize
;    (fn [points]
;      (if (empty? points)
;        ""
;        (str (ffirst points) " " (second (first points)) " "
;             (format-points (rest points)))))))
;

(ns limn.stroke
  (:require [goog.testing.PseudoRandom]
            [limn.geometry :as geometry]))

(let [counter (atom 0)]
  (defn- gen-id
    "Generates an uniqe id for the stroke."
    []
    (swap! counter inc)
    (str "stroke_" @counter)))

(defn- internal-format-points
  "A non-memoized version of format points."
  [point points]
  ;(apply str (interleave (flatten points) (repeat " ")))
  (str (first point) " " (second point) " " points)
  )

(defn format-points
  [stroke]
  (::formatted-points stroke))

(defn rand-seq
  [stroke]
  (let [rng (goog.testing.PseudoRandom. (:seed stroke))]
    (repeatedly #(.random rng))))

(defn stroke
  [& points]
  {:id (gen-id)
   :seed (rand)
   :points (apply list points)
   ::formatted-points " "
   :skin nil
   :granularity 3})

(defn append
  [stroke pt]
  (let [points (:points stroke)]
    (if (or (empty? points)
            (> (geometry/distance pt (first points)) (:granularity stroke)))
      (conj stroke {:points (cons pt points)
                    ::formatted-points (internal-format-points
                                         pt (::formatted-points stroke))})
      stroke)))

(defn length
  "The length of the stroke."
  [stroke]
  (geometry/length (:points stroke)))

(defn origin
  "Returns the originating point of the stroke."
  [stroke]
  (last (:points stroke)))

(defn destin
  "Returns the final point of the stroke."
  [stroke]
  (first (:points stroke)))

(defn- clamp-point
  [max-len [x y :as p1] p2]
  (if (< (geometry/distance p1 p2) max-len)
    p2
    (let [a (geometry/angle p1 p2)]
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
        (if (> (geometry/distance prev current) spacing)
          (recur (conj acc current) current remaining)
          (recur acc prev remaining))))))

(defmulti render :skin)

(defmethod render nil [_] nil)

(ns limn.models.easel)

(def default-easel
  {:scale-by 1
   :strokes []
   :width 0
   :height 0
   :view-box [0 0 512 512]})

(defn add-stroke
  [easel stroke]
  (update-in easel [:strokes] #(conj % stroke)))

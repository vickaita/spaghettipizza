(ns limn.models.easel)

#_(def default-easel
  {:scale-by 1
   :pizza (fresh-pizza)
   :strokes []
   :width 0
   :height 0
   :view-box [0 0 512 512]})

#_(defn add-stroke
  [easel stroke]
  (update-in easel [:strokes] #(conj % stroke)))

;(add-stroke default-easel 1)

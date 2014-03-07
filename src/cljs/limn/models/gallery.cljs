(ns limn.models.gallery)

(def empty-gallery
  {:width 0
   :height 0
   :image-url nil
   :loading? false})

(defn visible?
  [gallery]
  (or (:image-url gallery)
      (:loading? gallery)))

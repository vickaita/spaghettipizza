(ns limn.models.app
  (:require [spaghetti-pizza.pizza :refer [fresh-pizza]]
            [limn.models.actions]
            [limn.models.tools]
            [limn.models.colors]
            [limn.models.gallery]))

(def default-app-state
  {:debug true
   :granularity 5
   :show-toolbar? false
   :show-color-wheel? false
   :gallery limn.models.gallery/empty-gallery
   :easel {:scale-by 1
           :pizza (fresh-pizza)
           :strokes []
           :width 0
           :height 0
           :view-box [0 0 512 512]}
   :actions limn.models.actions/groups
   :tools limn.models.tools/toolbar
   :colors limn.models.colors/color-state
   :tool limn.models.tools/default-tool
   :color limn.models.colors/default-color
   })

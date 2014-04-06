(ns limn.models.app
  (:require [spaghetti-pizza.pizza :refer [fresh-pizza]]
            [limn.models.actions]
            [limn.models.tools]
            [limn.models.colors]
            [limn.models.gallery]
            [limn.models.easel]))

(def default-app-state
  {:debug true
   :granularity 4
   :show-toolbar? false
   :show-color-wheel? false
   :gallery limn.models.gallery/empty-gallery
   :easel (merge limn.models.easel/default-easel {:pizza (fresh-pizza)})
   :actions limn.models.actions/groups
   :tools limn.models.tools/toolbar
   :colors limn.models.colors/color-state
   :tool limn.models.tools/default-tool
   :color limn.models.colors/default-color
   })
